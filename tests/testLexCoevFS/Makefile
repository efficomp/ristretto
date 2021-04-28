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
# Test a lexicographic coevolutionary many-objective feature selection wrapper
# method with several fitness functions.
#
# Population 0 evolves the SVM parameters while the remaining populations look
# for the most relevant features. The following multi- and many-objective
# fitness functions are tested:
#  - Validation-only Kappa index, number of features, SVM C parameter
#  - Validation and training Kappa indices, number of features, SVM C parameter
#  - 2-fold cross-validation, number of features, SVM C parameter
#  - 5-fold cross-validation, number of features, SVM C parameter
#  - 10-fold cross-validation, number of features, SVM C parameter
#
# The validation and Kappa indices depend on the value of the
# eval.problem.validation-prop parameter, while the lexicographic comparison of
# fitnesses is performed taking into account the value of the
# lexicographic.fitness.threshold parameter. These two parameters have an
# important influence on the wrapper results and have to be set carefully
#

# A bash shell will be used for the recipes
SHELL        = /bin/bash

# Matlab scripts
NORMALITY_TEST = normalityTest
HOMOSCEDASTICITY_TEST = homoscedasticityTest
ANOVA_TEST = anovaTest
KRUSKALWALLIS_TEST= kwTest

# Experiments
EXPERIMENTS = $(subst /,,$(sort $(wildcard */) ))
EXP_KAPPAS = $(addsuffix /kappa_best.data, $(EXPERIMENTS))
EXP_RESULTS = $(addsuffix /error_best.data, $(EXPERIMENTS))
EXP_TIMES = $(addsuffix /time.stat, $(EXPERIMENTS))

KAPPAS = kappas.data
ERRORS = errors.data
NFEATS = nfeats.data
TIMES  = times.data

NORMALITY_KAPPA = normality_kappa.data
HOMOSCEDASTICITY_KAPPA = homoscedasticity_kappa.data
ANOVA_KAPPA = anova_kappa.data
KRUSKALWALLIS_KAPPA = kw_kappa.data

NORMALITY_ERROR = normality_error.data
HOMOSCEDASTICITY_ERROR = homoscedasticity_error.data
ANOVA_ERROR = anova_error.data
KRUSKALWALLIS_ERROR = kw_error.data

NORMALITY_NFEATS = normality_nfeats.data
HOMOSCEDASTICITY_NFEATS = homoscedasticity_nfeats.data
ANOVA_NFEATS = anova_nfeats.data
KRUSKALWALLIS_NFEATS = kw_nfeats.data

NORMALITY_TIME = normality_time.data
HOMOSCEDASTICITY_TIME = homoscedasticity_time.data
ANOVA_TIME = anova_time.data
KRUSKALWALLIS_TIME = kw_time.data

KAPPA_STATS = $(NORMALITY_KAPPA) $(HOMOSCEDASTICITY_KAPPA) $(ANOVA_KAPPA) $(KRUSKALWALLIS_KAPPA)
ERROR_STATS = $(NORMALITY_ERROR) $(HOMOSCEDASTICITY_ERROR) $(ANOVA_ERROR) $(KRUSKALWALLIS_ERROR)
NFEATS_STATS = $(NORMALITY_NFEATS) $(HOMOSCEDASTICITY_NFEATS) $(ANOVA_NFEATS) $(KRUSKALWALLIS_NFEATS)
TIME_STATS = $(NORMALITY_TIME) $(HOMOSCEDASTICITY_TIME) $(ANOVA_TIME) $(KRUSKALWALLIS_TIME)

.PHONY: all
all: stats

.PHONY: stats
stats:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e stats ;    \
	done

.PHONY: touch
touch:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e touch;     \
	done

# Kappa indices
$(KAPPAS): $(EXP_KAPPAS)
	@echo "Gathering the Kappa index of all tests ..."
	@for i in $^ ; do \
		d=$$(dirname $$i) ; \
		kappas=$$(eval cat $$i | head -n -2 | tail -n +2 | awk '{printf "%s" FS, $$3;}'); \
		echo $$d $$kappas >> $@.tmp ;\
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp

# Normality test
$(NORMALITY_KAPPA): $(KAPPAS)
	@echo "Performing the normality test of the Kappa index of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(NORMALITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Homocedasticity test
$(HOMOSCEDASTICITY_KAPPA): $(KAPPAS)
	@echo "Performing the homoscedasticity test of the Kappa index of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(HOMOSCEDASTICITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# ANOVA (ANalysis Of VAriance) test
$(ANOVA_KAPPA): $(KAPPAS)
	@echo "Performing the ANOVA test of the Kappa index of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(ANOVA_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Kruskal Wallis test
$(KRUSKALWALLIS_KAPPA): $(KAPPAS)
	@echo "Performing the Kruskal-Wallis test of the Kappa index of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(KRUSKALWALLIS_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Error rates
$(ERRORS): $(EXP_RESULTS)
	@echo "Gathering the error rate of all tests ..."
	@for i in $^ ; do \
		d=$$(dirname $$i) ; \
		errors=$$(eval cat $$i | head -n -2 | tail -n +2 | awk '{printf "%s" FS, $$3;}'); \
		echo $$d $$errors >> $@.tmp ;\
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp

# Normality test
$(NORMALITY_ERROR): $(ERRORS)
	@echo "Performing the normality test of the error of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(NORMALITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Homocedasticity test
$(HOMOSCEDASTICITY_ERROR): $(ERRORS)
	@echo "Performing the homoscedasticity test of the error of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(HOMOSCEDASTICITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# ANOVA (ANalysis Of VAriance) test
$(ANOVA_ERROR): $(ERRORS)
	@echo "Performing the ANOVA test of the error of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(ANOVA_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Kruskal Wallis test
$(KRUSKALWALLIS_ERROR): $(ERRORS)
	@echo "Performing the Kruskal-Wallis test of the error of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(KRUSKALWALLIS_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Number of features
$(NFEATS): $(EXP_RESULTS)
	@echo "Gathering the number of features of all tests ..."
	@for i in $^ ; do \
		d=$$(dirname $$i) ; \
		nfeats=$$(eval cat $$i | head -n -2 | tail -n +2 | awk '{printf "%s" FS, $$4;}'); \
		echo $$d $$nfeats >> $@.tmp ;\
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp

# Normality test
$(NORMALITY_NFEATS): $(NFEATS)
	@echo "Performing the normality test of number of features of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(NORMALITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Homocedasticity test
$(HOMOSCEDASTICITY_NFEATS): $(NFEATS)
	@echo "Performing the homoscedasticity test of the number of features of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(HOMOSCEDASTICITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# ANOVA (ANalysis Of VAriance) test
$(ANOVA_NFEATS): $(NFEATS)
	@echo "Performing the ANOVA test of the number of features of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(ANOVA_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Kruskal Wallis test
$(KRUSKALWALLIS_NFEATS): $(NFEATS)
	@echo "Performing the Kruskal-Wallis test of the number of features of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(KRUSKALWALLIS_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

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
$(NORMALITY_TIME): $(TIMES)
	@echo "Performing the normality test of the execution time of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(NORMALITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Homocedasticity test
$(HOMOSCEDASTICITY_TIME): $(TIMES)
	@echo "Performing the homoscedasticity test of the execution time of experiments ..."
	@matlab -nojvm -nodesktop -nodisplay -nosplash -r "$(HOMOSCEDASTICITY_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# ANOVA (ANalysis Of VAriance) test
$(ANOVA_TIME): $(TIMES)
	@echo "Performing the ANOVA test of the execution time of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(ANOVA_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

# Kruskal Wallis test
$(KRUSKALWALLIS_TIME): $(TIMES)
	@echo "Performing the Kruskal-Wallis test of the execution time of experiments ..."
	@matlab -nodesktop -nodisplay -nosplash -r "$(KRUSKALWALLIS_TEST)('$<', '$@.tmp');exit;"
	@cat $@.tmp | column -t > $@
	@$(RM) $@.tmp
	@echo

.PHONY: clean
clean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e clean;     \
	done
	@$(RM) *.tmp *~

.PHONY: distclean
distclean:
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e distclean; \
	done
	@$(RM) *.tmp *~ *.data


