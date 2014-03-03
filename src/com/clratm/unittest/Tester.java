/*
 * File: Tester.java
 * Copyright 2014 Alexander Meijer and Christopher Rung
 * January 28, 2014
 */

package com.clratm.unittest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.clratm.unittest.Result.Status;
import com.twmacinta.util.MD5;

/**
 * This class handles all of the front-end testing functionality.
 * 
 * @author Alexander Meijer and Christopher Rung
 * @version 1.0.0
 */
public enum Tester {
	INSTANCE("Tester instance 0", 0), INSTANCE_1("Tester instance 1", 1), INSTANCE_2("Tester instance 2", 2), INSTANCE_3("Tester instance 3", 3), INSTANCE_4("Tester instance 4", 4), INSTANCE_5(
			"Tester instance 5", 5);

	/**
	 * Stores the input-output relationship.
	 */
	private ConcurrentHashMap<String, Result> input_expected;

	/**
	 * The name of the tester
	 */
	private final String name;

	/**
	 * The number of the tester. This can be any number from 0 to 5, which is an
	 * arbitrary design choice.
	 */
	private final int num;

	/**
	 * Stores the inputs that haven't been loaded yet. This will initially
	 * mirror expectedHash_inObject.
	 */
	private ConcurrentHashMap<String, Result> notUsed;

	/**
	 * Uniquely identifies each expected output in a function
	 */
	private ConcurrentHashMap<String, Long> identifiers;

	/**
	 * Stores the inputs that have been tested
	 */
	private ConcurrentHashMap<String, Result> used;

	/**
	 * Stores all expected outputs we are looking for. If we log a hash that
	 * matches one in here, we know that that case has been covered. That
	 * mapping will then be removed from this, and the object will be added to
	 * the covered list. Thus, the objects that remain in this list at the end
	 * of execution are the ones that have not been tested.
	 */
	private ConcurrentHashMap<String, Result> expectedHash_inObject;

	/**
	 * Assists with thread safety
	 */
	private final Object mutex = new Object();

	/**
	 * Flag that determines whether the tester class is active. If this is
	 * false, all possible efforts should be made to minimize the effect of the
	 * disabled tester on the runtime. Covers the
	 * "Be globally controlled so it is simple to enable or disable" condition
	 * stipulated by the contest description.
	 */
	private boolean active;

	/**
	 * Determines the width of the class, method, input, output, and expected
	 * output columns of the result report, as well as the width of the class
	 * and method output columns of the coverage report. Default is 30.
	 */
	private static final int columnWidth = 30;

	/**
	 * Determines the result report's column width.
	 */
	private static final String resultReportColumnWidth = "%28s%" + (columnWidth + 2) + "s%" + columnWidth + "s%" + columnWidth + "s%" + columnWidth + "s%" + columnWidth
			+ "s%15s\n";

	/**
	 * Determines the coverage report's column width.
	 */
	private static final String coverageReportColumnWidth = "%" + columnWidth + "s%" + columnWidth + "s%12s";

	/**
	 * Instantiates a new tester object
	 * 
	 * @param name
	 *            the name of the tester
	 * @param num
	 *            the number of the tester
	 */
	private Tester(String name, int num) {
		this.name = name;
		this.active = false;
		this.num = num;
		input_expected = null;
		expectedHash_inObject = null;
		used = null;
		notUsed = null;
		identifiers = null;
	}

	/**
	 * Enables the tester. This instantiates every HashMap that is used later.
	 * 
	 * @param active
	 *            if true, enables the tester
	 * @return true, if successful
	 */
	public boolean enable(boolean active) {
		this.active = active;
		// only create new objects if the tester is activated and the objects
		// don't already exist
		if (active && (input_expected == null || expectedHash_inObject == null || notUsed == null || used == null)) {
			input_expected = new ConcurrentHashMap<String, Result>();
			expectedHash_inObject = new ConcurrentHashMap<String, Result>();
			used = new ConcurrentHashMap<String, Result>();
			notUsed = new ConcurrentHashMap<String, Result>();
			identifiers = new ConcurrentHashMap<String, Long>();
		}
		return true;
	}

	/**
	 * Converts an object to its binary representation using the byteify
	 * function and adds the result to a byte array along with its method name
	 * and calling class name.
	 * 
	 * @param toSign
	 *            the object to convert
	 * @param useThreadId
	 *            if true, include the thread ID in the byte array.
	 * @return a byte array containing the byte representation of toSign
	 * @throws UnIdentifiableException
	 */
	private byte[] toBytesWithSignature(Object toSign, boolean useThreadId) throws UnIdentifiableException {
		byte[] in = TesterUtils.byteify(toSign);

		byte[] threadId = null;
		if (useThreadId) {
			threadId = TesterUtils.longToBytes(Thread.currentThread().getId());
		}
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		byte[] callingClass = null;
		try {
			callingClass = stackTraceElements[3].getClassName().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		byte[] callingFunct = null;
		try {
			callingFunct = stackTraceElements[3].getMethodName().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] completeResult = TesterUtils.addAll(in, threadId);
		completeResult = TesterUtils.addAll(completeResult, callingClass);
		completeResult = TesterUtils.addAll(completeResult, callingFunct);

		return completeResult;
	}

	/**
	 * Inputs an expected value to the tester. This will result in the object's
	 * toString method being called to get report information, but otherwise
	 * analogous to the BuiltInTester.expecting() function in the example code.
	 * 
	 * @param expectedInput
	 *            An expected value of the input object.
	 * @param expectedResult
	 *            The expected object that will be logged later in the code
	 * @return true, if successful
	 * @throws UnIdentifiableException
	 */
	public boolean input(Object expectedInput, Object expectedResult) throws UnIdentifiableException {
		if (active) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

			// identify expectedInput
			MD5 md5 = new MD5();

			byte[] expected = this.toBytesWithSignature(expectedInput, true);

			md5.Update(expected);

			Result pending = new Result(new Date(System.currentTimeMillis()), stackTraceElements[2].getClassName(), stackTraceElements[2].getMethodName(), null, null,
					expectedResult, Status.INCOMPLETE);
			// input_expected contains the hashed input value as its key mapped
			// to the expected result of the input.
			if (!input_expected.containsKey(md5.asHex())) {
				input_expected.put(md5.asHex(), pending);
			}

			byte[] completeResult = this.toBytesWithSignature(expectedResult, false);
			md5.Init();
			md5.Update(completeResult);

			// only add new cases if they have not been covered
			// since this does not use the thread ID in the hash, it could
			// technically be thread unsafe
			synchronized (mutex) {
				if (!used.containsKey(md5.asHex())) {
					notUsed.put(md5.asHex(), pending);
				}
			}
		}

		return true;
	}

	/**
	 * Inputs the actual value into the tester. This is used to establish the
	 * expected outcome of the segment of code.
	 * 
	 * @param actualObject
	 *            The actual input object that will be run through the code.
	 *            This is analogous to the inputValue in the example code.
	 * @return true, if successful
	 * @throws UnIdentifiableException
	 */
	public boolean load(Object actualObject) throws UnIdentifiableException {
		// here, we detect an input. We search for it in our input_expected map,
		// and if we find it we load the expected result, hash it along with the
		// calling function string and the thread ID, then store it in our
		// expectedHash_serializedObject map
		if (active) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

			MD5 md5 = new MD5();
			byte[] act = this.toBytesWithSignature(actualObject, true);
			md5.Update(act);

			String md5_original = md5.asHex();
			// now, search for the actual input to see if we are expecting it
			Result expected_result = input_expected.get(md5_original);

			byte[] threadId = TesterUtils.longToBytes(Thread.currentThread().getId());

			byte[] callingClass = null;
			try {
				callingClass = stackTraceElements[2].getClassName().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return false;
			}
			byte[] callingFunct = null;
			try {
				callingFunct = stackTraceElements[2].getMethodName().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}

			// obtain/create an identifier for this input
			md5.Init();
			byte[] methodInfo = TesterUtils.addAll(callingClass, callingFunct);
			methodInfo = TesterUtils.addAll(methodInfo, threadId);

			md5.Update(methodInfo);

			Long idLongWrap = identifiers.remove(md5.asHex());

			long idLong;

			if (idLongWrap == null) {
				// create a new Identifier for this thread and function
				// base case
				idLongWrap = (long) 0;
				idLong = 0;
			} else {
				// we have an identifier. Obtain and save
				idLong = idLongWrap;
			}
			// increment and store the identifier back
			idLongWrap++;
			identifiers.put(md5.asHex(), idLongWrap);

			if (expected_result == null) {
				// then we are not expecting this input object
				if (!expectedHash_inObject.containsKey(md5_original)) {
					Result unexp = new Result(new Date(System.currentTimeMillis()), stackTraceElements[2].getClassName(), stackTraceElements[2].getMethodName(), actualObject,
							null, null, Status.UNEXPECTED);
					expectedHash_inObject.put(md5_original, unexp);
				}

				return true;
			} else { // then we are expecting this object
				// assume failed until logged properly
				expected_result.setStatus(Status.FAILED);
				// remember the input
				expected_result.setInput(actualObject);

				// now:
				// -we know that sometime in the future, this thread will log
				// this result from this function from this class
				// -so if we hash these three elements together, then only 1
				// possible combination can result in success (otherwise it is
				// failure)

				byte[] expect_objBArr = TesterUtils.byteify(expected_result.getExpectedResult());

				md5.Init();
				md5.Update(expect_objBArr);
				String expected_hashed = md5.asHex();

				expected_result.setExpectedHash(expected_hashed);
				// now combine into a big array
				// byte[] completeResult = this.addAll(expected_hashed,
				// threadId);
				byte[] completeResult = TesterUtils.addAll(null, threadId);
				completeResult = TesterUtils.addAll(completeResult, callingClass);
				completeResult = TesterUtils.addAll(completeResult, callingFunct);
				completeResult = TesterUtils.addAll(completeResult, TesterUtils.longToBytes(idLong));

				// and then hash the big array
				md5.Init();
				md5.Update(completeResult);

				// now, we will use the completeResultHash as the key for our
				// expectedHash_Object map. This should guarantee that only the
				// matching
				// result under identical
				// circumstances will map to a non-null (and therefore correct)
				// value
				expectedHash_inObject.put(md5.asHex(), expected_result);
				return true;
			}
		}
		return true;
	}

	/**
	 * Detect an input then search for it in the expectedHash_inObject Map. If
	 * we find it, we load the expected result, hash it along with the calling
	 * function String and the thread ID, and then store it in our
	 * expectedHash_serializedObject map
	 * 
	 * @param toLog
	 *            the to log
	 * @return true, if successful
	 * @throws UnIdentifiableException
	 */
	public boolean log(Object toLog) throws UnIdentifiableException {
		if (active) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

			byte[] threadId = TesterUtils.longToBytes(Thread.currentThread().getId());
			byte[] callingClass = null;
			try {
				callingClass = stackTraceElements[2].getClassName().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
			byte[] callingFunct = null;
			try {
				callingFunct = stackTraceElements[2].getMethodName().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
			MD5 md5 = new MD5();

			// obtain the identifier if it exists
			md5.Init();
			byte[] methodInfo = TesterUtils.addAll(callingClass, callingFunct);
			methodInfo = TesterUtils.addAll(methodInfo, threadId);

			md5.Update(methodInfo);

			Long idLongWrap = identifiers.get(md5.asHex());

			long idLong;

			if (idLongWrap == null) {
				return false;
			} else {
				// We have an identifier. Obtain and save
				// need to decrement since we incremented before storing
				idLong = idLongWrap - 1;
			}

			byte[] toLogBArr = TesterUtils.byteify(toLog);
			md5.Init();
			md5.Update(toLogBArr);

			String actual_hashed = md5.asHex();

			// now combine into a big array
			byte[] completeResult = TesterUtils.addAll(null, threadId);
			completeResult = TesterUtils.addAll(completeResult, callingClass);
			completeResult = TesterUtils.addAll(completeResult, callingFunct);
			completeResult = TesterUtils.addAll(completeResult, TesterUtils.longToBytes(idLong));

			// and then hash the big array
			md5.Init();
			md5.Update(completeResult);

			// now, check the expectedHash_inObject Map for this hash. If it
			// exists, we will get the inputs/outputs associated with this input
			// null otherwise
			Result matchingResult = expectedHash_inObject.remove(md5.asHex());
			if (matchingResult != null) {
				if (matchingResult.getExpectedHash().equals(actual_hashed)) {
					// there is a result associated with this logged object
					matchingResult.setResult(toLog);
					matchingResult.setStatus(Status.PASSED);
					expectedHash_inObject.put(md5.asHex(), matchingResult);
				} else {
					matchingResult.setResult(toLog);
					matchingResult.setStatus(Status.FAILED);
					expectedHash_inObject.put(md5.asHex(), matchingResult);
				}
			}

			// now combine into a big array
			byte[] loggedResult = this.toBytesWithSignature(toLog, false);

			md5.Init();
			md5.Update(loggedResult);
			// since this does not use the thread ID in the hash, it could
			// technically be thread unsafe.
			synchronized (mutex) {
				if (notUsed.containsKey(md5.asHex())) {
					used.put(md5.asHex(), notUsed.remove(md5.asHex()));
				}
			}

		}
		return true;
	}

	/**
	 * Prints the result report to a file, which shows the time of the test,
	 * class tested, method tested, input value, output value, expected output
	 * value, and whether or not the test passed.
	 * 
	 * @param outFile
	 *            the out file. If null, print to the console.
	 * @param verbose
	 *            the verbose
	 * @return true, if successful
	 * @throws IOException
	 * @throws UnIdentifiableException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public synchronized boolean printResultReport(File outFile, boolean verbose) throws UnIdentifiableException, FileNotFoundException, UnsupportedEncodingException {
		if (active) {
			boolean isFile = outFile != null;
			PrintWriter writer = null;

			if (isFile) {
				writer = new PrintWriter(outFile, "UTF-8");
			}

			if (isFile) {
				System.out.println("Printing result report to " + outFile.getName());
				writer.println("Result report for tester number " + this.num + ":\n");
			} else {
				System.out.println("Result report for tester number " + this.num + ":\n");
			}

			ArrayList<Result> results = getResultList();

			// print header
			if (isFile)
				writer.format(resultReportColumnWidth, "Time", "Class", "Method", "Input", "Output", "Expected Output", "Result");
			else
				System.out.format(resultReportColumnWidth, "Time", "Class", "Method", "Input", "Output", "Expected Output", "Result");

			for (int i = 0; i < results.size(); i++) {
				Result temp = results.get(i);
				if (temp.getStatus() != Status.UNEXPECTED || verbose) {
					String input = TesterUtils.objectPrinter(temp.getInput());
					String output = TesterUtils.objectPrinter(temp.getResult());
					String expectedOutput = TesterUtils.objectPrinter(temp.getExpectedResult());
					String clazz = temp.getClazz();

					// if the result is unexpected, then it won't have an output
					if (temp.getStatus() == Status.UNEXPECTED) {
						output = "N/A";
						expectedOutput = "N/A";
					}

					// truncate outputs if they are too long
					if (input.length() > columnWidth)
						input = input.substring(0, columnWidth - 5) + "...";
					if (output.length() > columnWidth)
						output = output.substring(0, columnWidth - 5) + "...";
					if (expectedOutput.length() > columnWidth)
						expectedOutput = expectedOutput.substring(0, (columnWidth - 5)) + "...";
					if (clazz.length() > columnWidth)
						clazz = "..." + clazz.substring(clazz.length() - columnWidth + 3);

					if (isFile)
						writer.format(resultReportColumnWidth, temp.getDate(), clazz, temp.getMethod(), input, output, expectedOutput, temp.getStatus());
					else
						System.out.format(resultReportColumnWidth, temp.getDate(), clazz, temp.getMethod(), input, output, expectedOutput, temp.getStatus());
				}
			}
			if (isFile)
				writer.close();
		}
		return true;
	}

	/**
	 * Outputs the coverage report to the specified output file, or the console
	 * if no file is passed.
	 * 
	 * @param outFile
	 *            the output file
	 * @param verbose
	 *            the verbose
	 * @return true, if successful
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws UnIdentifiableException
	 */
	public synchronized boolean printCoverageReport(File outFile, boolean verbose) throws FileNotFoundException, UnIdentifiableException {
		// if a file is not passed to the method, print to the console.
		boolean isFile = outFile != null;

		ArrayList<CoverageResult> coverageResults = new ArrayList<CoverageResult>();

		if (active) {
			PrintWriter writer = null;
			if (isFile) {
				try {
					writer = new PrintWriter(outFile, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return false;
				}
			}

			if (isFile) {
				System.out.println("Printing coverage report to " + outFile.getName());
				writer.println("Coverage report for tester number " + this.num + ":\n");
			} else
				System.out.println("Coverage report for tester number " + this.num + ":\n");

			int totNumCovered = used.size();
			int totNumMissed = notUsed.size();

			if (isFile)
				writer.println("Overall Coverage: " + totNumCovered + " / " + (totNumCovered + totNumMissed) + "\n");
			else
				System.out.println("Overall Coverage: " + totNumCovered + " / " + (totNumCovered + totNumMissed) + "\n");

			// Print header: class name, method name, and coverage
			if (isFile) {
				writer.format(coverageReportColumnWidth, "Class", "Method", "Coverage");
				writer.println();
			} else {
				System.out.format(coverageReportColumnWidth, "Class", "Method", "Coverage");
				System.out.println();
			}

			for (String key : notUsed.keySet()) {
				int numCovered = 0;
				int total = 0;
				Result a = notUsed.get(key);

				for (String key2 : used.keySet()) {
					Result b = used.get(key2);
					if (a.getClazz().equals(b.getClazz()) && a.getMethod().equals(b.getMethod())) {
						numCovered++;
						total++;
					}
				}
				for (String key3 : notUsed.keySet()) {
					Result b = notUsed.get(key3);
					if (a.getClazz().equals(b.getClazz()) && a.getMethod().equals(b.getMethod())) {
						total++;
					}
				}

				coverageResults.add(new CoverageResult(a.getClazz(), a.getMethod(), numCovered + " / " + total));
			}

			for (String key : used.keySet()) {
				int numCovered = 0;
				int total = 0;
				Result a = used.get(key);

				for (String key2 : used.keySet()) {
					Result b = used.get(key2);
					if (a.getClazz().equals(b.getClazz()) && a.getMethod().equals(b.getMethod())) {
						numCovered++;
						total++;
					}
				}
				for (String key3 : notUsed.keySet()) {
					Result b = notUsed.get(key3);
					if (a.getClazz().equals(b.getClazz()) && a.getMethod().equals(b.getMethod())) {
						total++;
					}
				}

				coverageResults.add(new CoverageResult(a.getClazz(), a.getMethod(), numCovered + " / " + total));
			}

			// remove duplicates
			List<CoverageResult> dupeFreeCoverage = new ArrayList<CoverageResult>();
			List<String> dupeFreeCoverageStrings = new ArrayList<String>();

			List<String> coverageStrings = new ArrayList<String>();
			for (int i = 0; i < coverageStrings.size(); i++) {
				coverageStrings.add(coverageResults.get(i).toString());
			}

			for (int i = 0; i < coverageResults.size(); i++) {
				CoverageResult temp = coverageResults.get(i);
				if (!dupeFreeCoverageStrings.contains(temp.toString())) {
					dupeFreeCoverageStrings.add(temp.toString());
					dupeFreeCoverage.add(temp);
				}
			}

			Collections.sort(dupeFreeCoverage);

			// print the contents of coverageResults
			for (int i = 0; i < dupeFreeCoverage.size(); i++) {
				CoverageResult temp = dupeFreeCoverage.get(i);

				String clazz = temp.getClazz();
				String method = temp.getMethod();
				String coverage = temp.getCoverage();

				// truncate outputs if they are too long
				if (clazz.length() > columnWidth)
					clazz = "..." + clazz.substring(clazz.length() - columnWidth + 3);
				if (method.length() > columnWidth)
					method = method.substring(0, columnWidth - 5) + "...";
				if (coverage.length() > columnWidth)
					coverage = coverage.substring(0, columnWidth - 5) + "...";

				if (isFile) {
					writer.format(coverageReportColumnWidth, clazz, method, coverage);
					writer.println();
				} else {
					System.out.format(coverageReportColumnWidth, clazz, method, coverage);
					System.out.println();
				}
			}

			if (isFile)
				writer.close();
		}
		return true;
	}

	/**
	 * Returns an ArrayList containing the results, alphabetized by class and
	 * method.
	 * 
	 * @return the ArrayList
	 */
	private ArrayList<Result> getResultList() {
		if (active) {
			ArrayList<Result> results = new ArrayList<Result>();

			// iterate through all results and add to ArrayList
			for (String key : expectedHash_inObject.keySet()) {
				results.add(expectedHash_inObject.get(key));
			}

			Collections.sort(results);

			return results;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String asString = "Info for: " + name + " @ " + this.hashCode() + "\n";

		if (active) {

			if (input_expected != null || !input_expected.isEmpty()) {
				Collection<Result> expecteds = input_expected.values();
				asString += "Expected results: \n";
				for (Result obj : expecteds) {
					try {
						asString += "Input: " + TesterUtils.objectPrinter(obj.getInput()) + " -> Output: " + TesterUtils.objectPrinter(obj.getExpectedResult()) + "\n";
					} catch (UnIdentifiableException e) {
						e.printStackTrace();
					}
				}

				asString += "\n";

			} else {
				asString += "No expected results detected by this tester \n\n";
			}
			if (expectedHash_inObject != null || !expectedHash_inObject.isEmpty()) {
				Collection<Result> loadeds = expectedHash_inObject.values();
				asString += "Inputs that passed their tests:\n";

				for (Result obj : loadeds) {
					if (obj.getStatus() == Status.PASSED)
						try {
							asString += "Input: " + TesterUtils.objectPrinter(obj.getInput()) + " -> Passed with result: " + TesterUtils.objectPrinter(obj.getResult()) + "\n";
						} catch (UnIdentifiableException e) {
							e.printStackTrace();
						}
				}
				asString += "\n";

				asString += "Inputs that have not yet passed their tests:\n";

				for (Result obj : loadeds) {
					if (obj.getStatus() == Status.INCOMPLETE || obj.getStatus() == Status.FAILED)
						try {
							asString += "Input: " + TesterUtils.objectPrinter(obj.getInput()) + " -> Expected output: " + TesterUtils.objectPrinter(obj.getExpectedResult()) + "\n";
						} catch (UnIdentifiableException e) {
							e.printStackTrace();
						}
				}

				asString += "\n";
			} else {
				asString += "No inputs that have not yet passed their tests \n\n";
			}

			if (used != null || !used.isEmpty()) {
				asString += "Tested output values:\n";
				Collection<Result> covereds = used.values();
				for (Result obj : covereds) {
					try {
						asString += "Covered output: " + TesterUtils.objectPrinter(obj.getExpectedResult()) + "\n";
					} catch (UnIdentifiableException e) {
						e.printStackTrace();
					}
				}

				asString += "\n";
			} else {
				asString += "No covered output values detected by this tester \n\n";
			}

			if (notUsed != null || !notUsed.isEmpty()) {
				asString += "Not covered output values:\n";
				Collection<Result> not_covereds = notUsed.values();
				for (Result obj : not_covereds) {
					try {
						asString += "Not Covered output: " + TesterUtils.objectPrinter(obj.getExpectedResult()) + "\n";
					} catch (UnIdentifiableException e) {
						e.printStackTrace();
					}
				}
			} else {
				asString += "No not-covered output values detected by this tester \n\n";
			}
		} else {
			asString = "The tester (" + this.name + ") @ " + this.hashCode() + " is disabled. Please enable the tester to allow printing";
		}
		return asString;
	}
}