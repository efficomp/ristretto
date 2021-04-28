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
# Test Naive Bayes as a wrapper classifier
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# App
APP   = TestNaiveBayes
APP_PACKAGE = tests.testNaiveBayes
CLASS = $(APP).class

# Datasets
TRAIN_DATA  = ../../../../datasets/seeds/seeds_train.data
TEST_DATA   = ../../../../datasets/seeds/seeds_test.data
CLASS_INDEX = 7
FEATURES = 0 1 3 4

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all : run
	
.PHONY: run
run: $(CLASS) $(TRAIN_DATA) $(TEST_DATA)
	@echo "Running $(APP)..."
	@echo
	@$(JAVA) $(APP_PACKAGE).$(APP) $(TRAIN_DATA) $(TEST_DATA) $(CLASS_INDEX) $(FEATURES)
	@echo
	
%.class: %.java
	@echo "Building $<..."
	@$(JAVAC) $<
	@echo

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) $(CLASS) *~
	@echo

.PHONY: distclean
distclean: clean
	@$(RM)

