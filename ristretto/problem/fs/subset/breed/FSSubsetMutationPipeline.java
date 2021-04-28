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
 * VectorMutationPipeline.java to grab and produce FSSubsetIndividuals.
 * 
 * The original ECJ's VectorMutationPipeline.java was licensed under the
 * Academic Free License (AFL) version 3.0. You may obtain a copy of the AFL
 * v. 3.0. at <https://opensource.org/licenses/AFL-3.0/>.
 *
 * Copyright (c) 2006, Sean Luke
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.problem.fs.subset.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ristretto.problem.fs.subset.FSSubsetDefaults;
import ristretto.problem.fs.subset.FSSubsetIndividual;

/**
 * FSSubsetMutationPipeline is a {@link ec.BreedingPipeline} which implements a
 * simple default Mutation for
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual}. Normally it takes an
 * individual and returns a mutated child individual. FSSubsetMutationPipeline
 * works by calling
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual#defaultMutate} on the
 * parent individual. It has been adapted from the original ECJ's
 * VectorMutationPipeline.java (Copyright 2006 by Sean Luke under the
 * <a href="https://opensource.org/licenses/AFL-3.0">AFL v. 3.0</a>) to grab and
 * produce FSSubsetIndividuals.
 * 
 * <p>
 * <b>Typical Number of Individuals Produced Per <tt>produce(...)</tt>
 * call</b><br>
 * (however many its source produces)
 * 
 * <p>
 * <b>Number of Sources</b><br>
 * 1
 * 
 * <p>
 * <b>Default Base</b><br>
 * fs-subset.mutate (not that it matters)
 * 
 * @author Jesús González
 * @version 1.0
 */
public class FSSubsetMutationPipeline extends BreedingPipeline {
	private static final long serialVersionUID = 1L;

	/** Base parameter for defaults */
	public static final String P_FS_SUBSET_MUTATION = "mutate";

	/** Number of sources */
	public static final int NUM_SOURCES = 1;

	/**
	 * Return the default parameter base.
	 */
	public Parameter defaultBase() {
		return FSSubsetDefaults.base().push(P_FS_SUBSET_MUTATION);
	}

	/** Return the number of sources */
	public int numSources() {
		return NUM_SOURCES;
	}

	/**
	 * Produce <i>n</i> individuals from the given subpopulation and put them into
	 * inds[start...start+n-1], where n = Min(Max(q,min),max), where <i>q</i> is the
	 * "typical" number of individuals the pipeline produces in one shot, and
	 * returns <i>n</i>. max must be &gt;= min, and min must be &gt;= 1.
	 */
	public int produce(final int min, final int max, final int start, final int subpopulation, final Individual[] inds,
			final EvolutionState state, final int thread) {
		// grab individuals from our source and stick 'em right into inds.
		// we'll modify them from there
		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			// DON'T produce children from source -- we already did
			return reproduce(n, start, subpopulation, inds, state, thread, false);

		// clone the individuals if necessary
		if (!(sources[0] instanceof BreedingPipeline))
			for (int q = start; q < n + start; q++)
				inds[q] = (Individual) (inds[q].clone());

		// mutate 'em
		for (int q = start; q < n + start; q++) {
			((FSSubsetIndividual) inds[q]).defaultMutate(state, thread);
			((FSSubsetIndividual) inds[q]).evaluated = false;
		}

		return n;
	}
}
