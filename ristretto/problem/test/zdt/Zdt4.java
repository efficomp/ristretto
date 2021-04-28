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

package ristretto.problem.test.zdt;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.DoubleVectorIndividual;

/**
 * Implementation of the zdt4 benchmark [1].
 * 
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td>E. Zitzler, K. Deb, and L. Thiele. Comparison of Multiobjective
 * Evolutionary Algorithms: Empirical Results, Evolutionary Computation,
 * 8(2):173-195, 2000. <a href=
 * "https://doi.org/10.1162/106365600568202">https://doi.org/10.1162/106365600568202</a>
 * </td>
 * </tr>
 * </table>
 *
 * @author Jesús González
 */
public class Zdt4 extends Problem implements SimpleProblemForm {
	private static final long serialVersionUID = 1L;

	private static final double FOUR_PI = Math.PI * 4;// ZDT4 uses it.

	/**
	 * Evaluate the individual (if not already evaluated)
	 * 
	 * @param state         The state of the evolutionary process
	 * @param ind           Individual to be evaluated
	 * @param subpopulation The subpopulation to which the individual belongs
	 * @param threadnum     The thread of execution
	 */
	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {
		if (!(ind instanceof DoubleVectorIndividual))
			state.output.fatal("The individuals for this problem should be DoubleVectorIndividuals.");

		DoubleVectorIndividual temp = (DoubleVectorIndividual) ind;
		double[] genome = temp.genome;
		int numDecisionVars = genome.length;

		double[] objectives = ((MultiObjectiveFitness) ind.fitness).getObjectives();

		double f, g, h, sum;

		f = genome[0];
		objectives[0] = f;
		sum = 0;
		for (int i = 1; i < numDecisionVars; ++i)
			sum += genome[i] * genome[i] - 10 * Math.cos(FOUR_PI * genome[i]);

		g = 1 + 10 * (numDecisionVars - 1.0) + sum;
		h = 1 - Math.sqrt(f / g);
		objectives[1] = (g * h);
		((MultiObjectiveFitness) ind.fitness).setObjectives(state, objectives);
		ind.evaluated = true;
	}
}
