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

package tests.testDirectLDA;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.math3.linear.RealMatrix;

import ristretto.jmltools.MoreDatasetTools;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Test the direct LDA method
 * 
 * @author Jesús González
 */
public class TestDirectLDA {

	private static boolean verbose = false;

	/*
	 * Dataset
	 */
	private static Dataset data, output;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>dataset: Path to the dataset (string)</li>
	 * <li>class-index: Index of the class column (int, &gt;= 0)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {
		/*
		 * The first argument is expected to be the path to the dataset
		 */
		if (args.length < 1) {
			System.err.println("Error: Missing dataset");
			System.exit(-1);
		}

		/*
		 * The second argument is expected to be the class index in the dataset (-1 if
		 * elements are unlabeled)
		 */
		if (args.length < 2) {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/* Parse the class_index */
		int classIndex = -1;
		try {
			classIndex = Integer.parseInt(args[1]);
			if (classIndex < 0) {
				System.err.println("Error: Datasets must be labeled");
				System.exit(-1);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[1] + " is not a correct class index");
			System.exit(-1);
		}

		/* Load the datasets */
		data = new DefaultDataset();
		try {
			data = FileHandler.loadDataset(new File(args[0]), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[0]);
			System.exit(-1);
		}

		if (verbose) {
			System.out.println("Data file: " + args[0]);
			System.out.println("Class index: " + classIndex);
		}
	}

	/**
	 * Make the test
	 */
	private static void makeTest() {
		RealMatrix proj = MoreDatasetTools.directLDA(data);
		output = MoreDatasetTools.project(data, proj);
	}

	/**
	 * Prints the results
	 */
	private static void writeResults() {
		/* Print the projected dataset */
		for (Instance i : output) {
			String s = "";

			for (Double d : i) {
				if (s.length() > 0)
					s += "\t";

				s += String.format("%.6f", d);
			}

			if (i.classValue() != null)
				s += "\t" + i.classValue();

			System.out.println(s);
		}
	}

	/**
	 * Test the direct LDA method
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>dataset: Path to the dataset (string)</li>
	 * <li>class-index: Index of the class column (int, &gt;= 0)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		readParams(args);
		makeTest();
		writeResults();
	}
}
