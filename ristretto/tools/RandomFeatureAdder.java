/**
 * This file is part of Ristretto.
 *
 * Ristretto is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Ristretto is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Ristretto. If not, see <http://www.gnu.org/licenses/>.
 *
 * This work was supported by project TIN2015-67020-P (Spanish "Ministerio de
 * Economía y Competitividad"), and by the European Regional Development Fund
 * (ERDF).
 *
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import ristretto.jmltools.MoreDatasetTools;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Append some random features to a training dataset, and also to a test dataset
 * if provided
 * 
 * @author Jesús González
 */
public class RandomFeatureAdder {
	private static boolean verbose = false;

	/* Training dataset */
	private static Dataset trainingData, testData;

	/* Number of extra random features to be appended to the dataset */
	private static int nRandomFeatures;

	/* Boolean value to decide whether the class should be written on not */
	private static boolean writeClass;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Training data: Path to the training dataset (string)</li>
	 * <li><Test data>: Path to the test dataset (string, optional)</li>
	 * <li>Class index: Class column index (int, -1 for unlabeled data)</li>
	 * <li>nRandom: Number of random features to be added (int, &gt; 0)</i>
	 * <li>writeClass: whether to write the class column in the output data
	 * (boolean)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void parseArgs(String[] args) {
		int trainingFileNameParam = 0;
		int testFileNameParam = -1; // optional
		int classIndexParam = 1;
		int nRandomFeaturesParam = 2;
		int writeClassParam = 3;

		/* Test is a test file name is provided */
		if (args.length > classIndexParam) {
			try {
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				testFileNameParam = 1;
				classIndexParam++;
				nRandomFeaturesParam++;
				writeClassParam++;
			}
		}

		/* Gets the training file name */
		String trainingFileName = null;
		if (args.length > trainingFileNameParam) {
			trainingFileName = args[trainingFileNameParam];
		} else {
			System.err.println("Error: Missing training dataset");
			System.exit(-1);
		}

		/* Gets the test file name */
		String testFileName = null;
		if (testFileNameParam > 0 && args.length > testFileNameParam)
			testFileName = args[testFileNameParam];

		/* Gets the class index */
		int classIndex = -1;
		if (args.length > classIndexParam) {
			try {
				classIndex = Integer.parseInt(args[classIndexParam]);
			} catch (NumberFormatException e) {
				System.err.println("Error: " + args[classIndexParam] + " is not a correct class index");
				System.exit(-1);
			}

		} else {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/* Loads the training dataset */
		trainingData = new DefaultDataset();
		try {
			trainingData = FileHandler.loadDataset(new File(trainingFileName), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open the training dataset file: " + trainingFileName);
			System.exit(-1);
		}

		/* Loads the test dataset */
		if (testFileName != null) {
			testData = new DefaultDataset();
			try {
				testData = FileHandler.loadDataset(new File(testFileName), classIndex, "\\s+");
			} catch (IOException e) {
				System.err.println("Error: Couldn't open the test dataset file: " + testFileName);
				System.exit(-1);
			}
		} else
			testData = null;

		/* Gets the number of extra random features */
		nRandomFeatures = -1;
		if (args.length > nRandomFeaturesParam) {
			try {
				nRandomFeatures = Integer.parseInt(args[nRandomFeaturesParam]);

				if (nRandomFeatures < 0) {
					System.err.println("Error: The number of extra random features can not be a negative number");
					System.exit(-1);
				}

			} catch (NumberFormatException e) {
				System.err.println(
						"Error: " + args[nRandomFeaturesParam] + " is not a correct number of extra random features");
				System.exit(-1);
			}

		} else {
			System.err.println("Error: Missing number of extra random features");
			System.exit(-1);
		}

		/*
		 * Decides whether the class should be written in the resulting datasets
		 */
		writeClass = false;
		if (classIndex >= 0 && args.length > writeClassParam)
			writeClass = Boolean.parseBoolean(args[writeClassParam]);

		if (verbose) {
			System.out.println("Training data file: " + trainingFileName);
			System.out.print("Test data file: ");
			if (testFileName != null)
				System.out.println(testFileName);
			else
				System.out.println("Not provided");

			System.out.println("Class index: " + classIndex);
			System.out.println("Extra random features: " + nRandomFeatures);
			System.out.println("Write Class: " + writeClass);
		}
	}

	/**
	 * Write a dataset to a stream
	 * 
	 * @param data   The dataset
	 * @param stream The stream
	 */
	private static void writeDataset(Dataset data, PrintStream stream) {
		for (Instance normDatum : data) {
			String s = "";

			for (Double d : normDatum) {
				if (s.length() > 0)
					s += "\t";

				s += String.format("%.6f", d);
			}

			if (writeClass && normDatum.classValue() != null)
				s += "\t" + normDatum.classValue();

			stream.println(s);
		}
	}

	/**
	 * Append some random features to a training dataset, and also to a test dataset
	 * if provided
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Training data: Path to the training dataset (string)</li>
	 * <li><Test data>: Path to the test dataset (string, optional)</li>
	 * <li>Class index: Class column index (int, -1 for unlabeled data)</li>
	 * <li>nRandom: Number of random features to be added (int, &gt; 0)</i>
	 * <li>writeClass: whether to write the class column in the output data
	 * (boolean)</li>
	 * </ol>
	 * 
	 * Write the new training data to stdoutand the test data, if provided, to
	 * stderr
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		parseArgs(args);

		/* Append the extra random features */
		MoreDatasetTools.appendRandomAttributes(trainingData, nRandomFeatures);
		if (testData != null)
			MoreDatasetTools.appendRandomAttributes(testData, nRandomFeatures);

		/* Normalize all the attributes in the range [0..1] */
		if (testData != null)
			MoreDatasetTools.normalize(trainingData, testData);
		else
			MoreDatasetTools.normalize(trainingData);

		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));

		writeDataset(trainingData, System.out);
		if (testData != null)
			writeDataset(testData, System.err);
	}
}
