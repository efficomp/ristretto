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

package ristretto.problem.fs.subset.unsupervised;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import ristretto.jmltools.MoreDatasetTools;
import ristretto.jmltools.clustering.evaluation.CVIFSNormalizer;
import ristretto.problem.fs.subset.FSSubsetIndividual;
import ristretto.problem.fs.subset.FSSubsetProblem;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.core.Dataset;

/**
 * Multi-objective feature selection for unsupervised datasets solved using a
 * subset of feature indices as individuals representation.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>clusterer</tt><br>
 * <font size=-1>{@link net.sf.javaml.clustering.Clusterer}</font></td>
 * <td valign=top>(the clustering algorithm)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>clusterer.num-centroids</tt><br>
 * <font size=-1>int &gt; 1 </font></td>
 * <td valign=top>(number of centroids to be used in the clustering algorithm)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>clusterer.stop-criterion</tt><br>
 * <font size=-1>double &gt; 0 </font></td>
 * <td valign=top>(stop criterion to be used in the clustering algorithm)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>separation-index</tt><br>
 * <font size=-1>{@link net.sf.javaml.clustering.evaluation.ClusterEvaluation}</font></td>
 * <td valign=top>(separation index used to evaluate potential solutions)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>separation-index.fs-norm</tt><br>
 * <font size=-1>method of {@link ristretto.jmltools.clustering.evaluation.CVIFSNormalizer}</font></td>
 * <td valign=top>(normalization factor for the separation index)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>compactness-index</tt><br>
 * <font size=-1>{@link net.sf.javaml.clustering.evaluation.ClusterEvaluation}</font></td>
 * <td valign=top>(compactness index used to evaluate potential solutions)</td>
 * </tr>
 *
 * <tr>
 * <td valign=top><i>base</i>.<tt>compactness-index.fs-norm</tt><br>
 * <font size=-1>method of {@link ristretto.jmltools.clustering.evaluation.CVIFSNormalizer}</font></td>
 * <td valign=top>(normalization factor for the compactness index)</td>
 * </tr>
 * </table>
 *
 * @author Jesús González
 */

public class FSSubsetUnsupervisedProblem extends FSSubsetProblem {

	private static final long serialVersionUID = 8094183456024089262L;

	/** Default parameter base for the problem */
	public static final String P_UNSUPERVISED = "unsupervised";

	/** Parameter for the clustering algorithm */
	public static final String P_CLUSTERER = "clusterer";

	/**
	 * Parameter for the number of centroids to be used in the clustering algorithm
	 */
	public static final String P_CLUSTERER_NUM_CENTROIDS = P_CLUSTERER + ".num-centroids";

	/**
	 * Parameter for the stop criterion to be used in the clustering algorithm
	 */
	public static final String P_CLUSTERER_STOP_CRITERION = P_CLUSTERER + ".stop-criterion";

	/** Parameter for the separation index */
	public static final String P_SEPARATION_INDEX = "separation-index";

	/** Parameter for the compactness index */
	public static final String P_COMPACTNESS_INDEX = "compactness-index";

	/**
	 * Parameter for the normalization factor for the separation index
	 */
	public static final String P_SEPARATION_FS_NORM = P_SEPARATION_INDEX + ".fs-norm";

	/**
	 * Parameter for the normalization factor for compactness index
	 */
	public static final String P_COMPACTNESS_FS_NORM = P_COMPACTNESS_INDEX + ".fs-norm";

	/**
	 * Parameter to know whether the separation index should be maximized
	 */
	private static final String P_MAXIMIZE_SEPARATION = "fitness.maximize.0";

	/**
	 * Parameter to know whether the compactness index should be maximized
	 */
	private static final String P_MAXIMIZE_COMPACTNESS = "fitness.maximize.1";

	/** Clustering algorithm */
	public Class<?> clustererClass;

	/** Minimum number of centroids for the clustering algorithm */
	public static int clustererMinNumCentroids = 2;

	/**
	 * Number of centroids to be used in the clustering algorithm
	 */
	public int clustererNumCentroids;

	/**
	 * Stop criterion to be used in the clustering algorithm
	 */
	public double clustererStopCriterion;

	/** Separation index */
	public Class<?> separationIndexClass;

	/** Compactness index */
	public Class<?> compactnessIndexClass;

	/**  Normalization factor for the separation index */
	public String separationNormMethodName;

	/** Normalization factor for compactness index */
	public String compactnessNormMethodName;

	/** Should the separation index be maximized? */
	private boolean maximizeSeparation;

	/** Should the compactness index be maximized? */
	private boolean maximizeCompactness;

	/**
	 * Return the default base for this problem
	 */
	public Parameter defaultBase() {
		return super.defaultBase().push(P_UNSUPERVISED);
	}

	/**
	 * Set up the problem by reading it from the parameters stored in state, built
	 * off of the parameter base base
	 * 
	 * @param state The evolution state
	 * @param base  The parameter base
	 */
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		try {
			/* default base for the parameters used to configure this problem */
			Parameter def = defaultBase();

			/* Obtain the number of centroids for the clustering algorithm */

			// gets an int greater or equal to minNumCentroids from the
			// parameter database,
			// returning a value of minNumCentroids-1 if the parameter doesn't
			// exist or was
			// smaller than minNumCentroids
			this.clustererNumCentroids = state.parameters.getInt(base.push(P_CLUSTERER_NUM_CENTROIDS),
					def.push(P_CLUSTERER_NUM_CENTROIDS), clustererMinNumCentroids);
			if (this.clustererNumCentroids < clustererMinNumCentroids)
				state.output.fatal(
						String.format("The number of centroids should be greater than %d",
								clustererMinNumCentroids - 1),
						base.push(P_CLUSTERER_NUM_CENTROIDS), def.push(P_CLUSTERER_NUM_CENTROIDS));

			this.clustererStopCriterion = state.parameters.getDouble(base.push(P_CLUSTERER_STOP_CRITERION),
					def.push(P_CLUSTERER_STOP_CRITERION), 0.0);

			/* Obtain the clustering algorithm class name */
			String clustererClassName = state.parameters.getStringWithDefault(base.push(P_CLUSTERER),
					def.push(P_CLUSTERER), null);

			/* Obtain the clusterer class */
			clustererClass = Class.forName(clustererClassName);

			/* Test if the clusterer class is a valid class */
			boolean isClusterer = Clusterer.class.isAssignableFrom(clustererClass);
			if (!isClusterer) {
				state.output.fatal("Not a valid clusterer", base.push(P_CLUSTERER), def.push(P_CLUSTERER));
			}

			/* Obtain the CVI class names */
			String separationIndexClassName = state.parameters.getStringWithDefault(base.push(P_SEPARATION_INDEX),
					def.push(P_SEPARATION_INDEX), null);
			String compactnessIndexClassName = state.parameters.getStringWithDefault(base.push(P_COMPACTNESS_INDEX),
					def.push(P_COMPACTNESS_INDEX), null);

			/* Obtain the CVI classes */
			separationIndexClass = Class.forName(separationIndexClassName);
			compactnessIndexClass = Class.forName(compactnessIndexClassName);

			separationNormMethodName = state.parameters.getStringWithDefault(base.push(P_SEPARATION_FS_NORM),
					def.push(P_SEPARATION_FS_NORM), null);
			compactnessNormMethodName = state.parameters.getStringWithDefault(base.push(P_COMPACTNESS_FS_NORM),
					def.push(P_COMPACTNESS_FS_NORM), null);

			/* Test if objectives should be maximized or minimized */
			maximizeSeparation = state.parameters.getBoolean(new Parameter(P_MAXIMIZE_SEPARATION),
					new Parameter("multi.fitness.maximize.0"), true);
			maximizeCompactness = state.parameters.getBoolean(new Parameter(P_MAXIMIZE_COMPACTNESS),
					new Parameter("multi.fitness.maximize.1"), true);
		} catch (ClassNotFoundException e) {
			state.output.fatal("Class not found" + e.getMessage());
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
		try {
			/* Evaluate only not already evaluated individuals */
			if (ind.evaluated)
				return;

			/* Tests the individual's representation */
			if (!(ind instanceof FSSubsetIndividual))
				state.output.fatal("The individuals for this problem should be FSSubsetIndividuals.");

			/* Tests the number of objectives */
			double[] objectives = ((MultiObjectiveFitness) ind.fitness).getObjectives();
			if (objectives.length != 2)
				state.output.fatal("This problem should have two objectives");

			/*
			 * Clusters the data according only to the features selected by the individual's
			 * genome
			 */
			FSSubsetIndividual fsInd = (FSSubsetIndividual) ind;
			int nFeatures = fsInd.genome.size();

			/* If no feature was selected, the worst fitness is assigned */
			if (nFeatures == 0) {
				if (maximizeSeparation)
					objectives[0] = Double.MIN_VALUE;
				else
					objectives[0] = Double.MAX_VALUE;
				if (maximizeCompactness)
					objectives[1] = Double.MIN_VALUE;
				else
					objectives[1] = Double.MAX_VALUE;
			} else {
				/* Project only the selected features */
				Dataset projectedData = MoreDatasetTools.project(data, fsInd.genome);

				/* Constructs the clusterer */
				Class<?> clustererParameters[];
				Constructor<?> clustererCons;
				Clusterer clusterer;

				if (clustererStopCriterion <= 0) {
					clustererParameters = new Class<?>[1];
					clustererParameters[0] = int.class;
					clustererCons = clustererClass.getConstructor(clustererParameters);
					clusterer = (Clusterer) clustererCons.newInstance(clustererNumCentroids);
				} else {
					clustererParameters = new Class<?>[2];
					clustererParameters[0] = int.class;
					clustererParameters[1] = double.class;
					clustererCons = clustererClass.getConstructor(clustererParameters);
					clusterer = (Clusterer) clustererCons.newInstance(clustererNumCentroids, clustererStopCriterion);
				}

				if (debug)
					System.out.print("NFeatures: " + nFeatures);

				/*
				 * Apply a clustering algorithm to a projection of the selected features
				 */
				long startTime = 0, stopTime = 0;
				if (debug) {
					System.out.print("\tClustering: ");
					startTime = System.currentTimeMillis();
				}

				Dataset[] clusters = clusterer.cluster(projectedData);
				if (debug) {
					stopTime = System.currentTimeMillis();
					System.out.printf("%.2fs", (stopTime - startTime) / 1000.0);
				}

				/* Constructs the CVIs */
				Constructor<?> separationIndexCons = separationIndexClass.getConstructor();
				Constructor<?> compactnessIndexCons = compactnessIndexClass.getConstructor();

				/* Evaluate the CVIs */
				ClusterEvaluation separationIndex = (ClusterEvaluation) separationIndexCons.newInstance();
				ClusterEvaluation compactnessIndex = (ClusterEvaluation) compactnessIndexCons.newInstance();

				if (debug)
					System.out.print("\tEvaluation: ");

				double separationScore = separationIndex.score(clusters);
				if (debug)
					System.out.print("separation");

				double compactnessScore = compactnessIndex.score(clusters);
				if (debug)
					System.out.print(" compactness");

				/* Normalize the values of CVIs */
				CVIFSNormalizer cviNormalizer = new CVIFSNormalizer(nFeatures, clusters);
				if (separationNormMethodName != null) {
					Method separationNormMethod = cviNormalizer.getClass().getMethod(separationNormMethodName);
					separationScore /= (double) separationNormMethod.invoke(cviNormalizer);
				}
				if (compactnessNormMethodName != null) {
					Method compactnessNormMethod = cviNormalizer.getClass().getMethod(compactnessNormMethodName);
					compactnessScore /= (double) compactnessNormMethod.invoke(cviNormalizer);
				}

				/* Evaluate the solution */
				objectives[0] = separationScore;
				objectives[1] = compactnessScore;
				if (debug)
					System.out.println("\tDONE!");
			}

			/* Sets the fitness */
			((MultiObjectiveFitness) ind.fitness).setObjectives(state, objectives);
			ind.evaluated = true;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			state.output.fatal(e.getMessage());
		}
	}
}
