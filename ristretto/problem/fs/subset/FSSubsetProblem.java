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

package ristretto.problem.fs.subset;

import java.io.File;
import java.io.IOException;

import ec.EvolutionState;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Base abstract class for Feature Selection problems solved using individuals
 * coded as subsets of feature indices.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>dataset</tt><br>
 * <font size=-1>String (a filename)</font></td>
 * <td valign=top>(the training dataset)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>dataset.class-index</tt><br>
 * <font size=-1>int</font></td>
 * <td valign=top>(index of the column containing the labels)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>debug</tt><br>
 * <font size=-1>boolean</font></td>
 * <td valign=top>(should debug information be shown?)</td>
 * </tr>

 * </table>
 * @author Jesús González
 */
public abstract class FSSubsetProblem extends Problem implements SimpleProblemForm {

	private static final long serialVersionUID = 8094183456024089262L;

	/** Default parameter base for the problem */
	public static final String P_FS_PROBLEM = "problem";

	/** Parameter to load the dataset */
	public static final String P_FS_DATASET = "dataset";

	/** Parameter to load the class index for labeled datasets */
	public static final String P_FS_DATASET_CLASS_INDEX = P_FS_DATASET + ".class-index";

	/** Parameter to activate the debugging logs */
	public static final String P_DEBUG = "debug";

	/** The dataset */
	public Dataset data;
	
	/** Whether to activate the debug logs */
	public boolean debug;

	/**
	 * Return the default parameter base for this problem.
	 */
	public Parameter defaultBase() {
		return FSSubsetDefaults.base().push(P_FS_PROBLEM);
	}

	/**
	 * Set up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base); // always call super.setup(...) first if it
									// exists!

		try {
			/* default base for the parameters used to configure this problem */
			Parameter def = defaultBase();

			/* class index */
			int classIndex = state.parameters.getInt(base.push(P_FS_DATASET_CLASS_INDEX),
					def.push(P_FS_DATASET_CLASS_INDEX), 0);

			/* Obtain the dataset */
			File dataFile = state.parameters.getFile(base.push(P_FS_DATASET), def.push(P_FS_DATASET));
			if (dataFile == null)
				state.output.fatal("Missing dataset", base.push(P_FS_DATASET), def.push(P_FS_DATASET));

			this.data = FileHandler.loadDataset(dataFile, classIndex, "\\s+");

			/* Test if debug logs should be provided */
			debug = state.parameters.getBoolean(base.push(P_DEBUG), def.push(P_DEBUG), false);
		} catch (IOException e) {
			state.output.fatal("Could not open the dataset file: " + e.getMessage());
		}
	}
}
