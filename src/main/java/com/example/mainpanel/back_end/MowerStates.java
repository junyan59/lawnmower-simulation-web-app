package com.example.mainpanel.back_end;


public class MowerStates {
    private int mower_id;
    private String mowerStatus;
    private int energyLevel;
    private int stallTurn;

    MowerStates (int mower_id, String mowerStatus, int energyLevel, int stallTurn) {
    	this.mower_id = mower_id+1;
    	this.mowerStatus = mowerStatus;
    	this.energyLevel = energyLevel;
    	this.stallTurn = stallTurn;
    }
    public int getMower_id() {
        return mower_id;
    }

    public void setMower_id(int mower_id) {
        this.mower_id = mower_id;
    }

    public String getMowerStatus() {
        return mowerStatus;
    }

    public void setMowerStatus(String mowerStatus) {
        this.mowerStatus = mowerStatus;
    }

    public int getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(int energyLevel) {
        this.energyLevel = energyLevel;
    }

    public int getStallTurn() {
        return stallTurn;
    }

    public void setStallTurn(int stallTurn) {
        this.stallTurn = stallTurn;
    }

}
