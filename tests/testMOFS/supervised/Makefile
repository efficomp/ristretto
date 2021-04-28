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
# Launches a set of experiments
#

# A bash shell will be used for the recipes
SHELL        = /bin/bash

EXPERIMENTS = $(subst /,,$(sort $(wildcard */) ))

# Include the parameters for the experiments
include ../params.mk

# Adapt paths of files in upper folders
TRAINING_DATA          := ../$(TRAINING_DATA)
TEST_DATA              := ../$(TEST_DATA)

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

.PHONY: clean
clean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e clean;     \
	done

.PHONY: distclean
distclean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e distclean; \
	done

