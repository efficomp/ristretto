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

# Include the parameters for the experiments
include ../../../params.mk
include ../params.mk
include params.mk

# Adapt paths of files in upper folders
TRAINING_DATA := ../../../$(TRAINING_DATA)
TEST_DATA     := ../../../$(TEST_DATA)

# Results
EXPERIMENTS  = $(shell for ((N=$(FIRST_EXPERIMENT_INDEX);N<=$(LAST_EXPERIMENT_INDEX);N=N+1)) ; do echo test$$N ; done)
RELEVANCE    = relevance.data
RELEVANCE_T  = relevance_t.data
RELEVANCE_M  = relevances_m.data
RANKING      = ranking.data
RANKING_M    = rankings_m.data
RELEVANCES   = $(addsuffix /$(RELEVANCE), $(EXPERIMENTS))
RANKINGS   = $(addsuffix /$(RANKING), $(EXPERIMENTS))
KAPPA_FRONT_BEST_TSTS = $(addsuffix /$(KAPPA_FRONT_BEST_TST), $(EXPERIMENTS))
KAPPA_FRONT_BEST_TRS = $(addsuffix /$(KAPPA_FRONT_BEST_TR), $(EXPERIMENTS))
KAPPA_FRONT_BEST = kappa_front_best.data
KAPPA_FRONT_BESTS = $(addsuffix /$(KAPPA_FRONT_BEST), $(EXPERIMENTS))
SENS_FRONT_BEST = sens_front_best.data
SENS_FRONT_BESTS = $(addsuffix /$(SENS_FRONT_BEST), $(EXPERIMENTS))
SPEC_FRONT_BEST = spec_front_best.data
SPEC_FRONT_BESTS = $(addsuffix /$(SPEC_FRONT_BEST), $(EXPERIMENTS))


MAKEFILE_TST = test.mk
MAKEFILES    = $(addsuffix /Makefile, $(EXPERIMENTS))
EXP_TIME     = time.stat
TIME         = $(addsuffix /$(EXP_TIME), $(EXPERIMENTS))
TIMES        = times.data

PARAMETERS = ../../../params.mk ../params.mk ./params.mk
ECJ_PARAMETERS = ../../../ecj.params ../../ecj.params ../ecj.params ./ecj.params

RELEVANCE_PLOT = $(RELEVANCE:.data=.png)
PLOTS          = $(RELEVANCE_PLOT)

# Relevances analysis
RELEVANCES_ANALYZER = ristretto.tools.FSRelevancesAnalyzer

# Ranking of features
FS_RANKER = ristretto.tools.FSRanker

# ParetoFront analysis
FRONT_ANALYZER = ristretto.tools.FSParetoFrontAnalyzer

# Feature Selection Validators
FS_VALIDATOR    = ristretto.tools.FSCrossValidator
KAPPA_VALIDATOR = ristretto.tools.FSKappaValidator
SENS_VALIDATOR = ristretto.tools.FSSensitivityValidator
SPEC_VALIDATOR = ristretto.tools.FSSpecificityValidator

# Stability scores
SPEARMAN_SCORER  = ristretto.tools.FSStabilitySpearmanScorer

# Tools
JAVA  = java
RM    = rm -rf
	
.PHONY: all
all : front stats

.PHONY: front
front: $(TRAINING_DATA) $(TEST_DATA) $(MAKEFILES)
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e front ;    \
	done

.PHONY: stats
stats: $(KAPPA_ANALYSIS) $(KAPPA_FRONT_BEST) $(SPEARMAN_SCORE) $(RANKING_M) $(TIMES)

.PHONY: plot
plot: $(PLOTS)
	@for e in $(EXPERIMENTS) ; do make -s -C $$e plot ; done
#	@for plot in $(PLOTS); do (display $$plot &) ; done

.SECONDARY: $(MAKEFILES)
.INTERMEDIATE: $(RELEVANCE_T) transpose.awk

../../../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/generations.*/generations = $(GENERATIONS)/g" $@
	@sed -i "s/pop.subpop.0.size.*/pop.subpop.0.size = $(POP_SIZE)/g" $@
	@sed -i "s/eval.problem.dataset.class-index.*/eval.problem.dataset.class-index = $(CLASS_INDEX)/g" $@
		
../../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/eval.problem.clusterer.num-centroids.*/eval.problem.clusterer.num-centroids = $(NUM_CENTROIDS)/g" $@
	@sed -i "s/eval.problem.perc-validation.*/eval.problem.perc-validation = $(PERC_VALIDATION)/g" $@

../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/fs.subset.species.n-features.*/fs.subset.species.n-features = $(N_FEATURES)/g" $@
	@sed -i "s/fs.subset.species.min-size.*/fs.subset.species.min-size = $(MIN_N_FEATS)/g" $@
	@sed -i "s/fs.subset.species.max-size.*/fs.subset.species.max-size = $(MAX_N_FEATS)/g" $@

%/Makefile: $(MAKEFILE_TST)
	@mkdir -p $(dir $@)
	@cd $(dir $@) ; ln -s ../$(MAKEFILE_TST) Makefile ; cd ..

%/$(RELEVANCE): $(ECJ_PARAMETERS) %/Makefile $(TRAINING_DATA)
	@echo "Starting $(@:/$(RELEVANCE)=) ..."
	@echo
	@make -s -C $(@:/$(RELEVANCE)=)

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
#	@cat $< | awk -f transpose.awk | column -t > $@
	@cat $< | awk -f transpose.awk > $@
	
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
	@cat $@.tmp | awk -f transpose.awk | column -t | awk 'NR == 1; NR > 1 {print $$0 | "sort -k2,2r -k1,1"}' > $@
	@rm $@.tmp

$(SEL_FEATS) : $(RELEVANCE_M)
	@cat $< | awk 'NR>1 && $$2 > $(RELEVANCE_TH) {printf "%s" FS, $$1;}' > $@
	@echo "Selected features:" `cat $@`
	@echo
	@echo "Storing the selected features to $@ ..."
	@echo
	
$(CLASS_ERROR): $(SEL_FEATS) $(TEST_DATA)
	@$(JAVA) $(FS_VALIDATOR) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $(shell cat $(SEL_FEATS)) | column -t | tee $@
	
$(KAPPA): $(SEL_FEATS) $(TRAINING_DATA) $(TEST_DATA)
	@$(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $(shell cat $(SEL_FEATS)) | column -t | tee $@

$(SENSITIVITY): $(SEL_FEATS) $(TRAINING_DATA) $(TEST_DATA)
	@$(JAVA) $(SENS_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $(shell cat $(SEL_FEATS)) | column -t | tee $@

$(SPECIFICITY): $(SEL_FEATS) $(TRAINING_DATA) $(TEST_DATA)
	@$(JAVA) $(SPEC_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $(shell cat $(SEL_FEATS)) | column -t | tee $@	


$(TH_ANALYSIS): $(RELEVANCE_M) $(TEST_DATA)
	@echo "Threshold analysis ..."
	@echo "Relevance Error nFeats" > $@.tmp
	@cat $< | awk 'NR>1 {print $$2}' | sort -r | uniq | while read rel ; do \
		awk_command='NR>1 && $$2 >= '$$rel' {printf "%s" FS, $$1;}' ; \
		command="cat $< | awk '$$awk_command'" ;  \
		sel_feats="$$(eval $$command)"; \
		n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
		error="$$(eval $(JAVA) $(FS_VALIDATOR) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats | tail -n1 | awk '{print $$2}')"; \
		echo $$rel $$error $$n_feats $$sel_feats >> $@.tmp; \
	done
	@cat $@.tmp | column -t > $@
	@rm $@.tmp
	@echo

$(KAPPA_ANALYSIS): $(RELEVANCE_M) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Kappa analysis of the best pareto front solutions ..."
	@echo  "Relevance KappaTr KappaTst nFeats" > $@.tmp
	@cat $< | awk 'NR>1 {print $$2}' | sort -r | uniq | while read rel ; do \
		st=`echo "$$rel >= $(RELEVANCE_LIMIT)" | bc` ; \
		if [ $$st -eq 1 ] ; then \
			awk_command='NR>1 && $$2 >= '$$rel' {printf "%s" FS, $$1;}' ; \
			command="cat $< | awk '$$awk_command'" ;  \
			sel_feats="$$(eval $$command)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			echo -e $$rel $$kappas $$n_feats $$sel_feats >> $@.tmp; \
		fi ; \
	done
#	@cat $@.tmp | column -t > $@
	@cat $@.tmp > $@
	@rm $@.tmp
	@echo

$(SENS_ANALYSIS): $(RELEVANCE_M) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Sensitivity analysis of the best pareto front solutions ..."
	@echo  "Relevance SensTr SensTstnFeats" > $@.tmp
	@cat $< | awk 'NR>1 {print $$2}' | sort -r | uniq | while read rel ; do \
		st=`echo "$$rel >= $(RELEVANCE_LIMIT)" | bc` ; \
		if [ $$st -eq 1 ] ; then \
			awk_command='NR>1 && $$2 >= '$$rel' {printf "%s" FS, $$1;}' ; \
			command="cat $< | awk '$$awk_command'" ;  \
			sel_feats="$$(eval $$command)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			sens="$$(eval $(JAVA) $(SENS_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			echo -e $$rel $$sens $$n_feats $$sel_feats >> $@.tmp; \
		fi ; \
	done
#	@cat $@.tmp | column -t > $@
	@cat $@.tmp > $@
	@rm $@.tmp
	@echo

$(SPEC_ANALYSIS): $(RELEVANCE_M) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Specificity analysis of the best pareto front solutions ..."
	@echo  "Relevance SpecTr SpecTst nFeats" > $@.tmp
	@cat $< | awk 'NR>1 {print $$2}' | sort -r | uniq | while read rel ; do \
		st=`echo "$$rel >= $(RELEVANCE_LIMIT)" | bc` ; \
		if [ $$st -eq 1 ] ; then \
			awk_command='NR>1 && $$2 >= '$$rel' {printf "%s" FS, $$1;}' ; \
			command="cat $< | awk '$$awk_command'" ;  \
			sel_feats="$$(eval $$command)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			specs="$$(eval $(JAVA) $(SPEC_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			echo -e $$rel $$specs $$n_feats $$sel_feats >> $@.tmp; \
		fi ; \
	done
#	@cat $@.tmp | column -t > $@
	@cat $@.tmp > $@
	@rm $@.tmp
	@echo


$(KAPPA_FRONT_BEST): $(KAPPA_FRONT_BESTS)
	@echo "Combining the solutions with best test kappa of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumKappaTr  += $$2; sumKappaTrX2  += (($$2)^2); \
	        sumKappaTst += $$3; sumKappaTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgKappaTr=sumKappaTr/NR; \
		    avgKappaTst=sumKappaTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevKappaTr=sqrt(sumKappaTrX2/(NR-1) - 2*avgKappaTr*(sumKappaTr/(NR-1)) + ((NR*(avgKappaTr^2))/(NR-1))); \
		    stdDevKappaTst=sqrt(sumKappaTstX2/(NR-1) - 2*avgKappaTst*(sumKappaTst/(NR-1)) + ((NR*(avgKappaTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgKappaTr, avgKappaTst, avgNF, stdDevKappaTr, stdDevKappaTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test KappaTr KappaTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

$(SENS_FRONT_BEST): $(SENS_FRONT_BESTS)
	@echo "Combining the solutions with best test sensibility of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumSensTr  += $$2; sumSensTrX2  += (($$2)^2); \
	        sumSensTst += $$3; sumSensTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgSensTr=sumSensTr/NR; \
		    avgSensTst=sumSensTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevSensTr=sqrt(sumSensTrX2/(NR-1) - 2*avgSensTr*(sumSensTr/(NR-1)) + ((NR*(avgSensTr^2))/(NR-1))); \
		    stdDevSensTst=sqrt(sumSensTstX2/(NR-1) - 2*avgSensTst*(sumSensTst/(NR-1)) + ((NR*(avgSensTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgSensTr, avgSensTst, avgNF, stdDevSensTr, stdDevSensTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test SensTr SensTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

$(SPEC_FRONT_BEST): $(SPEC_FRONT_BESTS)
	@echo "Combining the solutions with best test specificity of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumSpecTr  += $$2; sumSpecTrX2  += (($$2)^2); \
	        sumSpecTst += $$3; sumSpecTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgSpecTr=sumSpecTr/NR; \
		    avgSpecTst=sumSpecTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevSpecTr=sqrt(sumSpecTrX2/(NR-1) - 2*avgSpecTr*(sumSpecTr/(NR-1)) + ((NR*(avgSpecTr^2))/(NR-1))); \
		    stdDevSpecTst=sqrt(sumSpecTstX2/(NR-1) - 2*avgSpecTst*(sumSpecTst/(NR-1)) + ((NR*(avgSpecTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgSpecTr, avgSpecTst, avgNF, stdDevSpecTr, stdDevSpecTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test SpecTr SpecTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo




$(KAPPA_FRONT_BEST_TST): $(KAPPA_FRONT_BEST_TSTS)
	@echo "Combining the solutions with best test kappa of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumKappaTr  += $$2; sumKappaTrX2  += (($$2)^2); \
	        sumKappaTst += $$3; sumKappaTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgKappaTr=sumKappaTr/NR; \
		    avgKappaTst=sumKappaTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevKappaTr=sqrt(sumKappaTrX2/(NR-1) - 2*avgKappaTr*(sumKappaTr/(NR-1)) + ((NR*(avgKappaTr^2))/(NR-1))); \
		    stdDevKappaTst=sqrt(sumKappaTstX2/(NR-1) - 2*avgKappaTst*(sumKappaTst/(NR-1)) + ((NR*(avgKappaTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgKappaTr, avgKappaTst, avgNF, stdDevKappaTr, stdDevKappaTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test KappaTr KappaTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

$(KAPPA_FRONT_BEST_TR): $(KAPPA_FRONT_BEST_TRS)
	@echo "Combining the solutions with best training kappa of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumKappaTr  += $$2; sumKappaTrX2  += (($$2)^2); \
	        sumKappaTst += $$3; sumKappaTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgKappaTr=sumKappaTr/NR; \
		    avgKappaTst=sumKappaTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevKappaTr=sqrt(sumKappaTrX2/(NR-1) - 2*avgKappaTr*(sumKappaTr/(NR-1)) + ((NR*(avgKappaTr^2))/(NR-1))); \
		    stdDevKappaTst=sqrt(sumKappaTstX2/(NR-1) - 2*avgKappaTst*(sumKappaTst/(NR-1)) + ((NR*(avgKappaTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgKappaTr, avgKappaTst, avgNF, stdDevKappaTr, stdDevKappaTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test KappaTr KappaTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

$(SPEARMAN_SCORE): $(RANKINGS)
	@echo "Obtaining the stability score based on the Spearman Metric ..."
	@$(JAVA) $(SPEARMAN_SCORER) $(N_FEATURES) $^ > $@
	@echo

$(RANKING_M): $(RELEVANCE_M)
	@echo "Ranking the features ..."
	@$(JAVA) $(FS_RANKER) $(N_FEATURES) $< > $@
	@echo

$(SPEARMAN_BEST_MEAN): $(BEST_RANKING) $(RANKING_M)
	@echo "Obtaining the Spearman correlation index between the best and average relevances ..."
	@$(JAVA) $(SPEARMAN_SCORER) $(N_FEATURES) $^ > $@
	@echo

$(TIMES):
	@echo "Combining the execution time of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		if  ! [ -s "$$i/$(EXP_TIME)" ] ;\
			then echo 0 > $$i/$(EXP_TIME); \
		fi \
	done
	@for i in $(EXPERIMENTS) ; do \
		time="$$(eval tail -n 1 $$i/$(EXP_TIME))"; \
		echo "$$i $$time" >> times.tmp ; \
	done
	@awk '{ sumTimes += $$2; sumTimesX2 += (($$2)^2); } END \
		  { avgTimes=sumTimes/NR; \
		    stdDevTimes=sqrt(sumTimesX2/(NR-1) - 2*avgTimes*(sumTimes/(NR-1)) + ((NR*(avgTimes^2))/(NR-1))); \
		    printf "Avg %f\nStd %f\n", avgTimes, stdDevTimes}' times.tmp > stats.tmp
	@echo  "Test Time" > header.tmp
	@cat header.tmp times.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

%/kappa_front_best.data: %/Makefile
	@make -s -C $(@:/kappa_front_best.data=)

.PHONY: touch
touch: $(MAKEFILES)
	@for e in $(EXPERIMENTS) ; do \
		make -s -C $$e touch;     \
	done
	@touch -c $(RELEVANCES)
	@touch -c $(RELEVANCE)
	@touch -c $(RELEVANCE_T)
	@touch -c $(RELEVANCE_M)
	@touch -c $(RANKING)
	@touch -c $(RANKING_M)
	@touch -c $(SEL_FEATS)
	@touch -c $(KAPPA)
	@touch -c $(CLASS_ERROR)
	@touch -c $(KAPPA_FRONT_BEST)
	@touch -c $(KAPPA_FRONT_BEST_TST)
	@touch -c $(KAPPA_FRONT_BEST_TR)
	@touch -c $(KAPPA_ANALYSIS) $(SENS_ANALYSIS) $(SPEC_ANALYSIS)
	@touch -c $(TH_ANALYSIS)
	@touch -c $(SPEARMAN_SCORE)
	@touch -c $(SPEARMAN_BEST_MEAN)
	@touch -c $(TIMES)

.PHONY: clean
clean: $(MAKEFILES)
	@for e in $(EXPERIMENTS) ; do                                    \
		if [ -d $$e ] ; then                                         \
			echo "$$e:" ;                                            \
			if [ ! -f $$e/Makefile ]; then                           \
				cd $$e ; ln -s ../$(MAKEFILE_TST) Makefile ; cd .. ; \
			fi ;                                                     \
			make --no-print-directory -C $$e clean;                  \
		fi ;                                                         \
	done
	@$(RM) $(MAKEFILES) $(RELEVANCE) $(RELEVANCE_T) $(RELEVANCE_M) $(SEL_FEATS) $(CLASS_ERROR) $(PLOTS) $(TH_ANALYSIS) $(KAPPA) $(KAPPA_ANALYSIS) $(SENS_ANALYSIS) $(SPEC_ANALYSIS) $(KAPPA_FRONT_BEST_TR) $(KAPPA_FRONT_BEST_TST) $(RANKING_M) $(KAPPA_FRONT_BEST_TST_N_FEAT) $(KAPPA_FRONT_BEST_TR_N_FEAT) $(SPEARMAN_SCORE) $(SPEARMAN_BEST_MEAN) $(TIMES) $(CLASS_ERROR) $(KAPPA_FRONT_BEST)

.PHONY: distclean
distclean: clean
	@echo "Deleting all the experiments ..."
	@echo
	@$(RM) $(EXPERIMENTS)

