/*
 * File: TesterHealthCheck.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import com.twmacinta.util.MD5;

/**
 * A class used to demonstrate the functionality of the Tester. Each requirement
 * outlined in the problem statement is tested here.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public class TesterHealthCheck {

	/**
	 * A quick health check on our tester object.
	 * @param args
	 * @throws UnIdentifiableException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnIdentifiableException, FileNotFoundException, UnsupportedEncodingException {
		// Requirement 2
		byte _byte = 0x42;
		Tester.INSTANCE.enable(true);
		Tester.INSTANCE.input(_byte, _byte);
		Tester.INSTANCE.load(_byte);
		MD5 md5 = new MD5();
		System.out.println("Native libraries enabled: " + md5.initNativeLibrary());

		if (1 == (2 - 1)) {
			Tester.INSTANCE.log(_byte);
		}

		// Requirement 3
		short _short = 143;
		Tester.INSTANCE.input(_short, _short);
		Tester.INSTANCE.load(_short);

		if (1 == 3) {
			Tester.INSTANCE.log(_short);
		} else {
			Tester.INSTANCE.log(_short);
		}

		// Requirement 4
		int _int = 144;
		Tester.INSTANCE.input(_int, _int);
		Tester.INSTANCE.load(_int);

		if (1 == 3) {
			Tester.INSTANCE.log(_int);
		} else {
			Tester.INSTANCE.log(_int);
		}

		// Requirement 5
		long _long = 123456789;
		Tester.INSTANCE.input(_long, _long);
		Tester.INSTANCE.load(_long);

		if (1 == 3) {
			Tester.INSTANCE.log(_long);
		} else {
			Tester.INSTANCE.log(_long);
		}

		// Requirement 6
		float _float = (float) 123459.10;
		Tester.INSTANCE.input(_long, _long);
		Tester.INSTANCE.load(_long);

		if (1 == 3) {
			Tester.INSTANCE.log(_long);
		} else {
			Tester.INSTANCE.log(_long);
		}

		// Requirement 7
		double _double = 123459.11;
		Tester.INSTANCE.input(_double, _double);
		Tester.INSTANCE.load(_double);

		if (1 == 3) {
			Tester.INSTANCE.log(_double);
		} else {
			Tester.INSTANCE.log(_double);
		}

		// Requirement 8
		boolean _bool = true;
		Tester.INSTANCE.input(_bool, _bool);
		Tester.INSTANCE.load(_bool);

		if (1 == 3) {
			Tester.INSTANCE.log(_bool);
		} else {
			Tester.INSTANCE.log(_bool);
		}

		// Requirement 9
		char _char = 'c';
		Tester.INSTANCE.input(_char, _char);
		Tester.INSTANCE.load(_char);

		if (1 == 3) {
			Tester.INSTANCE.log(_char);
		} else {
			Tester.INSTANCE.log(_char);
		}

		// Requirement 10
		String _str = "string";
		Tester.INSTANCE.input(_str, _str);
		Tester.INSTANCE.load(_str);

		if (1 == 3) {
			Tester.INSTANCE.log(_str);
		} else {
			Tester.INSTANCE.log(_str);
		}

		// Requirement 10
		int[] _ints = new int[] { 1, 2, 4 };
		Tester.INSTANCE.input(_ints, _ints);
		Tester.INSTANCE.load(_ints);

		if (1 == 3) {
			Tester.INSTANCE.log(_ints);
		} else {
			Tester.INSTANCE.log(_ints);
		}

		// Requirement 15 - Disabling at runtime
		Tester.INSTANCE.enable(false);
		// This would normally fail if tester was enabled
		String[] _strs = new String[] { "Str1", "Str2" };
		Tester.INSTANCE.input(_strs, _strs);
		Tester.INSTANCE.load(_strs);

		if (1 == 3) {
			Tester.INSTANCE.log(false);
		} else {
			Tester.INSTANCE.log(false);
		}

		Tester.INSTANCE.enable(true);

		Tester.INSTANCE.printResultReport(null, false);
		Tester.INSTANCE.printCoverageReport(null, false);

	}
}
