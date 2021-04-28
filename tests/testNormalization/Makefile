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
# Test the dataset normalization
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# App
APP   = TestNormalization
APP_PACKAGE = tests.testNormalization
CLASS = $(APP).class

# Parameters
DATA        = ../../datasets/seeds/seeds.data
CLASS_INDEX = 7

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all : $(CLASS)
	@echo "Running ..."
	@java $(APP_PACKAGE).$(APP) $(DATA) $(CLASS_INDEX)

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
