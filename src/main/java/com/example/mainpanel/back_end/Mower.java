package com.example.mainpanel.back_end;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

// Functions of Mower: scan, turning, path planning and moving.
// Before determine each mower action, the mower will get the updated mowerMap and mower position from communication channel.
// It will then determine the next mower action based on the mower map.
// After determine the action, it will send its next action to the simulationRun. The simulationRun will validate the motion and call communicationChannel to updated all information.

public class Mower {
	class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	public int mowerID = 0;
	public static SimultaionRun sim;
	public static CommunicationChannel cc;

	// discovered: a list of mower that the mower can see, the Point records the
	// relative position of these mowers
	public Map<Integer, Point> discovered;
	Integer mowerX, mowerY;
	private String mowerDirection = "North";
	private static HashMap<String, Integer> xDIR_MAP = new HashMap<>();
	private static HashMap<String, Integer> yDIR_MAP = new HashMap<>();
	//a set to record the id that this mower has discovered


	private String trackAction;
	private Integer trackMoveDistance;
	private String trackNewDirection;
	private String trackMoveCheck;
	private String trackScanResults;

	// elements code
	private static final int UNKNOWN_CODE = -1;
	private static final int EMPTY_CODE = 0;
	private static final int GRASS_CODE = 1;
//	private static final int CRATER_CODE = 2;
//	private static final int FENCE_CODE = 3;
	private static final int CHARGE_CODE = 4;

//	int code = mowerID*10 + 100 + CHARGE_CODE;

	int code = mowerID + 5;
	private boolean crashed = false;
	int mapWidth = 2 * 17 + 1;
	int mapHeight = 2 * 17 + 1;


	private String[] dirs = { "north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest" };
	private List<String> path = new ArrayList<>();
	boolean enable = true;
	int curEnergy;
	int maxEnergy;
	public int stallTurn;



	//private HashSet<Point> chargeLocations = new HashSet<>();

	public Mower(String direction, int id, int energy_capacity, int numMowers) {

		mowerDirection = direction;
		mowerID = id;
		maxEnergy = energy_capacity;
		curEnergy = energy_capacity;

		mowerX = 17;
		mowerY = 17;

		stallTurn = 0;

		//chargeLocations.add(new Point(mowerX,mowerY));//add initial charge location

		xDIR_MAP.put("north", 0);
		xDIR_MAP.put("northeast", 1);
		xDIR_MAP.put("east", 1);
		xDIR_MAP.put("southeast", 1);
		xDIR_MAP.put("south", 0);
		xDIR_MAP.put("southwest", -1);
		xDIR_MAP.put("west", -1);
		xDIR_MAP.put("northwest", -1);


		yDIR_MAP.put("north", 1);
		yDIR_MAP.put("northeast", 1);
		yDIR_MAP.put("east", 0);
		yDIR_MAP.put("southeast", -1);
		yDIR_MAP.put("south", -1);
		yDIR_MAP.put("southwest", -1);
		yDIR_MAP.put("west", 0);
		yDIR_MAP.put("northwest", 1);


		//put self on the map: code = mowerID*10+100+CHARGE_CODE
//		int code = mowerID*10 + 100 + CHARGE_CODE;
	}


	private void scan(int mowerID) {
		curEnergy--;
		sim.scan(mowerID);
		/***
		for (Integer i: scannedInfo){
			if (i.equals(CHARGE_CODE)){
				int j = scannedInfo.indexOf(i);
				String dir = dirs[j];
				int xDir = xDIR_MAP.get(dir);
				int yDir = yDIR_MAP.get(dir);
				Point p = new Point(mowerX+xDir,mowerY+yDir);
				chargeLocations.add(p);
			}
		}
		***/
	}


	private boolean needScan() {
		InfoMap mowerMap = cc.getMap(mowerID);
		int i = mowerX;
		int j = mowerY;
		int[][] neis = new int[][] { { i, j + 1 }, { i + 1, j + 1 }, { i + 1, j }, { i + 1, j - 1 }, { i, j - 1 },
				{ i - 1, j - 1 }, { i - 1, j }, { i - 1, j + 1 } };
		for (int[] nei : neis) {
			if (mowerMap.checkSquare(nei[0], nei[1]) == -1) {
				return true;
			}
		}
		return false;
	}

	// canCut(): check if mower can cut grass by moving at the current direction
	private boolean canCut() {
		if (validMove()==0) {
			return false;
		}
		InfoMap mowerMap = cc.getMap(mowerID);
		return mowerMap.checkSquare(mowerX + xDIR_MAP.get(mowerDirection), mowerY + yDIR_MAP.get(mowerDirection)) == GRASS_CODE;
	}

	private boolean canCharge() {
		if (validMove()==0) {
			return false;
		}
		InfoMap mowerMap = cc.getMap(mowerID);
		return mowerMap.checkSquare(mowerX + xDIR_MAP.get(mowerDirection), mowerY + yDIR_MAP.get(mowerDirection)) == CHARGE_CODE;
	}

	// return valid move distance
	private int validMove() {
		int a = mowerX + xDIR_MAP.get(mowerDirection);
		int b = mowerY + yDIR_MAP.get(mowerDirection);
		InfoMap mowerMap = cc.getMap(mowerID);
		int square = mowerMap.checkSquare(a, b);
		int ret = 0;
		if (square == EMPTY_CODE || square == GRASS_CODE || square == CHARGE_CODE){
			ret = 1;
		}
		int square2 = mowerMap.checkSquare(a+xDIR_MAP.get(mowerDirection), b+yDIR_MAP.get(mowerDirection));
		if ((square2 == EMPTY_CODE || square2 == GRASS_CODE || square2 == CHARGE_CODE) && ret==1){
			ret = 2;
		}
		return ret;
	}

	// move() : mover the mower, if the mower hit the crate or the fence, make
	// crashed = true and return, otherwise, generate the motion.

	private void move(int distance) {
		int dx = xDIR_MAP.get(mowerDirection);
		int dy = yDIR_MAP.get(mowerDirection);
		for (int i=0;i<distance;i++){
			sim.validateMove(mowerID, dx, dy);
		}
		curEnergy-=2; //move 1/2 step spend 2 energy
		//added redirection after move
		int dir_index = canCutAfterTurning();
		if (dir_index >=0 ){
			mowerDirection = dirs[dir_index];
			trackNewDirection = mowerDirection;
		}
		trackAction = "move";
		trackMoveDistance = distance;
		trackNewDirection = mowerDirection;
		displayActionAndResponses();
	}

	private void turning(String dir) { //only turn
		this.curEnergy--; //move 0 step cost 1 energy
		trackAction = "move";
		trackMoveDistance = 0;
		mowerDirection = dir;

		trackNewDirection = mowerDirection;
		System.out.println(String.format("move,0,%s", trackNewDirection));
	//	displayActionAndResponses();
	}

	private int canCutAfterTurning() {
		int i = mowerX ;
		int j = mowerY;
		InfoMap mowerMap = cc.getMap(mowerID);
		int[][] neis = new int[][] { { i, j + 1 }, { i + 1, j + 1 }, { i + 1, j }, { i + 1, j - 1 }, { i, j - 1 },
				{ i - 1, j - 1 }, { i - 1, j }, { i - 1, j + 1 } };
		for (int k = 0; k < 8; k++) {
			if (mowerMap.checkSquare(neis[k][0], neis[k][1]) == GRASS_CODE) {
				return k;
			}
		}
		return -1;
	}

	private int canChargeAfterTurning() {
		int i = mowerX ;
		int j = mowerY;
		InfoMap mowerMap = cc.getMap(mowerID);
		int[][] neis = new int[][] { { i, j + 1 }, { i + 1, j + 1 }, { i + 1, j }, { i + 1, j - 1 }, { i, j - 1 },
				{ i - 1, j - 1 }, { i - 1, j }, { i - 1, j + 1 } };
		for (int k = 0; k < 8; k++) {
			if (mowerMap.checkSquare(neis[k][0], neis[k][1]) == CHARGE_CODE) {
				return k;
			}
		}
		return -1;
	}

	/***
	private void recharge(){
		InfoMap mowerMap = cc.getMap(mowerID);
		//if charger is on the direction of moving and energy is less than 4
		int xDir = xDIR_MAP.get(trackNewDirection);
		int yDir = yDIR_MAP.get(trackNewDirection);
		int square1 = mowerMap.checkSquare(mowerX+xDir, mowerY+yDir);
		int square2 = mowerMap.checkSquare(mowerX+2*xDir, mowerY+2*yDir);
		//recharge if energy is low
		if (square1==CHARGE_CODE && curEnergy<=4 && curEnergy>=1){
			move();
		} else if ((square1==EMPTY_CODE || square1==GRASS_CODE) && square2==CHARGE_CODE && curEnergy<=4 && curEnergy>=2){
			//need to move 2 distance to recharge;
		}
	}
	***/



	// find next boundary, or grass to cut, return the closest one
	private List<String> findPath() {
		InfoMap mowerMap = cc.getMap(mowerID);
		int[][] visited = new int[mapWidth][mapHeight];
		int[][] neis = new int[][] { { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 },
				{ -1, 1 } };
		// Use Best First Search to find
//		renderLawn(cc.mowerMaps[mowerID].map);
		Queue<Point> queue = new ArrayDeque<>();
		queue.offer(new Point(mowerX, mowerY));
		int step = 1;
		visited[mowerX][mowerY] = step;
		int target_x = 1000;
		int target_y = 1000;
		boolean flag = true;
		while (flag && !queue.isEmpty()) {
			int size = queue.size();
			step++;
			for (int k = 0; k < size && flag; k++) {
				Point cur = queue.poll();
				for (int[] nei : neis) {
					int neiX = cur.x + nei[0];
					int neiY = cur.y + nei[1];

					if (visited[neiX][neiY] == 0) {
						int square = mowerMap.checkSquare(neiX, neiY);
						if (square == -1 || square == 1) {
							visited[neiX][neiY] = step;
							target_x = neiX;
							target_y = neiY;
							flag = false;
							break;
						} else if (square == 0) {
							queue.offer(new Point(neiX, neiY));
							visited[neiX][neiY] = step;
						}
					}
				}

			}
		}

		List<String> res = new ArrayList<>();
		if (target_x == 1000) {
			return res;
		}

		// Find the path using greedy method, the last
		while (step > 1) {
			for (int k = 0; k < 8; k++) {
				int[] nei = neis[k];
				int neiX = target_x - nei[0];
				int neiY = target_y - nei[1];
				if (visited[neiX][neiY] == step - 1) {
					// if the target location is unexplored, do not move to that location, need to
					// scan it first
					if (mowerMap.checkSquare(target_x, target_y) != UNKNOWN_CODE) {
						res.add(dirs[k]);
					}
					target_x = neiX;
					target_y = neiY;
					step--;
					break;
				}
			}
		}
		Collections.reverse(res);
		// System.out.println(res.toString());
		return res;
	}

	/***
	private boolean isValid(int x, int y) {
		return x >=0 && x < 31 && y >= 0 && y < 31;
	}
	***/
	// (1) if there is unknown area around the mower, scan first;
	// (2) if mower can cut any grass without turning, move along this direction
	// (3) if the mower can cut grass after turning, change the direction of the
	// mower;
	// (4) if the mower cannot cut grass after turning, move to the nearest area
	// that is unknown or has grass based on the path finding method.
	// first, find the path; second move one step along the path. Since the map is
	// constantly updating, we might need to change the path everything time.
	// Therefore, path need to be recalculated at each pollMowerForAction.
	public String pollMowerForAction() {
		// Since the mowerMaps is updating, at the beginning of the action, get the
		// latest map, and the position of the mower;
		// the map and the mower position is updated in the commchannel.
		if(curEnergy <= 0) {
			return ("stall,0");
		}
		System.out.println(String.format("mower_%d", mowerID+1));
		while(stallTurn > 0) {
			stallTurn--;
			trackAction = "stall";
			System.out.println("stall,0");
			return "stall,0";
		}
		mowerX = cc.mowerRelativeLocation[mowerID][0];
		mowerY = cc.mowerRelativeLocation[mowerID][1];
		// check if need scan
		if (needScan()) {
			scan(mowerID);
			return "scan";
		}
		if(curEnergy < 0.5 * maxEnergy) {
			int dir_index = canChargeAfterTurning();
			if(canCharge()) {
				trackMoveDistance = 1;
				move(trackMoveDistance);
				return String.format("move,%d,%s",trackMoveDistance,trackNewDirection);
			} else if(dir_index >= 0) {
				turning(dirs[dir_index]);
				return String.format("move,%d,%s",trackMoveDistance,trackNewDirection);
			}
		}
		// check if can cut without turning
		if (canCut()) {
			trackMoveDistance = validMove();
			move(trackMoveDistance);
			return String.format("move,%d,%s",trackMoveDistance,trackNewDirection);
		}
		// check if can cut after turning
		int dir_index = canCutAfterTurning();
		if (dir_index >= 0) {
			turning(dirs[dir_index]);
			return String.format("move,%d,%s",trackMoveDistance,trackNewDirection); // return direction when redirect
		}
		// find the path, and move one step along the path.
		path = findPath();
		if (path.size() > 0) {
			String dir = path.get(0);
			if (dir == mowerDirection) {
				trackMoveDistance = validMove();
				move(trackMoveDistance);
				// path.remove(0);
			} else {
				turning(dir);
			}

		}
		return String.format("move,%d,%s",trackMoveDistance,trackNewDirection);
	}


	public void displayActionAndResponses() {
		// display the mower's actions - no need
	//	System.out.print(trackAction);
		if (trackAction.equals("move")) {
			System.out.println("move," + trackMoveDistance + "," + trackNewDirection);
		} else {
			System.out.println();
		}

		// display the simulation checks and/or responses
		if (trackAction.equals("move") | trackAction.equals("turn_off")) {
			trackMoveCheck = crashed ? "crash" : "ok";
			System.out.println(trackMoveCheck);
		} else if (trackAction.equals("scan")) {
			System.out.println(trackScanResults);
		} else {
			System.out.println("action not recognized");
		}
	}


	public void position() {
		System.out.println("mowerX: " + mowerX + " mowerY: " + mowerY);
	}

	public int getMoveDistance(){
		return this.trackMoveDistance;
	}

}
