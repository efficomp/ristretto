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
 * VectorSpecies.java to keep the individual's selected features in a Java's TreeSet.
 * 
 * The original ECJ's VectorSpecies.java was licensed under the
 * Academic Free License (AFL) version 3.0. You may obtain a copy of the AFL
 * v. 3.0. at <https://opensource.org/licenses/AFL-3.0/>.
 *
 * Copyright (c) 2006, Sean Luke and George Mason University
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.problem.fs.subset;

import ec.EvolutionState;
import ec.Individual;
import ec.Species;
import ec.util.Parameter;

/**
 * FSSubsetSpecies is a species which can create
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual}. It has been adapted
 * from the original ECJ's VectorSpecies.java (Copyright 2006 by Sean Luke and
 * George Mason University under
 * <a href="https://opensource.org/licenses/AFL-3.0">AFL v. 3.0</a>) to keep the
 * individual's selected features in a Java's TreeSet.
 *
 * <p>
 * FSSubsetSpecies also contain a number of parameters guiding how the
 * individual is generated, crossed over and mutated.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>n-features</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the total number of features in the feature selection
 * problem)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min-size</tt><br>
 * <font size=-1>int &gt;= 1, &lt; n-features</font></td>
 * <td valign=top>(the minimum size of the genome)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max-size</tt><br>
 * <font size=-1>int &gt;= min-size, &lt; n-features</font></td>
 * <td valign=top>(the maximum size of the genome)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min-feature</tt><br>
 * <font size=-1>int &gt;= 0, &lt; n-features</font></td>
 * <td valign=top>(the minimum feature index)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max-feature</tt><br>
 * <font size=-1>int &gt;= min-feature, &lt; n-features</font></td>
 * <td valign=top>(the maximum feature index)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>mutation-prob</tt><br>
 * <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 * <td valign=top>(probability that a gene will get mutated over default
 * mutation)</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * fs-subset.species
 * 
 * @author Jesús González
 * @version 1.0
 */
public class FSSubsetSpecies extends Species {
	private static final long serialVersionUID = 1L;

	/** Parameter base */
	public static final String P_FS_SUBSET_SPECIES = "species";

	/** Parameter for the number of features in the problem */
	public final static String P_N_FEATURES = "n-features";

	/** Parameter for the minimum size of individuals */
	public final static String P_MIN_SIZE = "min-size";

	/** Parameter for the maximum size of individuals */
	public final static String P_MAX_SIZE = "max-size";

	/** Parameter for the minimum feature index of individuals */
	public final static String P_MIN_FEATURE = "min-feature";

	/** Parameter for the maximum feature index of individuals */
	public final static String P_MAX_FEATURE = "max-feature";

	/** Parameter for the mutation probability of individuals */
	public final static String P_MUTATIONPROB = "mutation-prob";

	/** Probability that a gene will mutate, per gene. */
	public double mutationProb;

	/** How many features does it have the feature selection problem? */
	public int nFeatures;

	/** What's the smallest legal genome? */
	public int minSize;
	/** What's the largest legal genome? */
	public int maxSize;

	/** What's the smallest feature index to be used? */
	public int minFeature;
	/** What's the largest feature index to be used? */
	public int maxFeature;

	/** Return the mutation probability */
	public double mutationProbability() {
		return mutationProb;
	}

	/**
	 * Return the default parameter base.
	 */
	public Parameter defaultBase() {
		return FSSubsetDefaults.base().push(P_FS_SUBSET_SPECIES);
	}

	/**
	 * Set up the species.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = defaultBase();

		// We will construct, but NOT set up, a sacrificial individual here.

		i_prototype = (Individual) (state.parameters.getInstanceForParameter(base.push(P_INDIVIDUAL),
				def.push(P_INDIVIDUAL), Individual.class));

		// this will get thrown away and replaced with a new one during
		// super.setup(...).

		// read the parameters
		nFeatures = state.parameters.getInt(base.push(P_N_FEATURES), def.push(P_N_FEATURES), 0);
		if (nFeatures <= 0) {
			state.output.fatal("The number of features should be an integer greater than 0", base.push(P_N_FEATURES),
					def.push(P_N_FEATURES));
		}

		minSize = state.parameters.getInt(base.push(P_MIN_SIZE), def.push(P_MIN_SIZE), 0);
		if (minSize < 0 || minSize >= nFeatures) {
			state.output.fatal(
					"The minimum size of the subset of features should be an integer greater or equal than 0 and less or equal than the total number of features",
					base.push(P_MIN_SIZE), def.push(P_MIN_SIZE));
		}

		maxSize = state.parameters.getInt(base.push(P_MAX_SIZE), def.push(P_MAX_SIZE), 0);
		if (maxSize < minSize || maxSize > nFeatures) {
			state.output.fatal(
					"The maximum size of the subset of features should be an integer greater or equal than min-size and less or equal than the total number of features",
					base.push(P_MAX_SIZE), def.push(P_MAX_SIZE));
		}

		minFeature = state.parameters.getInt(base.push(P_MIN_FEATURE), def.push(P_MIN_FEATURE), 0);
		if (minFeature < 0) {
			minFeature = 0; // if no value was provided, the minimum possible value will be used
		} else if (minFeature >= nFeatures) {
			state.output.fatal(
					"The minimum value for the feature indexes should be an integer greater or equal than 0 and less than the total number of features",
					base.push(P_MIN_FEATURE), def.push(P_MIN_FEATURE));
		}

		maxFeature = state.parameters.getInt(base.push(P_MAX_FEATURE), def.push(P_MAX_FEATURE), 0);
		if (maxFeature < 0) {
			maxFeature = nFeatures - 1; // if no value was provided, the maximum possible value will be used
		} else if (maxFeature < minFeature) {
			state.output.fatal(
					"The maximum value for the feature indexes should greater or equal than the minimum value for the feature indexes",
					base.push(P_MAX_FEATURE), def.push(P_MAX_FEATURE));
		} else if (maxFeature >= nFeatures) {
			state.output.fatal(
					"The maximum value for the feature indexes should be an integer less than the total number of features",
					base.push(P_MAX_FEATURE), def.push(P_MAX_FEATURE));
		}

		mutationProb = state.parameters.getDoubleWithMax(base.push(P_MUTATIONPROB), def.push(P_MUTATIONPROB), 0.0, 1.0);
		if (mutationProb == -1.0)
			state.output.fatal("Global mutation probability must be between 0.0 and 1.0 inclusive",
					base.push(P_MUTATIONPROB), def.push(P_MUTATIONPROB));

		// NOW call super.setup(...), which will in turn set up the prototypical
		// individual
		super.setup(state, base);
	}

	/**
	 * Create a new individual belonging to this species
	 * 
	 * @param state  The evolution state
	 * @param thread Execution thread
	 */
	public Individual newIndividual(final EvolutionState state, int thread) {
		FSSubsetIndividual newind = (FSSubsetIndividual) (super.newIndividual(state, thread));
		newind.reset(state, thread);

		return newind;
	}
}
