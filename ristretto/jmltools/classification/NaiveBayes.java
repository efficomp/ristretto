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

package ristretto.jmltools.classification;

import java.util.HashMap;
import java.util.Map;

import net.sf.javaml.classification.AbstractClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.exception.TrainingRequiredException;

/**
 * Implementation of the Naive Bayes classification algorithm.
 * 
 * @author Jesús González
 * 
 */
public class NaiveBayes extends AbstractClassifier {
	private static final long serialVersionUID = -696347974952015400L;

	/* Mean of each training class */
	private Instance[] means;

	/* Variance of each training class */
	private Instance[] variances;

	/* Prior probability of each class */
	private double[] priorProbs;

	/** Training classes */
	Object[] classes;

	/* Number of features in the training data */
	private int nFeatures;

	/**
	 * Instantiate the naive Bayes algorithm.
	 */
	public NaiveBayes() {
		super();
		classes = null;
		means = null;
		variances = null;
		priorProbs = null;
	}

	/**
	 * Builds the classifier from the provided data
	 * 
	 * @param data Training data
	 */
	@Override
	public void buildClassifier(Dataset data) {
		classes = data.classes().toArray();
		int nClasses = classes.length;
		nFeatures = data.noAttributes();
		int nSamples = data.size();
		if (nClasses == 0) {
			System.err.println("ERROR: data must be labeled in NaiveBayes.buildClassifier");
			System.exit(-1);
		}

		/* mean and variance of each class */
		means = new DenseInstance[nClasses];
		variances = new DenseInstance[nClasses];
		for (int i = 0; i < nClasses; i++) {
			means[i] = new DenseInstance(new double[nFeatures]);
			variances[i] = new DenseInstance(new double[nFeatures]);
		}

		/* prior probabilities */
		priorProbs = new double[nClasses];

		/* number of samples in each class */
		int classSizes[] = new int[nClasses];

		/* mean and number of samples in each class */
		for (Instance sample : data) {
			int i = data.classIndex(sample.classValue());
			means[i] = means[i].add(sample);
			classSizes[i]++;
		}

		for (int i = 0; i < nClasses; i++) {
			means[i] = means[i].divide(classSizes[i]);
		}

		/* variance of each class */
		for (Instance sample : data) {
			int i = data.classIndex(sample.classValue());
			Instance diff = sample.minus(means[i]);
			variances[i] = variances[i].add(diff.multiply(diff));
		}

		for (int i = 0; i < nClasses; i++) {
			variances[i] = variances[i].divide(classSizes[i] - 1);
			priorProbs[i] = (double) classSizes[i] / nSamples;
		}
	}

	/**
	 * Generate the membership distribution for this instance using this classifier.
	 * All values should be in the interval [0,1].
	 * 
	 * <p>
	 * 
	 * Note: The returned map may not contain a value for all classes that were present in the data set used for
	 * training. If the map does not contain a value, the value for that class
	 * equals zero.
	 * @param instance The instance to be classified
	 * @return An array with membership degrees for all the various classes in the data set
	 */
	@Override
	public Map<Object, Double> classDistribution(Instance instance) {
		if (classes == null)
			throw new TrainingRequiredException();
		if (instance.noAttributes() != nFeatures)
			throw new TrainingRequiredException("Incorrect number of attributes");

		int nClasses = classes.length;
		HashMap<Object, Double> out = new HashMap<Object, Double>(nClasses);

		for (int i = 0; i < nClasses; i++) {
			Instance diff = instance.minus(means[i]);
			double posteriorProb = priorProbs[i];

			for (int j = 0; j < nFeatures; j++) {
				double diffj = diff.value(j);
				double varj = variances[i].value(j);

				// Use a gaussian distribution
				posteriorProb *= Math.exp(-(diffj * diffj) / (2 * varj)) / Math.sqrt(2 * Math.PI * varj);
			}
			out.put(classes[i], posteriorProb);
		}

		return out;
	}
}
