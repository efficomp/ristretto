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

package tests.testSplit;

import java.io.File;
import ristretto.jmltools.MoreDatasetTools;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Test of the split function of the MoreDatasetTools class
 * 
 * @author Jesús González
 * 
 */
public class TestSplit {
	/**
	 * Test of the split function of the MoreDatasetTools class.
	 * 
	 * Split a dataset into training and test and use KNN as classifier
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>dataset: Path to the dataset (string)</li>
	 * <li>class-index: Index of the class column (int, &gt;= 0)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) throws Exception {

		/*
		 * Load a data set and normalize all the attributes in the range [0..1]
		 */
		Dataset train = FileHandler.loadDataset(new File(args[0]), Integer.parseInt(args[1]), "\\s+");
		Dataset validation = MoreDatasetTools.split(train,0.4);

		/*
		 * Construct a KNN classifier that uses 5 neighbors to make a decision.
		 */
		Classifier knn = new KNearestNeighbors(5);
		knn.buildClassifier(train);
		
		/* Counters for correct and wrong predictions. */
		int correct = 0, wrong = 0;
		/* Classify all instances and check with the correct class values */
		for (Instance inst : validation) {
			Object predictedClassValue = knn.classify(inst);
			Object realClassValue = inst.classValue();
			if (predictedClassValue.equals(realClassValue))
				correct++;
			else
				wrong++;
		}
		System.out.println("Correct predictions  " + correct);
		System.out.println("Wrong predictions " + wrong);
	}
}
