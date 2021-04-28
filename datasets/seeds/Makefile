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
# Download and pre-processing of the seeds dataset
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# Parameters
INDATA      = seeds_dataset.txt
PROPORTION        = 0.75
ALL_DATA    = seeds.data
TRAIN_DATA  = $(ALL_DATA:.data=_train.data)
TEST_DATA   = $(ALL_DATA:.data=_test.data)
OUTDATA     = $(ALL_DATA) $(TRAIN_DATA) $(TEST_DATA)
MATFILES    = $(OUTDATA:.data=.mat)
PREPROCESS  = preprocessData.m

.PHONY: all
all : $(INDATA) $(MATFILE)
	@echo "Preprocessing..."
	@sed -E 's/[ \t]+/\t/g' $< > $(ALL_DATA)
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(PREPROCESS:.m=)('$(ALL_DATA)',$(PROPORTION));exit;"
	@cat $(ALL_DATA) | column -t > $(ALL_DATA).tmp
	@mv $(ALL_DATA).tmp $(ALL_DATA)
	@cat $(TRAIN_DATA) | column -t > $(TRAIN_DATA).tmp
	@mv $(TRAIN_DATA).tmp $(TRAIN_DATA)
	@cat $(TEST_DATA) | column -t > $(TEST_DATA).tmp
	@mv $(TEST_DATA).tmp $(TEST_DATA)
	@rm $(INDATA)
	@echo

$(INDATA):
	wget https://archive.ics.uci.edu/ml/machine-learning-databases/00236/seeds_dataset.txt

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) $(INDATA) $(OUTDATA) $(MATFILES) *.tmp *~
	@echo

