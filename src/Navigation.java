package SearchAndRescue;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * this class makes the robot to navigate
 * @author Oruj Ahmadov, Sangmoon Hwang
 *
 */
public class Navigation extends Thread {

	private final Odometer odometer;
	UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);

	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 100;
	private static final int MAX_ACCELERATION = 2000;
	private final double wheelRadius = 2.10;

	public double destinationX;
	public double destinationY;

	private boolean isNavigating;

	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;

	/**
	 * constructor
	 * @param odom
	 */
	public Navigation(Odometer odom) {
		this.odometer = odom;
		isNavigating = false;
		leftMotor.setAcceleration(MAX_ACCELERATION);
		rightMotor.setAcceleration(MAX_ACCELERATION);

	}

	@Override
	public void run() {

	}

	/**
	 * travel to the given point
	 * @param x
	 * @param y
	 */
	public void travelTo(double x, double y) {
		isNavigating = true;

		destinationX = x;
		destinationY = y;

		// until checkpoints are reached keep calculating theta and moving
		// motors forward
		while ((Math.abs(odometer.getX() - destinationX) > 1)
				|| (Math.abs(odometer.getY() - destinationY) > 1)) {

			double theta;

			double deltaX = destinationX - (odometer.getX());
			double deltaY = destinationY - (odometer.getY());

			theta = (Math.atan2(deltaX, deltaY) * (180 / Math.PI));
			// convert theta to be 0-360 degrees increasing clockwise
			if (theta < 0) {
				theta = theta + 360;
			}

			turnTo(theta);

			setSpeeds(FORWARD_SPEED, FORWARD_SPEED);

		}

		isNavigating = false;
		setSpeeds(0, 0);

	}

	/**
	 * turn to the given orientation
	 * @param heading
	 */
	public void turnTo(double heading) {
		isNavigating = true;
		double degree;

		degree = heading - getOdometerTheta();
		// find minimum turning angle
		if (degree > 180) {
			degree = degree - 360;
		}

		// keep rotation until correct heading is reached by checking error
		// between current theta and desired heading
		while (Math.abs(degree) > 3) {

			degree = heading - getOdometerTheta();
			// find minimum turning angle
			if (degree > 180) {
				degree = degree - 360;
			}

			if (degree < 0.0) {
				setSpeeds(-ROTATE_SPEED, ROTATE_SPEED);
			} else if (degree > 180) {
				setSpeeds(-ROTATE_SPEED, ROTATE_SPEED);
			} else if (degree < -180) {
				setSpeeds(ROTATE_SPEED, -ROTATE_SPEED);
			} else {
				setSpeeds(ROTATE_SPEED, -ROTATE_SPEED);
			}
		}
		setSpeeds(0, 0);

		isNavigating = false;

	}

	/**
	 * return true if the robot is navigating or false if not
	 * @return
	 */
	boolean isNavigating() {
		return isNavigating;
	}

	/**
	 * return the heading of the odometer
	 * @return
	 */
	public double getOdometerTheta() {
		if (odometer.getTheta() * (180 / Math.PI) > 360) {
			return (odometer.getTheta() * (180 / Math.PI)) - 360.0;
		} else if (odometer.getTheta() * (180 / Math.PI) > 720) {
			return (odometer.getTheta() * (180 / Math.PI)) - 720.0;
		} else {
			return (odometer.getTheta() * (180 / Math.PI));
		}
	}

	//set speeds of motor
	/**
	 * set the speed of the right and left motors
	 * @param left
	 * @param right
	 */
	public void setSpeeds(int left, int right) {
		leftMotor.setSpeed(left);
		rightMotor.setSpeed(right);
		if (left < 0) {
			leftMotor.backward();
		} else {
			leftMotor.forward();
		}
		if (right < 0) {
			rightMotor.backward();
		} else {
			rightMotor.forward();
		}
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

	/**
	 * calculate the angle
	 * @param radius
	 * @param width
	 * @param angle
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	//moves robot vertically while avoiding obstacles kindly
	/**
	 * move the robot vertically while avoiding obstacles
	 */
	public void moveVerticallyAvoidingObstacles(){
		double initialTheta = ((odometer.getTheta())*180)/Math.PI;

		while(odometer.getY() != destinationY){
			if(getObstacle() == 1){

				if(destinationX-odometer.getX() >= 0){
					turnTo(90);
					if(getObstacle()==0){
						move(30);
					}
					else{
						turnTo(270);
						move(30);
					}
					turnTo(initialTheta);
					moveVerticallyAvoidingObstacles();
				}
				else if(destinationX-odometer.getX() < 0){
					turnTo(270);
					if(getObstacle()==0){
						move(30);
					}
					else{
						turnTo(90);
						move(30);
					}
					turnTo(initialTheta);
					moveVerticallyAvoidingObstacles();
				}



			}
			else if(getObstacle() == 0){
				while(getObstacle()!=1){
					travelTo(odometer.getX(),destinationY);
				}
				moveVerticallyAvoidingObstacles();
			}
		}

	}

	//moves robot horizontally while avoiding obstacles kindly
	/**
	 * move the robot horizontally while avoiding obstacles 
	 */
	public void moveHorizontallyAvoidingObstacles(){
		double initialTheta = ((odometer.getTheta())*180)/Math.PI;

		while(odometer.getX() != destinationX){
			if(getObstacle() == 1){

				if(destinationY-odometer.getY() >= 0){
					turnTo(360);
					if(getObstacle()==0){
						move(30);
					}
					else{
						turnTo(180);
						move(30);
					}
					turnTo(initialTheta);
					moveHorizontallyAvoidingObstacles();
				}
				else if(destinationY-odometer.getY() < 0){
					turnTo(180);
					if(getObstacle()==0){
						move(30);
					}
					else{
						turnTo(360);
						move(30);
					}
					turnTo(initialTheta);
					moveHorizontallyAvoidingObstacles();
				}



			}
			else if(getObstacle() == 0){
				while(getObstacle()!=1){
					travelTo(destinationX,odometer.getY());
				}
				moveHorizontallyAvoidingObstacles();
			}
		}
	}

	//check whether there is obatcle in the next tile or not
	/**
	 * check if an obstacle is in the next tile
	 * @return
	 */
	private int getObstacle() {

		// do a ping
		us.ping();

		// wait for the ping to complete
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		// there will be a delay here
		int sensorReading = us.getDistance();

		if (sensorReading < 22) {
			return 1;
		}

		return 0;

	}

	//moves robot for distance indicated
	/**
	 * move the robot by the given distance
	 * @param distance
	 */
	public void move(double distance){

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), false);
	}




}