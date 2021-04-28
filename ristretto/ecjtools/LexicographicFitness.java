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
 * MultiObjectiveFitness.java to handle the multiple objectives
 * lexicographically.
 * 
 * The original ECJ's MultiObjectiveFitness.java was licensed under the
 * Academic Free License (AFL) version 3.0. You may obtain a copy of the AFL
 * v. 3.0. at <https://opensource.org/licenses/AFL-3.0/>.
 *
 * Copyright (c) 2006, Sean Luke
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.ecjtools;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Code;
import ec.util.DecodeReturn;
import ec.util.Parameter;

/**
 * Lexicographic Fitness is a subclass of Fitness which implements a fitness
 * estimation composed of several objectives that have to be optimized. The key
 * difference with a typical multi-objective fitness is that in this case the
 * objectives are prioritized, avoiding the Pareto dominance handling. So, two
 * fitnesses can be compared lexicographically.
 * 
 * <p>
 * 
 * Since it is quite unusual to obtain identical double values when comparing a
 * couple of objectives of two lexicographic fitnesses, a similarity threshold
 * is used to determine if the two values are similar.
 * 
 * <p>
 * 
 * The object contains two items: an array of floating point values representing
 * the various multiple fitnesses, and a flag (maximize) indicating whether
 * higher is considered better. By default, isIdealFitness() always returns
 * false.
 * 
 * <p>
 * 
 * The object also contains maximum and minimum fitness values suggested for the
 * problem, on a per-objective basis. By default the maximum values are all 1.0
 * and the minimum values are all 0.0, but you can change these. Note that
 * maximum does not mean "best" unless maximize is true.
 *
 * <p>
 * 
 * The fitness() method returns the value of the first objective, since this is
 * the objective with highest priority.
 * 
 * <p>
 *
 * This class has been adapted from the original ECJ's
 * MultiObjectiveFitness.java (Copyright 2006 by Sean Luke under <a
 * href="https://opensource.org/licenses/AFL-3.0">AFL v. 3.0</a>) to handle the
 * multiple objectives lexicographically.
 *
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>threshold</tt><br>
 * (else)<tt>lexicographic.threshold</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the similarity threshold)</td>
 * </tr>
 * <tr>
 * <td valign=top><i>base</i>.<tt>num-objectives</tt><br>
 * (else)<tt>lexicographic.num-objectives</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the number of fitnesses in the objectives array)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>maximize</tt><br>
 * <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 * <td valign=top>(are higher values considered "better"?)
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>maximize</tt>.<i>i</i><br>
 * <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 * <td valign=top>(are higher values considered "better"?). Overrides the
 * all-objective maximization setting.
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max</tt><br>
 * <font size=-1> double (<tt>1.0</tt> default)</font></td>
 * <td valign=top>(maximum fitness value for all objectives) </r>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max</tt>.<i>i</i><br>
 * <font size=-1> double (<tt>1.0</tt> default)</font></td>
 * <td valign=top>(maximum fitness value for objective <i>i</i>. Overrides the
 * all-objective maximum fitness.)
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min</tt><br>
 * <font size=-1> double (<tt>0.0</tt> (default)</font></td>
 * <td valign=top>(minimum fitness value for all objectives)
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min</tt>.<i>i</i><br>
 * <font size=-1> double = <tt>0.0</tt> (default)</font></td>
 * <td valign=top>(minimum fitness value for objective <i>i</i>. Overrides the
 * all-objective minimum fitness.)
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * lexicographic.fitness
 * 
 * @author Sean Luke
 * @author Jesús González
 */

public class LexicographicFitness extends Fitness {
	private static final long serialVersionUID = 1L;

	/** Base parameter for defaults */
	public static final String P_LEXICOGRAPHIC = "lexicographic";

	/** Parameter for the number of objectives */
	public static final String P_NUMOBJECTIVES = "num-objectives";

	/** Parameter for the maximum fitness values */
	public static final String P_MAXOBJECTIVES = "max";

	/** Parameter for the minimum fitness values */
	public static final String P_MINOBJECTIVES = "min";

	/** Is higher better? */
	public static final String P_MAXIMIZE = "maximize";

	/** Basic preamble for printing Fitness values out */
	public static final String LEXICOGRAPHIC_PREAMBLE = "[";

	/** Basic postamble for printing Fitness values out */
	public static final String FITNESS_POSTAMBLE = "]";

	/** Desired maximum fitness values. By default these are 1.0. Shared. */
	public double[] maxObjective;

	/** Desired minimum fitness values. By default these are 0.0. Shared. */
	public double[] minObjective;

	/** Maximization. Shared. */
	public boolean[] maximize;

	/** The various objective values. */
	protected double[] objectives; // values range from 0 (worst) to 1 INCLUSIVE

	/** Similarity threshold for fitness comparisons */
	public static final String P_THRESHOLD = "threshold";

	/** Default value for the similarity threshold */
	public static final double defaultThreshold = 0.0;

	/** Similarity threshold to compare two objectives */
	private double threshold;

	/**
	 * Return true if the given objective is being maximized.
	 * 
	 * @param objective An objective index
	 */
	public boolean isMaximizing(int objective) {
		return maximize[objective];
	}

	/** Return the number of objectives. */
	public int getNumObjectives() {
		return objectives.length;
	}

	/**
	 * Return the objectives as an array. Note that this is the *actual array*.
	 * Though you could set values in this array, you should NOT do this -- rather,
	 * set them using setObjectives().
	 */
	public double[] getObjectives() {
		return objectives;
	}

	/**
	 * Get an objective value.
	 * 
	 * @param i The objective index
	 * @return Its value
	 */
	public double getObjective(int i) {
		return objectives[i];
	}

	/**
	 * Set new values for the objectives
	 * 
	 * @param state     The evolution state
	 * @param newValues The new values for the objectives
	 */
	public void setObjectives(final EvolutionState state, double[] newValues) {
		if (newValues == null) {
			state.output.fatal("Null objective array provided to PrioritizedCompoundFitness.");
		}
		if (newValues.length != objectives.length) {
			state.output.fatal("New objective array length does not match current length.");
		}
		for (int i = 0; i < newValues.length; i++) {
			double _f = newValues[i];
			if (_f >= Double.POSITIVE_INFINITY || _f <= Double.NEGATIVE_INFINITY || Double.isNaN(_f)) {
				state.output
						.warning("Bad objective #" + i + ": " + _f + ", setting to worst value for that objective.");
				if (maximize[i])
					objectives[i] = minObjective[i];
				else
					objectives[i] = maxObjective[i];
			} else {
				objectives[i] = newValues[i];
			}
		}
	}

	/**
	 * Return the default parameter base.
	 */
	public Parameter defaultBase() {
		return new Parameter(P_LEXICOGRAPHIC).push(P_FITNESS);
	}

	/**
	 * Clone this fitness.
	 */
	public Object clone() {
		LexicographicFitness f = (LexicographicFitness) (super.clone());
		f.objectives = (double[]) (objectives.clone()); // cloning an array

		// note that we do NOT clone max and min fitness, or maximizing -- they're
		// shared
		return f;
	}

	/**
	 * Return the value of the first objective, since this is the objective with
	 * highest priority.
	 */
	public double fitness() {
		return objectives[0];
	}

	/**
	 * Set up. This must be called at least once in the prototype before
	 * instantiating any fitnesses that will actually be used in evolution.
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base); // unnecessary really

		Parameter def = defaultBase();
		int numFitnesses;

		double th;
		try {
			th = state.parameters.getDouble(base.push(P_THRESHOLD), def.push(P_THRESHOLD));
		} catch (NumberFormatException e) {
			th = defaultThreshold;
		}
		setThreshold(th);

		numFitnesses = state.parameters.getInt(base.push(P_NUMOBJECTIVES), def.push(P_NUMOBJECTIVES), 0);
		if (numFitnesses <= 0)
			state.output.fatal("The number of objectives must be an integer >= 1.", base.push(P_NUMOBJECTIVES),
					def.push(P_NUMOBJECTIVES));

		objectives = new double[numFitnesses];
		maxObjective = new double[numFitnesses];
		minObjective = new double[numFitnesses];
		maximize = new boolean[numFitnesses];

		for (int i = 0; i < numFitnesses; i++) {
			// load default globals
			minObjective[i] = state.parameters.getDoubleWithDefault(base.push(P_MINOBJECTIVES),
					def.push(P_MINOBJECTIVES), 0.0);
			maxObjective[i] = state.parameters.getDoubleWithDefault(base.push(P_MAXOBJECTIVES),
					def.push(P_MAXOBJECTIVES), 1.0);
			maximize[i] = state.parameters.getBoolean(base.push(P_MAXIMIZE), def.push(P_MAXIMIZE), true);

			// load specifics if any
			minObjective[i] = state.parameters.getDoubleWithDefault(base.push(P_MINOBJECTIVES).push("" + i),
					def.push(P_MINOBJECTIVES).push("" + i), minObjective[i]);
			maxObjective[i] = state.parameters.getDoubleWithDefault(base.push(P_MAXOBJECTIVES).push("" + i),
					def.push(P_MAXOBJECTIVES).push("" + i), maxObjective[i]);
			maximize[i] = state.parameters.getBoolean(base.push(P_MAXIMIZE).push("" + i),
					def.push(P_MAXIMIZE).push("" + i), maximize[i]);

			// test for validity
			if (minObjective[i] >= maxObjective[i])
				state.output
						.error("For objective " + i + "the min fitness must be strictly less than the max fitness.");
		}
		state.output.exitIfErrors();
	}

	/**
	 * Return true if this fitness is the "ideal" fitness. Default always return
	 * false. You may want to override this.
	 */
	public boolean isIdealFitness() {
		return false;
	}

	/**
	 * Return true if I'm equivalent in fitness (neither better nor worse) to
	 * other fitness. This will be true if all the components are equivalent.
	 * @param other The other fitness.
	 */
	public boolean equivalentTo(Fitness other) {
		LexicographicFitness theOther = (LexicographicFitness) other;

		if (objectives.length != theOther.objectives.length)
			throw new RuntimeException(
					"Attempt made to compare two prioritized compund fitnesses; but they have different numbers of objectives.");

		for (int x = 0; x < objectives.length; x++) {
			if (maximize[x] != theOther.maximize[x]) // uh oh
				throw new RuntimeException(
						"Attempt made to compare two prioritized compund fitnesses; but for objective #" + x
								+ ", one expects higher values to be better and the other expectes lower values to be better.");

			if (objectives[x] - theOther.objectives[x] > this.threshold)
				return false;
		}

		return true;
	}

	/**
	 * Return true if I'm better than other. The DEFAULT rule I'm using is this:
	 * if I am better in one criteria, with the more relevant criteria being
	 * equivalent, then betterThan is true, else it is false.
	 * @param other The other fitness.
	 */
	public boolean betterThan(Fitness other) {
		LexicographicFitness theOther = (LexicographicFitness) other;

		if (objectives.length != theOther.objectives.length)
			throw new RuntimeException(
					"Attempt made to compare two prioritized compund fitnesses; but they have different numbers of objectives.");

		for (int x = 0; x < objectives.length; x++) {
			if (maximize[x] != theOther.maximize[x]) // uh oh
				throw new RuntimeException(
						"Attempt made to compare two prioritized compund fitnesses; but for objective #" + x
								+ ", one expects higher values to be better and the other expectes lower values to be better.");

			if (maximize[x]) {
				if (objectives[x] - theOther.objectives[x] > this.threshold)
					return true;
				else if (theOther.objectives[x] - objectives[x] > this.threshold)
					return false;
			} else {
				if (theOther.objectives[x] - objectives[x] > this.threshold)
					return true;
				else if (objectives[x] - theOther.objectives[x] > this.threshold)
					return false;
			}
		}

		return false;
	}

	/**
	 * Convert the fitness to a string
	 */
	public String fitnessToString() {
		String s = FITNESS_PREAMBLE + LEXICOGRAPHIC_PREAMBLE;
		for (int x = 0; x < objectives.length; x++) {
			if (x > 0)
				s = s + " ";
			s = s + Code.encode(objectives[x]);
		}
		return s + FITNESS_POSTAMBLE;
	}

	/**
	 * Convert the fitness to a human-friendly string
	 */
	public String fitnessToStringForHumans() {
		String s = FITNESS_PREAMBLE + LEXICOGRAPHIC_PREAMBLE;
		for (int x = 0; x < objectives.length; x++) {
			if (x > 0)
				s = s + " ";
			s = s + objectives[x];
		}
		return s + FITNESS_POSTAMBLE;
	}

	/**
	 * Read a fitness from a numbered reader
	 * @param state The evolution state
	 * @param reader The reader
	 * @throws IOException If the fitness can not be read
	 */
	public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException {
		DecodeReturn d = Code.checkPreamble(FITNESS_PREAMBLE + LEXICOGRAPHIC_PREAMBLE, state, reader);
		for (int x = 0; x < objectives.length; x++) {
			Code.decode(d);
			if (d.type != DecodeReturn.T_DOUBLE)
				state.output
						.fatal("Reading Line " + d.lineNumber + ": " + "Bad Fitness (objectives value #" + x + ").");
			objectives[x] = (double) d.d;
		}
	}

	/**
	 * Write a fitness to a file
	 * @param state The evolution state
	 * @param dataOutput The file interface
	 * @throws IOException If the fitness can not be written
	 */
	public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(objectives.length);
		for (int x = 0; x < objectives.length; x++)
			dataOutput.writeDouble(objectives[x]);
		writeTrials(state, dataOutput);
	}

	/**
	 * Read a fitness from a file
	 * @param state The evolution state
	 * @param dataInput The file interface
	 * @throws IOException If the fitness can not be read
	 */
	public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException {
		int len = dataInput.readInt();
		if (objectives == null || objectives.length != len)
			objectives = new double[len];
		for (int x = 0; x < objectives.length; x++)
			objectives[x] = dataInput.readDouble();
		readTrials(state, dataInput);
	}

	/**
	 * Set the threshold to be used when comparing a couple of objective values
	 * @param th Threshold to be used when comparing two objective values
	 */
	private void setThreshold(double th) {
		threshold = th;
	}

	/**
	 * Given another Fitness, returns true if the trial which produced my current
	 * context is "better" in fitness than the trial which produced his current
	 * context, and thus should be retained in lieu of his. This method by default
	 * assumes that trials are Doubles, and that higher Doubles are better. If you
	 * are using distributed evaluation and coevolution and your trials are
	 * otherwise, you need to override this method.
	 * @param other The other fitness
	 */
	@SuppressWarnings("unchecked")
	public boolean contextIsBetterThan(Fitness other) {
		if (other.trials == null || other.trials.size() == 0)
			return true; // I win
		else if (trials == null || trials.size() == 0)
			return false; // he wins

		return bestTrial(trials).betterThan(bestTrial(other.trials));
	}

	/**
	 * Return the best trial of the list of trials
	 * @param trials List of trials
	 */
	LexicographicFitness bestTrial(ArrayList<LexicographicFitness> trials) {
		LexicographicFitness best = ((LexicographicFitness) (trials.get(0)));
		int len = trials.size();
		for (int i = 1; i < len; i++) {
			LexicographicFitness next = ((LexicographicFitness) (trials.get(i)));
			if (next.betterThan(best))
				best = next;
		}
		return best;
	}
}
