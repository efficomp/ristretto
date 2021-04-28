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

import ec.DefaultsForm;
import ec.util.Parameter;
import ristretto.problem.fs.FSDefaults;

/**
 * Default class for all the Feature Selection problems solved using individuals
 * coded as subsets of feature indices.
 * 
 * @author Jesús González
 */
public final class FSSubsetDefaults implements DefaultsForm {
	/** Base parameter */
	public static final String P_SUBSET = "subset";

	/** Returns the default base. */
	public static final Parameter base() {
		return FSDefaults.base().push(P_SUBSET);
	}
}
