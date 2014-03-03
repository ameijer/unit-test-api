/*
 * File: IdentifiableCar.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

//used to demonstrate the identifiable interface
public class IdentifiableCar extends Car implements Identifiable{

	public IdentifiableCar(int mileage, String model, String make) {
		super(mileage, model, make);
	}

	@Override
	public String id() {
		
		//here we simply concatenate all the class data members
		return make + " " + model + ", mileage: " + mileage;
	}

}
