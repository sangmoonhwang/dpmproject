package SearchAndRescue;


import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * This class contains the main method that will do the mission 
 * @author Oruj Ahmadov, Sangmoon Hwang
 * 
 */
public class DPMProject {

	/**
	 * The setup for the odometer, the display, and the ultrasonic and light sensors are done as initiatives.
	 * Then the mission begins by waiting for the user to send inputs
	 */
	public static void main(String[] args) {
		int buttonChoice;

		// setup the odometer, display, and ultrasonic and light sensors
		Odometer odo = new Odometer();
		OdometryDisplay display = new OdometryDisplay(odo);
		UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S1);
		UltrasonicSensor usBack = new UltrasonicSensor(SensorPort.S2);
		Navigation nav = new Navigation(odo);
		Localization ori = new Localization(odo, nav,usFront, usBack);
		SearchAndRescue saveAndRescue = new SearchAndRescue(odo, nav, usFront, usBack, ori);


		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should drive in a square or float
			LCD.drawString("< Left  | Right >", 0, 0);
			LCD.drawString("        |        ", 0, 1);
			LCD.drawString(" Start  |   ", 0, 2);
			LCD.drawString("Mission |  ", 0, 3);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { Motor.A,
					Motor.B, Motor.C }) {
				motor.forward();
				motor.flt();

			}

			//start odometer, odometer display and MISSION!
			display.start();
			odo.start();
			saveAndRescue.startMission();

		} else {



		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

}
