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

package ristretto.problem.fs.subset.supervised;

import java.util.ArrayList;
import java.util.TreeSet;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import ristretto.ecjtools.LexicographicFitness;
import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import ristretto.problem.fs.subset.FSSubsetIndividual;
import libsvm.LibSVM;
import libsvm.svm_parameter;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.core.Dataset;

/**
 * Co-evolutive Lexicographic Multi-objective wrapper for supervised (labeled)
 * datasets solved using a subset of feature indices as individuals
 * representation.
 * 
 * The training data is split into two datasets, TR and VAL, according to the
 * validation-prop parameter, and then, some objectives are evaluated, depending
 * on the evaluation-mode parameter. Since the algorithm performs a
 * lexicographic search, the order of objectives matters:
 * <p>
 * <table>
 * <tr>
 * <td>Evaluation mode</td>
 * <td>Objectives</td>
 * </tr>
 * <tr>
 * <td valign=top>validation-only</td>
 * <td>
 * <ol>
 * <li>The Kappa index obtained with VAL (maximized)</li>
 * <li>The number of features (minimized)</li>
 * <li>The SVM C parameter (minimized)</li>
 * </ol>
 * </td>
 * </tr>
 * <tr>
 * <td valign=top>validation-training</td>
 * <td>
 * <ol>
 * <li>The Kappa index obtained with VAL (maximized)</li>
 * <li>The Kappa index obtained with TR (maximized)</li>
 * <li>The number of features (minimized)</li>
 * <li>The SVM C parameter (minimized)</li>
 * </ol>
 * </td>
 * </tr>
 * <tr>
 * <td valign=top>cross-validation</td>
 * <td>
 * <ol>
 * <li>The Kappa index obtained using cross-validation over the whole training
 * data (maximized)</li>
 * <li>The number of features (minimized)</li>
 * <li>The SVM C parameter (minimized)</li>
 * </ol>
 * </td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>classifier</tt><br>
 * <font size=-1>{@link net.sf.javaml.classification.Classifier}</font></td>
 * <td valign=top>(the classifier to be used within the wrapper procedure)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>apply-lda</tt><br>
 * <font size=-1>boolean</font></td>
 * <td valign=top>(whether to apply LDA to the selected features before the
 * classifier)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>validation-prop</tt><br>
 * <font size=-1>double &gt; 0, &lt; 1 </font></td>
 * <td valign=top>(Proportion of samples used for validation)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>evaluation-mode</tt><br>
 * <font size=-1>String: "validation-only", "validation-training" or
 * "cross-validation"</font></td>
 * <td valign=top>(Mode of evaluation for the individuals)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>folds</tt><br>
 * <font size=-1>int: &gt; 1</font></td>
 * <td valign=top>(Number of folds for cross-validation)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>set-context</tt><br>
 * <font size=-1>Boolean</font></td>
 * <td valign=top>(Whether to include the context or not in the
 * co-evaluation)</td>
 * </tr>
 * </table>
 * 
 * @author Jesús González
 */
public class FSSubsetSupervisedCoevolutionaryProblem extends FSSubsetSupervisedProblem implements GroupedProblemForm {
	private static final long serialVersionUID = -8839144439355611200L;

	/** Default parameter base for the problem */
	public static final String P_COEVOLUTIONARY = "coevolutionary";

	/** Parameter to decide whether to include the context or not */
	public static final String P_SHOULD_SET_CONTEXT = "set-context";

	/** Parameter to set the evaluation mode */
	public static final String P_EVALUATION_MODE = "evaluation-mode";

	/** Parameter to set the number of folds (if cross-validation is set) */
	public static final String P_NFOLDS = "folds";

	/** Boolean variable to decide whether to include the context or not */
	boolean shouldSetContext;

	/** Validation-only value for the evaluation mode parameter */
	public static final String V_VALIDATION_ONLY = "validation-only";

	/** Validation-training value for the evaluation mode parameter */
	public static final String V_VALIDATION_TRAINING = "validation-training";

	/** Cross-validation value for the evaluation mode parameter */
	public static final String V_CROSS_VALIDATION = "cross-validation";

	/** Validation-only code the evaluation mode */
	public static final int C_VALIDATION_ONLY = 0;

	/** Validation-training code the evaluation mode */
	public static final int C_VALIDATION_TRAINING = 2;

	/** Cross-validation code the evaluation mode */
	public static final int C_CROSS_VALIDATION = 4;

	/** Default evaluation mode */
	public static final int C_EVALUATION_DEFAULT = C_VALIDATION_TRAINING;

	/** Default number of folds for cross-validation */
	public static final int C_NFOLDS_DEFAULT = 5;

	/** Evaluation mode */
	int evaluationMode = C_EVALUATION_DEFAULT;

	/** Number of folds for cross-validation */
	int nFolds = C_NFOLDS_DEFAULT;

	/**
	 * Returns the default base for this problem.
	 */
	public Parameter defaultBase() {
		return super.defaultBase().push(P_COEVOLUTIONARY);
	}

	/**
	 * Sets up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base.
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		Parameter def = defaultBase();

		// load whether we should set context or not
		shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT), def.push(P_SHOULD_SET_CONTEXT),
				true);

		String mode = state.parameters.getString(base.push(P_EVALUATION_MODE), def.push(P_EVALUATION_MODE));

		if (mode != null) {
			if (mode.compareToIgnoreCase(V_VALIDATION_ONLY) == 0)
				evaluationMode = C_VALIDATION_ONLY;
			else if (mode.compareToIgnoreCase(V_VALIDATION_TRAINING) == 0)
				evaluationMode = C_VALIDATION_TRAINING;
			else if (mode.compareToIgnoreCase(V_CROSS_VALIDATION) == 0) {
				evaluationMode = C_CROSS_VALIDATION;
				nFolds = state.parameters.getIntWithDefault(base.push(P_NFOLDS), def.push(P_NFOLDS), C_NFOLDS_DEFAULT);

				if (nFolds < 2)
					state.output.fatal("The number of folds should be at least 2", base.push(P_NFOLDS),
							def.push(P_NFOLDS));
				else if (nFolds > this.data.size())
					state.output.fatal(
							"The number of folds cannot be greater the number of data (" + data.size() + ")",
							base.push(P_NFOLDS), def.push(P_NFOLDS));
			} else
				state.output.fatal("Incorrect evaluation mode", base.push(P_NFOLDS), def.push(P_NFOLDS));
		}
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
		for (int i = 0; i < pop.subpops.length; i++) {
			if (prepareForAssessment[i])
				for (int j = 0; j < pop.subpops[i].individuals.length; j++)
					((LexicographicFitness) (pop.subpops[i].individuals[j].fitness)).trials = new ArrayList<LexicographicFitness>();
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
		for (int i = 0; i < pop.subpops.length; i++) {
			if (assessFitness[i]) {
				for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
					Individual ind = pop.subpops[i].individuals[j];
					LexicographicFitness fit = ((LexicographicFitness) (ind.fitness));

					// Get the best trial
					LexicographicFitness bestTrial = (LexicographicFitness) fit.trials.get(0);
					fit.setObjectives(state, bestTrial.getObjectives());

					// prepare trials for the garbage collector
					fit.trials = null;
					ind.evaluated = true;
				}
			}
		}
	}

	/**
	 * Combine the features of the individuals from all subpopulations
	 * 
	 * @param state The state of the evolutionary process
	 * @param inds  Individuals to be combined
	 * @return The combined individual
	 */
	public FSSubsetIndividual combineFeatures(final EvolutionState state, final Individual[] inds) {

		FSSubsetIndividual combinedFeatures = new FSSubsetIndividual();
		combinedFeatures.genome = new TreeSet<Integer>();

		/*
		 * Since classifiers are also co-evolved with features, and individuals are not
		 * included in their own context (null is used instead), only not null
		 * individuals containing features will be taken into account
		 */
		for (int i = 0; i < inds.length; i++) {
			if (inds[i] != null && inds[i] instanceof FSSubsetIndividual) {
				FSSubsetIndividual coind = (FSSubsetIndividual) (inds[i]);
				combinedFeatures.genome.addAll(coind.genome);
			}
		}

		return combinedFeatures;
	}

	/**
	 * Combine the features of and individual with all the features selected in its
	 * context
	 * 
	 * @param state   The state of the evolutionary process
	 * @param ind     Individual to be evaluated
	 * @param context Context of ind
	 * @return The combined individual
	 */
	public FSSubsetIndividual combineFeatures(final EvolutionState state, final Individual ind,
			final Individual[] context) {

		FSSubsetIndividual combinedFeatures = combineFeatures(state, context);

		// Individuals are not included in their own context. Thus, if an individual
		// stores features, its features must also be taken into account to obtain the
		// total number of features
		if (ind instanceof FSSubsetIndividual) {
			combinedFeatures.genome.addAll(((FSSubsetIndividual) ind).genome);
		}

		return combinedFeatures;
	}

	/**
	 * Get the value of the penalty parameter (C) of the SVM used to co-evaluate an
	 * individual
	 * 
	 * @param state   The state of the evolutionary process
	 * @param ind     Individual to be evaluated
	 * @param context Context of ind
	 * @return The value of C
	 */
	public double getSVMC(final EvolutionState state, final Individual ind, final Individual[] context) {
		double svmC = Double.NaN;

		// Individuals are not included in their own context. Firstly we will try with
		// ind
		if (ind instanceof DoubleVectorIndividual) {
			svmC = ((DoubleVectorIndividual) ind).genome[0];
		} else {
			/*
			 * Since classifiers are also co-evolved with features, and individuals are not
			 * included in their own context (null is used instead), only not null
			 * individuals will be taken into account
			 */
			for (int i = 0; i < context.length; i++) {
				if (context[i] != null && context[i] instanceof DoubleVectorIndividual) {
					svmC = ((DoubleVectorIndividual) context[i]).genome[0];
					break;
				}
			}
		}

		return svmC;
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
	@SuppressWarnings("unchecked")
	public void evaluate(final EvolutionState state, final Individual[] inds, final boolean[] updateFitness,
			final boolean countVictoriesOnly, int[] subpops, final int threadnum) {

		/* There should be at least one sub-population evolving features */
		boolean featuresPresent = false;
		for (int i = 0; !featuresPresent && i < inds.length; i++)
			if (inds[i] instanceof FSSubsetIndividual)
				featuresPresent = true;

		if (!featuresPresent)
			state.output.fatal("There should be at least one sub-population evolving features.");

		// Gather all the features from all the individuals
		FSSubsetIndividual combinedFeatures = combineFeatures(state, inds);

		// Evaluate the combined solution
		int nFeatures = combinedFeatures.genome.size();
		double validationKappa = -Double.MAX_VALUE;
		double trainingKappa = -Double.MAX_VALUE;
		double svmC = ((DoubleVectorIndividual) inds[0]).genome[0];
		double svmGamma = ((DoubleVectorIndividual) inds[0]).genome[1];

		if (nFeatures > 0) {
			/*
			 * Create the classifier according to the first individual parameters
			 */
			// double genome[] = ((DoubleVectorIndividual) inds[0]).genome;
			LibSVM svm = new LibSVM();
			svm_parameter SVMparams = svm.getParameters();
			SVMparams.C = svmC;
			SVMparams.gamma = svmGamma;
			SVMparams.kernel_type = svm_parameter.RBF;
			svm.setParameters(SVMparams);

			try {

				/*
				 * Generate the training dataset according to the selected features
				 */
				Dataset training = generateTrainingData(combinedFeatures);

				if (evaluationMode == C_VALIDATION_ONLY || evaluationMode == C_VALIDATION_TRAINING) {
					Dataset validation = MoreDatasetTools.split(training, validationProp);

					svm.buildClassifier(training);

					// Evaluate the solution
					validationKappa = PerformanceIndexes.kappa(svm, validation);

					if (evaluationMode == C_VALIDATION_TRAINING)
						trainingKappa = PerformanceIndexes.kappa(svm, training);
				} else {
					// Construct new cross validation instance with the classifier
					CrossValidation cv = new CrossValidation(svm);

					// Perform cross-validation on the training set and calculate the kappa index
					validationKappa = PerformanceIndexes.kappa(cv.crossValidation(training, nFolds));
				}

				if (debug) {
					System.out.println("SVM C: " + svmC);
					System.out.println("SVM gamma: " + svmGamma);
					System.out.println("NFeatures: " + nFeatures);
					System.out.println("Features: " + combinedFeatures.genome);
					System.out.println("Validation Kappa: " + validationKappa);
					if (evaluationMode == C_VALIDATION_TRAINING)
						System.out.println("Training Kappa: " + trainingKappa);
				}

			} catch (Exception e) {
				state.output.fatal(e.getMessage());
			}
		}

		// Objectives: Validation Kappa, number of features and C
		int nObjectives = 3;
		int validationKappaIndex = 0;
		int trainingKappaIndex = 0; // won't be used unless evaluationMode == C_VALIDATION_TRAINING
		int nFeaturesIndex = 1;
		int svmCIndex = 2;
		if (evaluationMode == C_VALIDATION_TRAINING) {
			nObjectives = 4;
			trainingKappaIndex++;
			nFeaturesIndex++;
			svmCIndex++;
		}

		double objectives[] = new double[nObjectives];
		objectives[validationKappaIndex] = validationKappa;
		objectives[nFeaturesIndex] = nFeatures;
		objectives[svmCIndex] = svmC;
		if (evaluationMode == C_VALIDATION_TRAINING)
			objectives[trainingKappaIndex] = trainingKappa;

		// update individuals to reflect the trial
		for (int i = 0; i < inds.length; i++) {
			Individual coind = inds[i];
			LexicographicFitness thisTrial = (LexicographicFitness) coind.fitness.clone();
			thisTrial.context = null;
			thisTrial.trials = null;
			thisTrial.setObjectives(state, objectives);
			if (updateFitness[i]) {
				// Update the context if this is the best trial. We're going
				// to assume that the best trial is trial #0 so we don't
				// have to search through them.
				int len = coind.fitness.trials.size();

				if (len == 0) { // easy
					if (shouldSetContext)
						coind.fitness.setContext(inds, i);
					coind.fitness.trials.add(thisTrial);
				} else {
					// best trial is presently #0
					LexicographicFitness bestTrial = (LexicographicFitness) coind.fitness.trials.get(0);
					if (thisTrial.betterThan(bestTrial)) {
						if (shouldSetContext)
							coind.fitness.setContext(inds, i);
						// put me at position 0
						coind.fitness.trials.set(0, thisTrial); // put me at 0
						coind.fitness.trials.add(bestTrial); // move him to the end

						/*
						 * System.out.print("This trial (should be better):"); for (int k=0 ;
						 * k<thisTrial.getNumObjectives() ; k++) System.out.print(" " +
						 * thisTrial.getObjective(k)); System.out.println();
						 * System.out.print("The best until now:"); for (int k=0 ;
						 * k<bestTrial.getNumObjectives() ; k++) System.out.print(" " +
						 * bestTrial.getObjective(k)); System.out.println(); System.out.println();
						 */

					}
				}

				// finally set the fitness for good measure
				((LexicographicFitness) (coind.fitness)).setObjectives(state, objectives);
			}
		}

		if (debug)
			System.out.println("\tDONE!");
	}
}
