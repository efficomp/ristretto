#
# This file is part of Ristretto.
#
# Ristretto is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option)
# any later version.
#
# Ristretto is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along with
# Ristretto. If not, see <http://www.gnu.org/licenses/>.
#
# This work was supported by project TIN2015-67020-P (Spanish "Ministerio de
# Economía y Competitividad"), and by the European Regional Development Fund
# (ERDF).
#
# Copyright (c) 2018, EFFICOMP
#

#
# Test of some validators
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# Parameters
DATA         = ../../datasets/seeds/seeds.data
TR_DATA      = ../../datasets/seeds/seeds_train.data
TST_DATA     = ../../datasets/seeds/seeds_test.data
CLASS_INDEX  = 7
FEATURES   = 1 6
LABEL        = 1

#APPLYLDA     = ""
#APPLYLDA     = "applyLDA"
#CLASSIFIER   = "knn"
CLASSIFIER   = "nbc"

# Apps
CROSS_VALIDATOR = ristretto.tools.FSCrossValidator
KAPPA_VALIDATOR = ristretto.tools.FSKappaValidator
SENSITIVITY_VALIDATOR  = ristretto.tools.FSSensitivityValidator
SPECIFICITY_VALIDATOR  = ristretto.tools.FSSpecificityValidator

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all : cross kappa sensitivity specificity

.PHONY: cross
cross:
	@echo Testing $(CROSS_VALIDATOR) ...
	@$(JAVA) $(CROSS_VALIDATOR) $(DATA) $(CLASS_INDEX) $(APPLYLDA) $(CLASSIFIER) $(FEATURES)
	@echo

.PHONY: kappa
kappa:
	@echo Testing $(KAPPA_VALIDATOR) ...
	@$(JAVA) $(KAPPA_VALIDATOR) $(TR_DATA) $(TST_DATA) $(CLASS_INDEX) $(APPLYLDA) $(CLASSIFIER) $(FEATURES)
	@echo

.PHONY: sensitivity
sensitivity:
	@echo Testing $(SENSITIVITY_VALIDATOR) ...
	@$(JAVA) $(SENSITIVITY_VALIDATOR) $(TR_DATA) $(TST_DATA) $(CLASS_INDEX) $(LABEL) $(APPLYLDA) $(CLASSIFIER) $(FEATURES)
	@echo
.PHONY: specificity
specificity:
	@echo Testing $(SPECIFICITY_VALIDATOR) ...
	@$(JAVA) $(SPECIFICITY_VALIDATOR) $(TR_DATA) $(TST_DATA) $(CLASS_INDEX) $(LABEL) $(APPLYLDA) $(CLASSIFIER) $(FEATURES)
	@echo
