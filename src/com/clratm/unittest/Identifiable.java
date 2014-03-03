/*
 * File: Identifiable.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

/**
 * The Interface Identifiable. Implement this interface to permit comparison of
 * objects in testing code.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public interface Identifiable {

	/**
	 * Generate a String that uniquely identifies this object. This String can
	 * be created from class data/state, and will be used to determine compare
	 * with other objects of the same type to determine equivalence.
	 * 
	 * @return the String identifying the state of the object to compare
	 */
	public String id();
}