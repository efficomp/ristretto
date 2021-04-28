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
# Test both, a supervised and an unsupervised multi-objective wrapper methods.
# Both use the NSGA-2 algorithm as the search engine.
#
# For supervised (classification) problems, several classification schemes are
# tried:
#  - KNN (k Nearest Neighbors)
#  - NBC (Naive Bayes Classifier)
#  - LDA + KNN (Dimensionality reduction using the direct LDA method before
#    applying KNN)
#  - LDA + KNNNBC (Dimensionality reduction using the direct LDA method before
#    applying NBC)
#
# For unsupervised (clustering) problems, different combinations of a couple of
# CVIs (a compactness index and a separation index) are tested. 
#

# A bash shell will be used for the recipes
SHELL        = /bin/bash

# Include the parameters for the experiments
include params.mk

# Random feature adder
RND_ADDER         = ristretto.tools.RandomFeatureAdder

# Experiments
EXPERIMENTS = $(subst /,,$(sort $(wildcard */) ))

# Tools
JAVA  = java
RM    = rm -rf

.PHONY: all
all : front stats

.PHONY: front
front: $(TRAINING_DATA) $(TEST_DATA)
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e front ;    \
	done

.PHONY: stats
stats:  $(TRAINING_DATA) $(TEST_DATA)
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e stats ;    \
	done

.PHONY: touch
touch:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e touch;     \
	done

add_random:
	@echo "Appending $(RND_FEATS) random features to $(TRAINING_DATA_ORIG) and $(TEST_DATA_ORIG) ..."
	@echo
	@$(JAVA) $(RND_ADDER) $(TRAINING_DATA_ORIG) $(TEST_DATA_ORIG) $(CLASS_INDEX_ORIG) $(RND_FEATS) $(WRITE_CLASS) 1> $(TRAINING_DATA).tmp 2> $(TEST_DATA).tmp
	@cat $(TRAINING_DATA).tmp | column -t > $(TRAINING_DATA)
	@cat $(TEST_DATA).tmp | column -t > $(TEST_DATA)
	@$(RM) *.tmp
	@echo

$(TRAINING_DATA): $(TRAINING_DATA_ORIG) $(TEST_DATA_ORIG)
	@make -s add_random

$(TEST_DATA): $(TRAINING_DATA_ORIG) $(TEST_DATA_ORIG)
	@make -s add_random

.PHONY: clean
clean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e clean;     \
	done
	@$(RM) *.tmp

.PHONY: distclean
distclean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e distclean; \
	done
	@$(RM) $(TRAINING_DATA) $(TEST_DATA) *.tmp

