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

package tests.testLibSVM;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import ristretto.problem.fs.subset.FSSubsetProblem;
import libsvm.LibSVM;
import libsvm.svm_parameter;
import net.sf.javaml.core.Dataset;

/**
 * Test SVM as a wrapper classifier
 * 
 * @author Jesús González
 */
public class TestLibSVM extends FSSubsetProblem {
	private static final long serialVersionUID = -8839144439355611200L;

	/** Parameter for the proportion of samples used for validation */
	public static final String P_VALIDATION_PROP = "validation-prop";

	/** Default validation proportion */
	public static final double defaultValidationProp = 0.33;

	/** Validation proportion */
	public double validationProp;

	/**
	 * Return the default base for this problem
	 */
	public Parameter defaultBase() {
		return super.defaultBase();
	}

	/**
	 * Set up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		// always call super.setup(...) first if it exists!
		super.setup(state, base);

		/* default base for the parameters used to configure this problem */
		Parameter def = defaultBase();

		try {
			this.validationProp = state.parameters.getDouble(base.push(P_VALIDATION_PROP), def.push(P_VALIDATION_PROP));
			if (this.validationProp <= 0 || this.validationProp >= 1) {
				state.output.fatal("The validation proportion must be greater than 0 and lower than 1",
						base.push(P_VALIDATION_PROP), def.push(P_VALIDATION_PROP));
			}
		} catch (NumberFormatException e) {
			this.validationProp = defaultValidationProp;
		}
	}

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
		/* Evaluate only not already evaluated individuals */
		if (ind.evaluated)
			return;

		/* Tests the individual's representation */
		if (!(ind instanceof DoubleVectorIndividual))
			state.output.fatal("The individuals for this problem should be DoubleVectorIndividual.");

		/*
		 * Create the classifier according to the individual parameters
		 */
		double genome[] = ((DoubleVectorIndividual) ind).genome;
		LibSVM svm = new LibSVM();
		svm_parameter SVMparams = svm.getParameters();
		SVMparams.C = genome[0];
		SVMparams.gamma = genome[1];
		SVMparams.kernel_type = svm_parameter.RBF;
		svm.setParameters(SVMparams);

		/* Split the dataset into training and validation datasets */
		Dataset training = data.copy();
		Dataset validation = MoreDatasetTools.split(training, validationProp);
		svm.buildClassifier(training);

		/* Evaluate the solution and sets the fitness */
		double kappaVal = PerformanceIndexes.kappa(svm, validation);

		((SimpleFitness) ind.fitness).setFitness(state,
				// ...the fitness...
				kappaVal,
				/// ... is the individual ideal? Indicate here...
				false);
		ind.evaluated = true;
	}
}
