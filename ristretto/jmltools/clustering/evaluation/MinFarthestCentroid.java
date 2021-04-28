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

import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;

/**
 * This index estimates the separation of clusters as the minimum of
 * the max distance from a centroid to all the remaining centroids. This minimum
 * distance should be maximized.
 * 
 * @author Jesús González
 */
public class MinFarthestCentroid implements ClusterEvaluation {
	private DistanceMeasure dm;

	/**
	 * Construct a new evaluator that will use the Euclidean distance to measure
	 * the errors.
	 */
	public MinFarthestCentroid() {
		this(new EuclideanDistance());
	}

	/**
	 * Construct a new evaluator that will use the supplied distance metric to
	 * measure the errors
	 * 
	 * @param dm
	 *            Distance measure to be used in the evaluator
	 */
	public MinFarthestCentroid(DistanceMeasure dm) {
		this.dm = dm;
	}

	/**
	 * Return the score the current clusterer obtains on the dataset
	 * 
	 * @param clusters
	 *            Clusters to be evaluated
	 * @return The score the clusterer obtained on this particular dataset
	 */
	public double score(Dataset[] clusters) {
		// Obtain the centroids
		Dataset centroids = new DefaultDataset();

		for (int i = 0; i < clusters.length; i++) {
			if (clusters[i].size()>0)
				centroids.add(DatasetTools.average(clusters[i]));
		}

		double minMaxSeparation = Double.MAX_VALUE;

		for (int i = 0; i < centroids.size(); i++) {
			double maxSeparation = 0;
			for (int j = 0; j < centroids.size(); j++) {
				if (i != j) {
					double separation = dm.measure(centroids.instance(i), centroids.instance(j));
					if (separation > maxSeparation)
						maxSeparation = separation;
				}
			}
			if (maxSeparation < minMaxSeparation)
				minMaxSeparation = maxSeparation;
		}

		return minMaxSeparation;
	}

	/**
	 * Compare the two scores according to the criterion in the implementation
	 * Some criterions should be maximized, others should be minimized. This
	 * method returns true if the second score is 'better' than the first one
	 * 
	 * @param score1
	 *            The first score
	 * @param score2
	 *            The second score
	 *
	 * @return true if the second score is better than the first, false in all
	 *         other cases
	 */
	public boolean compareScore(double score1, double score2) {
		// TODO solve bug: score is NaN when clusters with 0 instances
		// should be minimized
		return score2 > score1;
	}
}
