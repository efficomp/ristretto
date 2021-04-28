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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.math3.linear.RealMatrix;

import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.NaiveBayes;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import libsvm.LibSVM;
import libsvm.svm_parameter;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Test the results of a Feature Selector via cross-validation
 * 
 * @author Jesús González
 */
public class FSCrossValidator {

	private static boolean verbose = false;

	/* Optional arguments */
	private static String applyLDAArg = "applylda";
	private static String naiveBayesClassifierArg = "nbc";
	private static String knnClassifierArg = "knn";
	private static String svmClassifierArg = "svm";
	private static final int N_FOLDS = 10;


	/* Parameters file for the SVM classifier */
	private static String svmParamsFileName = "svm_params.data";

	/*
	 * Dataset
	 */
	private static Dataset data;

	/*
	 * Classification
	 */
	private static boolean applyLDA;
	private static Classifier classifier;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Path to the original dataset</li>
	 * <li>Class column index (-1 for unlabeled data)</li>
	 * </ol>
	 * <b>Optional args:</b>
	 * <ul>
	 *  <li>applylda: If this flag appears, LDA is applied to the selected features of the dataset before classifying</li>
	 *  <li>classifier: Possible values are knn, nbc and svm. If omitted, nbc is used by default</li>
	 *  <li>selected features: A list of feature indices, if provided, only these features are taken into account. Otherwise all features are used</li>
	 * </ul>
	 * <p>
	 * Notes:<br>
	 * <ul>
	 *  <li>For knn: <i>k</i> is set as sqrt(num_samples) if knn is selected</li>
	 *  <li>For svm: a RBF kernel is assumed and <i>C</i> and <i>gamma</i> are read from a file named "svm_params.data"</li>
	 *  <li>A 10-fold cross-validation is applied</li>
     * </ul>
     *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {
		/* The first argument is expected to be the path to the dataset */
		if (args.length < 1) {
			System.err.println("Error: Missing dataset");
			System.exit(-1);
		}

		/* The second argument is expected to be class index */
		if (args.length < 2) {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/* Load the dataset */
		Dataset originalData = new DefaultDataset();
		try {
			int classIndex = Integer.parseInt(args[1]);

			originalData = FileHandler.loadDataset(new File(args[0]), classIndex, "\\s+");
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[1] + " is not a correct class index");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[0]);
			System.exit(-1);
		}

		/*
		 * Subsequent arguments are optional and define the classification algorithm to
		 * be used, whether LDA should be used to reduce the dimensionality of data, and
		 * the selected attributes
		 */
		int maxAttrIndex = originalData.noAttributes();
		boolean selectedFeatures = false;
		boolean[] mask = new boolean[maxAttrIndex];
		Arrays.fill(mask, false);
		applyLDA = false;
		classifier = new NaiveBayes();

		/* Parse the rest of arguments */
		for (int i = 2; i < args.length; i++) {
			try {

				int feat = Integer.parseInt(args[i]);

				if (feat >= mask.length || feat < 0) {
					System.err.println(
							"Error: Feature index " + feat + " is not in the range [0.." + (mask.length - 1) + "]");
					System.exit(-1);
				}
				mask[feat] = true;
				selectedFeatures = true;
			} catch (NumberFormatException e) {
				if (args[i].toLowerCase().trim().compareTo(applyLDAArg) == 0) {
					applyLDA = true;
				} else if (args[i].toLowerCase().trim().compareTo(naiveBayesClassifierArg) == 0) {
					classifier = new NaiveBayes();
				} else if (args[i].toLowerCase().trim().compareTo(knnClassifierArg) == 0) {
					int k = (int) Math.round(Math.sqrt(originalData.size()));
					if ((k & 1) == 0)
						k++;
					classifier = new KNearestNeighbors(k);
				} else if (args[i].toLowerCase().trim().compareTo(svmClassifierArg) == 0) {
					try {

						// create the SVM classifier
						classifier = new LibSVM();
						svm_parameter SVMparams = ((LibSVM) classifier).getParameters();
						SVMparams.kernel_type = svm_parameter.RBF;

						// parse the SVM parameters
						File svmParamsFile = new File(svmParamsFileName);
						Scanner input;
						input = new Scanner(svmParamsFile);

						// skip the header line
						input.nextLine();

						if (input.hasNext())
							SVMparams.C = Double.parseDouble(input.next());
						else {
							System.err.println("Error: Missing C parameter in " + svmParamsFileName);
							System.exit(-1);
						}

						if (input.hasNext())
							SVMparams.gamma = Double.parseDouble(input.next());
						else {
							System.err.println("Error: Missing gamma parameter in " + svmParamsFileName);
							System.exit(-1);
						}

						input.close();

						((LibSVM) classifier).setParameters(SVMparams);
					} catch (FileNotFoundException e2) {
						System.err.println("Error: " + svmParamsFileName + " file not found");
						System.exit(-1);
					}
				} else {
					System.err.println("Error: Invalid argument: " + args[i]);
					System.exit(-1);
				}
			}
		}

		/*
		 * If no attributes are specified, all attributes will be used by default
		 */
		if (!selectedFeatures)
			Arrays.fill(mask, true);

		if (verbose) {
			System.err.println("Data file: " + args[0]);
			System.err.println("Class index: " + args[1]);
			System.err.println("ApplyLDA: " + applyLDA);
			System.err.println("Classifier: " + classifier.getClass().toString());

			System.err.print("Selected attributes:");
			if (!selectedFeatures) {
				System.err.println(" ALL");
			} else {
				for (int i = 0; i < mask.length; i++)
					if (mask[i])
						System.err.print(" " + i);
				System.err.println();
			}
		}

		/*
		 * Project the selected attributes of the original dataset
		 */
		data = MoreDatasetTools.project(originalData, mask);
	}

	/**
	 * Validate the feature selection using cross-validation
	 */
	private static void validate() {
		/* Check if LDA should be applied */
		Dataset finalData;

		if (applyLDA) {
			RealMatrix proj = MoreDatasetTools.directLDA(data);
			finalData = MoreDatasetTools.project(data, proj);
		} else {
			finalData = data;
		}

		/* Construct new cross validation instance with the classifier */
		/* Use Leave-one-out cross-validation */
		CrossValidation cv = new CrossValidation(classifier);

		/* Perform cross-validation on the data set and obtain the error rate */
		double errorRate = PerformanceIndexes.errorRate(cv.crossValidation(finalData, N_FOLDS));

		if (verbose) {
			System.err.print("ERROR\t");
			System.err.println(String.format("%.6f", errorRate));
		}
		System.out.println(String.format("%.6f", errorRate));
	}

	/**
	 * Test the results of a Feature Selector via cross-validation
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Path to the original dataset</li>
	 * <li>Class column index (-1 for unlabeled data)</li>
	 * </ol>
	 * <p>
	 * <b>Optional args:</b>
	 * <ul>
	 * <li>applylda: If this flag appears, LDA is applied to the selected features
	 * of the dataset before classifying</li>
	 * <li>classifier: Possible values are knn, nbc and svm. If omitted, nbc is used
	 * by default</li>
	 * <li>selected features: A list of feature indices, if provided, only these
	 * features are taken into account. Otherwise all features are used</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>For knn: <i>k</i> is set as sqrt(num_samples) if knn is selected</li>
	 * <li>For svm: a RBF kernel is assumed and <i>C</i> and <i>gamma</i> are read
	 * from a file named "svm_params.data"</li>
	 * <li>A 10-fold cross-validation is applied</li>
	 * </ul>
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
		validate();
	}
}
