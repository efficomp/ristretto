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

package ristretto.jmltools.classification.evaluation;

import java.util.Map;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;

/**
 * This class implements some performance metrics based on the confusion matrix
 * of a classifier
 * 
 * @author Jesús González
 */

public class PerformanceIndexes {
	/**
	 * This method implements the Kappa statistic [1], which measures inter-rater
	 * agreement for qualitative (categorical) items.
	 * 
	 * <p>
	 * <table>
	 * <tr>
	 * <td style="vertical-align:top">[1]</td>
	 * <td>J. Cohen. A coefficient of agreement for nominal scales. Educational and
	 * Psychological Measurement, 20(1):37-46, 1960. <a href=
	 * "https://doi.org/10.1177/001316446002000104">https://doi.org/10.1177/001316446002000104</a>
	 * </td>
	 * </tr>
	 * </table>
	 * 
	 * @param results The results of classifier evaluation
	 * @return The Kappa coefficient
	 */
	public static double kappa(Map<Object, PerformanceMeasure> results) {

		double observedAccuracy = 0;
		double expectedAccuracy = 0;

		for (Object o : results.keySet()) {
			observedAccuracy += results.get(o).tp;
			expectedAccuracy += (results.get(o).tp + results.get(o).fp) * ((results.get(o).tp + results.get(o).fn));
		}

		PerformanceMeasure p = results.get(results.keySet().iterator().next());
		double nSamples = p.tp + p.tn + p.fp + p.fn;

		observedAccuracy /= nSamples;
		expectedAccuracy /= nSamples * nSamples;

		return (observedAccuracy - expectedAccuracy) / (1.0 - expectedAccuracy);

	}

	/**
	 * This method implements the Kappa statistic [1], which measures inter-rater
	 * agreement for qualitative (categorical) items.
	 * 
	 * <p>
	 * <table>
	 * <tr>
	 * <td style="vertical-align:top">[1]</td>
	 * <td>J. Cohen. A coefficient of agreement for nominal scales. Educational and
	 * Psychological Measurement, 20(1):37-46, 1960. <a href=
	 * "https://doi.org/10.1177/001316446002000104">https://doi.org/10.1177/001316446002000104</a>
	 * </td>
	 * </tr>
	 * </table>
	 * 
	 * @param cls      The classifier to test
	 * @param testData The data set to test on
	 * @return The Kappa coefficient
	 */
	public static double kappa(Classifier cls, Dataset testData) {
		return kappa(EvaluateDataset.testDataset(cls, testData));
	}

	/**
	 * This method obtains the accuracy after a classifier has been evaluated
	 * 
	 * @param results the results of classifier evaluation
	 * @return the accuracy index
	 */
	public static double accuracy(Map<Object, PerformanceMeasure> results) {

		double tp = 0;
		double tn = 0;
		double fp = 0;
		double fn = 0;

		for (Object o : results.keySet()) {
			tp += results.get(o).tp;
			tn += results.get(o).tn;
			fp += results.get(o).fp;
			fn += results.get(o).fn;
		}

		return (tp + tn) / (tp + tn + fp + fn);
	}

	/**
	 * This method obtains the accuracy for a classifier and a given dataset
	 * 
	 * @param cls      The classifier to test
	 * @param testData The data set to test on
	 * @return The accuracy index
	 */
	public static double accuracy(Classifier cls, Dataset testData) {

		return accuracy(EvaluateDataset.testDataset(cls, testData));
	}

	/**
	 * This method obtains the error rate after a classifier has been evaluated
	 * 
	 * @param results the results of classifier evaluation
	 * @return the error rate index
	 */
	public static double errorRate(Map<Object, PerformanceMeasure> results) {
		return 1 - accuracy(results);
	}

	/**
	 * This method obtains the error rate for a classifier and a given dataset
	 * 
	 * @param cls      The classifier to test
	 * @param testData The data set to test on
	 * @return The error rate index
	 */
	public static double errorRate(Classifier cls, Dataset testData) {
		return 1 - accuracy(cls, testData);
	}

	/**
	 * This method implements sensitivity of a classification. As sensitivity is
	 * defined for binary classifications, this method implements the one against
	 * all approach, thus it is necessary to provide the label of the class to be
	 * tested
	 * 
	 * @param results The results of classifier evaluation
	 * @param label   The class label
	 * @return The sensitivity index (of 0 if label is not a valid class)
	 */
	public static double sensitivity(Map<Object, PerformanceMeasure> results, Object label) {
		if (results.containsKey(label)) {
			double tp = results.get(label).tp;
			double fn = results.get(label).fn;

			return tp / (tp + fn);
		} else
			return 0;
	}

	/**
	 * This method implements sensitivity of a classification. As sensitivity is
	 * defined for binary classifications, this method implements the one against
	 * all approach, thus it is necessary to provide the label of the class to be
	 * tested
	 * 
	 * @param cls      The classifier to test
	 * @param testData The data set to test on
	 * @param label    The class label
	 * @return The sensitivity index (of 0 if label is not a valid class)
	 */
	public static double sensitivity(Classifier cls, Dataset testData, Object label) {

		return sensitivity(EvaluateDataset.testDataset(cls, testData), label);
	}

	/**
	 * This method implements specificity of a classification. As specificity is
	 * defined for binary classifications, this method implements the one against
	 * all approach, thus it is necessary to provide the label of the class to be
	 * tested
	 * 
	 * @param results The results of classifier evaluation
	 * @param label   The class label
	 * @return The specificity index (of 0 if label is not a valid class)
	 */
	public static double specificity(Map<Object, PerformanceMeasure> results, Object label) {

		if (results.containsKey(label)) {
			double tn = results.get(label).tn;
			double fp = results.get(label).fp;

			return tn / (tn + fp);
		} else
			return 0;
	}

	/**
	 * This method implements specificity of a classification. As specificity is
	 * defined for binary classifications, this method implements the one against
	 * all approach, thus it is necessary to provide the label of the class to be
	 * tested
	 * 
	 * @param cls      The classifier to test
	 * @param testData The data set to test on
	 * @param label    The class label
	 * @return The specificity index (of 0 if label is not a valid class)
	 */
	public static double specificity(Classifier cls, Dataset testData, Object label) {

		return specificity(EvaluateDataset.testDataset(cls, testData), label);
	}
}
