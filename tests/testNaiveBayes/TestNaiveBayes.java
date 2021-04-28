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

package tests.testNaiveBayes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.NaiveBayes;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Test Naive Bayes as a wrapper classifier
 * 
 * @author Jesús González
 */
public class TestNaiveBayes {
	private static boolean verbose = false;

	/*
	 * Datasets
	 */
	private static Dataset trainingData;
	private static Dataset testData;

	/* Number of folds for cross-validation */
	private static int xValidationNFolds = 10;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>training: Path to the training dataset (string)</li>
	 * <li>test: Path to the test dataset (string)</li>
	 * <li>class-index: Index of the class column (int, &gt;= 0)</li>
	 * <li>feats: list of selected features (sequence of int)</li>
	 * </ol>
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

		/*
		 * The second argument is expected to be the path to the test dataset
		 */
		if (args.length < 2) {
			System.err.println("Error: Missing test dataset");
			System.exit(-1);
		}

		/*
		 * The third argument is expected to be the class index in the dataset
		 */
		if (args.length < 3) {
			System.err.println("Error: Missing class index");
			System.exit(-1);
		}

		/* Parse the class_index */
		int classIndex = -1;
		try {
			classIndex = Integer.parseInt(args[2]);
			if (classIndex < 0) {
				System.err.println("Error: Datasets must be labeled");
				System.exit(-1);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[2] + " is not a correct class index");
			System.exit(-1);
		}

		/* Load the datasets */
		Dataset originalTrainingData = new DefaultDataset();
		try {
			originalTrainingData = FileHandler.loadDataset(new File(args[0]), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[0]);
			System.exit(-1);
		}

		Dataset originalTestData = new DefaultDataset();
		try {
			originalTestData = FileHandler.loadDataset(new File(args[1]), classIndex, "\\s+");
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[1]);
			System.exit(-1);
		}

		/*
		 * Subsequent arguments, if given, are the selected features. If none are given,
		 * all the features will be used
		 */
		int maxAttrIndex = originalTrainingData.noAttributes();
		boolean selectedFeatures = false;
		boolean[] mask = new boolean[maxAttrIndex];
		Arrays.fill(mask, false);

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
				System.err.println("Error: Invalid feature index: " + args[i]);
				System.exit(-1);
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
	 * Classify the samples in the dataset
	 * 
	 * @param cls     Classifier
	 * @param dataset Dataset
	 * @param label   Label for the dataset
	 */
	private static void classify(Classifier cls, Dataset dataset, String label) {

		int correct = 0;
		int wrong = 0;
		ArrayList<Object> predictedValues = new ArrayList<Object>();
		/* Classify all instances and check with the correct class values */
		for (Instance inst : dataset) {
			Object predictedClassValue = cls.classify(inst);
			predictedValues.add(predictedClassValue);
			Object realClassValue = inst.classValue();
			if (predictedClassValue.equals(realClassValue))
				correct++;
			else {
				wrong++;
			}
		}
		double kappa = PerformanceIndexes.kappa(cls, dataset);

		/* Construct new cross validation instance with the classifier */
		CrossValidation cv = new CrossValidation(cls);

		/* Perform 10-fold cross-validation on the training data */
		Map<Object, PerformanceMeasure> pm = cv.crossValidation(dataset, xValidationNFolds);

		writeResults(label, dataset, predictedValues, correct, wrong, kappa, pm);
	}

	/**
	 * Write the results
	 * 
	 * @param label           Label for the dataset
	 * @param dataset         The dataset
	 * @param predictedValues Values Values predicted by the classifier
	 * @param correct         Number of correct classified samples
	 * @param wrong           Number of wrong classified samples
	 * @param kappa           Kappa index
	 * @param pm              Performance meassures
	 */
	private static void writeResults(String label, Dataset dataset, ArrayList<Object> predictedValues, int correct,
			int wrong, double kappa, Map<Object, PerformanceMeasure> pm) {

		System.out.println("\n" + label + " Data");

		if (verbose) {
			/* writes the correct and predicted class for each instance of the dataset */
			System.out.println("\tSample\tClass\tPredicted\n");

			for (int i = 0; i < dataset.size(); i++)
				System.out.println(
						"\t" + i + "\t" + dataset.instance(i).classValue() + "\t" + predictedValues.get(i) + "\n");
		}

		System.out.println("\n\tcorrect:\t" + correct);
		System.out.println("\tincorrect:\t" + wrong);
		System.out.println("\tkappa:\t\t" + kappa);

		double errorRateAcc = 0;
		System.out.println("\tCross-validation error:");
		for (Object o : pm.keySet()) {
			double errorRate = pm.get(o).getErrorRate();
			System.out.println("\t\tClass " + o.toString() + ": \t" + String.format("%.6f", errorRate));
			errorRateAcc += errorRate;
		}
		System.out.println("\t\tTOTAL:\t\t" + String.format("%.6f", errorRateAcc));
	}

	/**
	 * Test Naive Bayes as a wrapper classifier
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>training: Path to the training dataset (string)</li>
	 * <li>test: Path to the test dataset (string)</li>
	 * <li>class-index: Index of the class column (int, &gt;= 0)</li>
	 * <li>feats: list of selected features (sequence of int)</li>
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
		Classifier nbc = new NaiveBayes();
		nbc.buildClassifier(trainingData);

		classify(nbc, trainingData, "Training");
		classify(nbc, testData, "Test");
	}
}
