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
 * Copyright (c) 2017, EFFICOMP
 */

package ristretto.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.math3.linear.RealMatrix;

import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.NaiveBayes;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Obtain the training and test specificities of the results of a Feature Selector
 * 
 * @author Jesús González
 */
public class FSSpecificityValidator {

	private static boolean verbose = false;

	/* Optional arguments */
	private static String applyLDAArg = "applylda";
	private static String naiveBayesClassifierArg = "nbc";
	private static String knnClassifierArg = "knn";

	/*
	 * Dataset
	 */
	private static Dataset trainingData, testData;
	private static String label;

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
	 * <li>Path to the training dataset</li>
	 * <li>Path to the test dataset</li>
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
		/*
		 * The first argument is expected to be the path to the training dataset
		 */
		if (args.length < 1) {
			System.err.println("Error: Missing training dataset");
			System.exit(-1);
		}

		/* The second argument is expected to be the path to the test dataset */
		if (args.length < 2) {
			System.err.println("Error: Missing test dataset");
			System.exit(-1);
		}
		/* The third argument is expected to be class index */
		if (args.length < 3) {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/*
		 * The forth argument is the label to be considered for the specificity
		 * analysis
		 */
		if (args.length < 4) {
			System.err.println("Error: Missing class label");
			System.exit(-1);
		}

		/* Load the datasets */
		Dataset originalTrainingData = new DefaultDataset();
		Dataset originalTestData = new DefaultDataset();
		int classIndex = -1;
		try {
			classIndex = Integer.parseInt(args[2]);
			originalTrainingData = FileHandler.loadDataset(new File(args[0]), classIndex, "\\s+");
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[2] + " is not a correct class index");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[0]);
			System.exit(-1);
		}
		try {
			originalTestData = FileHandler.loadDataset(new File(args[1]), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[1]);
			System.exit(-1);
		}

		/* Get the label for the specificity analysis */
		label = args[3];

		/*
		 * Subsequent arguments are optional and define the classification
		 * algorithm to be used, whether LDA should be used to reduce the
		 * dimensionality of data, and the selected attributes
		 */
		int maxAttrIndex = originalTrainingData.noAttributes();
		boolean selectedFeatures = false;
		boolean[] mask = new boolean[maxAttrIndex];
		Arrays.fill(mask, false);
		applyLDA = false;
		classifier = new NaiveBayes();

		/* Parse the rest of arguments */
		for (int i = 4; i < args.length; i++) {

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
					int k = (int) Math.round(Math.sqrt(originalTrainingData.size()));
					if ((k & 1) == 0)
						k++;
					classifier = new KNearestNeighbors(k);
				} else {
					System.err.println("Error: Invalid argument: " + args[i]);
					System.exit(-1);
				}
			}
		}

		/*
		 * If no attributes are specified, all attributes will be used by
		 * default
		 */
		if (!selectedFeatures)
			Arrays.fill(mask, true);

		if (verbose) {
			System.out.println("Training Data file: " + args[0]);
			System.out.println("Test Data file: " + args[1]);
			System.out.println("Class index: " + classIndex);
			System.out.println("Label: " + label);
			System.out.println("ApplyLDA: " + applyLDA);
			System.out.println("Classifier: " + classifier.getClass().toString());

			System.out.print("Selected attributes:");
			if (!selectedFeatures) {
				System.out.println(" ALL");
			} else {
				for (int i = 0; i < mask.length; i++)
					if (mask[i])
						System.out.print(" " + i);
				System.out.println();
			}
		}

		/*
		 * Project the selected attributes of the original dataset
		 */
		trainingData = MoreDatasetTools.project(originalTrainingData, mask);
		testData = MoreDatasetTools.project(originalTestData, mask);
	}

	/**
	 * Validate the feature selection
	 */
	private static void validate() {

		/* Check if LDA should be applied */
		Dataset finalTrainingData;
		Dataset finalTestData;

		if (applyLDA) {
			RealMatrix proj = MoreDatasetTools.directLDA(trainingData);
			finalTrainingData = MoreDatasetTools.project(trainingData, proj);
			finalTestData = MoreDatasetTools.project(testData, proj);
		} else {
			finalTrainingData = trainingData;
			finalTestData = testData;
		}

		classifier.buildClassifier(finalTrainingData);

		double specTraining = PerformanceIndexes.specificity(classifier, finalTrainingData, label);
		double specTest = PerformanceIndexes.specificity(classifier, finalTestData, label);

		if (verbose) {
			System.out.printf("\nSpecificity Training: %.6f\n", specTraining);
			System.out.printf("Specificity Test:     %.6f\n", specTest);
		} else {
			System.out.printf("%.6f\t%.6f\n", specTraining, specTest);
		}
	}

	/**
     * Obtain the training and test specificities of the results of a Feature Selector
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Path to the training dataset</li>
	 * <li>Path to the test dataset</li>
	 * <li>Class column index (-1 for unlabeled data)</li>
	 * </ol>
	 * <p>
	 * <b>Optional args:</b>
	 * <ul>
	 *  <li>applylda: If this flag appears, LDA is applied to the selected features of the dataset before classifying</li>
	 *  <li>classifier: Possible values are knn, nbc and svm. If omitted, nbc is used by default</li>
	 *  <li>selected features: A list of feature indices, if provided, only these features are taken into account. Otherwise all features are used</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 *  <li>For knn: <i>k</i> is set as sqrt(num_samples) if knn is selected</li>
	 *  <li>For svm: a RBF kernel is assumed and <i>C</i> and <i>gamma</i> are read from a file named "svm_params.data"</li>
     * </ul>
     *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas
		 * for decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));

		readParams(args);
		validate();
	}
}
