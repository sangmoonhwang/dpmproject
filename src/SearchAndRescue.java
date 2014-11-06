package SearchAndRescue;

import java.util.ArrayList;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;

/**
 * This class searches and rescues blocks
 * @author Oruj Ahmadov, Sangmoon Hwang
 * 
 */
public class SearchAndRescue {

	private static final int FORWARD_SPEED = 120;



	private final Odometer odometer;
	private final Navigation navigation;
	private final UltrasonicSensor usFront;
	private final UltrasonicSensor usBack;
	private final Localization localization;



	private int startX;
	private int startY;
	private int startTheta;



	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;



	/**
	 * constructor
	 * @param odo
	 * @param nav
	 * @param usFront
	 * @param usBack
	 * @param localization
	 */
	public SearchAndRescue(Odometer odo, Navigation nav, UltrasonicSensor usFront,UltrasonicSensor usBack, Localization localization) {
		this.odometer = odo;
		this.usFront = usFront;
		this.usBack = usBack;
		this.navigation = nav;
		this.localization = localization;


	}

	//start the Search and Rescue mission
	/**
	 * begin the Search and Rescue mission
	 */
	public void startMission() {

		localization.startObservation();

		goToPickupArea();
		startPickingUp();
		goToDropOffArea();

		startX = localization.getStartX();
		startY = localization.getStartY();
		startTheta = localization.getStartTheta();


		LCD.drawInt(startX, 0, 4);
		LCD.drawInt(startY, 0, 5);
		LCD.drawInt(startTheta, 0, 6);

	}


	/**
	 * calculate the distance
	 * @param radius
	 * @param distance
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// pick up area navigation
	/**
	 * go to the pick-up location
	 */
	private void goToPickupArea() {
		navigation.destinationX = pickupAreaX;
		navigation.destinationY = pickupAreaY;

		if(navigation.destinationY - odometer.getY()>=0){
			navigation.turnTo(0);
			navigation.moveVerticallyAvoidingObstacles();
		}
		else if(navigation.destinationY - odometer.getY()<0){
			navigation.turnTo(180);
			navigation.moveVerticallyAvoidingObstacles();
		}
		if(navigation.destinationX - odometer.getX()>=0){
			navigation.turnTo(90);
			navigation.moveHorizontallyAvoidingObstacles();
		}
		else if(navigation.destinationX - odometer.getX()<0){
			navigation.turnTo(270);
			navigation.moveHorizontallyAvoidingObstacles();
		}
		//taking into account that after last movement
		//robot might go away from its correct destination Y
		// we call move vertical again here
		navigation.moveVerticallyAvoidingObstacles();



	}

	// drop off area navigation
	/**
	 * go to the drop-off location
	 */
	private void goToDropOffArea() {
		navigation.destinationX = dropOffAreaX;
		navigation.destinationY = dropOffAreaY;

		if(navigation.destinationX - odometer.getX()>=0){
			navigation.turnTo(90);
			navigation.moveHorizontallyAvoidingObstacles();
		}
		else if(navigation.destinationX - odometer.getX()<0){
			navigation.turnTo(270);
			navigation.moveHorizontallyAvoidingObstacles();
		}

		if(navigation.destinationY - odometer.getY()>=0){
			navigation.turnTo(0);
			navigation.moveVerticallyAvoidingObstacles();
		}
		else if(navigation.destinationY - odometer.getY()<0){
			navigation.turnTo(180);
			navigation.moveVerticallyAvoidingObstacles();
		}

		//taking into account that after last movement
		//robot might go away from its correct destination X
		// we call move horizontal again here
		navigation.moveHorizontallyAvoidingObstacles();


	}

	//starts hardware to pick up blocks
	/**
	 * pick up the block
	 */
	private void startPickingUp(){

	}

	/**
	 * release the block
	 */
	private void dropBlock(){
		
	}



}
