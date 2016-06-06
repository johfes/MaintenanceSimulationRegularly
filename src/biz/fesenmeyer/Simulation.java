package biz.fesenmeyer;
import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;
import java.util.TreeSet;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;


public class Simulation {
	/**
	 * Future Event List.
	 */
	private static final SortedMap<Double, ArrayList<String>> FEL = new TreeMap<>();
	private static double simulationTime = 0.0;
	/**
	 * Mean Time Between Failure.
	 */
	private static double mtbf; //6
	private static double maintenanceInterval; //5
	private static final String FORMAT = "%.1f";
	private static double simulationDurance; //14
	private static double lastMaintenanceEnd = 0.0;
	protected static Machine machine;
	
	
	public static double getMaintenanceInterval() {
		return maintenanceInterval;
	}

	public static void setLastMaintenanceEnd(double lastMaintenanceEnd) {
		Simulation.lastMaintenanceEnd = lastMaintenanceEnd;
	}


	public static double getSimulationTime() {
		return simulationTime;
	}


	public static double getMtbf() {
		return mtbf;
	}


	public static String getFormat() {
		return FORMAT;
	}


	public static double getLastMaintenanceEnd() {
		return lastMaintenanceEnd;
	}


	public static SortedMap<Double, ArrayList<String>> getFel() {
		return FEL;
	}


	public static void main(String[] args) {
		
		Scanner s = new Scanner(System.in);
		System.out.println("Wie lange ist die MTBF in Tagen?");
		mtbf = s.nextDouble();
		System.out.println("In welchem Intervall in Tagen sollen Wartungen durchgeführt werden");
		maintenanceInterval = s.nextDouble();
		System.out.println("Wieviele Tage soll die Simulation dauern?");
		simulationDurance = s.nextDouble();
		machine = new Machine();
	    addToFel(0.1, "Ankunft");
	    addToFel(maintenanceInterval, "Wartungsbeginn");
	    addToFel(RandomGenerator.generateNextFailure(), "Ausfall");

	    System.out.println("Simulationsbeginn");
	    
		while(true){
			System.out.println("*****************************************");
			
		    for(Entry<Double, ArrayList<String>> entry : FEL.entrySet()) {
		    	  final Double key = entry.getKey();
		    	  final Object value = entry.getValue();

		    	  System.out.println(String.format(FORMAT, key) + " => " + value);
		    }
		    
	    	  System.out.println("WS" + " => " + machine.getCountWS());
	    	  simulationTime = FEL.firstKey();
	    	  if(simulationTime > simulationDurance){
	    		  break;
	    	  }
			System.out.println("*****************************************");
		    
		    ArrayList<String> eventTypes = FEL.get(FEL.firstKey());
		    for (String eventType: eventTypes){
		    	execute(eventType, machine);
		    }
		    FEL.remove(simulationTime);
		    
		}

		System.out.println("*****************************************");
		System.out.println("Simulationsende");
		System.out.println("*****************************************");
		System.out.println("Statistik:");
		System.out.println("Verfügbarkeit: "+
				String.format(FORMAT, calculateAvailability(machine.getDownTime()))+"%");
		System.out.println("durchschnittliche Warteschlangenlänge: "+
				String.format(FORMAT, getAverageWSLength(machine.getWSLengths())));
		
	}
	
	
	private static void execute(String eventType, Machine machine) {
		switch(eventType){
		case "Ankunft":
			machine.arrival();
			break;
		case "Bearbeitungsende":
			machine.processingEnd();
			break;
		case "Wartungsbeginn":
			machine.maintenanceStart();
			break;
		case "Wartungsende":
			machine.maintenanceEnd();
			break;
		case "Reparaturende":
			machine.repairEnd();
			break;
		case "Ausfall":
			machine.failure();
			break;
		default:
			throw new IllegalStateException("Unknown eventType: "+eventType);
	}
}
	
	public static void addToFel(Double key, String type){
		if(FEL.containsKey(key)){
			ArrayList<String> eventList= FEL.get(key);
			eventList.add(type);
		} else {
			ArrayList<String> eventList = new ArrayList<String>();
			eventList.add(type);
			FEL.put(key,eventList);
		}
	}
	
	public static void removeFromFel(Double key, String type){
  	  ArrayList<String> list = (ArrayList<String>) FEL.get(key);
  	  if(list.size() > 1){
  		list.remove(type);
  		FEL.replace(key, list);
  	  } else{
		  FEL.remove(key);
  	  }
	}
	
	public static double calculateAvailability(Double downTime){
		double availabilityTime = simulationDurance-downTime;
		return (availabilityTime/simulationDurance)*100;
	}
	
	public static double getAverageWSLength(SortedMap<Double, Integer> WSLengths){
		double sum = 0.0;
		SortedMap<Double, Integer> tmpMap = new TreeMap<Double, Integer>();
		
		for (Entry<Double, Integer> entry : WSLengths.entrySet()) {
			Double time = entry.getKey();
		    Integer wsCount = entry.getValue();
		    if(tmpMap.size() > 0 && !tmpMap.containsValue(wsCount)){
		    	sum+= (time-(Double)tmpMap.firstKey()) *(Integer) tmpMap.get(tmpMap.firstKey());
		    	tmpMap.clear();
		    }
		    tmpMap.put(time, wsCount);
		}

		return sum/simulationDurance;
	}
	
	public static Double getTimeOfEventType(String eventType, SortedMap<Double, ArrayList<String>> map){
	    for(Entry<Double, ArrayList<String>> entry : map.entrySet()) {
	    	  Double time = entry.getKey();
	    	  ArrayList<String> events = entry.getValue();
	    	  for(String event : events){
	    		  if(eventType.equalsIgnoreCase(event)){
	    			  return time;
	    		  }
	    	  }    	 
	    }
	    return null;
	}
}
