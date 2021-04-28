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
import java.util.Scanner;

/**
 * Computes the Spearman score achieved by a set of experiments
 * 
 * @author Jesús González
 */
public class FSStabilitySpearmanScorer {
	private static int nFeatures;
	private static int nExperiments;
	private static double[][] rankings;

	/* Scanner for ranking files */
	private static Scanner scanner;

	/**
	 * Read the parameters from the command-line
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Number of features in the dataset</li>
	 * <li>Paths to ranking files from different experiments
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	private static void readParams(String[] args) {
		/*
		 * The first argument should be the number of features in the dataset
		 */
		/* The first argument is expected to be the path to the dataset */
		if (args.length < 1) {
			System.err.println("Error: The first argument should be the number of features in the dataset");
			System.exit(-1);
		}

		/*
		 * The remaining arguments should be ranking files from different experiments
		 */
		if (args.length < 2) {
			System.err.println("Error: Missing ranking files");
			System.exit(-1);
		}

		try {
			nFeatures = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Error: " + args[0] + " is not a correct number of features");
			System.exit(-1);
		}

		nExperiments = args.length - 1;
		rankings = new double[nFeatures][nExperiments];

		for (int i = 1; i < args.length; i++) {
			String rankingFile = args[i];
			try {
				scanner = new Scanner(Paths.get(rankingFile));

				// The first line is a header
				if (!(scanner.hasNext() && scanner.next().equalsIgnoreCase("feature")))
					System.err.println("Error: Missing header in " + rankingFile);

				if (!(scanner.hasNext() && scanner.next().equalsIgnoreCase("rank")))
					System.err.println("Error: Missing header in " + rankingFile);

				while (scanner.hasNext()) {
					/* Obtains the next feature */
					int feature = scanner.nextInt();
					double rank = scanner.nextDouble();
					rankings[feature][i - 1] = rank;

					/* Skips the rest of the line */
					scanner.nextLine();
				}

				scanner.close();
			} catch (IOException e) {
				System.err.println("Error: Couldn't open the ranking file: " + rankingFile);
				System.exit(-1);
			}
		}
	}

	/**
	 * Computes the Spearman correlation index for a couple of experiments
	 * 
	 * @param i Index of the first experiment
	 * @param j Index of the second experiment
	 */
	private static double spearmanIndex(int i, int j) {
		double acc = 0;
		double nFeatures2 = nFeatures * nFeatures;

		for (int k = 0; k < nFeatures; k++) {
			double diff = rankings[k][i] - rankings[k][j];
			acc += (diff * diff);
		}

		return 1 - (6 * acc / (nFeatures * (nFeatures2 - 1)));
	}

	/**
	 * Return the Spearman score achieved by a set of experiments
	 */
	private static double computeScore() {
		double acc = 0;
		for (int i = 0; i < nExperiments - 1; i++) {
			for (int j = i + 1; j < nExperiments; j++) {
				acc += spearmanIndex(i, j);

			}
		}

		return (2 * acc) / (nExperiments * (nExperiments - 1));
	}

	/**
	 * Computes the Spearman score achieved by a set of experiments
	 * <p>
	 * <b>Expected args:</b>
	 * <ol>
	 * <li>Number of features in the dataset</li>
	 * <li>Paths to ranking files from different experiments
	 * </ol>
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));
		readParams(args);
		System.out.println(computeScore());
	}
}
