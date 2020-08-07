package com.example.mainpanel.back_end;


import java.util.HashMap;

public class InfoMap {

	private int mapHeight; // lawn height is 1~10 inclusive
	private int mapWidth; // lawn width is 1~15 inclusive
	public int[][] map;

	// hashmap to define mower movement on coordinate for each direction
	private static HashMap<String, Integer> xDIR_MAP = new HashMap<>();
	private static HashMap<String, Integer> yDIR_MAP = new HashMap<>();
	private static HashMap<Integer, String> type = new HashMap<>();

	// elements code
	private static final int UNKNOWN_CODE = -1;
	private static final int EMPTY_CODE = 0;
	private static final int GRASS_CODE = 1;
	private static final int CRATER_CODE = 2;
	private static final int FENCE_CODE = 3;
	private static final int CHARGE_CODE = 4;
	// mower start from 5(mowerID=0)
	// if a square contains both charge and mower, code = mowerID*10+100+CHARGE_CODE

	public InfoMap(int width, int height, int chargeNo, int[][] chargePosition, int craterNo, int[][] craterLocation,
			boolean fullMap) {

		// set hashmap for mower movement
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

		type.put(EMPTY_CODE, "empty");
		type.put(GRASS_CODE, "grass");
		type.put(CRATER_CODE, "crater");
		type.put(FENCE_CODE, "fence");
		type.put(CHARGE_CODE, "charge");
		for (int i = 5; i < chargeNo + 5; i++) {// mowerCode start from 5 (id=0)
			String mowerName = String.format("mower_%d", i - 4);
			type.put(i, mowerName);// mowerName start from 1
		}

		this.mapWidth = width;
		this.mapHeight = height;
		this.map = new int[width][height];
		// check if it is full map or partial map
		if (fullMap) { // full map -> fill grass
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					map[i][j] = GRASS_CODE;
				}
			}
		} else { // partial map -> fill unknown
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					map[i][j] = UNKNOWN_CODE;
				}
			}
		}

		// fill in the lawn location with CHARGE & mowers , chargeNo==mowerNo
		if (chargeNo > 0 && fullMap) {
			for (int id = 0; id < chargeNo; id++) {
				int mowerX = chargePosition[id][0];
				int mowerY = chargePosition[id][1];
				// if a square contains both charge and mower, code = mowerID*10+100+CHARGE_CODE
				// map[mowerX][mowerY] = id*10 + 100 + CHARGE_CODE;
				map[mowerX][mowerY] = id*10 + 100 + CHARGE_CODE;
			}
		}

		// fill in the map with craters
		if (craterNo > 0) {
			for (int i = 0; i < craterNo; i++) {
				int x = craterLocation[i][0];
				int y = craterLocation[i][1];
				map[x][y] = CRATER_CODE;
			}
		}

	}
	

	// return scanned info (integer)
	public int[] scan(int mowerX, int mowerY){
  		int[] info = new int[8];
  		//check from north and clockwise
  		String[] directions = {"north","northeast","east","southeast","south","southwest","west","northwest"};
  		for(int i=0;i<8;i++){
  			String direction = directions[i];
  			int xDir = xDIR_MAP.get(direction);
  			int yDir = yDIR_MAP.get(direction);
  			info[i] = this.checkSquare(mowerX+xDir, mowerY+yDir);
  		}
  		return info;
  	}

    //a function to check the square element (integer)
  	public int checkSquare(int x, int y){
  		if (x<0 || y<0 || x>=this.mapWidth || y>=this.mapHeight){
  			return FENCE_CODE;
  		}
  		return map[x][y];

  	}


  	public void updateScannedInfo(int mowerX, int mowerY, int[] scannedInfo){
  		String[] directions = {"north","northeast","east","southeast","south","southwest","west","northwest"};
  		for(int i=0;i<8;i++){
  			String direction = directions[i];
  			int xDir = xDIR_MAP.get(direction);
  			int yDir = yDIR_MAP.get(direction);
  			map[mowerX+xDir][mowerY+yDir] = scannedInfo[i];
  		}
  	}


  	public void updateMapSquare(int X, int Y, int newCode){
  		map[X][Y] = newCode;
  	}

  	//a function to translate square integer to element string
  	public static String translateSquare(int code){
  		if (code>100 && code%10 == CHARGE_CODE){ //mower + charge, code = mowerID*10+100+CHARGE_CODE
  			int mowerID = (code - CHARGE_CODE -100)/10;
  			int mowerCode = mowerID + 5; //mowerCode start from 5 (id+5)
  			return String.format("%s-%s", type.get(mowerCode), type.get(CHARGE_CODE));
  		}
  		return type.get(code);
  	}

  	public Integer getLawnWidth() {
        return mapWidth;
    }


    public Integer getLawnHeight() {
        return mapHeight;
    }


    public String[][] getLawnStatus() {
    	String[][] lawnStatus = new String[mapWidth][mapHeight];
    	for (int i=0;i<mapWidth;i++){
    		for (int j=0;j<mapHeight;j++){
    			int square_code = map[i][j];
    			lawnStatus[i][j] = translateSquare(square_code);
    		}
        }
        String[][] rotatedMap = rotateMatrix(lawnStatus);
        return rotatedMap;
    }

    // rotate lawnStatus map for UI implement
    public String[][] rotateMatrix(String[][] lawnStatus) {
        String[][] rotated = new String[lawnStatus[0].length][lawnStatus.length];
        for (int i = 0; i < lawnStatus[0].length; ++i) {
            for (int j = 0; j < lawnStatus.length; ++j) {
                rotated[i][j] = lawnStatus[j][lawnStatus[0].length - i - 1];
            }
        }
        return rotated;
    }


}