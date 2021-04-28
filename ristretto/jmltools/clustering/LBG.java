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

import ec.util.MersenneTwister;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;

/**
 * Implements the LBG algorithm [1].
 * 
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td>Y. Linde, A. Buzo and R. Gray. An Algorithm for Vector Quantization
 * Design, IEEE Transactions on Communications, 28(1):84-94, 1980. <a href=
 * "https://doi.org/10.1109/TCOM.1980.1094577">https://doi.org/10.1109/TCOM.1980.1094577</a>
 * </td>
 * </tr>
 * </table>
 *
 * @author Jesús González
 * 
 */
public class LBG implements Clusterer {
	/**
	 * Number of clusters.
	 */
	protected int numberOfClusters = -1;

	/**
	 * Stop criterion
	 */
	protected double stopCriterion;

	/**
	 * Random generator for this clusterer.
	 */
	protected MersenneTwister rg;

	/**
	 * Distance measure used in the algorithm, defaults to Euclidean distance.
	 */
	protected DistanceMeasure dm;

	/**
	 * Centroids of the different clusters.
	 */
	protected Instance[] centroids;

	/**
	 * Instances length
	 */
	protected int instanceLength;

	/**
	 * Distortion of each cluster
	 */
	protected double[] distortions;

	/**
	 * Cluster assignment for each datum
	 */
	protected Dataset[] assignment;

	/**
	 * Default value for the stop criterion
	 */
	protected static double defaultStopCriterion = 1e-4;

	/**
	 * Default value for the number of centroids
	 */
	protected static int defaultNumberOfClusters = 4;

	/**
	 * Construct a default LBG clusterer with 4 clusters, a stop criterion of 1e-4,
	 * a default random generator and using the Euclidean distance.
	 */
	public LBG() {
		this(defaultNumberOfClusters);
	}

	/**
	 * Construct a default LBG clusterer with the specified number of clusters, a
	 * stop criterion of 1e-4, a default random generator and using the Euclidean
	 * distance.
	 * 
	 * @param nClusters Number of clusters to create
	 */
	public LBG(int nClusters) {
		this(nClusters, defaultStopCriterion);
	}

	/**
	 * Create a new LBG clusterer with the given number of clusters and stop
	 * criterion. The internal random generator is a new one based upon the current
	 * system time. For the distance we use the Euclidean n-space distance.
	 * 
	 * @param nClusters     Number of clusters
	 * @param stopCriterion Stop criterion
	 */
	public LBG(int nClusters, double stopCriterion) {
		this(nClusters, stopCriterion, new EuclideanDistance());
	}

	/**
	 * Create a new LBG clusterer with the given number of clusters and distance
	 * measure. The internal random generator is a new one based upon the current
	 * system time.
	 * 
	 * @param nClusters Number of clusters
	 * @param dm        Distance measure to use
	 */
	public LBG(int nClusters, DistanceMeasure dm) {
		this(nClusters, defaultStopCriterion, dm);
	}

	/**
	 * Create a new LBG clusterer with the given number of clusters, stop criterion
	 * and distance measure. The internal random generator is a new one based upon
	 * the current system time.
	 * 
	 * @param nClusters     Number of clusters
	 * @param stopCriterion Stop criterion
	 * @param dm            Distance measure to use
	 */
	public LBG(int nClusters, double stopCriterion, DistanceMeasure dm) {
		this.numberOfClusters = nClusters;
		this.stopCriterion = stopCriterion;
		this.dm = dm;
		this.rg = new MersenneTwister(System.currentTimeMillis());
		this.centroids = new Instance[numberOfClusters];
		this.distortions = new double[numberOfClusters];
		this.assignment = new Dataset[numberOfClusters];
	}

	/**
	 * Initialize the centroids to random values
	 * 
	 * @param data Data set to cluster
	 */
	protected void init(Dataset data) {
		instanceLength = data.instance(0).noAttributes();
		Instance maxAttributes = DatasetTools.maxAttributes(data);
		Instance minAttributes = DatasetTools.minAttributes(data);

		for (int i = 0; i < numberOfClusters; i++) {
			double[] randomInstance = new double[instanceLength];
			for (int j = 0; j < instanceLength; j++) {
				double dist = Math.abs(maxAttributes.value(j) - minAttributes.value(j));
				randomInstance[j] = (float) (minAttributes.value(j) + rg.nextDouble() * dist);
			}

			this.centroids[i] = new DenseInstance(randomInstance);
		}
	}

	/**
	 * Partition the data according to the centroids. Modify the assignment and
	 * utilities arrays.
	 * 
	 * @param data Data set to cluster
	 * 
	 * @return The mean distortion
	 */
	protected double partition(Dataset data) {
		Arrays.fill(this.distortions, 0.0);
		double meanDistortion = 0;

		for (int i = 0; i < this.numberOfClusters; i++)
			this.assignment[i] = new DefaultDataset();

		// Assign each object to the group that has the closest centroid.
		for (int i = 0; i < data.size(); i++) {
			int tmpCluster = 0;
			double minDistance = dm.measure(this.centroids[tmpCluster], data.instance(i));
			for (int j = tmpCluster + 1; j < this.centroids.length; j++) {
				double dist = dm.measure(this.centroids[j], data.instance(i));
				if (dm.compare(dist, minDistance)) {
					minDistance = dist;
					tmpCluster = j;
				}
			}
			this.assignment[tmpCluster].add(data.instance(i));
			this.distortions[tmpCluster] += minDistance;
			meanDistortion += minDistance;
		}
		meanDistortion /= this.numberOfClusters;

		return meanDistortion;
	}

	/**
	 * Actualize the centroids according to the last partition
	 */
	protected void actualize() {
		for (int i = 0; i < this.numberOfClusters; i++) {
			if (this.assignment[i].size() != 0)
				centroids[i] = DatasetTools.average(this.assignment[i]);
		}
	}

	/**
	 * Execute the LBG clustering algorithm on the data set that is provided.
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
			actualize();
			distortion = partition(data);
			improvement = (lastDistortion - distortion) / distortion;
			lastDistortion = distortion;
		} while (improvement >= stopCriterion);

		return this.assignment;
	}
}
