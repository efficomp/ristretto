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

package ristretto.jmltools.clustering;

import java.util.Arrays;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;

/**
 * Implements the ELBG algorithm [1]
 * 
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td>G. Patanè and M. Russo. The Enhanced-LBG Algorithm, Neural Networks,
 * 14(9):1219-1237, 2001. <a href=
 * "https://doi.org/10.1016/S0893-6080(01)00104-6">https://doi.org/10.1016/S0893-6080(01)00104-6</a>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jesús González
 */
public class ELBG extends LBG {

	/**
	 * Utility of each cluster
	 */
	protected double[] utilities;

	/**
	 * Constructs a default ELBG clusterer with 4 clusters, a stop criterion of
	 * 1e-4, a default random generator and using the Euclidean distance.
	 */
	public ELBG() {
		this(defaultNumberOfClusters);
	}

	/**
	 * Constructs a default ELBG clusterer with the specified number of clusters, a
	 * stop criterion of 1e-4, a default random generator and using the Euclidean
	 * distance.
	 * 
	 * @param numberOfClusters The number of clusters to create
	 */
	public ELBG(int numberOfClusters) {
		this(numberOfClusters, defaultStopCriterion);
	}

	/**
	 * Create a new ELBG clusterer with the given number of clusters and stop
	 * criterion. The internal random generator is a new one based upon the current
	 * system time. For the distance we use the Euclidean n-space distance.
	 * 
	 * @param clusters      The number of clusters
	 * @param stopCriterion The stop criterion
	 */
	public ELBG(int clusters, double stopCriterion) {
		this(clusters, stopCriterion, new EuclideanDistance());
	}

	/**
	 * Create a new ELBG clusterer with the given number of clusters and distance
	 * measure. The internal random generator is a new one based upon the current
	 * system time.
	 * 
	 * @param clusters The number of clusters
	 * @param dm       The distance measure to use
	 */
	public ELBG(int clusters, DistanceMeasure dm) {
		this(clusters, defaultStopCriterion, dm);
	}

	/**
	 * Create a new ELBG clusterer with the given number of clusters, stop criterion
	 * and distance measure. The internal random generator is a new one based upon
	 * the current system time.
	 * 
	 * @param clusters      The number of clusters
	 * @param stopCriterion The stop criterion
	 * @param dm            The distance measure to use
	 */
	public ELBG(int clusters, double stopCriterion, DistanceMeasure dm) {
		super(clusters, stopCriterion, dm);
		this.utilities = new double[numberOfClusters];
	}

	/**
	 * Partitions the data according to the centroids. Modifies the assignment and
	 * utilities arrays.
	 * 
	 * @param data Data set to cluster
	 * 
	 * @return The mean distortion
	 */
	protected double partition(Dataset data) {

		double meanDistortion = super.partition(data);
		updateUtilities(meanDistortion);

		return meanDistortion;
	}

	/**
	 * Update the utility of each cluster
	 * 
	 * @param meanDistortion The mean distortion of the partition
	 * 
	 */
	protected void updateUtilities(double meanDistortion) {

		for (int i = 0; i < this.numberOfClusters; i++)
			this.utilities[i] = this.distortions[i] / meanDistortion;
	}

	/**
	 * Tries to migrate all the prototypes with an utility lower than one.
	 * Migrations will be confirmed only if the total distortion is lowered
	 */
	protected void elbgBlock() {
		double destinationProb[] = new double[this.numberOfClusters];

		for (int i = 0; i < this.numberOfClusters; i++) {
			// If cluster i has a utility lower than 1 ...
			if (this.utilities[i] < 1) {

				// Calculate the probability of each destination cluster
				Arrays.fill(destinationProb, 0.0);
				double utilitiesAcc = 0;
				for (int j = 0; j < this.numberOfClusters; j++) {
					if (this.utilities[j] > 1) {
						destinationProb[j] = this.utilities[j];
						utilitiesAcc += this.utilities[j];
					}
				}

				for (int j = 0; j < this.numberOfClusters; j++) {
					destinationProb[j] /= utilitiesAcc;
				}

				// Select a destination cluster according to the probabilities
				double rnd = rg.nextDouble();
				int dest = 0;
				double acc = destinationProb[dest];
				while (acc < rnd) {
					dest++;
					acc += destinationProb[dest];
				}

				migrationAttempt(i, dest);
			}
		}
	}

	/**
	 * Migrates the given centroid to the cluster with more distortion
	 *
	 * @param emigrant    Emigrant centroid
	 * @param destination Destination cluster
	 * @return true if the migration successes
	 */
	private boolean migrationAttempt(int emigrant, int destination) {
		// Find the closest prototype to emigrant
		double minDist = Double.POSITIVE_INFINITY;
		int closest = -1;
		for (int i = 0; i < this.numberOfClusters; i++) {
			if (i != emigrant) {
				double dist = dm.measure(this.centroids[i], this.centroids[emigrant]);
				if (dist < minDist) {
					minDist = dist;
					closest = i;
				}
			}
		}

		// Find the minimum and maximum point of the hyperbox containing the
		// destination cluster
		Instance maxAttributes = DatasetTools.maxAttributes(this.assignment[destination]);
		Instance minAttributes = DatasetTools.minAttributes(this.assignment[destination]);

		// Shift the prototypes to the principal diagonal of the hyperbox
		double tmp1[] = new double[instanceLength];
		double tmp2[] = new double[instanceLength];
		for (int i = 0; i < instanceLength; i++) {
			double offset = ((maxAttributes.value(i) - minAttributes.value(i)) / 4);
			tmp1[i] = minAttributes.value(i) + offset;
			tmp2[i] = minAttributes.value(i) + offset * 3;
		}
		Instance newEmigrantCentroid = new DenseInstance(tmp1);
		Instance newDestinationCentroid = new DenseInstance(tmp2);

		// Arrange the prototypes with a local LBG within the destination
		// cluster
		LocalLBG localLBG = this.new LocalLBG(newEmigrantCentroid, newDestinationCentroid);
		Dataset[] splitDestinationCluster = localLBG.cluster(this.assignment[destination]);
		if (splitDestinationCluster[0].size() > 0)
			newEmigrantCentroid = DatasetTools.average(splitDestinationCluster[0]);
		if (splitDestinationCluster[1].size() > 0)
			newDestinationCentroid = DatasetTools.average(splitDestinationCluster[1]);

		// Assign data belonging to the old emigrant cluster to its closest
		// cluster
		Dataset newClosestCluster = new DefaultDataset();
		for (int i = 0; i < this.assignment[emigrant].size(); i++)
			newClosestCluster.add(this.assignment[emigrant].instance(i));

		for (int i = 0; i < this.assignment[closest].size(); i++)
			newClosestCluster.add(this.assignment[closest].instance(i));

		// Estimate the closest centroid position after the migration
		Instance newClosestCentroid;
		if (newClosestCluster.size() > 0)
			newClosestCentroid = DatasetTools.average(newClosestCluster);
		else
			newClosestCentroid = centroids[closest];

		// Estimate whether the migration attempt has lowered the distortion
		double oldDistortion = this.distortions[emigrant] + this.distortions[closest] + this.distortions[destination];

		double newEmigrantDistortion = 0;
		for (int i = 0; i < splitDestinationCluster[0].size(); i++)
			newEmigrantDistortion += dm.measure(newEmigrantCentroid, splitDestinationCluster[0].instance(i));

		double newDestinationDistortion = 0;
		for (int i = 0; i < splitDestinationCluster[1].size(); i++)
			newDestinationDistortion += dm.measure(newDestinationCentroid, splitDestinationCluster[1].instance(i));

		double newClosestDistortion = 0;
		for (int i = 0; i < newClosestCluster.size(); i++)
			newClosestDistortion += dm.measure(newClosestCentroid, newClosestCluster.instance(i));

		double newDistortion = newEmigrantDistortion + newDestinationDistortion + newClosestDistortion;

		// The migration attempt is confirmed only if the distortion has been
		// lowered
		if (newDistortion < oldDistortion) {
			// Update the centroids
			this.centroids[emigrant] = newEmigrantCentroid;
			this.centroids[destination] = newDestinationCentroid;
			this.centroids[closest] = newClosestCentroid;

			// Update the assignment of data to the new clusters
			this.assignment[emigrant] = splitDestinationCluster[0];
			this.assignment[destination] = splitDestinationCluster[1];
			this.assignment[closest] = newClosestCluster;

			// Update the distortions
			this.distortions[emigrant] = newEmigrantDistortion;
			this.distortions[destination] = newDestinationDistortion;
			this.distortions[closest] = newClosestDistortion;

			// Update the utilities
			double meanDistortion = 0;
			for (int i = 0; i < this.numberOfClusters; i++)
				meanDistortion += this.distortions[i];

			meanDistortion /= this.numberOfClusters;

			updateUtilities(meanDistortion);

			return true;
		}
		return false;
	}

	/**
	 * Execute the ELBG clustering algorithm on the data set that is provided.
	 * 
	 * @param data Data set to cluster
	 */
	public Dataset[] cluster(Dataset data) {
		if (data.size() == 0)
			throw new RuntimeException("The dataset should not be empty");
		if (numberOfClusters == 0)
			throw new RuntimeException("There should be at least one cluster");

		// Initialize the centroids
		init(data);
		double lastDistortion = partition(data);
		double distortion;
		double improvement = Double.POSITIVE_INFINITY;
		do {
			elbgBlock();
			actualize();
			distortion = partition(data);
			improvement = (lastDistortion - distortion) / distortion;
			lastDistortion = distortion;
		} while (improvement >= stopCriterion);

		return this.assignment;
	}

	private class LocalLBG extends LBG {

		/**
		 * Constructs the Local LBG with the two provided prototypes
		 * 
		 * @param c1 First Centroid
		 * @param c2 Second Centroid
		 */
		public LocalLBG(Instance c1, Instance c2) {
			this(c1, c2, 0.2);
		}

		/**
		 * Constructs the Local LBG with the two provided prototypes
		 * 
		 * @param c1 First Centroid
		 * @param c2 Second Centroid
		 */
		public LocalLBG(Instance c1, Instance c2, double stopCriterion) {
			super(2, stopCriterion, ELBG.this.dm);
			this.centroids[0] = c1;
			this.centroids[1] = c2;
		}

		/**
		 * Initializes the centroids to random values This methods is empty, as the two
		 * centroids have been already set in the constructor
		 * 
		 * @param data Data set to cluster
		 */
		protected void init(Dataset data) {
		}
	}
}
