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
# Test of the FSRelevancesAnalyzer class
#

# A bash shell will be used for the recipes
SHELL            = /bin/bash

# App
RELEVANCES_ANALYZER = ristretto.tools.FSRelevancesAnalyzer

# Parameters
RELEVANCES     = $(shell for ((N=0;N<=9;N=N+1)) ; do echo relevance$$N.data ; done)
RELEVANCE      = relevances.data
SEL_FEATS      = selected.data
RELEVANCE_T    = relevances_t.data
RELEVANCE_M    = relevances_m.data
RELEVANCE_PLOT = $(RELEVANCE:.data=.png)
RELEVANCE_TH   = 0.5

# Tools
JAVA  = java
JAVAC = javac
RM    = rm -rf

.INTERMEDIATE: $(RELEVANCE_T) transpose.awk

.PHONY: all
all : $(SEL_FEATS) $(RELEVANCE_PLOT)
	@display $(RELEVANCE_PLOT) &

$(RELEVANCE): $(RELEVANCES)
	@echo "Combining the relevance of all experiments ..."
	@$(JAVA) $(RELEVANCES_ANALYZER) $^ | column -t > $(RELEVANCE)
	@echo

transpose.awk:
	@echo '{for (i=1; i<=NF; i++) a[i,NR]=$$'i > $@
	@echo '    max=(max<NF?NF:max)' >> $@
	@echo '}' >> $@
	@echo 'END {' >> $@
	@echo '  for (i=1; i<=max; i++)' >> $@
	@echo '  {' >> $@
	@echo '	   for (j=1; j<=NR; j++)' >> $@
	@echo '	     printf "%s%s", a[i,j], (j==NR?RS:FS)' >> $@
	@echo '  }' >> $@
	@echo '}' >> $@

$(RELEVANCE_T): $(RELEVANCE) transpose.awk
	@cat $< | awk -f transpose.awk | column -t > $@
	
$(RELEVANCE_PLOT): $(RELEVANCE_T)
	@echo Plotting ...
	@echo 'set term png' > relevance.plot
	@echo 'set output "$@"' >> relevance.plot
	@echo 'set xlabel "Features"' >> relevance.plot
	@echo 'set ylabel "Relevance"' >> relevance.plot
	@echo 'set yrange [-0.1:1.1]' >> relevance.plot
	@echo 'set key autotitle columnhead' >> relevance.plot
	@echo 'file = "$<"' >> relevance.plot
	@echo 'header = system("head -1 ".file);' >> relevance.plot
	@echo 'N = words(header)' >> relevance.plot
	@echo 'set xtics ("" 1)' >> relevance.plot
	@echo 'set for [i=2:N] xtics add (word(header, i) i)' >> relevance.plot
	@echo 'set style data boxplot' >> relevance.plot
	@echo 'set style fill solid 0.25 border lt -1' >> relevance.plot
	@echo 'unset key' >> relevance.plot
	@echo 'set arrow from graph 0,0.5 to graph 1,0.5 nohead' >> relevance.plot
	@echo 'plot for [i=2:N] file using (i):i lc rgb "black"' >> relevance.plot
	@gnuplot relevance.plot
	@rm relevance.plot

$(RELEVANCE_M) : $(RELEVANCE_T) transpose.awk
	@cat $< | awk 'NR==1 {print; exit}' > $@.tmp
	@cat $< | awk 'NR>1 {for (i=2;i<=NF;i++){acc[i-1]+=$$i;}} END {printf "Relevance\t" ; for (i=1;i<NF;i++){printf "%f", acc[i]/(NR-1); printf "\t"};printf "\n"}' >> $@.tmp
	@cat $@.tmp | awk -f transpose.awk | column -t > $@
	@rm $@.tmp

$(SEL_FEATS) : $(RELEVANCE_M)
	@cat $< | awk 'NR>1 && $$2 > $(RELEVANCE_TH) {printf "%s" FS, $$1;}' > $@
	@echo "Selected features:" `cat $@`
	@echo
	@echo "Storing the selected features to $@ ..."
	@echo

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~ $(RELEVANCE) $(RELEVANCE_PLOT) $(RELEVANCE_M) $(SEL_FEATS) $(RELEVANCE_T)
	@echo
