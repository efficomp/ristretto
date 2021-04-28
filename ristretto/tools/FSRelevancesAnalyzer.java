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
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Gather the relevances from several experiments
 * 
 * @author Jesús González
 */
public class FSRelevancesAnalyzer {
	/** Scanner for relevance files */
	static Scanner scanner;

	/** Relevances obtained for each feature */
	static TreeMap<Integer, ArrayList<Double>> combinedRelevances = new TreeMap<Integer, ArrayList<Double>>();

	/** Maximum number of relevance values for a feature */
	static int maxValues;

	/**
	 * Make the analysis of the relevance files
	 * 
	 * @param args Paths to several relevance files (from different experiments over
	 *             the same dataset)
	 */
	private static void makeAnalysis(String[] args) {
		/*
		 * The arguments should be relevance files from different experiments
		 */
		if (args.length < 1) {
			System.err.println("Error: Missing relevance files");
			System.exit(-1);
		}

		maxValues = 0;

		for (String relevanceFile : args) {
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
					ArrayList<Double> relevances = combinedRelevances.get(feature);
					if (relevances == null)
						relevances = new ArrayList<Double>();
					relevances.add(relevance);
					combinedRelevances.put(feature, relevances);
					maxValues = Math.max(maxValues, relevances.size());

					/* Skips the rest of the line */
					scanner.nextLine();
				}

				scanner.close();

			} catch (IOException e) {
				System.err.println("Error: Couldn't open the relevance file: " + relevanceFile);
				System.exit(-1);
			}
		}
	}

	/**
	 * Print the results of the analysis
	 */
	private static void writeResults() {
		System.out.print("Feature");
		for (int i = 0; i < maxValues; i++)
			System.out.print("\tValue" + i);
		System.out.println();
		for (Map.Entry<Integer, ArrayList<Double>> entry : combinedRelevances.entrySet()) {

			int feature = entry.getKey();
			System.out.print(feature);
			ArrayList<Double> relevances = entry.getValue();
			Collections.sort(relevances);

			for (int i = relevances.size(); i < maxValues; i++)
				System.out.print("\t" + "0.0");

			for (double relevance : relevances) {
				System.out.print("\t" + relevance);
			}
			System.out.println();
		}
	}

	/**
	 * Gather the relevances from several experiments
	 * 
	 * @param args Paths to several relevance files (from different experiments over
	 *             the same dataset)
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		makeAnalysis(args);
		writeResults();
	}
}
