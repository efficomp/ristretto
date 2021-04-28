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
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Obtain the training and test Kappa indices of the results of a Feature Selector
 * 
 * @author Jesús González
 */
public class FSKappaValidator {

	private static boolean verbose = false;

	/* Optional arguments */
	private static String applyLDAArg = "applylda";
	private static String naiveBayesClassifierArg = "nbc";
	private static String knnClassifierArg = "knn";
	private static String svmClassifierArg = "svm";

	/* Parameters file for the SVM classifier */
	private static String svmParamsFileName = "svm_params.data";

	/*
	 * Dataset
	 */
	private static Dataset trainingData, testData;

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

		/*
		 * Subsequent arguments are optional and define the classification algorithm to
		 * be used, whether LDA should be used to reduce the dimensionality of data, and
		 * the selected attributes
		 */
		int maxAttrIndex = originalTrainingData.noAttributes();
		boolean selectedFeatures = false;
		boolean[] mask = new boolean[maxAttrIndex];
		Arrays.fill(mask, false);
		applyLDA = false;
		classifier = new NaiveBayes();

		/* Parse the rest of arguments */
		for (int i = 3; i < args.length; i++) {
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
				/* try an optional argument */
				if (args[i].toLowerCase().trim().compareTo(applyLDAArg) == 0) {
					applyLDA = true;
				} else if (args[i].toLowerCase().trim().compareTo(naiveBayesClassifierArg) == 0) {
					classifier = new NaiveBayes();
				} else if (args[i].toLowerCase().trim().compareTo(knnClassifierArg) == 0) {
					int k = (int) Math.round(Math.sqrt(originalTrainingData.size()));
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
			System.out.println("Training Data file: " + args[0]);
			System.out.println("Test Data file: " + args[1]);
			System.out.println("Class index: " + classIndex);
			System.out.println("ApplyLDA: " + applyLDA);
			System.out.print("Classifier: " + classifier.getClass().toString());
			if (classifier instanceof libsvm.LibSVM) {
				svm_parameter p = ((LibSVM) classifier).getParameters();
				System.out.print("\t C: "+ p.C);
				System.out.print("\t Gamma: "+ p.gamma);
			}
			System.out.println();

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

		double kappaTraining = PerformanceIndexes.kappa(classifier, finalTrainingData);
		double kappaTest = PerformanceIndexes.kappa(classifier, finalTestData);

		if (verbose) {
			System.out.printf("\nKappa Training: %.6f\n", kappaTraining);
			System.out.printf("Kappa Test:     %.6f\n", kappaTest);
		} else {
			System.out.printf("%.6f\t%.6f\n", kappaTraining, kappaTest);
		}
	}

	/**
	 * Obtain the training and test Kappa indices of the results of a Feature
	 * Selector
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
