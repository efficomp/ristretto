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
 * VectorCrossoverPipeline.java to grab and produce FSSubsetIndividuals.
 * 
 * The original ECJ's VectorCrossoverPipeline.java was licensed under the
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
 *
 * FSSubsetCrossoverPipeline is a {@link ec.BreedingPipeline} which implements a
 * simple default crossover for a
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual}. Normally it takes two
 * individuals and returns two crossed-over child individuals. Optionally, it
 * can take two individuals, cross them over, but throw away the second child (a
 * one-child crossover). FSSubsetCrossoverPipeline works by calling
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual#defaultCrossover} on
 * the first parent individual. It has been adapted from the original ECJ's
 * VectorCrossoverPipeline.java (Copyright 2006 by Sean Luke under the
 * <a href="https://opensource.org/licenses/AFL-3.0">AFL v. 3.0</a>) to grab and
 * produce FSSubsetIndividuals.
 * 
 * <p>
 * <b>Typical Number of Individuals Produced Per <tt>produce(...)</tt>
 * call</b><br>
 * 2 * minimum typical number of individuals produced by each source, unless
 * tossSecondParent is set, in which case it's simply the minimum typical
 * number.
 * 
 * <p>
 * <b>Number of Sources</b><br>
 * 2
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>toss</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(after crossing over with the first new individual, should its
 * second sibling individual be thrown away instead of adding it to the
 * population?)</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * fs-subset.xover
 * 
 * @author Jesús González
 * @version 1.0
 */
public class FSSubsetCrossoverPipeline extends BreedingPipeline {
	private static final long serialVersionUID = 1L;

	/** Base parameter for defaults */
	public static final String P_FS_SUBSET_CROSSOVER = "xover";

	/** Parameter to configure if the second parent should be tossed */
	public static final String P_TOSS = "toss";

	/** Number of sources */
	public static final int NUM_SOURCES = 2;

	/** Should the pipeline discard the second parent after crossing over? */
	public boolean tossSecondParent;

	/** Temporary holding place for parents */
	FSSubsetIndividual parents[];

	/** Default constructor */
	public FSSubsetCrossoverPipeline() {
		parents = new FSSubsetIndividual[2];
	}

	/**
	 * Return the default parameter base.
	 */
	public Parameter defaultBase() {
		return FSSubsetDefaults.base().push(P_FS_SUBSET_CROSSOVER);
	}

	/** Return the number of sources */
	public int numSources() {
		return NUM_SOURCES;
	}

	/** Clone this pipeline */
	public Object clone() {
		FSSubsetCrossoverPipeline c = (FSSubsetCrossoverPipeline) (super.clone());

		// deep-cloned stuff
		c.parents = (FSSubsetIndividual[]) parents.clone();

		return c;
	}

	/**
	 * Set up the pipeline.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS), def.push(P_TOSS), false);
	}

	/**
	 * Return 2 * minimum number of typical individuals produced by any sources,
	 * else 1 * minimum number if
	 * {@link ristretto.problem.fs.subset.breed.FSSubsetCrossoverPipeline#tossSecondParent}
	 * is true.
	 */
	public int typicalIndsProduced() {
		return (tossSecondParent ? minChildProduction() : minChildProduction() * 2);
	}

	/**
	 * Produce <i>n</i> individuals from the given subpopulation and put them into
	 * inds[start...start+n-1], where n = Min(Max(q,min),max), where <i>q</i> is the
	 * "typical" number of individuals the pipeline produces in one shot, and
	 * returns <i>n</i>. max must be &gt;= min, and min must be &gt;= 1.
	 */
	public int produce(final int min, final int max, final int start, final int subpopulation, final Individual[] inds,
			final EvolutionState state, final int thread)

	{
		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min)
			n = min;
		if (n > max)
			n = max;

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			// DO produce children from source -- we've not done so already
			return reproduce(n, start, subpopulation, inds, state, thread, true);

		for (int q = start; q < n + start; /* no increment */)
		// keep on going until we're filled up
		{
			// grab two individuals from our sources
			if (sources[0] == sources[1]) // grab from the same source
			{
				sources[0].produce(2, 2, 0, subpopulation, parents, state, thread);
				if (!(sources[0] instanceof BreedingPipeline))
				// it's a selection method probably
				{
					parents[0] = (FSSubsetIndividual) (parents[0].clone());
					parents[1] = (FSSubsetIndividual) (parents[1].clone());
				}
			} else // grab from different sources
			{
				sources[0].produce(1, 1, 0, subpopulation, parents, state, thread);
				sources[1].produce(1, 1, 1, subpopulation, parents, state, thread);
				if (!(sources[0] instanceof BreedingPipeline))
					// it's a selection method probably
					parents[0] = (FSSubsetIndividual) (parents[0].clone());
				if (!(sources[1] instanceof BreedingPipeline))
					// it's a selection method probably
					parents[1] = (FSSubsetIndividual) (parents[1].clone());
			}

			// at this point, parents[] contains our two selected individuals,
			// AND they're copied so we own them and can make whatever
			// modifications
			// we like on them.

			// so we'll cross them over now. Since this is the default pipeline,
			// we'll just do it by calling defaultCrossover on the first child

			parents[0].defaultCrossover(state, thread, parents[1]);
			parents[0].evaluated = false;
			parents[1].evaluated = false;

			// add 'em to the population
			inds[q] = parents[0];
			q++;
			if (q < n + start && !tossSecondParent) {
				inds[q] = parents[1];
				q++;
			}
		}
		return n;
	}
}
