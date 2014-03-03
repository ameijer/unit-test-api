package com.clratm.unittest;

/*
 * File: IdentifibleTest.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class IdentifiableTest {
	
	public static void main(String[] args) throws UnIdentifiableException, FileNotFoundException, UnsupportedEncodingException{
		
		//Part 1: Our tester does not accept objects that it cannot identify
		Tester.INSTANCE.enable(true);
		
		Car expected = new Car(2000, "Mustang", "Ford");
		Car same_expected = new Car(2000, "Mustang", "Ford");
		
		System.out.println("toString of 2 identical cars: " + expected + "   and   " + same_expected + "\n\n");
		
		Car toTest = new Car(0, "Mustang", "Ford");
	
		//exception will be thrown here
		Tester.INSTANCE.input(toTest, expected);
		Tester.INSTANCE.load(toTest);
		
		toTest.drive(2000);
		
		Tester.INSTANCE.log(expected);
		
		
		
		
		//Part 2: Same object will work once interface is implemented
		Tester.INSTANCE.enable(true);
		
		IdentifiableCar _expected = new IdentifiableCar(2000, "Mustang", "Ford");
		IdentifiableCar _toTest = new IdentifiableCar(0, "Mustang", "Ford");
	
		Tester.INSTANCE.input(_toTest, _expected);
		Tester.INSTANCE.load(_toTest);
		
		_toTest.drive(2000);
		
		Tester.INSTANCE.log(_expected);
		
		
		Tester.INSTANCE.printResultReport(null, true);
	}
	
}
