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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparator;

/**
 * MultiObjective Statistics. It has been adapted from the original ECJ's
 * MultiObjectiveStatistics.java (Copyright 2010 by Sean Luke and George Mason
 * University under <a href="https://opensource.org/licenses/AFL-3.0">AFL v.
 * 3.0</a>) to print also the individual in the Pareto front stats file.
 *
 * <p>
 * MultiObjectiveStatistics are a SimpleStatistics subclass which overrides the
 * finalStatistics method to output the current Pareto Front in various ways:
 *
 * <ul>
 * <li>
 * <p>
 * Every individual in the Pareto Front is written to the end of the statistics
 * log.
 * <li>
 * <p>
 * A summary of the objective values of the Pareto Front is written to stdout.
 * <li>
 * <p>
 * The objective values of the Pareto Front are written in tabular form to a
 * special Pareto Front file specified with the parameters below. This file can
 * be easily read by gnuplot or Excel etc. to display the Front (if it's 2D or
 * perhaps 3D).
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>front</tt><br>
 * <font size=-1>String (a filename)</font></td>
 * <td valign=top>(The Pareto Front file, if any)</td>
 * </tr>
 * </table>
 *
 * @author Faisal Abidi
 * @author Sean Luke
 * @author Jesús González
 */

public class MultiObjectiveStatistics extends SimpleStatistics {
	private static final long serialVersionUID = 1L;

	/** Parameter for the front file */
	public static final String P_PARETO_FRONT_FILE = "front";

	/** Parameter for the silent front file */
	public static final String P_SILENT_FRONT_FILE = "silent.front";

	/** Silent front */
	public boolean silentFront;

	/** The pareto front log */
	public int frontLog = 0; // stdout by default

	/**
	 * Set up the statistics by reading it from the parameters stored in state,
	 * built off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		silentFront = state.parameters.getBoolean(base.push(P_SILENT), null, false);
		// yes, we're stating it a second time. It's correct logic.
		silentFront = state.parameters.getBoolean(base.push(P_SILENT_FRONT_FILE), null, silentFront);

		File frontFile = state.parameters.getFile(base.push(P_PARETO_FRONT_FILE), null);

		if (silentFront) {
			frontLog = Output.NO_LOGS;
		} else if (frontFile != null) {
			try {
				frontLog = state.output.addLog(frontFile, !compress, compress);
			} catch (IOException i) {
				state.output.fatal("An IOException occurred while trying to create the log " + frontFile + ":\n" + i);
			}
		} else
			state.output.warning("No Pareto Front statistics file specified, printing to stdout at end.",
					base.push(P_PARETO_FRONT_FILE));
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
	@SuppressWarnings("rawtypes")
	public void finalStatistics(final EvolutionState state, final int result) {
		bypassFinalStatistics(state, result); // just call super.super.finalStatistics(...)

		if (doFinal)
			state.output.println("\n\n\n PARETO FRONTS", statisticslog);
		for (int s = 0; s < state.population.subpops.length; s++) {
			MultiObjectiveFitness typicalFitness = (MultiObjectiveFitness) (state.population.subpops[s].individuals[0].fitness);
			if (doFinal)
				state.output.println("\n\nPareto Front of Subpopulation " + s, statisticslog);

			// build front
			@SuppressWarnings("static-access")
			ArrayList front = typicalFitness.partitionIntoParetoFront(state.population.subpops[s].individuals, null,
					null);

			// sort by objective[0]
			Object[] sortedFront = front.toArray();
			QuickSort.qsort(sortedFront, new SortComparator() {
				public boolean lt(Object a, Object b) {
					return (((MultiObjectiveFitness) (((Individual) a).fitness))
							.getObjective(0) < (((MultiObjectiveFitness) ((Individual) b).fitness)).getObjective(0));
				}

				public boolean gt(Object a, Object b) {
					return (((MultiObjectiveFitness) (((Individual) a).fitness))
							.getObjective(0) > ((MultiObjectiveFitness) (((Individual) b).fitness)).getObjective(0));
				}
			});

			// print out front to statistics log
			if (doFinal)
				for (int i = 0; i < sortedFront.length; i++)
					((Individual) (sortedFront[i])).printIndividualForHumans(state, statisticslog);

			// write short version of front out to disk
			if (!silentFront) {
				if (state.population.subpops.length > 1)
					state.output.println("Subpopulation " + s, frontLog);
				for (int i = 0; i < sortedFront.length; i++) {
					Individual ind = (Individual) (sortedFront[i]);
					MultiObjectiveFitness mof = (MultiObjectiveFitness) (ind.fitness);
					double[] objectives = mof.getObjectives();

					String line = "";

					/* Prints the individual as the first column of the Pareto Front */
					line += ind.genotypeToString() + "\t";

					for (int f = 0; f < objectives.length; f++)
						line += (objectives[f] + "\t");
					state.output.println(line, frontLog);
				}
			}
		}
	}
}
