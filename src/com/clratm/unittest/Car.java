/*
 * File: Car.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */
package com.clratm.unittest;

//Does not implement our identifiable interface
public class Car {
	
	public int mileage;
	public String model, make;
	
	
	public Car(int mileage, String model, String make){
		this.mileage = mileage;
		this.model = model;
		this.make = make;
	}

	public void drive(int miles){
		mileage += miles;
	}
	
	
	//Note that we use java's default toString
	
	
	
}
