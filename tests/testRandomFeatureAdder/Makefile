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
# Test of the random features adder class
#

# A bash shell will be used for the recipes
SHELL        = /bin/bash

# App
APP          = ristretto.tools.RandomFeatureAdder

# Dataset
TRAIN_ORIG = ../../datasets/dummy/train.data
TEST_ORIG  = ../../datasets/dummy/test.data

CLASS_INDEX  = 2
RANDOM_FEATS = 5

TRAIN_RND = train.data
TEST_RND = test.data
WRITE_CLASS  = true

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all :
	@echo "Appending $(RANDOM_FEATS) random features to $<..."
	@echo
	@$(JAVA) $(APP) $(TRAIN_ORIG) $(TEST_ORIG) $(CLASS_INDEX) $(RANDOM_FEATS) $(WRITE_CLASS) 1> $(TRAIN_RND).tmp 2> $(TEST_RND).tmp
	@cat $(TRAIN_RND).tmp | column -t > $(TRAIN_RND)
	@cat $(TEST_RND).tmp | column -t > $(TEST_RND)
	@$(RM) *.tmp
	@echo
# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~
	@echo

.PHONY: distclean
distclean: clean
	@$(RM) $(TRAIN_RND) $(TEST_RND) *.tmp
