package biz.fesenmeyer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;



public class Machine {
	
	private String status = "frei";
	private int countWS = 0;
	private double downTime = 0.0;
	private Queue<Integer> qualityQueue = new LinkedList<Integer>();
	private SortedMap<Double, Integer> WSLengths = new TreeMap<Double,Integer>();
	
	public void setStatus(String status) {
		this.status = status;
	}

	void setDownTime(double downTime) {
		this.downTime = downTime;
	}

	public double getDownTime() {
		return downTime;
	}

	public int getCountWS() {
		return countWS;
	}
	
	public SortedMap<Double, Integer> getWSLengths() {
		return WSLengths;
	}

	public void arrival(){
		System.out.println("Ankunft "+ String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		double arrivalTime = Simulation.getSimulationTime()+RandomGenerator.generateNextArrival();
		Simulation.addToFel(arrivalTime, "Ankunft");
		System.out.println("Ankunftsereignis wurde geplant: "+String.format(Simulation.getFormat(), arrivalTime));
		if(countWS == 0 && status.equalsIgnoreCase("frei")){
			double processingEnd = Simulation.getSimulationTime()+RandomGenerator.generateNextProcessingEnd();
			status = "aktiv";
			Simulation.addToFel(processingEnd, "Bearbeitungsende");
			System.out.println("WS leer, Bearbeitung hat begonnen, \nBearbeitungsendeereignis wurde geplant: "+String.format(Simulation.getFormat(), processingEnd));
		} else {
			countWS++;
			WSLengths.put(Simulation.getSimulationTime(), countWS);
			System.out.println("Das Teil wurde der Warteschlange hinzugefügt");
		}
	}

	public void processingEnd(){
		System.out.println("Bearbeitungsende "+ String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		
		if (!status.equalsIgnoreCase("wartung")){
			if(countWS > 0){
				countWS--;
				WSLengths.put(Simulation.getSimulationTime(), countWS);
				double processingEndTime = Simulation.getSimulationTime()+RandomGenerator.generateNextProcessingEnd();
				Simulation.addToFel(processingEndTime, "Bearbeitungsende");
				System.out.println("Teil wurde nachgezogen, \nBearbeitungsende wurde geplant: "+String.format(Simulation.getFormat(), processingEndTime));
			} else {
				status = "frei";
			}
		}
		
	}
	
	public void maintenanceStart(){
		System.out.println("Wartungsbeginn "+
						String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		status = "wartung";
		double timeToMaintenanceEnd = RandomGenerator.generateNextMaintenanceEnd();
		downTime += timeToMaintenanceEnd;
		double maintenanceEndTime = Simulation.getSimulationTime() + timeToMaintenanceEnd;
		Simulation.addToFel(maintenanceEndTime, "Wartungsende");
		System.out.println("Wartungsende wurde geplant: "+
							String.format(Simulation.getFormat(), maintenanceEndTime));
		
	    Double entryToDelete = Simulation.getTimeOfEventType("Ausfall", Simulation.getFel());
	    
	    if(entryToDelete != null){
			Simulation.removeFromFel(entryToDelete, "Ausfall");
	    	System.out.println("Ausfall abgewendet: "+
	    					String.format(Simulation.getFormat(), entryToDelete));
	    }
	}
	
	public void maintenanceEnd(){
		System.out.println("Wartungsende "+ 
					String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		Simulation.setLastMaintenanceEnd(Simulation.getSimulationTime());
		double nextMaintenanceTime = Simulation.getSimulationTime()+
					Simulation.getMaintenanceInterval();
		Simulation.addToFel(nextMaintenanceTime, "Wartungsbeginn");
		double failureTime = Simulation.getSimulationTime()+
					RandomGenerator.generateNextFailure();
    	Simulation.addToFel(failureTime, "Ausfall");
		System.out.println("Nächster Ausfall wurde geplant: "+
				String.format(Simulation.getFormat(), failureTime));
		
		if(countWS >= 1){
			countWS--;
			WSLengths.put(Simulation.getSimulationTime(), countWS);
			status = "aktiv";
			double processingEndTime = Simulation.getSimulationTime()+
						RandomGenerator.generateNextProcessingEnd();
			Simulation.addToFel(processingEndTime, "Bearbeitungsende");
			System.out.println("Teil wurde nachgezogen, \nBearbeitungsende wurde geplant: "+
					String.format(Simulation.getFormat(), processingEndTime));
		} else {
			status = "frei";
		}
	}
	
	public void failure(){
		System.out.println("Ausfall "+ String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		double timeToRepairEnd= RandomGenerator.generateNextRepairEnd();
		downTime += timeToRepairEnd;
		double repairEndTime = Simulation.getSimulationTime()+timeToRepairEnd;
		Simulation.addToFel(repairEndTime, "Reparaturende");
		System.out.println("Reparatur wird begonnen, \nReparaturende wurde geplant: "+String.format(Simulation.getFormat(), repairEndTime));
		
		if(status.equals("aktiv")){
			status = "defekt_aktiv";
			
		    Double entryToDelete = Simulation.getTimeOfEventType("Bearbeitungsende", Simulation.getFel());
		    
		    if(entryToDelete != null){
  			  	Simulation.removeFromFel(entryToDelete, "Bearbeitungsende");
		    	Simulation.addToFel(entryToDelete+timeToRepairEnd, "Bearbeitungsende");
		    	System.out.println("Maschine war aktiv, \nBearbeitungsende wurde hinausgeschoben: "+String.format(Simulation.getFormat(), entryToDelete)+" + "+String.format(Simulation.getFormat(), timeToRepairEnd));
		    }

		} else {
			status = "defekt_frei";
		}
		
		Double entryToDelete = Simulation.getTimeOfEventType("Wartungsbeginn", Simulation.getFel());
	    
	    if(entryToDelete != null){
			Simulation.removeFromFel(entryToDelete, "Wartungsbeginn");
	    	Simulation.addToFel(Simulation.getSimulationTime()+Simulation.getMaintenanceInterval(), "Wartungsbeginn");
	    	System.out.println("Maschine wurde repariert, "+
	    	"\nDie nächste Wartung findet in: "+
	    			String.format(Simulation.getFormat(), Simulation.getMaintenanceInterval())+" Tagen statt. ");
	    }
	}
	
	public void repairEnd(){
		System.out.println("Reparaturende "+ String.format(Simulation.getFormat(), Simulation.getSimulationTime()));
		double failureTime = Simulation.getSimulationTime()+RandomGenerator.generateNextFailure();;
		Simulation.addToFel(failureTime, "Ausfall");
		
		if (status.equals("defekt_aktiv")){
			status = "aktiv";
		} else {
			if(countWS >= 1){
				countWS--;
				WSLengths.put(Simulation.getSimulationTime(), countWS);
				status = "aktiv";
				double processingEndTime = Simulation.getSimulationTime()+RandomGenerator.generateNextProcessingEnd();
				Simulation.addToFel(processingEndTime, "Bedienungsende");
				System.out.println("Teil wurde nachgezogen, \nBedienungsende wurde geplant: "+String.format(Simulation.getFormat(), processingEndTime));
			} else {
				status = "frei";
			}
		}
	}
}

