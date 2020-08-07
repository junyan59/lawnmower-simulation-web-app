package com.example.mainpanel.back_end;


public class Report {
    private int initalGrassCount;
    private int cutGrassCount;
    private int grassRemaining;
    private int turnCount;

    public Report(int initalGrassCount, int cutGrassCount, int grassRemaining, int turnCount){
    	this.initalGrassCount = initalGrassCount;
    	this.cutGrassCount = cutGrassCount;
    	this.grassRemaining = grassRemaining;
    	this.turnCount = turnCount;
    }

    public int getGrassRemaining() {
        return grassRemaining;
    }

    public void setGrassRemaining(int grassRemaining) {
        this.grassRemaining = grassRemaining;
    }


    public int getInitalGrassCount() {
        return initalGrassCount;
    }

    public void setInitalGrassCount(int initalGrassCount) {
        this.initalGrassCount = initalGrassCount;
    }

    public int getCutGrassCount() {
        return cutGrassCount;
    }

    public void setCutGrassCount(int cutGrassCount) {
        this.cutGrassCount = cutGrassCount;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
	}

}
