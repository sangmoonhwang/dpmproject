package SearchAndRescue;

import java.util.ArrayList;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;

/**
 * this class localizes the robot
 * @author Oruj Ahmadov, Sangmoon Hwang
 * 
 */
public class Localization {

	private static final int FORWARD_SPEED = 120;

	private static final int LENGTH_OF_BOARD = 12;
	private static final int WIDTH_OF_BOARD = 12;

	//basically for each point, we need 8 data
	//obstacle values for 4 cardinal directions
	// north, west, south and east accordingly
	//and boolean value indicating whether it is potential
	//starting positions or not. for the beginning, it will 
	//all set to 1, but as we do observation, we will eliminate them by setting to 0
	//e.g point -15,-15, with obstacle on right will be respresented as
	// {0,1,1,1,1,1,1,1}
	private static final int DATA_FOR_EACH_POINT = 8;


	private final Odometer odo;
	private final Navigation nav;
	private final UltrasonicSensor usFront;
	private final UltrasonicSensor usBack;

	private int observationCount = 0;
	private int potentialPositions = 48;
	private int obstacleValue = 0;

	private int rotationsCount = 0;

	private double theta = -90;

	private int startX;
	private int startY;
	private int startTheta;

	// using list to keep record of movements of robots
	// so that those movements can be applied to potential
	// positions as well when we check them for obstacle for another time

	private final ArrayList movementsList = new ArrayList();

	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;

	// 3 dimensional array is used to build fixed map
	// the map is built like chess model
	//so each row corresponds the points vertically
	// and each column corresponds to points horizontally
	//to find the X and Y values of any point from map
	// we have
	//x = -15 + 30*ROW
	//y = -15 + 30*COLUMN
	final int[][][] map = new int[LENGTH_OF_BOARD][WIDTH_OF_BOARD][DATA_FOR_EACH_POINT];

	//the obstacle positions for 6 different maps
	//once we get maps, we will file these arrays accordingly
	final int[][]   obstaclePositionsMap1 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;
	final int[][]   obstaclePositionsMap2 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;
	final int[][]   obstaclePositionsMap3 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;
	final int[][]   obstaclePositionsMap4 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;
	final int[][]   obstaclePositionsMap5 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;
	final int[][]   obstaclePositionsMap6 = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}} ;







	/**
	 * constructor
	 * @param odo
	 * @param nav
	 * @param usFront
	 * @param usBack
	 */
	public Localization(Odometer odo, Navigation nav, UltrasonicSensor usFront, UltrasonicSensor usBack) {
		this.odo = odo;
		this.usFront = usFront;
		this.usBack = usBack;
		this.nav = nav;


	}

	//method start observing to find where it is
	/**
	 * start the localization
	 */
	public void startObservation() {

		int mapNumber = Bluetooth.getMapValue();
		//find corresponding map value, say map1


		//Map1 is just sample, will be changed once we know which map we use
		updateMap(obstaclePositionsMap1);


		obstacleValue = getObstacleForOneTile();
		doObservation(obstacleValue);

		while (potentialPositions >= 2) {

			int rotationsTrack = 0;
			int movementTrack = 0;

			// if any of sensors sees an obstacle , we turn for -90 degree
			//after doing an observation and then perform another observation
			if (obstacleValue/10 == 1 || obstacleValue%10 ==1) {

				nav.turnTo(theta);

				theta = theta - 90;


				doObservation(getObstacleForOneTile());
				if(getObstacleForOneTile()/10==0){
					nav.move(30);
				}
				if(getObstacleForOneTile()%10!=0){
					nav.move(-30);
				}

				rotationsTrack = 1;
				rotationsCount++;

			}

			//if no sensor sees any obstacle, do another observation for one tile further
			//without moving the robot
			else if (obstacleValue/10 != 1 && obstacleValue%10 !=1) {

				doFurtherObservation(getObstacleForTwoTile());
				obstacleValue = getObstacleForTwoTile();

				movementTrack = 2;

			}
			if (rotationsTrack == 1) {
				movementsList.add(rotationsTrack);
			}
			if (movementTrack == 2) {
				movementsList.add(movementTrack);
			}

		}

		for (int row = 0; row < 12; row++) {
			for (int column = 0; column < 12; column++) {
				for(int direction =4; direction <8; direction++ ){
					// now that we have only one potential position left,
					// which has boolean potential 1, we correct Odometer accordingly
					if (map[row][column][direction] == 1) {
						startX = -15 + 30 * row;
						startY = -15 + 30 * column;
						startTheta = 360 + (4-direction) * 90;
						odo.setX(map[applyMovements(row, column)][0][0]);
						odo.setY(map[applyMovements(row, column)][0][1]);
						odo.setTheta(2 * Math.PI
								+ (1 - switchColumn(column + rotationsCount))
								* (Math.PI / 2));
					}

				}

			}

		}



		LCD.drawInt(startX, 0, 4);
		LCD.drawInt(startY, 0, 5);
		LCD.drawInt(startTheta, 0, 6);
		LCD.drawInt(observationCount, 0, 7);

	}


	/**
	 * make an observation that compares obstacle value with potential positions value and 
	 * make appropriate eliminations. Also, potential positions are checked according to the movements and rotations
	 * @param obstacleValues
	 */
	private void doObservation(int obstacleValues) {

		for (int row = 0; row < 12; row++) {
			for (int column = 0; column < 12; column++) {
				for(int booleanPotential =4; booleanPotential <8; booleanPotential++ ){

					// only check potential starting positions
					if (map[row][column][booleanPotential] != 0) {

						int frontUSObstacleValue = obstacleValues / 10;
						int backUSObstacleValue  = obstacleValues % 10;

						// below statement apply movements/rotations
						// to potential positions,then check for obstacle

						if (map[applyMovements(row, column)][column][(booleanPotential - 4 + rotationsCount)%4 ]  != frontUSObstacleValue &&
								map[applyMovements(row, column)][column][((booleanPotential - 2 + rotationsCount)%4)] != backUSObstacleValue) {

							// eliminate positions that don't pass obstacle test
							map[row][column][booleanPotential] = 0;
							potentialPositions--;
						}
					}

				}

			}
		}

		observationCount++;
	}


	/**
	 * make an observation that compares obstacle value with potential positions
	 * value and make appropriate eliminations as well as potential positions according to the movements
	 * and rotations, then move arrows one tile and then check for obstacle value
	 * @param obstacleValues
	 */
	private void doFurtherObservation(int obstacleValues){

	}


	/**
	 * check whether robot facing an obstacle or not in the range of one tile
	 * @return
	 */
	private int getObstacleForOneTile() {

		int frontUS = 0;
		int backUS  = 0;
		// do a ping
		usFront.ping();
		usBack.ping();

		// wait for the ping to complete
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		// there will be a delay here
		int sensorReadingFront = usFront.getDistance();
		int sensorReadingBack = usBack.getDistance();

		if (sensorReadingFront < 22) {
			frontUS =  1;
		}
		if (sensorReadingBack < 22) {
			backUS = 1;
		}


		return frontUS * 10 + backUS * 1;

	}

	/**
	 * check whether the robot has an obstacle or not in the range of two tiles
	 * @return
	 */
	private int getObstacleForTwoTile() {

		int frontUS = 0;
		int backUS  = 0;
		// do a ping
		usFront.ping();
		usBack.ping();

		// wait for the ping to complete
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		// there will be a delay here
		int sensorReadingFront = usFront.getDistance();
		int sensorReadingBack = usBack.getDistance();

		if (sensorReadingFront < 52) {
			frontUS =  1;
		}
		if (sensorReadingBack < 52) {
			backUS = 1;
		}


		return frontUS * 10 + backUS * 1;

	}

	/**
	 * calculate the future positions, then finds the row that has those position values
	 * in order to check potential position after each movement
	 * @param row
	 * @param column
	 * @return
	 */
	private int switchRow(int row, int column) {

		int movedPosX = map[row][0][0]
				+ (int) (30 * Math.sin(((1 - column) * Math.PI) / 2));
		int movedPosY = map[row][0][1]
				+ (int) (30 * Math.cos(((1 - column) * Math.PI) / 2));
		int wantedRow = 0;

		for (int r = 0; r < 12; r++) {
			if (map[r][0][0] == movedPosX && map[r][0][1] == movedPosY) {
				wantedRow = r;
			}

		}
		return wantedRow;

	}

	// the way we set up the map
	// is based such that, starting first column
	// in each row, the columns correspond to the 4 possible potential positions
	// of certain point. 1 for 0degree, 2 for 90, 3 for 180 and 4 for 270
	// after each rotations, the method below switches column and compares
	// obstacle value
	// at switchedColumn

	/**
	 * switch column after each rotation
	 * @param column
	 * @return
	 */
	private int switchColumn(int column) {
		if (column % 4 == 0) {
			column = 4;
		} else {
			column = column % 4;
		}
		return column;
	}



	// once we have the list of movements
	// we can apply them any potential position
	// to check for obstacle test

	/**
	 * According to a list of movements, apply all potential positions to the obstacle test
	 * @param Row
	 * @param Column
	 * @return
	 */
	private int applyMovements(int Row, int Column) {

		for (int i = 0; i < movementsList.size(); i++) {

			// if it is rotation, do appropriate change to arrow position
			// so we basically update column here

			if ((Integer) (movementsList.get(i)) == 1) {

				Column = switchColumn(Column + 1);

			}
			// if it is a tile movement, do appropriate change to arrow position
			// so we basically update row here, since we have positions located
			// on different rows

			if ((Integer) (movementsList.get(i)) == 2) {
				Row = switchRow(Row, Column);

			}

		}

		return Row;

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



	//the method below will update obstacle values
	//inside the map after getting which map
	//is being used via Bluetooth

	/**
	 * update obstacle values after getting the map information via Bluetooth
	 * @param competitionMap
	 */
	private void updateMap(int competitionMap[][]){


		for(int row=0; row<12 ;row++){
			for(int column = 0; column<12; column++){
				if(row==0){
					map[row][column][3]=1;
				}
				if(row==11){
					map[row][column][5]=1;
				}
				if(column==0){
					map[row][column][4]=1;
				}
				if(column==11){
					map[row][column][2]=1;
				}
			}
		}


		for(int row =0; row < competitionMap.length; row++){
			int i = (competitionMap[row][0]+15)/30;
			int j = (competitionMap[row][1]+15)/30;
			if(i-1>=0){
				map[i-1][j][5]=1;
			}

			if(i+1<=11){
				map[i+1][j][3]=1;
			}
			if(j-1>=0){
				map[i][j-1][2]=1;
			}
			if(j+1<=11){
				map[i][j+1][4]=1;
			}
		}

	}

	//return X position of start point
	/**
	 * return X-position of the starting point
	 * @return
	 */
	public int getStartX(){
		return startX;
	}

	//return Y position of start point
	/**
	 * return Y-position of the starting point
	 * @return
	 */
	public int getStartY(){
		return startY;
	}

	//return start Prientation of Robot
	/**
	 * return the starting orientation of the robot
	 * @return
	 */
	public int getStartTheta(){
		return startTheta;
	}


}
