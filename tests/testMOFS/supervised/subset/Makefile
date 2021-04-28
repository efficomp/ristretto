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
KAPPA_TST = $(addsuffix /kappa_front_best.data, $(EXPERIMENTS))
EXP_TIMES = $(addsuffix /times.data, $(EXPERIMENTS))

# Include the parameters for the experiments
include ../../params.mk
include ./params.mk

# Adapt paths of files in upper folders
TRAINING_DATA          := ../../$(TRAINING_DATA)
TEST_DATA              := ../../$(TEST_DATA)



# Statistics
RESULTS = results.data
TIMES = times.data
NORMALITY = normality.data
HOMOSCEDASTICITY = homoscedasticity.data
ANOVA = anova.data
KRUSKALWALLIS = kw.data
STATS = $(NORMALITY) $(HOMOSCEDASTICITY) $(ANOVA) $(KRUSKALWALLIS)

# Stability
RANKING_M    = rankings_m.data

# Stability scores
SPEARMAN_SCORER  = ristretto.tools.FSStabilitySpearmanScorer


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
stats: $(RESULTS) $(TIMES) $(SPEARMAN_SCORE) $(STATS)

%/kappa_front_best_tst.data: %/Makefile
	@make -s -C $(@:/kappa_front_best_tst.data=)

%/kappa_front_best.data: %/Makefile
	@make -s -C $(@:/kappa_front_best.data=)

%/times.data: %/Makefile
	@make -s -C $(@:/times.data=)

# Results
$(RESULTS): $(KAPPA_TST)
	@echo "Gathering the results of all tests ..."
	@for i in $^ ; do \
		d=$$(dirname $$i) ; \
		kappas=$$(eval cat $$i | head -n -2 | tail -n +2 | awk '{printf "%s" FS, $$3;}'); \
		echo $$d $$kappas >> $@.tmp ;\
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp

# Times
$(TIMES): $(EXP_TIMES)
	@echo "Gathering the times of all tests ..."
	@for i in $^ ; do \
		d=$$(dirname $$i) ; \
		times=$$(eval cat $$i | head -n -2 | tail -n +2 | awk '{printf "%s" FS, $$2;}'); \
		echo $$d $$times >> $@.tmp ;\
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp

# Normality test
$(NORMALITY): $(RESULTS)
	@echo "Performing the normality test of the experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(@:.data=Test)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Homocedasticity test
$(HOMOSCEDASTICITY): $(RESULTS)
	@echo "Performing the homoscedasticity test of the experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(@:.data=Test)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# ANOVA (ANalysis Of VAriance) test
$(ANOVA): $(RESULTS)
	@echo "Performing the ANOVA test of the experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(@:.data=Test)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Kruskal Wallis test
$(KRUSKALWALLIS): $(RESULTS)
	@echo "Performing the Kruskal-Wallis test of the experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(@:.data=Test)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

$(SPEARMAN_SCORE):
	@echo "Calculating the Spearman correlation between tests ..."
	@echo "Test1 Test2 Spearman" > $@.tmp
	@array=($(EXPERIMENTS)) ;                     \
	for ((i=0; i<$${#array[*]}; i++)) ;           \
	do									          \
		test1=$${array[i]};                       \
		ranking1="$$test1/$(RANKING_M)";          \
		if [ -f $$ranking1 ] ;                    \
		then                                      \
			for ((j=i+1; j<$${#array[*]}; j++)) ; \
			do								      \
				test2=$${array[j]};               \
				ranking2="$$test2/$(RANKING_M)";  \
				if [ -f $$ranking1 ] ;            \
				then                              \
					command="$(JAVA) $(SPEARMAN_SCORER) $(N_FEATURES) $$ranking1 $$ranking2" ; \
					spearman="$$(eval $$command)"; \
			    	echo $$test1 $$test2 $$spearman >> $@.tmp ;\
			    fi; 			                  \
		    done;                                 \
		fi;                                       \
	done
	@cat $@.tmp | column -t > $@                
	@$(RM) $@.tmp
	@echo
	
.PHONY: touch
touch:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e touch;     \
	done
	@touch -c $(RESULTS)
	@touch -c $(NORMALITY)
	@touch -c $(HOMOSCEDASTICITY)
	@touch -c $(ANOVA)
	@touch -c $(TIMES)

.PHONY: clean
clean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e clean;     \
	done
	$(RM) $(RESULTS) $(STATS) $(SPEARMAN_SCORE) $(TIMES)

.PHONY: distclean
distclean: clean
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e distclean; \
	done


