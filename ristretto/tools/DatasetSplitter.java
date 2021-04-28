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
 * Copyright (c) 2018, EFFICOMP
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
 * Split a dataset into a training and test datasets given a test proportion,
 * that is, the proportion of data reserved to the test dataset. If data are
 * labeled, this proportion is applied to each class.
 * 
 * @author Jesús González
 *
 */
public class DatasetSplitter {
	private static boolean verbose = false;
	/*
	 * Original dataset
	 */
	private static Dataset originalData;

	/*
	 * Proportion of data to be used for testing
	 */
	private static float testProp;

	/**
	 * Parses the command line arguments
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Path to the original dataset</li>
	 * <li>Class column index (-1 for unlabeled data)</li>
	 * <li>Proportion of data used for testing (double in (0, 1))</li>
	 * </ol>
	 * 
	 * @param args Command line arguments
	 */
	private static void parseArgs(String[] args) {

		/* Gets the original data file name */
		String originalDataFileName = null;
		if (args.length > 0) {
			originalDataFileName = args[0];
		} else {
			System.err.println("Error: Missing dataset");
			System.exit(-1);
		}

		/* Gets the class index */
		int classIndex = -1;
		if (args.length > 1) {
			try {
				classIndex = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.err.println("Error: " + args[1] + " is not a correct class index");
				System.exit(-1);
			}

		} else {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/* Gets the class index */
		testProp = 0;
		if (args.length > 2) {
			try {
				testProp = Float.parseFloat(args[2]);

				if (testProp < 0 || testProp > 1) {
					System.err.println("Error: the proportion of data to be used for testing (" + args[2]
							+ ") should be between 0 and 1");
					System.exit(-1);

				}
			} catch (NumberFormatException e) {
				System.err.println("Error: " + args[2] + " is not a correct proportion");
				System.exit(-1);
			}

		} else {
			System.err.println("Error: Missing test proportion");
			System.exit(-1);
		}

		/* Loads the training dataset */
		originalData = new DefaultDataset();
		try {
			originalData = FileHandler.loadDataset(new File(originalDataFileName), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open the dataset file: " + originalDataFileName);
			System.exit(-1);
		}

		if (verbose) {
			System.out.println("Data file: " + originalDataFileName);
			System.out.println("Class index: " + classIndex);
			System.out.println("Test proportion: " + testProp);
		}

	}

	private static void writeDataset(Dataset data, PrintStream stream) {
		for (Instance datum : data) {
			String s = "";

			for (Double d : datum) {
				if (s.length() > 0)
					s += "\t";

				s += String.format("%.6f", d);
			}

			if (datum.classValue() != null)
				s += "\t" + datum.classValue();

			stream.println(s);
		}
	}

	/**
	 * Split a dataset into a training and test datasets given a test proportion,
	 * that is, the proportion of data reserved to the test dataset. If data are
	 * labeled, this proportion is applied to each class.
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Path to the original dataset</li>
	 * <li>Class column index (-1 for unlabeled data)</li>
	 * <li>Proportion of data used for testing (double in (0, 1))</li>
	 * </ol>
	 * <p>
	 * The resulting training dataset is written to stdout, while the test dataset
	 * is written to stderr
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		parseArgs(args);

		/* Split the dataset into training and validation datasets */
		Dataset testData = MoreDatasetTools.split(originalData, testProp);

		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));

		writeDataset(originalData, System.out);
		writeDataset(testData, System.err);
	}
}
