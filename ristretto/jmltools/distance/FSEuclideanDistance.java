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
 * This file has been modified by Jesús González from the original Java Machine
 * Library's EuclideanDistance.java to take into account only the selected
 * features of an instance.
 * 
 * The original Java Machine Library's EuclideanDistance.java was licensed
 * under the GNU General Public License (GPL) version 2. You may obtain a copy
 * of the GPL v2 at <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
 * Copyright (c) 2006-2012, Thomas Abeel
 * Copyright (c) 2016, EFFICOMP
 */

package ristretto.jmltools.distance;

import net.sf.javaml.core.Instance;

/**
 * This class implements the Euclidean distance.
 * <p>
 * The Euclidean distance between two points P=(p1,p2,...,pn) and
 * Q=(q1,q2,...,qn) in the Euclidean n-space is defined as: sqrt((p1-q1)^2 +
 * (p2-q2)^2 + ... + (pn-qn)^2)
 * <p>
 * The Euclidean distance is a special instance of the NormDistance. The
 * Euclidean distance corresponds to the 2-norm distance.
 * <p>
 * This class modifies the original Java Machine Library's
 * EuclideanDistance.java (Copyright 2006-2012 by Thomas Abeel under the
 * <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.html">GPL v2</a>)
 * to take into account only the selected features of an instance.
 * <p>
 * <table>
 * <tr>
 * <td style="vertical-align:top">[1]</td>
 * <td><a href= "http://en.wikipedia.org/wiki/Euclidean_distance">Euclidean
 * Distance</a></td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">[2]</td>
 * <td><a href= "http://en.wikipedia.org/wiki/Euclidean_space">Euclidean
 * Space</a></td>
 * </tr>
 * </table>
 * 
 * @author Thomas Abeel
 * @author Jesús González
 */
public class FSEuclideanDistance extends FSAbstractDistance {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. All features are used by default
	 */
	public FSEuclideanDistance() {
		super();
	}

	/**
	 * Construct a distance measure which only will take into account the features
	 * selected by the mask
	 * 
	 * @param mask Mask of selected features
	 */
	public FSEuclideanDistance(boolean[] mask) {
		super(mask);
	}

	/**
	 * Calculate the Euclidean distance as the sum of the absolute differences of
	 * their coordinates.
	 * 
	 * @return The Euclidean distance between the two instances.
	 */
	public double measure(Instance x, Instance y) {
		if (x.noAttributes() != y.noAttributes())
			throw new RuntimeException("Both instances should contain the same number of values.");
		double sum = 0.0;

		int l = Math.min(x.noAttributes(), mask.length);
		for (int i = 0; i < l; i++) {
			sum += ((mask == null || mask[i]) ? 1 : 0) * (x.value(i) - y.value(i)) * (x.value(i) - y.value(i));
		}
		return Math.sqrt(sum);
	}
}
