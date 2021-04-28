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

package ristretto.jmltools;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;

import ec.util.MersenneTwister;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.InstanceTools;

/**
 * Add several tools to those provided by
 * <a href="http://java-ml.sourceforge.net/">Java-ML</a>.
 * 
 * @author Jesús González
 */
public class MoreDatasetTools {
	/**
	 * Generates a new dataset from the input data as a projection of some selected
	 * features according to mask. The length of mask should match the number of
	 * attributes in the dataset. Otherwise, the smaller value between the number of
	 * features in the dataset and the length of the mask will be used as the
	 * maximum number of selectable features.
	 * 
	 * @param data Input dataset
	 * @param mask Mask to select the features in the new dataset
	 * @return A new dataset containing only the features selected by the mask
	 */
	public static Dataset project(Dataset data, boolean[] mask) {
		/* Indexes of the attributes to be selected according to the mask */
		TreeSet<Integer> indexes = new TreeSet<Integer>();
		for (int i = 0; i < Math.min(data.noAttributes(), mask.length); i++)
			if (mask[i])
				indexes.add(i);

		return project(data, indexes);
	}

	/**
	 * Generates a new dataset from the input data as a projection of some selected
	 * features according to some selected features.
	 * 
	 * @param data     Input dataset
	 * @param selected Set of selected features
	 * @return A new dataset containing only the selected features
	 */
	public static Dataset project(Dataset data, TreeSet<Integer> selected) {

		/* Number of features in the dataset */
		int instanceLength = data.instance(0).noAttributes();

		/* Validate the features */
		SortedSet<Integer> validated = selected.subSet(0, instanceLength);

		/* Create an empty dataset */
		Dataset outData = new DefaultDataset();

		/* For every instance in the dataset */
		double[] selectedAttrs = new double[validated.size()];
		for (Instance inputIns : data) {
			Iterator<Integer> it = validated.iterator();
			for (int j = 0; j < selectedAttrs.length; j++)
				selectedAttrs[j] = inputIns.value(it.next());

			outData.add(new DenseInstance(selectedAttrs, inputIns.classValue()));
		}

		/* Return a dataset containing only the selected features */
		return outData;
	}

	/**
	 * Normalizes all the attributes of a dataset in the range [0..1]
	 * 
	 * @param data The dataset
	 */
	public static void normalize(Dataset data) {
		Instance minAttrs = DatasetTools.minAttributes(data);
		for (int i = 0; i < data.size(); i++) {
			Instance current = data.instance(i);
			Instance modified = current.minus(minAttrs);
			modified.setClassValue(current.classValue());
			data.set(i, modified);
		}

		Instance maxAttrs = DatasetTools.maxAttributes(data);
		for (int i = 0; i < data.size(); i++) {
			Instance current = data.instance(i);
			Instance modified = current.divide(maxAttrs);
			modified.setClassValue(current.classValue());
			data.set(i, modified);
		}
	}

	/**
	 * Normalizes all the attributes of a couple of training and test datasets in
	 * the range [0..1]
	 * 
	 * @param training The training dataset
	 * @param test     The test dataset
	 */
	public static void normalize(Dataset training, Dataset test) {
		/* Obtain the minimum of each attribute */
		Instance minAttrs = DatasetTools.minAttributes(training);
		Instance minAttrsTest = DatasetTools.minAttributes(test);

		for (Integer index : minAttrs.keySet()) {
			double val = minAttrsTest.value(index);
			if (minAttrs.get(index) > val)
				minAttrs.put(index, val);
		}

		/* Subtracts the minimum from all the training data */
		for (int i = 0; i < training.size(); i++) {
			Instance current = training.instance(i);
			Instance modified = current.minus(minAttrs);
			modified.setClassValue(current.classValue());
			training.set(i, modified);
		}

		/* Subtracts the minimum from all the test data */
		for (int i = 0; i < test.size(); i++) {
			Instance current = test.instance(i);
			Instance modified = current.minus(minAttrs);
			modified.setClassValue(current.classValue());
			test.set(i, modified);
		}

		/* Obtain the maximum of each attribute */
		Instance maxAttrs = DatasetTools.maxAttributes(training);
		Instance maxAttrsTest = DatasetTools.maxAttributes(test);

		for (Integer index : maxAttrs.keySet()) {
			double val = maxAttrsTest.value(index);
			if (maxAttrs.get(index) < val)
				maxAttrs.put(index, val);
		}

		/* Divides all the training data by the max values */
		for (int i = 0; i < training.size(); i++) {
			Instance current = training.instance(i);
			Instance modified = current.divide(maxAttrs);
			modified.setClassValue(current.classValue());
			training.set(i, modified);
		}

		/* Divides all the test data by the max values */
		for (int i = 0; i < test.size(); i++) {
			Instance current = test.instance(i);
			Instance modified = current.divide(maxAttrs);
			modified.setClassValue(current.classValue());
			test.set(i, modified);
		}
	}

	/**
	 * Appends some extra random attributes to each sample in the dataset.
	 * 
	 * @param data        The dataset
	 * @param nExtraAttrs Number of random extra attributes to be appended
	 */
	public static void appendRandomAttributes(Dataset data, int nExtraAttrs) {
		/* Random generator */
		MersenneTwister rg = new MersenneTwister(System.currentTimeMillis());

		/* Create an empty dataset */
		int nAttrs = data.noAttributes();

		for (int i = 0; i < data.size(); i++) {
			Instance current = data.instance(i);

			double[] newSample = new double[nAttrs + nExtraAttrs];
			for (int j = 0; j < nAttrs; j++)
				newSample[j] = current.value(j);

			for (int j = nAttrs; j < nAttrs + nExtraAttrs; j++)
				newSample[j] = rg.nextDouble();

			data.set(i, new DenseInstance(newSample, current.classValue()));
		}
	}

	/**
	 * Performs a projection of a given dataset with the directLDA algorithm [1].
	 * 
	 * <p>
	 * <table>
	 * <tr>
	 * <td style="vertical-align:top">[1]</td>
	 * <td>H. Yu, J. Yang. A direct LDA Algorithm for High-Dimensional Data - with
	 * application to Face Recognition. Pattern Recognition, 34(10):2067-2070,
	 * October 2001. <a href=
	 * "https://doi.org/10.1016/S0031-3203(00)00162-X">https://doi.org/10.1016/S0031-3203(00)00162-X</a>
	 * </td>
	 * </tr>
	 * </table>
	 * 
	 * @param data Input dataset. As LDA is a supervised dimensionality reduction
	 *             technique, this dataset must be labeled
	 * @param SbTh Threshold to select the valid Sb eigenvalues.
	 * @return The projection matrix
	 */
	public static RealMatrix directLDA(Dataset data, double SbTh) {
		Object[] classes = data.classes().toArray();
		int nClasses = classes.length;
		int nFeatures = data.noAttributes();
		if (nClasses == 0) {
			System.err.println("ERROR: data must be labeled in MoreDatasetTools.directLDA");
			System.exit(-1);
		}

		/* Mean of each class */
		Instance means[] = new DenseInstance[nClasses];
		for (int i = 0; i < nClasses; i++)
			means[i] = new DenseInstance(new double[nFeatures]);

		/* number of samples in each class */
		int classSizes[] = new int[nClasses];

		/* mean and number of samples in each class */
		for (Instance sample : data) {
			int i = data.classIndex(sample.classValue());
			means[i] = means[i].add(sample);
			classSizes[i]++;
		}

		/* global mean */
		Instance globalMean = new DenseInstance(new double[nFeatures]);
		RealMatrix classData[] = new RealMatrix[nClasses];
		for (int i = 0; i < nClasses; i++) {
			classData[i] = MatrixUtils.createRealMatrix(classSizes[i], nFeatures);
			means[i] = means[i].divide(classSizes[i]);
			globalMean = globalMean.add(means[i]);
		}
		globalMean = globalMean.divide(nClasses);

		/* split data for each class */
		int indices[] = new int[nClasses];
		for (Instance sample : data) {
			int i = data.classIndex(sample.classValue());
			RealVector v = MatrixUtils.createRealVector(InstanceTools.array(sample));
			classData[i].setRowVector(indices[i]++, v);
		}

		/* within-class scatter matrix */
		RealMatrix Sw = MatrixUtils.createRealMatrix(nFeatures, nFeatures);
		for (int i = 0; i < nClasses; i++) {
			Covariance cov = new Covariance(classData[i]);
			RealMatrix c = cov.getCovarianceMatrix();
			Sw = Sw.add(c);
		}

		/* between-class scatter matrix */
		RealMatrix Sb = MatrixUtils.createRealMatrix(nFeatures, nFeatures);
		for (int i = 0; i < nClasses; i++) {
			double[][] tmp = new double[1][nFeatures];
			tmp[0] = InstanceTools.array(means[i].minus(globalMean));
			RealMatrix diff = MatrixUtils.createRealMatrix(tmp);
			RealMatrix m = diff.transpose().multiply(diff).scalarMultiply(classSizes[i]);
			Sb = Sb.add(m);
		}

		/* getting the projection vectors */
		EigenDecomposition eig = new EigenDecomposition(Sb);
		double[] Lambda = eig.getRealEigenvalues();
		RealMatrix V = eig.getV();

		/*
		 * obtain the projection matrix using only the most relevant eigenvectors
		 */
		int nRelevant = 0;
		for (int i = 0; i < nFeatures; i++) {
			if (Lambda[i] > SbTh)
				nRelevant++;
		}

		double[] relevantLambda;
		RealMatrix relevantV;
		if (nRelevant == nFeatures) {
			relevantLambda = Lambda;
			relevantV = V;
		} else if (nRelevant == 0) {
			int maxEigIndex = 0;
			double maxEig = Lambda[0];
			for (int i = 1; i < nFeatures; i++) {
				if (Lambda[i] > maxEig) {
					maxEigIndex = i;
					maxEig = Lambda[i];
				}
			}
			relevantLambda = new double[1];
			relevantLambda[0] = maxEig;
			relevantV = MatrixUtils.createRealMatrix(nFeatures, 1);
			relevantV.setColumn(0, V.getColumn(maxEigIndex));
			nRelevant = 1;
		} else {
			relevantLambda = new double[nRelevant];
			relevantV = MatrixUtils.createRealMatrix(nFeatures, nRelevant);
			int j = 0;
			for (int i = 0; i < nFeatures; i++) {
				if (Lambda[i] > SbTh) {
					relevantLambda[j] = Lambda[i];
					relevantV.setColumn(j, V.getColumn(i));
					j++;
				}
			}
		}

		RealMatrix Db = relevantV.transpose().multiply(Sb).multiply(relevantV);
		double[] aux = new double[nRelevant];
		for (int i = 0; i < nRelevant; i++)
			aux[i] = 1.0 / Math.sqrt(Db.getEntry(i, i));
		RealMatrix Z = relevantV.multiply(new DiagonalMatrix(aux));

		eig = new EigenDecomposition(Z.transpose().multiply(Sw).multiply(Z));
		RealMatrix U = eig.getV();
		RealMatrix Dw = eig.getD();
		RealMatrix A = U.transpose().multiply(Z.transpose());

		for (int i = 0; i < nRelevant; i++)
			aux[i] = 1.0 / Math.sqrt(Dw.getEntry(i, i));
		RealMatrix Projs = new DiagonalMatrix(aux).multiply(A);

		return Projs;
	}

	/**
	 * Performs a projection of a given dataset with the directLDA algorithm. Uses a
	 * default value of 0.001 for the SbTh threshold.
	 * 
	 * @param data Input dataset. As LDA is a supervised dimensionality reduction
	 *             technique, this dataset must be labeled
	 * @return The projection matrix
	 */
	public static RealMatrix directLDA(Dataset data) {
		return directLDA(data, 0.001);
	}

	/**
	 * Generates a new dataset from the input data as a projection according to the
	 * given projection matrix.
	 * 
	 * @param data Input dataset
	 * @param proj Projection Matrix
	 * @return A new dataset containing only the selected features
	 */
	public static Dataset project(Dataset data, RealMatrix proj) {
		/* Number of features and samples in the dataset */
		int nFeatures = data.instance(0).noAttributes();
		int nData = data.size();

		/* Create the matrices */
		RealMatrix dataMatrix = MatrixUtils.createRealMatrix(nFeatures, nData);
		for (int i = 0; i < nData; i++) {
			double[] d = InstanceTools.array(data.instance(i));
			dataMatrix.setColumn(i, d);
		}

		/* Project the data */
		RealMatrix ProjectedData = proj.multiply(dataMatrix);

		/* Return the projected dataset */
		Dataset outData = new DefaultDataset();
		for (int i = 0; i < nData; i++) {
			outData.add(new DenseInstance(ProjectedData.getColumn(i), data.get(i).classValue()));
		}

		return outData;
	}

	/**
	 * Extracts a proportion of samples ramdomly from a original dataset and returns
	 * them as a new dataset. If data are labeled, this proportion is applied to
	 * each class.
	 * 
	 * @param original Original data
	 * @param prop     Proportion of samples extracted from the original dataset. It
	 *                 must have a value greater the 0 and lower than 1.
	 * @return A new dataset containing the extracted samples
	 */
	public static Dataset split(Dataset original, double prop) {
		/* Test the value of parameter prop */
		if (prop < 0 || prop > 1) {
			System.err.println(
					"ERROR: Parameter prop should have a value greater the 0 and lower than 1 in MoreDatasetTools.split");
			System.exit(-1);
		}

		/* Disjoin the original dataset into several datasets, one per class */
		Object classValues[] = original.classes().toArray();
		int nClasses = classValues.length;

		Dataset[] theClasses;
		if (nClasses > 1) {
			theClasses = new Dataset[nClasses];
			for (int i = 0; i < nClasses; i++)
				theClasses[i] = new DefaultDataset();

			while (original.size() > 0) {
				Instance ins = original.instance(0);
				original.remove(0);
				Object c = ins.classValue();
				for (int i = 0; i < nClasses; i++) {
					if (c.equals(classValues[i])) {
						theClasses[i].add(ins);
						break;
					}
				}
			}
		} else {
			nClasses = 1;
			theClasses = new Dataset[nClasses];
			theClasses[0] = original;
		}

		/* Create an empty dataset */
		Dataset outData = new DefaultDataset();

		/* Random generator */
		MersenneTwister rg = new MersenneTwister(System.currentTimeMillis());

		/* Generate the output dataset randomly */
		for (int i = 0; i < nClasses; i++) {
			int outSize = ((int) (((double) theClasses[i].size()) * prop));
			for (int j = 0; j < outSize; j++) {
				int pos = rg.nextInt(theClasses[i].size());
				outData.add(theClasses[i].instance(pos));
				theClasses[i].remove(pos);
			}
		}

		/* Leave the remaining samples in the original dataset */
		if (nClasses > 1) {
			for (int i = 0; i < nClasses; i++) {
				while (theClasses[i].size() > 0) {
					original.add(theClasses[i].instance(0));
					theClasses[i].remove(0);
				}
			}
		}

		return outData;
	}
}
