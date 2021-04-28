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

package tests.testCVI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import ristretto.jmltools.clustering.ELBG;
import ristretto.jmltools.clustering.evaluation.CVIFSNormalizer;
import ristretto.jmltools.clustering.evaluation.FarthestCentroids;
import ristretto.jmltools.clustering.evaluation.OverallDeviation;
import ristretto.jmltools.distance.FSEuclideanDistance;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Test a separation and a compactness CVI
 * 
 * @author jesus
 */
public class TestCVI {

	private static boolean verbose = true;

	/*
	 * Dataset
	 */
	private static Dataset data;

	/*
	 * Number of clusters
	 */
	private static int nClusters;

	/*
	 * Mask for the selected attributes
	 */
	private static boolean[] mask;

	/*
	 * Number of selected features
	 */
	private static int nFeatures;

	/*
	 * Clusters
	 */
	private static Dataset[] clusters;

	/*
	 * The centroids of the different clusters
	 */
	private static Dataset centroids;

	/**
	 * CVIs
	 */
	private static double separationScore;
	private static double compactnessScore;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Dataset: Path to the dataset (string)</li>
	 * <li>nClusters: Number of clusters (int, &gt; 1)</li>
	 * <li>feats: list of selected features (sequence of int)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {
		/* The first argument is expected to be the path to the dataset */
		if (args.length < 1) {
			System.err.println("Error: Missing dataset");
			System.exit(-1);
		}

		/* The second argument is expected to be the number of clusters */
		if (args.length < 2) {
			System.err.println("Error: Missing number of clusters");
			System.exit(-1);
		}

		/* Load the dataset */
		data = new DefaultDataset();
		try {
			data = FileHandler.loadDataset(new File(args[0]), "\\s+");
			if (verbose) {
				System.out.println("Data file: " + args[0]);
			}
		} catch (IOException e) {
			System.err.println("Error: Couldn't open dataset file: " + args[0]);
			System.exit(-1);
		}

		/* Obtain the number of clusters */
		try {
			nClusters = Integer.parseInt(args[1]);

			if (nClusters < 2) {
				System.err.println("Error: The number of clusters should be greater than 1");
				System.exit(-1);
			}
			if (verbose)
				System.out.println("Number of clusters: " + nClusters);
		} catch (NumberFormatException e) {
			System.err.println("Invalid number of clusters: " + nClusters);
			System.exit(-1);
		}

		/* Obtain the selected features */
		int maxAttrIndex = data.noAttributes();
		mask = new boolean[maxAttrIndex];

		Arrays.fill(mask, false);

		for (int i = 2; i < args.length; i++) {
			try {
				int feat = Integer.parseInt(args[i]);

				if (feat >= mask.length || feat < 0) {
					System.err.println(
							"Error: Feature index " + feat + " is not in the range [0.." + (mask.length - 1) + "]");
					System.exit(-1);
				}
				mask[feat] = true;
			} catch (NumberFormatException e) {
				System.err.println("Error: Invalid feature index: " + args[i]);
				System.exit(-1);
			}
		}

		/* Obtain the number of selected features */
		nFeatures = 0;
		for (int i = 0; i < mask.length; i++)
			if (mask[i])
				nFeatures++;
	}

	/**
	 * Makes the test
	 */
	private static void makeTest() {
		/*
		 * Create a new instance the Euclidean distance that will only use the masked
		 * features
		 */
		DistanceMeasure dm = new FSEuclideanDistance(mask);

		/* Create a new instance of the ELBG algorithm */
		Clusterer clusterer = new ELBG(nClusters, dm);

		/* Cluster the data according only to some selected features */
		clusters = clusterer.cluster(data);

		/* Obtain the centroids */
		centroids = new DefaultDataset();
		for (int i = 0; i < nClusters; i++)
			centroids.add(DatasetTools.average(clusters[i]));

		CVIFSNormalizer cviNormalizer = new CVIFSNormalizer(nFeatures, clusters);

		ClusterEvaluation separation = new FarthestCentroids(dm);
		separationScore = separation.score(clusters);

		ClusterEvaluation compactness = new OverallDeviation(dm);
		compactnessScore = compactness.score(clusters) / cviNormalizer.refClusterSize();
	}

	/**
	 * Prints the results and/or save the data
	 */
	private static void writeResults() {
		/* Save the cluster prototypes */
		try {
			File centroidsFile = new File("centroids.data");
			BufferedWriter centroidsWriter = new BufferedWriter(new FileWriter(centroidsFile));

			for (Instance centroid : centroids) {
				String s = "";

				for (Double d : centroid) {
					if (s.length() > 0)
						s += "\t";

					s += String.format("%.6f", d);
				}

				centroidsWriter.write(s + "\n");
			}
			centroidsWriter.close();
			if (verbose)
				System.out.println("Saving centroids to file centroids.data");
		} catch (IOException e) {
			System.err.println("Error: Couldn't create the centroids file");
			System.exit(-1);
		}

		/* Print the selected results */
		if (verbose) {
			System.out.print("\nSelected features:");
			for (int i = 0; i < mask.length; i++)
				if (mask[i])
					System.out.print(" " + i);

			System.out.print("\n");
			System.out.printf("Separation: % .6f\n", separationScore);
			System.out.printf("Compactness: % .6f\n", compactnessScore);
		} else {
			for (int i = 0; i < mask.length; i++) {
				if (mask[i])
					System.out.print(i);
				else
					System.out.print("-");
				System.out.print("\t");
			}

			System.out.printf("% .6f\t", separationScore);
			System.out.printf("% .6f\n", compactnessScore);
		}
	}

	/**
	 * Test a separation and a compactness CVI
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Dataset: Path to the dataset (string)</li>
	 * <li>nClusters: Number of clusters (int, &gt; 1)</li>
	 * <li>feats: list of selected features (sequence of int)</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		readParams(args);
		makeTest();

		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		writeResults();
	}
}
