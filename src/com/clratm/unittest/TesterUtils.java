/*
 * File: TesterUtils.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A collection of methods that assist the Tester.
 * 
 * @author Alex Meijer and Christopher Rung
 */
public class TesterUtils {

	/**
	 * <p>
	 * Compliments of Apache Commons 3.1.
	 * </p>
	 * Thread safe due to synchronization
	 */
	public static synchronized byte[] addAll(byte[] array1, byte... array2) {
		if (array1 == null) {
			return array2.clone();
		} else if (array2 == null) {
			return array1.clone();
		}
		byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * Helper function for byetify method.
	 * 
	 * @param arrayToCheck
	 * @return true, if successful
	 */
	public static boolean checkArray(Object arrayToCheck) {
		for (int i = 0; i < Array.getLength(arrayToCheck); i++) {
			try {
				byteify(Array.get(arrayToCheck, i));
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (UnIdentifiableException e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a byte array that represents the passed long. This was modified
	 * from http://stackoverflow.com/questions/18687772/java-converting-long-to-
	 * bytes-which-approach-is-more-efficient
	 * 
	 * @param v
	 *            the long to be converted
	 * @return a byte array representing v
	 */
	public static byte[] longToBytes(long v) {
		byte[] writeBuffer = new byte[8];
		writeBuffer[0] = (byte) (v >>> 56);
		writeBuffer[1] = (byte) (v >>> 48);
		writeBuffer[2] = (byte) (v >>> 40);
		writeBuffer[3] = (byte) (v >>> 32);
		writeBuffer[4] = (byte) (v >>> 24);
		writeBuffer[5] = (byte) (v >>> 16);
		writeBuffer[6] = (byte) (v >>> 8);
		writeBuffer[7] = (byte) (v >>> 0);

		return writeBuffer;
	}

	/**
	 * Converts the passed object to a more readable String that represents the
	 * object.
	 * 
	 * @param toPrint
	 *            object to print
	 * @return a String representing the object
	 * @throws UnIdentifiableException
	 */
	public static String objectPrinter(Object toPrint) throws UnIdentifiableException {
		String toReturn = "";
		if (toPrint == null) {
			return toReturn;
		}
		if (toPrint.getClass().isPrimitive()) {
			// we can ID primitives using toString
			toReturn = toPrint.toString();
		} else if (toPrint.getClass().isArray()) {
			if (Array.getLength(toPrint) >= 1) {
				if (toPrint.getClass().getComponentType().isPrimitive()) {
					switch (toPrint.getClass().getComponentType().toString()) {
					case ("int"):
						toReturn = Arrays.toString((int[]) toPrint);
						break;
					case ("byte"):
						toReturn = Arrays.toString((byte[]) toPrint);
						break;
					case ("boolean"):
						toReturn = Arrays.toString((boolean[]) toPrint);
						break;
					case ("short"):
						toReturn = Arrays.toString((short[]) toPrint);
						break;
					case ("long"):
						toReturn = Arrays.toString((long[]) toPrint);
						break;
					case ("float"):
						toReturn = Arrays.toString((float[]) toPrint);
						break;
					case ("double"):
						toReturn = Arrays.toString((double[]) toPrint);
						break;
					case ("char"):
						toReturn = Arrays.toString((char[]) toPrint);
						break;

					default:
						System.out.println("CASE STATEMENT ERROR, OBJECTPRINTER FUNCTION");
						throw new UnIdentifiableException();
					}

				} else if (Identifiable.class.isAssignableFrom(toPrint.getClass().getComponentType())) {
					// then we can id the elements in the array, and combine
					// them to get the overall id
					toReturn = "[ ";
					for (int i = 0; i < Array.getLength(toPrint); i++) {
						toReturn = toReturn + ((Identifiable) Array.get(toPrint, i)).id() + ", ";
					}
					// remove extraneous , from string and append ]
					toReturn = toReturn.substring(0, toReturn.length() - 2) + " ]";

				} else if (checkArray(toPrint)) {

					for (int i = 0; i < Array.getLength(toPrint); i++) {
						toReturn = toReturn + objectPrinter(Array.get(toPrint, i));
					}

				} else {
					// we can't ID the components in the array
					throw new UnIdentifiableException("Cannot ID array elements for printing");
				}

			} else {
				// as far as I know, we cannot ID an empty array, so to be safe
				// we should throw the exception
				// admittedly an edge case
				throw new UnIdentifiableException();
			}

		} else if (Identifiable.class.isAssignableFrom(toPrint.getClass())) {
			// we can call the id method to get a unique identifier
			toReturn = ((Identifiable) toPrint).id();

		} else if (!(toPrint.toString().equalsIgnoreCase(toPrint.getClass().getName() + '@' + Integer.toHexString(toPrint.hashCode())))) {
			// then we can use the toString method to identify it
			toReturn = toPrint.toString();
		} else {
			// we have no way to id the object
			throw new UnIdentifiableException();
		}

		// strip leading/trailing newlines
		while (toReturn.charAt(0) == '\n' || toReturn.charAt(0) == '\r') {
			toReturn = toReturn.substring(1);
		}

		while (toReturn.charAt(toReturn.length() - 1) == '\n' || toReturn.charAt(toReturn.length() - 1) == '\r') {
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}

		return toReturn;
	}

	/**
	 * Converts input object into array of bytes representing the object.
	 * 
	 * @param toConvert
	 *            the object to convert
	 * @return the byte array representing the object
	 * @throws UnIdentifiableException
	 */
	public static byte[] byteify(Object toConvert) throws UnIdentifiableException {
		byte[] out;
		if (toConvert.getClass().isPrimitive()) {
			// we can ID primitives using toString
			out = null;
			try {
				out = toConvert.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else if (toConvert.getClass().isArray()) {
			if (Array.getLength(toConvert) >= 1) {
				if (toConvert.getClass().getComponentType().isPrimitive()) {
					// we can id primitives using toString
					// combine the id to get the overall id
					ArrayList<Byte> listBytes = new ArrayList<Byte>();
					for (int i = 0; i < Array.getLength(toConvert); i++) {
						try {
							for (int j = 0; j < Array.get(toConvert, i).toString().getBytes("UTF-8").length; j++) {
								listBytes.add(Array.get(toConvert, i).toString().getBytes("UTF-8")[j]);
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					out = new byte[listBytes.size()];
					for (int k = 0; k < listBytes.size(); k++) {
						out[k] = listBytes.get(k);
					}

				} else if (Identifiable.class.isAssignableFrom(toConvert.getClass().getComponentType())) {
					// then we can id the elements in the array, and combine
					// them to get the overall id
					ArrayList<Byte> listBytes = new ArrayList<Byte>();
					for (int i = 0; i < Array.getLength(toConvert); i++) {
						try {
							for (int j = 0; j < ((Identifiable) Array.get(toConvert, i)).id().getBytes("UTF-8").length; j++) {
								listBytes.add(((Identifiable) Array.get(toConvert, i)).id().getBytes("UTF-8")[j]);
							}
						} catch (ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}

					out = new byte[listBytes.size()];

					for (int k = 0; k < listBytes.size(); k++) {
						out[k] = listBytes.get(k);
					}

				} else if (checkArray(toConvert)) { // if to string can be used
					ArrayList<Byte> listBytes = new ArrayList<Byte>();

					for (int i = 0; i < Array.getLength(toConvert); i++) {
						try {
							byte[] row = byteify(Array.get(toConvert, i));
							for (int j = 0; j < row.length; j++) {
								listBytes.add(row[j]);
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							e.printStackTrace();
						}
					}

					out = new byte[listBytes.size()];

					for (int k = 0; k < listBytes.size(); k++) {
						out[k] = listBytes.get(k);
					}

				} else { // we can't ID the components in the array
					throw new UnIdentifiableException("The components in the array are unidentifable");
				}

			} else {
				// as far as I know, we cannot ID an empty array, so to be safe
				// we should throw the exception
				// admittedly an edge case
				throw new UnIdentifiableException("Cannot ID an empty array");
			}

		} else if (Identifiable.class.isAssignableFrom(toConvert.getClass())) {
			// we can call the id method to get a unique identifier
			out = null;
			try {
				out = ((Identifiable) toConvert).id().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else if (!(toConvert.toString().equalsIgnoreCase(toConvert.getClass().getName() + '@' + Integer.toHexString(toConvert.hashCode())))) {
			// then we can use the toString method to identify it
			out = null;
			try {
				out = toConvert.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			// we have no way to id the object
			throw new UnIdentifiableException("Please implement the identifable interface");
		}

		return out;
	}
}