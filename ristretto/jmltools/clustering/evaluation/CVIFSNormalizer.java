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

package ristretto.jmltools.clustering.evaluation;

import net.sf.javaml.core.Dataset;

/**
 * This class allows to normalize CVI values for Feature Selection problems.
 * <p>
 * As CVI indexes are usually based on distances, two CVI values can not be
 * directly compared if different number of input features have been used to
 * make each clustering process, as all distance metrics depend on the number of
 * features or attributes of the dataset.
 * <p>
 * In order to make this comparison possible, this class provides several scale
 * factors to make possible the comparison of two CVIs regardless of the number
 * of input features used by the clustering algorithm.
 * 
 * @author Jesús González
 */
public class CVIFSNormalizer {

	private double _refClusterSize;
	private double _maxDistance;

	/**
	 * Construct the normalizer
	 * 
	 * @param nFeatures Number of selected features
	 * @param clusters  Clusters detected by the clusterer
	 */
	public CVIFSNormalizer(int nFeatures, Dataset[] clusters) {
		_refClusterSize = 1 / Math.pow((double) countNonEmptyClusters(clusters), 1 / ((double) nFeatures));
		_maxDistance = Math.sqrt(nFeatures);
	}

	/**
	 * Check if there are empty clusters. If so, empty clusters are moved to the end
	 * of the assignment array
	 * 
	 * @param clusters Clusters detected by the clusterer
	 */
	protected int countNonEmptyClusters(Dataset[] clusters) {
		int nonEmptyClusters = 0;
		/* Test every cluster */
		for (int i = 0; i < clusters.length; i++) {
			/* If cluster i is empty */
			if (clusters[i].size() != 0)
				nonEmptyClusters++;
		}

		return nonEmptyClusters;
	}

	/**
	 * Return the average size of the clusters that would be obtained for a random
	 * dataset with the same input features and number of clusters
	 * 
	 * refClusterDiameter = 1 / Math.pow(nClusters, 1/nSelectedFeatures)
	 */
	public double refClusterSize() {
		return _refClusterSize;
	}

	/**
	 * Return the maximum distance between two data in a normalized dataset
	 */
	public double maxDistance() {
		return _maxDistance;
	}

}
