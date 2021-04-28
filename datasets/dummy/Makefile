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
# Dataset generation
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# Parameters
DATAGENERATOR = dataset_generator.m
TRAINDATA     = train.data
TESTDATA      = test.data
TRAINPLOT     = $(TRAINDATA:.data=.png)
TESTPLOT      = $(TESTDATA:.data=.png)
DATA          = $(TRAINDATA) $(TESTDATA)
PLOTS         = $(TRAINPLOT) $(TESTPLOT)
DATAINFO      = dataset.info

# Tools
RM    = rm -rf

.PHONY: all
all : $(DATA) $(PLOTS)

$(DATA): $(DATAGENERATOR)
	@echo "Generating the dataset ..."
	@echo
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(DATAGENERATOR:.m=);exit;"
	@cp $@ $@.tmp
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

%.png: %.data
	@echo "set term png" > data.plot
	@echo "set output \"$@\"" >> data.plot
	@echo "set xrange [0:25]" >> data.plot
	@echo "set yrange [0:25]" >> data.plot
	@echo "plot \"< awk '{if($$""3 == 1) print}' $<\" u 1:2 notitle, \\" >> data.plot
	@echo "     \"< awk '{if($$""3 == 2) print}' $<\" u 1:2 notitle, \\" >> data.plot
	@echo "     \"< awk '{if($$""3 == 3) print}' $<\" u 1:2 notitle" >> data.plot
	@gnuplot data.plot
	@rm data.plot

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~
	@echo

.PHONY: distclean
distclean: clean
	@$(RM) $(DATA) $(PLOTS) $(DATAINFO) *.tmp *.mat
