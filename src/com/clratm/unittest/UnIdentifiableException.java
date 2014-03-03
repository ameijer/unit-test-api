/*
 * File: UnIdentifiableException.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * Created for Innovative Defense Technology's Winter 2014 Winter programming contest, representing Bucknell University.
 * January 28, 2014
 */

package com.clratm.unittest;

/**
 * Thrown when an object cannot be identified in a unique (but unable to be
 * replicated) fashion.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public class UnIdentifiableException extends Exception {

	// Parameterless Constructor
	public UnIdentifiableException() {
	}

	private static final long serialVersionUID = 1L;

	// Constructor that accepts a message
	public UnIdentifiableException(String message) {
		super(message);
	}
}
