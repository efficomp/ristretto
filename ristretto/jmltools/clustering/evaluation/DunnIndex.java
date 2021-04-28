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
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;

/**
 * This class implements the CVI proposed by Dunn in [1].
 * 
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td>J. C. Dunn. A fuzzy relative of the ISODATA process and its use in
 * detecting compact well-separated clusters. Journal of Cybernetics, 3:32-57,
 * 1973. <a href=
 * "https://doi.org/10.1080/01969727308546046">https://doi.org/10.1080/01969727308546046</a>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jesús González
 */
public class DunnIndex implements ClusterEvaluation {
	private DistanceMeasure dm;

	/**
	 * Construct a new evaluator that will use the Euclidean distance to measure the
	 * errors.
	 */
	public DunnIndex() {
		this(new EuclideanDistance());
	}

	/**
	 * Construct a new evaluator that will use the supplied distance metric to
	 * measure the errors
	 *
	 * @param dm Distance measure to be used in the evaluator
	 */
	public DunnIndex(DistanceMeasure dm) {
		this.dm = dm;
	}

	/**
	 * Return the score the current clusterer obtains on the dataset
	 * 
	 * @param clusters Clusters to be evaluated
	 *
	 * @return The score the clusterer obtained on this particular dataset
	 */
	public double score(Dataset[] clusters) {
		double minSeparation = Double.MAX_VALUE;
		double maxDiameter = 0;

		for (int i = 0; i < clusters.length - 1; i++) {
			for (int s = 0; s < clusters[i].size(); s++) {
				for (int j = i + 1; j < clusters.length; j++) {
					for (int t = 0; t < clusters[j].size(); t++) {
						double distance = dm.measure(clusters[i].instance(s), clusters[j].instance(t));
						if (distance < minSeparation)
							minSeparation = distance;
					}
				}

				for (int k = s + 1; k < clusters[i].size(); k++) {
					double distance = dm.measure(clusters[i].instance(s), clusters[i].instance(k));
					if (distance > maxDiameter)
						maxDiameter = distance;
				}
			}
		}
		return minSeparation / maxDiameter;
	}

	/**
	 * Compare the two scores according to the criterion in the implementation Some
	 * criterions should be maximized, others should be minimized. This method
	 * returns true if the second score is 'better' than the first one
	 * 
	 * @param score1 The first score
	 * @param score2 The second score
	 *
	 * @return true if the second score is better than the first, false in all other
	 *         cases
	 */
	public boolean compareScore(double score1, double score2) {
		// TODO solve bug: score is NaN when clusters with 0 instances
		// should be minimized
		return score2 < score1;
	}
}
