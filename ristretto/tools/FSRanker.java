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
 * Copyright (c) 2017, EFFICOMP
 */

package ristretto.tools;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.Comparator;

/**
 * Translate a relevances file into a ranks file [1]
 *
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td>J. González, J. Ortega, M. Damas, P. Martín-Smith and John Q. Gan. A new
 * multi-objective wrapper method for feature selection -- Accuracy and
 * stability analysis for BCI. Neurocomputing, 333:407-418, 2019. <a href=
 * "https://doi.org/10.1016/j.neucom.2019.01.017">https://doi.org/10.1016/j.neucom.2019.01.017</a>
 * </td>
 * </tr>
 * </table>
 *
 * @author Jesús González
 */
public class FSRanker {

	/**
	 * Allow the comparison of two features in terms of their relevance 
	 * @author Jesús González
	 */
	static class RankedFeature implements Comparable<RankedFeature> {
		/** Feature index */
		public int feature;
		
		/** Relevance */
		public double relevance;
		
		/** Rank */
		public double rank;

		/**
		 * Default constructor
		 */
		public RankedFeature() {
			this.feature = 0;
			this.relevance = 0;
			this.rank = 0;
		}

		/**
		 * Constructor
		 * 
		 * @param feature
		 *            A feature index
		 */
		public RankedFeature(int feature) {
			this();
			this.feature = feature;
		}

		/**
		 * Constructor
		 * 
		 * @param feature
		 *            A feature index
		 * @param relevance
		 *            Its relevance
		 */
		public RankedFeature(int feature, double relevance) {
			this(feature);
			this.relevance = relevance;
		}

		/**
		 * Compare with other ranked feature
		 * @param other The other ranked feature
		 * @return this.feature - other.feature
		 */
		@Override
		public int compareTo(RankedFeature other) {
			return this.feature - other.feature;
		}

		/**
		 * Comparator of two ranked features
		 */
		public static Comparator<RankedFeature> RelevanceComparator = new Comparator<RankedFeature>() {

			/**
			 * Compare two ranked features in terms of theur relevance
			 * @param rf1 The first ranked feature
			 * @param rf2 The second ranked feature
			 * @return rf2.relevance - rf1.relevance
			 */
			public int compare(RankedFeature rf1, RankedFeature rf2) {
				double diff = rf2.relevance - rf1.relevance; 
				if (diff < 0) return -1;
				else if (diff > 0) return 1;
				else return 0;
			}
		};
	}

	/** Number of features */
	static int nFeatures;

	/** All the features */
	static RankedFeature[] featureSet;

	/** Scanner for relevance files */
	static Scanner scanner;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Number of features in the dataset</li>
	 * <li>Path to the relevances file</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {

		/*
		 * The first argument should be the number of features in the dataset
		 */
		if (args.length < 1) {
			System.err.println("Error: The first argument should be the number of features in the dataset");
			System.exit(-1);
		}

		/*
		 * The second argument should be a relevances file
		 */
		if (args.length < 2) {
			System.err.println("Error: Missing relevance file");
			System.exit(-1);
		}

		try {
			nFeatures = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[0] + " is not a correct number of features");
			System.exit(-1);
		}

		featureSet = new RankedFeature[nFeatures];
		for (int i = 0; i < nFeatures; i++)
			featureSet[i] = new RankedFeature(i);

		String relevanceFile = args[1];
		try {
			scanner = new Scanner(Paths.get(relevanceFile));

			// The first line is a header
			if (!(scanner.hasNext() && scanner.next().equalsIgnoreCase("feature")))
				System.err.println("Error: Missing header in " + relevanceFile);

			if (!(scanner.hasNext() && scanner.next().equalsIgnoreCase("relevance")))
				System.err.println("Error: Missing header in " + relevanceFile);

			while (scanner.hasNext()) {
				/* Obtains the next feature */
				int feature = scanner.nextInt();
				double relevance = scanner.nextDouble();
				featureSet[feature] = new RankedFeature(feature, relevance);

				/* Skips the rest of the line */
				scanner.nextLine();
			}

			scanner.close();
		} catch (IOException e) {
			System.err.println("Error: Couldn't open the relevance file: " + relevanceFile);
			System.exit(-1);
		}
	}

	/**
	 * Rank the features
	 */
	private static void rank() {

		Arrays.sort(featureSet, RankedFeature.RelevanceComparator);
		
		int index=0;
		
		while (index<nFeatures){
			int begin = index;
			double relevance = featureSet[begin].relevance;
			double rank = index;

			index++;
			while (index<nFeatures) {
				double nextRelevance = featureSet[index].relevance;
				if (nextRelevance == relevance) {
					rank += index;
					index++;
				}
				else {
					break;
				}
			}
			rank /= (index-begin);
			
			for (int j=begin ; j<index ; j++)
				featureSet[j].rank = rank;
		}
	}

	/**
	 * Print the results
	 */
	private static void writeResults() {

		Arrays.sort(featureSet);
		
		System.out.println("Feature\tRank");
		for (int i = 0; i < nFeatures; i++) {
			System.out.println(featureSet[i].feature + "\t" + featureSet[i].rank);
		}
	}

	/**
	 * Translate a relevances file into a ranks file
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Number of features in the dataset</li>
	 * <li>Path to the relevances file</li>
	 * </ol>
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas
		 * for decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		readParams(args);
		rank();
		writeResults();
	}

}
