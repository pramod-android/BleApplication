package com.mywork.bleapplication;


import java.io.Serializable;

public class AirGraphItem implements Serializable {

	private double volume;

	private double second;

	private double flow;

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getSecond() {
		return second;
	}

	public void setSecond(double second) {
		this.second = second;
	}

	public double getFlow() {
		return flow;
	}

	public void setFlow(double flow) {
		this.flow = flow;
	}

	@Override
 	public String toString(){
		return 
			"AirGraphItem{" + 
			"volume = '" + volume + '\'' + 
			",seconds = '" + second + '\'' +
			",flow = '" + flow + '\'' + 
			"}";
		}


}