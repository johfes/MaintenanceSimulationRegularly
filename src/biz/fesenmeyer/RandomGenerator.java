package biz.fesenmeyer;
import java.util.Random;

import com.espertech.esper.client.EPRuntime;


public class RandomGenerator {
    static Random random = new Random();

	public static double generateNextRepairEnd(){
		int number = generateRandomInt(10, 15);
		return (double)number/10;
	}

	public static double generateNextFailure(){
		int number;
		double mtbf = Simulation.getMtbf();
		if(generateRandomInt(1, 10) > 8){
			number = generateRandomInt((int)(mtbf*10)/2, (int)(mtbf*10)-1);
		} else {
			number = generateRandomInt((int)(mtbf*10), (int)(mtbf*10)+ (int)(mtbf*10)/6);
		}
		 
		return (double) number/10;
	}
	
	public static double generateNextMaintenanceEnd(){
		int number = generateRandomInt(1, 3);
		return (double)number/10;
	}

	public static double generateNextArrival(){
		return exponential(0.5)/10.0;
	}	
	
	public static double generateNextProcessingEnd(){
		int number = generateRandomInt(1, 2);
		return (double) number/10;
	}
	
	public static double exponential(double lambda){
		return (-1/lambda) * Math.log(1-random.nextDouble());
	}
	
	public static int generateRandomInt(int min, int max) {
	    int randomNum = random.nextInt((max - min) + 1) + min;
	    return randomNum;
	}

}