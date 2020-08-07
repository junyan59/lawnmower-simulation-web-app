package com.example.mainpanel.back_end;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//The key logic of communication channel is to updated the information on the individual mower map and the shared mower map.
//If the mower send a scan request to the simulator, the simulator will call the updateMowerMap() method here to update the map (including shared map).
//if the mower send a move request to the simulator, the simulator will validate the move and update the mower position and map in communication channel.

//Logic to update map
//case 1: mergePartialMowerMap(int mowerOneID, int mowerTwoID, int dx, int dy)
//in this case, mowerOne discover mowerTwo through scanning. And at least one of then does not know its absolute position in lawn.
//They can share their partial map.
//After merge, we should check if all boundaries are found. If all are found, we should meger it into full mowerMap. Call the mergeFullMowerMap function.

//case 2:  mergeFullMowerMap(int mowerIDOne)
//in this case, mowerIDOne discovered its absolute position, it can merger with the maps of all other mowers that already find their positions.
//Since all the mowers (with absolute position know) shares the same map, then we merge map of mower one with the shared map.
//We need to find the relative position of the maps and , we need to update the position of the mower in the new map.

//These fields are new
//boolean[][] mowerBoundary = new boolean[mowerNumber][4], it represents whether the four boundaries of the map are found.
//0: left, 1: right, 2: up, 3: down;
//for example,  mowerBoundary[1][0] = false: mower1 left boundary not found;
//            mowerBoundary[1][1] = true: mower 1 right boundary is found;
//boolean[] mowerFindAbsolutePosition, check if the mower find its absolute position in the map.

public class CommunicationChannel {
	public InfoMap[] mowerMaps;
	public int[][] mowerRelativeLocation;
	// left, right, up, down;
	private static boolean[][] mowerBoundary;
	private static boolean[] mowerFindAbsolutePosition;
	Map<Integer, Set<Integer>> mergedMap;

	// Add these static variables
	private final int UNKNOWN_CODE = -1;
//	private final int EMPTY_CODE = 0;
//	private final int GRASS_CODE = 1;
//	private final int CRATER_CODE = 2;
	private final int FENCE_CODE = 3;
	private final int CHARGE_CODE = 4;
//	private final int MOWER_CODE = 5;

	// Add the list of mower here, and the size of the map. we need to access the
	// individual mowers using the mowerList
	public static Mower[] mowerList;
	int mapWidth = 35;
	int mapHeight = 35;

	// should pass in the list of mowers to the constructor
	public CommunicationChannel(int numMowers) {
		// all the mowerMaps and mowerRelativeLocation are the same at beginning
		mowerMaps = new InfoMap[numMowers];
		mowerRelativeLocation = new int[numMowers][2];
		mapHeight = 35; // lawn height is 1~15 inclusive
		mapWidth = 35; // lawn width is 1~15 inclusive
		for (int i = 0; i < numMowers; i++) {
			//In the beginning all mower maps are the same.
			mowerMaps[i] = new InfoMap(mapWidth, mapHeight, 0, null, 0, null, false);
			mowerRelativeLocation[i][0] = 17;
			mowerRelativeLocation[i][1] = 17;
		}

		mowerBoundary = new boolean[numMowers][4];
		mowerFindAbsolutePosition = new boolean[numMowers];
		mergedMap = new HashMap<>();		
		for (int i = 0; i < numMowers; i++) {
			mergedMap.put(i, new HashSet<Integer>());
		}
		
	}



	public InfoMap getMap(int mowerID) {
		return mowerMaps[mowerID];
	}

	public void updateMowerLocation(int mowerID, int mowerX, int mowerY) {
		mowerRelativeLocation[mowerID][0] = mowerX;
		mowerRelativeLocation[mowerID][1] = mowerY;
	}

	public void shareMaps(int mowerID, HashSet<Integer> discovered_mowers) {
		InfoMap shared_map = mowerMaps[mowerID];
		for (int i : discovered_mowers) {
			mowerMaps[i] = shared_map;
		}
	}

	public void updateMowerMap(int mowerID, int[] scanResult) {
		int x = mowerRelativeLocation[mowerID][0];
		int y = mowerRelativeLocation[mowerID][1];
		int[][] mowerMap = mowerMaps[mowerID].map;
		int[][] neis = new int[][] { { x, y + 1 }, { x + 1, y + 1 }, { x + 1, y }, { x + 1, y - 1 }, { x, y - 1 },
				{ x - 1, y - 1 }, { x - 1, y }, { x - 1, y + 1 } };
		for (int i = 0; i < 8; i++) {
			int[] nei = neis[i];
			x = nei[0];
			y = nei[1];
			int grid_type = scanResult[i];
			if (!mowerFindAbsolutePosition[mowerID] && grid_type == FENCE_CODE) {
				// check left/west, right/east, up/north and down/south boundary
				if (!mowerBoundary[mowerID][0] && i == 6) {
					for (int k = 0; k < mapHeight; k++) {
						mowerMap[x][k] = 3;
					}
					mowerBoundary[mowerID][0] = true;
				} else if (!mowerBoundary[mowerID][1] && i == 2) {
					for (int k = 0; k < mapHeight; k++) {
						mowerMap[x][k] = 3;
					}
					mowerBoundary[mowerID][1] = true;
				} else if (!mowerBoundary[mowerID][2] && i == 0) {
					for (int k = 0; k < mapWidth; k++) {
						mowerMap[k][y] = 3;
					}
					mowerBoundary[mowerID][2] = true;
				} else if (!mowerBoundary[mowerID][3] && i == 4) {
					for (int k = 0; k < mapWidth; k++) {
						mowerMap[k][y] = 3;
					}
					mowerBoundary[mowerID][3] = true;
				}
				// check if it finds all of the boundaries, if yes, it knows its absolute
				// position in lawn and can also see other mowers
				if (checkIfKnowPositionInLawn(mowerID)) {
					mergeFullMowerMap(mowerID);
					mowerFindAbsolutePosition[mowerID] = true;
				}
			} else {
				mowerMap[x][y] = grid_type;
				if (grid_type >= 5) { //case mower square
					int secondMowerID;
					if (grid_type>100 && grid_type%10==CHARGE_CODE){ //mower+charge
						secondMowerID = (grid_type - CHARGE_CODE - 100)/10;//if a square contains both charge and mower, code = mowerID*10+100+CHARGE_CODE						
					} else {
						secondMowerID = grid_type - 5;
					}
					// if(!mergedMap.get(mowerID).contains(secondMowerID)) {
					int dx = x - mowerRelativeLocation[mowerID][0];
					int dy = y - mowerRelativeLocation[mowerID][1];

					// check: at least one of them is not in full Map;
					if (mowerMaps[mowerID].map != mowerMaps[secondMowerID].map) {
					//	System.out.println(mowerRelativeLocation[mowerID][0] +"ssssssss" +mowerRelativeLocation[mowerID][1]);
					//	System.out.println(secondMowerID + " relative pos: " + dx + " " + dy);
						mergePartialMowerMap(mowerID, secondMowerID, dx, dy);

					}
				}
			}

		}
	}

	// find the relative origin of the lawn in the map
	private int[] findRelativeOrigin(int mowerID) {
		InfoMap mowerMap = mowerMaps[mowerID];
		int[] res = new int[2];
		for (int i = 0; i < mowerMap.getLawnWidth() - 1; i++) {
			for (int j = 0; j < mowerMap.getLawnHeight() - 1; j++) {
				if (mowerMap.checkSquare(i, j) == FENCE_CODE && mowerMap.checkSquare(i+1, j) == FENCE_CODE && mowerMap.checkSquare(i, j+1) == FENCE_CODE) {
					res[0] = i + 1;
					res[1] = j + 1;
					return res;
				}
			}
		}
		return res;
	}

	private void mergeFullMowerMap(int mowerID) {
		int secondMowerID = findMowerWithKnowPosition();
		if (secondMowerID >= 0) {
			int dx = findRelativeOrigin(secondMowerID)[0] - findRelativeOrigin(mowerID)[0];
			int dy = findRelativeOrigin(secondMowerID)[1] - findRelativeOrigin(mowerID)[1];
			mergeToSharedMowerMap(mowerID, secondMowerID, dx, dy);
		}
	}

	// merge mower partial map (at least one of them does not know its absolute
	// position), merge mower one map with mower two map;
	// java will pass the reference and the two maps will be the same (share the
	// same object) after assign reference.
	private void mergePartialMowerMap(int mowerOneID, int mowerTwoID, int dx, int dy) {
		// if these two mower already found their absolution position, no need to merge
//		Mower.renderLawn(mowerMaps[mowerTwoID].map);
		int dxx = dx + mowerRelativeLocation[mowerOneID][0] - 17 - (mowerRelativeLocation[mowerTwoID][0] - 17);
		int dyy = dy + mowerRelativeLocation[mowerOneID][1] - 17 - (mowerRelativeLocation[mowerTwoID][1] - 17);
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				int x = i + dxx;
				int y = j + dyy;
				if (isValidPosition(x, y) && mowerMaps[mowerTwoID].map[i][j] != -1 && mowerMaps[mowerOneID].map[x][y] == -1) {
					// System.out.println(mowerOneID + " " + mowerTwoID);

					mowerMaps[mowerOneID].map[x][y] = mowerMaps[mowerTwoID].map[i][j];
				}
			}
		}
		// assign the mower one map with the merge map; From now on, the two mowers will
		// use the same mower map.
		// They share the same mower map now, all updates will be on this map.
		mowerMaps[mowerTwoID].map = mowerMaps[mowerOneID].map;
		mowerRelativeLocation[mowerTwoID][0] = mowerRelativeLocation[mowerOneID][0] + dx;
		mowerRelativeLocation[mowerTwoID][1] = mowerRelativeLocation[mowerOneID][1] + dy;
		//System.out.println(mowerRelativeLocation[mowerTwoID][0] + "  y: " + mowerRelativeLocation[mowerTwoID][1]);

		// merge the discovered boundary
		mowerBoundary[mowerOneID][0] |= mowerBoundary[mowerTwoID][0];
		mowerBoundary[mowerOneID][1] |= mowerBoundary[mowerTwoID][1];
		mowerBoundary[mowerOneID][2] |= mowerBoundary[mowerTwoID][2];
		mowerBoundary[mowerOneID][3] |= mowerBoundary[mowerTwoID][3];

		// if after merger, it can find its absolute position, then merge with
		// fullMowerMap
		if (checkIfKnowPositionInLawn(mowerOneID)) {
			mergeFullMowerMap(mowerOneID);
			mowerFindAbsolutePosition[mowerOneID] = true;
		}
	}

	// merge mower map when their absolute position is know;
	private void mergeToSharedMowerMap(int mowerOneID, int mowerTwoID, int dx, int dy) {
		// merge mowerOne map into mowerTwo map
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				int x = i - dx;
				int y = j - dy;
				if (isValidPosition(x, y) && mowerMaps[mowerOneID].checkSquare(i, j) != UNKNOWN_CODE) {
					mowerMaps[mowerTwoID].updateMapSquare(x, y, mowerMaps[mowerOneID].checkSquare(i, j));
				}
			}
		}
		// assign the mower one map with the reference of mower two map;
		mowerMaps[mowerOneID] = mowerMaps[mowerTwoID];
		// update the position of mower one in the new map;
		mowerRelativeLocation[mowerOneID][0] = mowerRelativeLocation[mowerTwoID][0] - dx;
		mowerRelativeLocation[mowerOneID][1] = mowerRelativeLocation[mowerTwoID][1] - dy;
	}

	private boolean isValidPosition(int x, int y) {
		return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
	}

	private boolean checkIfKnowPositionInLawn(int mowerID) {
		return mowerBoundary[mowerID][0] && mowerBoundary[mowerID][1] && mowerBoundary[mowerID][2]
				&& mowerBoundary[mowerID][3];
	}

	private int findMowerWithKnowPosition() {
		for (int i = 0; i < mowerFindAbsolutePosition.length; i++) {
			if (mowerFindAbsolutePosition[i] == true) {
				return i;
			}
		}
		return -1;
	}

	// check how many mowers found by each other
	public void check() {
		for (int i = 0; i < mowerMaps.length; i++) {
			for (int j = i + 1; j < mowerMaps.length; j++) {

			//	System.out.println(i + " " + j + "  : " + (mowerMaps[i] == mowerMaps[j]));
			}
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.print(mowerBoundary[i][j]);
			}
			System.out.println();
		}
	}
}
