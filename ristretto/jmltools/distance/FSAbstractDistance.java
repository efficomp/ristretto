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

package ristretto.jmltools.distance;

import net.sf.javaml.distance.AbstractDistance;

/**
 * Abstract super class for all distances used to solve Feature Selection
 * problems
 * 
 * @see net.sf.javaml.distance.DistanceMeasure
 * @see net.sf.javaml.distance.AbstractDistance
 * 
 * @author Jesús González
 * 
 */
public abstract class FSAbstractDistance extends AbstractDistance {

	private static final long serialVersionUID = 1L;

	/**
	 * Mask for the selected attributes
	 */
	protected boolean[] mask;

	/**
	 * Default constructor. All features are used by default
	 */
	public FSAbstractDistance() {
		super();
		this.mask = null;
	}

	/**
	 * Construct a distance measure which only will take into account the features
	 * selected by the mask
	 * 
	 * @param mask Mask of selected features
	 */
	public FSAbstractDistance(boolean[] mask) {
		super();
		this.mask = mask;
	}

}
