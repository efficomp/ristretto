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

import java.util.Locale;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

/**
 * Applies a Kolmogorov-Smirnov normality test to a set of values and prints the
 * p-value
 * 
 * @author Jesús González
 */
public class KolmogorovSmirnovNormalityTest {

	private static boolean verbose = false;

	/**
	 * Read the parameters from the command-line
	 *
	 * @param args List of floating point numbers
	 */
	private static double[] readValues(String[] args) {
		double values[] = new double[args.length];
		for (int i = 0; i < args.length; i++) {
			try {
				values[i] = Double.parseDouble(args[i]);
			} catch (NumberFormatException e) {
				System.err.println("Error: " + args[i] + " is not a valid number");
				System.exit(-1);
			}
		}

		return values;
	}

	/**
	 * Applies the test
	 * 
	 * @param values Values for the test
	 * @return the KS p-value
	 */
	private static double applyTest(double[] values) {
		Mean mean = new Mean();
		Variance var = new Variance();
		double m = mean.evaluate(values, 0, values.length);
		double sd = Math.sqrt(var.evaluate(values, 0, values.length));

		if (verbose) {
			System.out.println("Mean: " + m);
			System.out.println("Std: " + sd);
		}

		NormalDistribution normal = new NormalDistribution(m, sd);

		KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
		return ksTest.kolmogorovSmirnovTest(normal, values);
	}

	/**
	 * Applies a Kolmogorov-Smirnov normality test to a set of values and prints the
	 * p-value
	 * 
	 * @param args List of floating point numbers
	 */
	public static void main(String[] args) {
		/*
		 * Sets the locale to English, in order to use dots instead of commas for
		 * decimal numbers
		 */
		Locale.setDefault(new Locale("en", "US"));

		double pValue = applyTest(readValues(args));

		if (verbose)
			System.out.print("p-value: ");

		System.out.println(pValue);
	}
}
