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
 * This file has been modified by Jesús González from the original ECJ's
 * MultiObjectiveStatistics.java to print also the individual in the Pareto front
 * stats file.
 * 
 * The original ECJ's MultiObjectiveStatistics.java was licensed under the
 * Academic Free License (AFL) version 3.0. You may obtain a copy of the AFL
 * v. 3.0. at <https://opensource.org/licenses/AFL-3.0/>.
 *
 * Copyright (c) 2010, Sean Luke and George Mason University
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.ecjtools;

import java.util.TreeSet;

import ec.EvolutionState;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ristretto.problem.fs.subset.FSSubsetIndividual;

/**
 * Coevolutionary statistics. It has been adapted from the original ECJ's
 * MultiObjectiveStatistics.java (Copyright 2010 by Sean Luke and George Mason
 * University under <a href="https://opensource.org/licenses/AFL-3.0">AFL v.
 * 3.0</a>) to print also the individual in the Pareto front stats file.
 * 
 * @author Faisal Abidi
 * @author Sean Luke
 * @author Jesús González
 */

public class CoevolutionaryStatistics extends SimpleStatistics {

	private static final long serialVersionUID = 1L;

	/**
	 * Set up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
	}

	/**
	 * Log the best individual of the run. Called immediately after the run has
	 * completed.
	 * 
	 * @param state  The evolution state
	 * @param result It is either state.R_FAILURE, indicating that an ideal
	 *               individual was not found, or state.R_SUCCESS, indicating that
	 *               an ideal individual was found.
	 */
	public void finalStatistics(final EvolutionState state, final int result) {
		super.finalStatistics(state, result);

		if (doFinal) {
			state.output.println("\nBest Configuration of Run:", statisticslog);

			// Print the best SVM parameters found so far ...
			double svmParams[] = ((DoubleVectorIndividual) best_of_run[0]).genome;
			state.output.println("SVM.C     " + svmParams[0], statisticslog);
			state.output.println("SVM.gamma " + svmParams[1], statisticslog);

			TreeSet<Integer> combinedFeatures = new TreeSet<Integer>();

			// Print the selected features ...
			for (int i = 1; i < best_of_run.length; i++) {
				if (best_of_run[i] != null && best_of_run[i] instanceof FSSubsetIndividual) {
					FSSubsetIndividual ind = (FSSubsetIndividual) (best_of_run[i]);
					combinedFeatures.addAll(ind.genome);
				}
			}
			state.output.print("Features ", statisticslog);
			for (int i : combinedFeatures)
				state.output.print(" " + i, statisticslog);
			state.output.println("", statisticslog);
		}
	}
}
