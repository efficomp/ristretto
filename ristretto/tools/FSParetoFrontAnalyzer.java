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

package ristretto.tools;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import ec.util.Code;
import ec.util.DecodeReturn;

/**
 * Obtain the relevance of each feature in the whole Pareto front.
 * <p>
 * The relevance of one feature is calculated as the number of occurrences of
 * the feature in the Pareto front divided by the number of solutions in the
 * Pareto front [1]
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
public class FSParetoFrontAnalyzer {
	/** Pareto front file */
	static Scanner frontScanner;

	/** Number of occurrences of each feature in the Pareto front */
	static TreeMap<Integer, Integer> featureOccurrences = new TreeMap<Integer, Integer>();

	/** Number of solutions in the Pareto front */
	static int nSolutions;

	/**
	 * True if solutions are represented with a bitvector, false if a subset of
	 * features is used
	 */
	static boolean bitVectorIndividuals;

	/** Individual types */
	private static String bitVectorType = "bitvector";
	private static String subsetType = "subset";

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Individual representation: "bitvector" or "subset"</li>
	 * <li>Path to the front file</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {
		try {
			/*
			 * The first argument is expected to be the kind of individual representation
			 * bitvector or subset
			 */
			if (args.length < 1) {
				System.err.println("Error: Missing individual type");
				System.exit(-1);
			}
			if (args[0].equalsIgnoreCase(bitVectorType))
				bitVectorIndividuals = true;
			else if (args[0].equalsIgnoreCase(subsetType))
				bitVectorIndividuals = false;
			else {
				System.err.println(
						"Error: the first argument should be the individual type, bitvector or subset are the only allowed types");
				System.exit(-1);
			}

			/*
			 * The second argument is expected to be the path to the fronts.stat file
			 */
			if (args.length < 2) {
				System.err.println("Error: Missing front file");
				System.exit(-1);
			}

			frontScanner = new Scanner(Paths.get(args[1]));

		} catch (IOException e) {
			System.err.println("Error: Couldn't open the front file: " + args[1]);
			System.exit(-1);
		}
	}

	/**
	 * Make the analysis of the front file for bitvector individuals
	 */
	private static void makeAnalysisBitVector() {
		while (frontScanner.hasNext()) {
			/* Obtains the next individual */
			String individual = frontScanner.next();
			nSolutions++;

			DecodeReturn d = new DecodeReturn(individual);
			Code.decode(d);
			if (d.type != DecodeReturn.T_INTEGER) {
				System.out.println("Individual with genome:\n" + individual
						+ "\n... does not have an integer at the beginning indicating the genome count.");
				System.exit(-1);
			}

			int genomeLength = (int) (d.l);

			// read in the genes
			for (int i = 0; i < genomeLength; i++) {
				Code.decode(d);
				if (d.l != 0) {
					Integer currentOccurrences = featureOccurrences.get(i);
					if (currentOccurrences == null)
						featureOccurrences.put(i, 1);
					else
						featureOccurrences.put(i, currentOccurrences + 1);
				}
			}

			/* Skips the rest of the line */
			frontScanner.nextLine();
		}

		frontScanner.close();
	}

	/**
	 * Make the analysis of the front file for subset individuals
	 */
	private static void makeAnalysisSubset() {
		while (frontScanner.hasNext()) {
			/* Obtains the next individual */
			String individual = frontScanner.next();
			nSolutions++;

			DecodeReturn d = new DecodeReturn(individual);
			Code.decode(d);
			if (d.type != DecodeReturn.T_INTEGER) {
				System.out.println("Individual with genome:\n" + individual
						+ "\n... does not have an integer at the beginning indicating the genome count.");
				System.exit(-1);
			}

			int genomeLength = (int) (d.l);

			// read in the genes
			for (int i = 0; i < genomeLength; i++) {
				Code.decode(d);
				int feature = (int) d.l;
				Integer currentOccurrences = featureOccurrences.get(feature);
				if (currentOccurrences == null)
					featureOccurrences.put(feature, 1);
				else
					featureOccurrences.put(feature, currentOccurrences + 1);
			}

			/* Skips the rest of the line */
			frontScanner.nextLine();
		}

		frontScanner.close();
	}

	/**
	 * Print the results of the analysis
	 */
	private static void writeResults() {
		System.out.println("Feature\tRelevance");
		for (Map.Entry<Integer, Integer> entry : featureOccurrences.entrySet()) {
			int feature = entry.getKey();
			int ocurrences = entry.getValue();
			System.out.println("" + feature + "\t" + ((double) ocurrences / nSolutions));
		}
	}

	/**
	 * Obtain the relevance of each feature in the whole Pareto front.
	 * <p>
	 * The relevance of one feature is calculated as the number of occurrences of
	 * the feature in the Pareto front divided by the number of solutions in the
	 * Pareto front
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Individual representation: "bitvector" or "subset"</li>
	 * <li>Path to the front file</li>
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		readParams(args);
		if (bitVectorIndividuals)
			makeAnalysisBitVector();
		else
			makeAnalysisSubset();

		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		writeResults();
	}

}
