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
# Test of the FSParetoFrontAnalyzer class
#

# A bash shell will be used for the recipes
SHELL            = /bin/bash

# App
FRONT_ANALYZER   = ristretto.tools.FSParetoFrontAnalyzer

# Parameters
IND_TYPES        = BitVector Subset
FRONTS           = $(addsuffix .stat, $(addprefix front, $(IND_TYPES)))
RELEVANCE_TH     = 0.5

# Results
RELEVANCES       = $(addsuffix .data, $(addprefix relevance, $(IND_TYPES)))
SEL_FEATS        = $(addsuffix .data, $(addprefix selected, $(IND_TYPES)))
FRONT_PLOTS      = $(FRONTS:.stat=.png)
RELEVANCE_PLOTS  = $(RELEVANCES:.data=.png)
PLOTS            = $(FRONT_PLOTS) $(RELEVANCE_PLOTS)

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.PHONY: all
all : plot $(SEL_FEATS)

selected%.data : relevance%.data
	$(eval TYPE=$(patsubst selected%.dat,%,$@))
	@cat $< | awk 'NR>1 && $$2 > $(RELEVANCE_TH) {printf "%s" FS, $$1;}' > $@
	@echo "Selected features:" `cat $@`
	@echo
	@echo "Storing the selected features to $@ ..."
	@echo

.PHONY: plot
plot: $(PLOTS)
	@for plot in $(PLOTS); do (display $$plot &) ; done

relevance%.png: relevance%.data
	@echo 'set term png' > relevance.plot
	@echo 'set xlabel "Features"' >> relevance.plot
	@echo 'set ylabel "Relevance"' >> relevance.plot
	@echo 'set output "$@"' >> relevance.plot
	@echo 'set boxwidth 0.75' >> relevance.plot
	@echo 'set style data histogram' >> relevance.plot
	@echo 'set style fill solid border -1' >> relevance.plot
	@echo 'unset key' >> relevance.plot
	@echo 'plot "$<" using 2:xtic(1) with boxes lc rgb "grey" notitle, $(RELEVANCE_TH) with lines lc rgb "black"' >> relevance.plot
	@gnuplot relevance.plot
	@rm relevance.plot

relevance%.data: front%.stat
	$(eval TYPE=$(patsubst relevance%.data,%,$@))
	@echo "Analyzing the Pareto front ..."
	@$(JAVA) $(FRONT_ANALYZER) $(TYPE) $< | column -t > $@
	@echo

front%.png: front%.stat
	$(eval TYPE=$(patsubst front%.data,%,$@))
	@echo 'set term png' > front.plot
	@echo 'set xlabel "CVI 1"' >> front.plot
	@echo 'set ylabel "CVI 2"' >> front.plot
	@echo 'set output "$@"' >> front.plot
	@echo 'set key bmargin' >> front.plot
	@echo 'plot "$<" using 2:3 with points notitle' >> front.plot
	@gnuplot front.plot
	@rm front.plot

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~ *.plot $(PLOTS) $(RELEVANCES) $(SEL_FEATS)
	@echo

