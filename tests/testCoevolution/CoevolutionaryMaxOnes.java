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
package tests.testCoevolution;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.BitVectorIndividual;

/**
 * Dummy test for coevolutionary problems. Maximize the number of ones in a
 * bitvector in a coevolutionary way
 * 
 * @author Jesús González
 */
public class CoevolutionaryMaxOnes extends Problem implements GroupedProblemForm {
	private static final long serialVersionUID = 1L;

	/** Parameter to decide whether to include the context or not */
	public static final String P_SHOULD_SET_CONTEXT = "set-context";

	/** Boolean variable to decide whether to include the context or not */
	boolean shouldSetContext;

	/** Optimal fitness */
	int optimalFitness;

	/**
	 * Sets up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		// load whether we should set context or not
		shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT), null, true);
	}

	/**
	 * Prepare the fitness of individuals belonging to a population (clear trials)
	 * before their evaluation
	 * 
	 * @param state                The state of the evolutionary process
	 * 
	 * @param pop                  The population
	 * @param prepareForAssessment Only clear the trials for Individuals in
	 *                             Subpopulations for which prepareForAssessment is
	 *                             true
	 * @param countVictoriesOnly   Can be neglected in cooperative coevolution
	 * 
	 */
	public void preprocessPopulation(final EvolutionState state, Population pop, boolean[] prepareForAssessment,
			boolean countVictoriesOnly) {
		optimalFitness = 0;
		for (int i = 0; i < pop.subpops.length; i++) {
			optimalFitness += pop.subpops[i].individuals.length;
			if (prepareForAssessment[i])
				for (int j = 0; j < pop.subpops[i].individuals.length; j++)
					((SimpleFitness) (pop.subpops[i].individuals[j].fitness)).trials = new ArrayList();
		}
	}

	/**
	 * Called after evaluation of a Population to form final Fitness scores for the
	 * individuals based on the various performance scores they accumulated during
	 * trials
	 * 
	 * @param state              The state of the evolutionary process
	 * 
	 * @param pop                The population
	 * @param assessFitness      Only assess the Fitness and set the evaluated flags
	 *                           for Individuals in Subpopulations for which
	 *                           assessFitness is true
	 * @param countVictoriesOnly Can be neglected in cooperative coevolution
	 * 
	 */
	public void postprocessPopulation(final EvolutionState state, Population pop, boolean[] assessFitness,
			boolean countVictoriesOnly) {
		for (int i = 0; i < pop.subpops.length; i++)
			if (assessFitness[i])
				for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
					SimpleFitness fit = ((SimpleFitness) (pop.subpops[i].individuals[j].fitness));

					// we take the max over the trials
					int max = Integer.MIN_VALUE;
					int len = fit.trials.size();
					for (int l = 0; l < len; l++)
						max = Math.max(((Integer) (fit.trials.get(l))).intValue(), max); // it'll
																							// be
																							// the
																							// first
																							// one,
																							// but
																							// whatever

					fit.setFitness(state, max, isOptimal(max));
					pop.subpops[i].individuals[j].evaluated = true;
				}
	}

	/**
	 * Return true if the fitness is optimal
	 * 
	 * @param fitness The fitness
	 */
	public boolean isOptimal(int fitness) {
		if (fitness == optimalFitness)
			return true;

		return false;
	}

	/**
	 * Evaluates the individual (in not already evaluated)
	 * 
	 * @param state              The state of the evolutionary process
	 * @param inds               The individuals to evaluate together
	 * @param updateFitness      Should this individuals' fitness be updated?
	 * @param countVictoriesOnly Can be neglected in cooperative coevolution
	 * @param subpops            Subpopulations
	 * @param threadnum          The thread of execution
	 */
	public void evaluate(final EvolutionState state, final Individual[] inds, // the
																				// individuals
																				// to
																				// evaluate
																				// together
			final boolean[] updateFitness, // should this individuals' fitness
											// be updated?
			final boolean countVictoriesOnly, // can be neglected in cooperative
												// coevolution
			int[] subpops, final int threadnum) {
		if (inds.length == 0)
			state.output.fatal("Number of individuals provided to CoevolutionaryECSuite is 0!");
		if (inds.length == 1)
			state.output
					.warnOnce("Coevolution used, but number of individuals provided to CoevolutionaryECSuite is 1.");

		int size = 0;
		for (int i = 0; i < inds.length; i++)
			if (!(inds[i] instanceof BitVectorIndividual))
				state.output.error("Individual " + i + "in coevolution is not a BitVectorIndividual.");
			else {
				BitVectorIndividual coind = (BitVectorIndividual) (inds[i]);
				size += coind.genome.length;
			}
		state.output.exitIfErrors();

		// concatenate all the arrays
		boolean[] vals = new boolean[size];
		int pos = 0;
		for (int i = 0; i < inds.length; i++) {
			BitVectorIndividual coind = (BitVectorIndividual) (inds[i]);
			System.arraycopy(coind.genome, 0, vals, pos, coind.genome.length);
			pos += coind.genome.length;
		}

		// Count the number of ones
		int trial = 0;
		for (int i = 0; i < vals.length; i++)
			if (vals[i])
				trial++;

		// update individuals to reflect the trial
		for (int i = 0; i < inds.length; i++) {
			BitVectorIndividual coind = (BitVectorIndividual) (inds[i]);
			if (updateFitness[i]) {
				// Update the context if this is the best trial. We're going to
				// assume that the best
				// trial is trial #0 so we don't have to search through them.
				int len = coind.fitness.trials.size();

				if (len == 0) // easy
				{
					if (shouldSetContext)
						coind.fitness.setContext(inds, i);
					coind.fitness.trials.add(new Integer(trial));
				} else if (((Integer) (coind.fitness.trials.get(0))).intValue() < trial) // best
																							// trial
																							// is
																							// presently
																							// #0
				{
					if (shouldSetContext)
						coind.fitness.setContext(inds, i);
					// put me at position 0
					Integer t = (Integer) (coind.fitness.trials.get(0));
					coind.fitness.trials.set(0, new Integer(trial)); // put me at
																		// 0
					coind.fitness.trials.add(t); // move him to the end
				}

				// finally set the fitness for good measure
				((SimpleFitness) (coind.fitness)).setFitness(state, trial, false);
			}
		}
	}
}
