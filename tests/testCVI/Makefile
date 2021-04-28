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
# Test of some CVIs
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# App
APP   = TestCVI
APP_PACKAGE = tests.testCVI
CLASS = $(APP).class

# Dataset
DATA_ORIG    = ../../datasets/dummy/train.data
CLASS_INDEX  = -1

# Random features
DATA             = dataset.data
RND_FEATS        = 3
WRITE_CLASS      = false	# We don't need the class for the feature selection problem
RND_ADDER        = ristretto.tools.RandomFeatureAdder

NCLUSTERS        = 27
FEATURES         = 0 1

DATAPLOT         = $(DATA:.data=.png)
CENTROIDS        = centroids.data
PLOTS            = $(DATAPLOT)

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all : run
	
.PHONY: run
run: plot

.PHONY: plot
plot: $(PLOTS)
	@for plot in $(PLOTS); do (display $$plot &) ; done

$(CENTROIDS): $(CLASS) $(DATA)
	@echo "Running $(APP)..."
	@echo
	@$(JAVA) $(APP_PACKAGE).$(APP) $(DATA) $(NCLUSTERS) $(FEATURES)
	@echo

$(DATAPLOT): $(DATA) $(CENTROIDS)
	@echo 'set term png' > data.plot
	@echo 'set xlabel "Dimension $(word 1, $(FEATURES))"' >> data.plot
	@echo 'set ylabel "Dimension $(word 2, $(FEATURES))"' >> data.plot
	@echo 'set output "$@"' >> data.plot
	@echo 'set xrange [0:1]' >> data.plot
	@echo 'set yrange [0:1]' >> data.plot
	@echo 'set key bmargin' >> data.plot
	@echo 'plot "$<" using $(word 1, $(FEATURES))+1:$(word 2, $(FEATURES))+1 with points lc rgb "red" title "Data", \' >> data.plot
	@echo '     "$(CENTROIDS)" using $(word 1, $(FEATURES))+1:$(word 2, $(FEATURES))+1 with points pt 7 lc rgb "blue" title "Centroids"' >> data.plot
	@gnuplot data.plot
	@rm data.plot

$(DATA): $(DATA_ORIG)
	@echo "Appending $(RND_FEATS) random features to $<..."
	@$(JAVA) $(RND_ADDER) $< $(CLASS_INDEX) $(RND_FEATS) $(WRITE_CLASS) | column -t > $@
	@echo
	
%.class: %.java
	@echo "Building $<..."
	@$(JAVAC) $<
	@echo

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) $(PLOTS) $(CLASS) $(CENTROIDS) *~
	@echo

.PHONY: distclean
distclean: clean
	@$(RM) $(DATA) 

