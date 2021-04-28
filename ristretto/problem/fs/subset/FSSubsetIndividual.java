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
 * IntegerVectorIndividual.java to keep the genome in a Javas's TreeSet.
 * 
 * The original ECJ's IntegerVectorIndividual.java was licensed under the
 * Academic Free License (AFL) version 3.0. You may obtain a copy of the AFL
 * v. 3.0. at <https://opensource.org/licenses/AFL-3.0/>.
 *
 * Copyright (c) 2006, Sean Luke
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.problem.fs.subset;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Code;
import ec.util.DecodeReturn;
import ec.util.Parameter;

/**
 * FSSubsetIndividual is an individual whose genome is a subset of selected
 * features for a feature selection problem. As features are referenced by
 * indexes, genomes will be subsets of integer indexes (starting from 0). It has
 * been adapted from the original ECJ's IntegerVectorIndividual.java (Copyright
 * 2006 by Sean Luke under the
 * <a href="https://opensource.org/licenses/AFL-3.0">AFL v. 3.0</a>) to keep the
 * genome in a Javas's TreeSet.
 *
 * <p>
 * This class contains two methods,
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual#defaultCrossover} and
 * {@link ristretto.problem.fs.subset.FSSubsetIndividual#defaultMutate}, which
 * can be overridden if all you need is a simple crossover and a simple mutate
 * mechanism. The
 * {@link ristretto.problem.fs.subset.breed.FSSubsetCrossoverPipeline} and
 * {@link ristretto.problem.fs.subset.breed.FSSubsetMutationPipeline} classes
 * use these methods to do their handwork. For more sophisticated crossover and
 * mutation, you'll need to write a custom breeding pipeline.
 *
 * <p>
 * FSSubsetIndividuals must belong to the species
 * {@link ristretto.problem.fs.subset.FSSubsetSpecies} (or any subclass of it).
 *
 * <p>
 * <b>From ec.Individual:</b>
 *
 * <p>
 * In addition to serialization for checkpointing, Individuals may read and
 * write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This
 * method transmits or receives an individual in binary. It is the most
 * efficient approach to sending individuals over networks, etc. These methods
 * write the evaluated flag and the fitness, then call
 * <b>readGenotype/writeGenotype</b>, which you must implement to write those
 * parts of your Individual special to your functions-- the default versions of
 * readGenotype/writeGenotype throw errors. You don't need to implement them if
 * you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an individual in text encoded such that the
 * individual is largely readable by humans but can be read back in 100% by ECJ
 * as well. To do this, these methods will encode numbers using the
 * <tt>ec.util.Code</tt> class. These methods are mostly used to write out
 * populations to files for inspection, slight modification, then reading back
 * in later on. <b>readIndividual</b> reads in the fitness and the evaluation
 * flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there
 * to help you. <b>printIndividual</b> writes out the fitness and evaluation
 * flag, then calls <b>genotypeToString</b> and printlns the resultant string.
 * You are responsible for implementing the genotypeToString method in such a
 * way that parseGenotype can read back in the individual println'd with
 * genotypeToString. The default form of genotypeToString simply calls
 * <b>toString</b>, which you may override instead if you like. The default form
 * of <b>parseGenotype</b> throws an error. You are not required to implement
 * these methods, but without them you will not be able to write individuals to
 * files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption
 * only. <b>printIndividualForHumans</b> writes out the fitness and evaluation
 * flag, then calls <b>genotypeToStringForHumans</b> and printlns the resultant
 * string. You are responsible for implementing the genotypeToStringForHumans
 * method. The default form of genotypeToStringForHumans simply calls
 * <b>toString</b>, which you may override instead if you like (though note that
 * genotypeToString's default also calls toString). You should handle one of
 * these methods properly to ensure individuals can be printed by ECJ.
 * </ul>
 * 
 * <p>
 * In general, the various readers and writers do three things: they tell the
 * Fitness to read/write itself, they read/write the evaluated flag, and they
 * read/write the gene subset. If you add instance variables to a
 * FSSubsetIndividual or subclass, you'll need to read/write those variables as
 * well.
 * 
 * @author Jesús González
 * @version 1.0
 */

public class FSSubsetIndividual extends Individual {
	private static final long serialVersionUID = 1L;

	/** Base parameter */
	public static final String P_FS_SUBSET_INDIVIDUAL = "ind";

	/** Subset of selected feature indices */
	public TreeSet<Integer> genome;

	/**
	 * Return the default parameter base.
	 */
	public Parameter defaultBase() {
		return FSSubsetDefaults.base().push(P_FS_SUBSET_INDIVIDUAL);
	}

	/**
	 * Returns a clone of this individual
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		FSSubsetIndividual myobj = (FSSubsetIndividual) (super.clone());

		// must clone the genome
		myobj.genome = ((TreeSet<Integer>) (genome.clone()));

		return myobj;
	}

	/**
	 * Set up this individual.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base); // actually unnecessary (Individual.setup() is
									// empty)

		Parameter def = defaultBase();

		if (!(species instanceof FSSubsetSpecies))
			state.output.fatal("FSSubsetIndividual requires an FSSubsetSpecies", base, def);
		genome = new TreeSet<Integer>();
	}

	/**
	 * Destructively crosses over the individual with another in some default
	 * manner. The default form preserves the common selected features in both
	 * individuals and exchanges a random number of feature indexes.
	 * 
	 * @param state  The evolution state
	 * @param thread Execution thread
	 * @param other  The other individual
	 */
	@SuppressWarnings("unchecked")
	public void defaultCrossover(EvolutionState state, int thread, FSSubsetIndividual other) {

		// Detect repeated features in both individuals, as they should be
		// repeated in their offspring
		TreeSet<Integer> repeated = new TreeSet<Integer>();
		ArrayList<Integer> nonRepeatedThis = new ArrayList<Integer>();
		Iterator<Integer> it = this.genome.iterator();
		while (it.hasNext()) {
			int feature = it.next();
			if (other.genome.contains(feature))
				repeated.add(feature);
			else
				nonRepeatedThis.add(feature);
		}

		ArrayList<Integer> nonRepeatedInd = new ArrayList<Integer>();
		it = other.genome.iterator();
		while (it.hasNext()) {
			int feature = it.next();
			if (!repeated.contains(feature))
				nonRepeatedInd.add(feature);
		}

		// The repeated features should be in both parents again
		this.genome = (TreeSet<Integer>) repeated.clone();
		other.genome = (TreeSet<Integer>) repeated.clone();

		// shuffle the non repeated features
		Collections.shuffle(nonRepeatedThis);
		Collections.shuffle(nonRepeatedInd);

		// Cross the non-repeated features randomly
		int minSize = Math.min(nonRepeatedThis.size(), nonRepeatedInd.size());
		for (int i = 0; i < minSize; i++) {
			if (state.random[thread].nextBoolean()) {
				this.genome.add(nonRepeatedInd.get(i));
				other.genome.add(nonRepeatedThis.get(i));
			} else {
				this.genome.add(nonRepeatedThis.get(i));
				other.genome.add(nonRepeatedInd.get(i));
			}
		}

		// add the remaining features
		for (int i = minSize; i < nonRepeatedThis.size(); i++)
			this.genome.add(nonRepeatedThis.get(i));
		for (int i = minSize; i < nonRepeatedInd.size(); i++)
			other.genome.add(nonRepeatedInd.get(i));
	}

	/**
	 * Destructively mutates the individual in some default manner.
	 * 
	 * @param state  The evolution state
	 * @param thread Execution thread
	 */
	public void defaultMutate(EvolutionState state, int thread) {
		FSSubsetSpecies s = (FSSubsetSpecies) species;

		TreeSet<Integer> toBeMutated = new TreeSet<Integer>();
		TreeSet<Integer> toBeRemoved = new TreeSet<Integer>();
		int prevGenomeSize = this.genome.size();
		int featureLimit = s.maxFeature - s.minFeature + 1;

		Iterator<Integer> it = this.genome.iterator();
		while (it.hasNext()) {
			int feature = it.next();
			// test if this gene should be altered
			if (state.random[thread].nextBoolean(s.mutationProbability())) {
				// if the genome contains all the features is impossible to mutate any of them
				// thus, the feature must be removed
				if (prevGenomeSize + toBeMutated.size() < featureLimit) {
					if (state.random[thread].nextBoolean(0.5))
						toBeRemoved.add(feature);
					else
						toBeMutated.add(feature);
				} else {
					toBeRemoved.add(feature);
				}
			}
		}

		this.genome.removeAll(toBeRemoved);
		this.genome.removeAll(toBeMutated);

		int newGenomeSize = this.genome.size() + toBeMutated.size();

		// test if a new feature should be added, without increasing the maximum
		// length
		if (prevGenomeSize + toBeMutated.size() + toBeRemoved.size() < featureLimit && newGenomeSize < s.maxSize
				&& state.random[thread].nextBoolean(s.mutationProbability()))
			newGenomeSize++;

		// Assure the minimum length
		if (newGenomeSize < s.minSize)
			newGenomeSize = s.minSize;

		// If too many features are to be removed or mutated, it could be
		// possible that there were not enough available features for the new
		// genome. If this were the case, some features should remain in the
		// genome
		while (featureLimit - toBeMutated.size() - toBeRemoved.size() < s.minSize) {
			if (state.random[thread].nextBoolean(0.5)) {
				if (toBeRemoved.size() > 0)
					toBeRemoved.remove(toBeRemoved.first());
				else if (toBeMutated.size() > 0)
					toBeMutated.remove(toBeMutated.first());
			} else {
				if (toBeMutated.size() > 0)
					toBeMutated.remove(toBeMutated.first());
				else if (toBeRemoved.size() > 0)
					toBeRemoved.remove(toBeRemoved.first());
			}
		}

		while (this.genome.size() < newGenomeSize) {
			int newFeature = s.minFeature + state.random[thread].nextInt(featureLimit);

			while (toBeMutated.contains(newFeature) || toBeRemoved.contains(newFeature)
					|| !this.genome.add(newFeature)) {
				newFeature = s.minFeature + state.random[thread].nextInt(featureLimit);
			}
		}
	}

	/**
	 * Initializes the individual by randomly choosing features.
	 * 
	 * @param state  The evolution state
	 * @param thread Execution thread
	 */
	public void reset(EvolutionState state, int thread) {
		FSSubsetSpecies s = (FSSubsetSpecies) species;
		int genomeSize = s.minSize + state.random[thread].nextInt((s.maxSize - s.minSize + 1));
		while (genome.size() < genomeSize) {
			int featureLimit = s.maxFeature - s.minFeature + 1;
			int newFeature = s.minFeature + state.random[thread].nextInt(featureLimit);
			this.genome.add(newFeature);
		}
	}

	/**
	 * Return the hascode of this individual
	 */
	public int hashCode() {
		int hash = this.getClass().hashCode();

		hash = (hash << 1 | hash >>> 31);

		Iterator<Integer> it = this.genome.iterator();
		while (it.hasNext())
			hash = (hash << 1 | hash >>> 31) ^ it.next();

		return hash;
	}

	/**
	 * Return the gnome in a human-readable format
	 */
	public String genotypeToStringForHumans() {
		StringBuilder s = new StringBuilder();

		Iterator<Integer> it = this.genome.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (!first)
				s.append(" ");
			s.append(it.next());
			first = false;
		}

		return s.toString();
	}

	/**
	 * Convert the gnome into a string
	 */
	public String genotypeToString() {
		StringBuilder s = new StringBuilder();
		s.append(Code.encode(genome.size()));

		Iterator<Integer> it = this.genome.iterator();
		while (it.hasNext())
			s.append(Code.encode(it.next()));

		return s.toString();
	}

	/**
	 * Parse a genome from a numbered reader.
	 * 
	 * @param state  The evolution state
	 * @param reader The reader
	 * @throws IOException if the genome can not be parsed
	 */
	protected void parseGenotype(final EvolutionState state, final LineNumberReader reader) throws IOException {
		// read in the next line. The first item is the number of genes
		String s = reader.readLine();
		DecodeReturn d = new DecodeReturn(s);
		Code.decode(d);

		// of course, even if it *is* an integer, we can't tell if it's a gene
		// or a genome count, argh...
		if (d.type != DecodeReturn.T_INTEGER) // uh oh
			state.output.fatal("Individual with genome:\n" + s
					+ "\n... does not have an integer at the beginning indicating the genome count.");
		int lll = (int) (d.l);

		genome = new TreeSet<Integer>();

		// read in the genes
		for (int i = 0; i < lll; i++) {
			Code.decode(d);
			genome.add((int) d.l);
		}
	}

	/**
	 * Return true if this individual equals the other
	 * 
	 * @param other The other individual
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;

		if (!(this.getClass().equals(other.getClass())))
			return false;

		FSSubsetIndividual i = (FSSubsetIndividual) other;
		if (genome.size() != i.genome.size())
			return false;

		Iterator<Integer> it = this.genome.iterator();
		Iterator<Integer> it2 = i.genome.iterator();
		while (it.hasNext())
			if (it.next() != it2.next())
				return false;

		return true;
	}

	/**
	 * Return the genome.
	 */
	public Object getGenome() {
		return genome;
	}

	/**
	 * Set the genome.
	 * 
	 * @param gen The new genoms
	 */
	@SuppressWarnings("unchecked")
	public void setGenome(Object gen) {
		genome = (TreeSet<Integer>) gen;
	}

	/**
	 * Return the length of the genome.
	 */
	public int genomeLength() {
		return genome.size();
	}

	/**
	 * Write the genome to a binary stream.
	 * 
	 * @param state      The evolution state
	 * @param dataOutput The stream writer
	 * @throws IOException If the genome can not be written
	 */
	public void writeGenotype(final EvolutionState state, final DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(genome.size());
		Iterator<Integer> it = this.genome.iterator();
		while (it.hasNext())
			dataOutput.writeInt(it.next());
	}

	/**
	 * Read the genome from a binary stream.
	 * 
	 * @param state     The evolution state
	 * @param dataInput The stream reader
	 * @throws IOException If the genome can not be read
	 */
	public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException {
		int len = dataInput.readInt();
		genome = new TreeSet<Integer>();
		for (int i = 0; i < len; i++)
			genome.add(dataInput.readInt());
	}

	/**
	 * Return the number of selected features
	 */
	public long size() {
		return genomeLength();
	}

}
