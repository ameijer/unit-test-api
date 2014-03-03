/*
 * File: Result.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

import java.util.Date;

/**
 * Stores information about a test result. This includes the time the test was
 * run, the class tested, the method tested, the input to be tested, the tested
 * code's output, the expected output, and the result.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public class Result implements Comparable<Result> {
	public enum Status {
		INCOMPLETE, PASSED, FAILED, UNEXPECTED;
	}

	protected String method, clazz, expectedHash;
	protected Object input, result, expectedResult;
	protected Date date;
	protected Status status;

	public Result(Date date, String className, String method, Object input, Object result, Object expectedResult, Status status) {
		this.method = method;
		this.clazz = className;
		this.input = input;
		this.result = result;
		this.expectedResult = expectedResult;
		this.date = date;
		this.status = status;
	}

	@Override
	public int compareTo(Result result) {
		if (this.clazz != null && result.clazz != null && this.method != null && result.method != null) {
			if (this.clazz.compareToIgnoreCase(result.clazz) == 0) // same class
				return this.method.compareToIgnoreCase(result.method);
			else
				return this.clazz.compareToIgnoreCase(result.clazz);
		}
		return 0;
	}

	public String getExpectedHash() {
		return expectedHash;
	}

	public void setExpectedHash(String expectedHash) {
		this.expectedHash = expectedHash;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(Object expectedResult) {
		this.expectedResult = expectedResult;
	}

	public Object getInput() {
		return input;
	}

	public void setInput(Object input) {
		this.input = input;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Input: " + input + " Output: " + expectedResult;
	}
}