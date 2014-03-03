/*
 * File: CoverageResult.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

/**
 * Stores information about a coverage result. This includes everything that a
 * Result contains in addition to a coverage String.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public class CoverageResult extends Result {

	/**
	 * Stores the fraction of the covered outputs over the total outputs.
	 */
	private String coverage;

	public CoverageResult(String clazz, String method, String coverage) {
		super(null, clazz, method, null, null, null, null);
		this.coverage = coverage;
	}

	@Override
	public String toString() {
		return "Class: " + clazz + "\nMethod: " + method + "\nCoverage: " + coverage;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}
}
