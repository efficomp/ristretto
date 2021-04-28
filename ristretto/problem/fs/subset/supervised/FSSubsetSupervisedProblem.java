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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.math3.linear.RealMatrix;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.classification.evaluation.PerformanceIndexes;
import ristretto.problem.fs.subset.FSSubsetIndividual;
import ristretto.problem.fs.subset.FSSubsetProblem;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;

/**
 * Multi-objective wrapper for supervised (labeled) datasets solved using a
 * subset of feature indices as individuals representation.
 * 
 * The training data is split into two datasets, TR and VAL, according to the
 * validation-prop parameter, and then, the following two objectives are
 * maximized:
 * <p>
 * <ul>
 * <li>The Kappa index obtained with TR</li>
 * <li>The Kappa index obtained with VAL</li>
 * </ul>
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
 * </table>
 * 
 * @author Jesús González
 */
public class FSSubsetSupervisedProblem extends FSSubsetProblem {
	private static final long serialVersionUID = -8839144439355611200L;

	/** Default parameter base for the problem */
	public static final String P_SUPERVISED = "supervised";

	/** Parameter for the clustering algorithm */
	public static final String P_CLASSIFIER = "classifier";

	/** Parameter to know whether LDA should be applied */
	public static final String P_APPLY_LDA = "apply-lda";

	/**
	 * Parameter for the proportion of samples used for validation
	 */
	public static final String P_VALIDATION_PROP = "validation-prop";

	/** Default classifier */
	public static final String defaultClassifier = "ristretto.jmltools.classification.NaiveBayes";

	/** Classifier class */
	public Class<?> classifierClass;

	/** Whether LDA should be applied */
	public boolean applyLDA;

	/** Default validation proportion */
	public static final double defaultValidationProp = 0.33;

	/** Validation proportion */
	public double validationProp;

	/** Classifier parameters */
	public static ClassifierParameters classifierParams;

	/**
	 * Return the default base for this problem.
	 */
	public Parameter defaultBase() {
		return super.defaultBase().push(P_SUPERVISED);
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

		/* default base for the parameters used to configure this problem */
		Parameter def = defaultBase();

		try {
			/* Obtain the classifier algorithm class name */
			String classifierClassName = state.parameters.getStringWithDefault(base.push(P_CLASSIFIER),
					def.push(P_CLASSIFIER), null);
			if (classifierClassName == null)
				classifierClassName = defaultClassifier;

			/* Obtain the classifier class */
			this.classifierClass = Class.forName(classifierClassName);
		} catch (ClassNotFoundException e) {
			state.output.fatal("Class not found" + e.getMessage());
		}

		/* Test if the classifier class is a valid class */
		boolean isClassifier = Classifier.class.isAssignableFrom(this.classifierClass);
		if (!isClassifier)
			state.output.fatal("Not a valid classifier", base.push(P_CLASSIFIER), def.push(P_CLASSIFIER));

		/* Test if LDA should be applied */
		this.applyLDA = state.parameters.getBoolean(base.push(P_APPLY_LDA), def.push(P_APPLY_LDA), false);

		try {
			this.validationProp = state.parameters.getDouble(base.push(P_VALIDATION_PROP), def.push(P_VALIDATION_PROP));
			if (this.validationProp <= 0 || this.validationProp >= 1) {
				state.output.fatal("The validation proportion must be greater than 0 and lower than 1",
						base.push(P_VALIDATION_PROP), def.push(P_VALIDATION_PROP));
			}
		} catch (NumberFormatException e) {
			this.validationProp = defaultValidationProp;
		}

		// For KNN, fix k to an odd number near sqrt(number of samples)
		classifierParams = new ClassifierParameters();
		classifierParams.k = (int) Math.round(Math.sqrt(data.size()));
		if ((classifierParams.k & 1) == 0)
			classifierParams.k++;
	}

	/**
	 * Project the selected features according to ind and apply LDA depending on the
	 * value of applyLDA
	 * 
	 * @param ind The individual containing the selected features
	 * @return The training data
	 */
	public Dataset generateTrainingData(FSSubsetIndividual ind) {

		/* Project only the selected features */
		Dataset selectedFeatures = MoreDatasetTools.project(data, ind.genome);
		Dataset training = selectedFeatures;

		/* Apply LDA to the selected features */
		if (applyLDA) {
			RealMatrix proj = MoreDatasetTools.directLDA(selectedFeatures);
			training = MoreDatasetTools.project(selectedFeatures, proj);
		}

		return training;
	}

	/**
	 * Construct the classifier
	 * 
	 * @return The classifier
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Classifier constructClassifier() throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Constructor<?> classifierCons;
		Classifier classifier = null;

		if (classifierClass.getName().endsWith("KNearestNeighbors")) {

			Class<?> classifierParameters[];
			classifierParameters = new Class<?>[1];
			classifierParameters[0] = int.class;
			classifierCons = this.classifierClass.getConstructor(classifierParameters);

			classifier = (Classifier) classifierCons.newInstance(classifierParams.k);
		} else {
			classifierCons = this.classifierClass.getConstructor();
			classifier = (Classifier) classifierCons.newInstance();
		}

		return classifier;
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
		if (!(ind instanceof FSSubsetIndividual))
			state.output.fatal("The individuals for this problem should be FSSubsetIndividuals.");

		/*
		 * Classify the data according only to the features selected by the individual's
		 * genome
		 */
		FSSubsetIndividual fsInd = (FSSubsetIndividual) ind;
		int nFeatures = fsInd.genome.size();

		double[] objectives = ((MultiObjectiveFitness) ind.fitness).getObjectives();

		/* Test if the right number of objectives has been selected */
		if (objectives.length != 2)
			state.output.fatal("The number of objectives for this problem should be 2.");

		/* If no feature was selected, the worst fitness is assigned */
		if (nFeatures == 0) {
			for (int i = 0; i < objectives.length; i++)
				objectives[i] = -Double.MAX_VALUE;
		} else {
			if (debug) {
				System.out.println("NFeatures: " + nFeatures);
				System.out.print("Features:");
				for (int i : fsInd.genome)
					System.out.print(" " + i);
				System.out.println();
			}

			try {
				/*
				 * Generate the training dataset according to the selected features
				 */
				Dataset training = generateTrainingData(fsInd);

				/* Split the dataset into training and validation datasets */
				Dataset validation = MoreDatasetTools.split(training, validationProp);

				/* Construct the classifier */
				Classifier classifier = constructClassifier();
				classifier.buildClassifier(training);

				/* Evaluate the solution */
				objectives[0] = PerformanceIndexes.kappa(classifier, training);
				objectives[1] = PerformanceIndexes.kappa(classifier, validation);

			} catch (Exception e) {
				state.output.fatal(e.getMessage());
			}

			if (debug)
				System.out.println("\tDONE!");
		}

		/* Sets the fitness */
		((MultiObjectiveFitness) ind.fitness).setObjectives(state, objectives);
		ind.evaluated = true;
	}

	/** Classifier parameters. */
	public class ClassifierParameters {
		/** k value for KNN */
		public int k;

		/** C Value for SVM */
		public double C;

		/** gamma value for RBF kernels (SVM) */
		public double gamma;
	}
}
