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

# Parameters
BASE           = .
PARAMETERS     = ../params.mk

.PHONY: all
all : stats

# Include the parameters for the experiments
include $(PARAMETERS)

# Adapt paths of files in upper folders
TRAINING_DATA_ORIG := ../$(TRAINING_DATA_ORIG)
TEST_DATA_ORIG     := ../$(TEST_DATA_ORIG)

# Results
EXPERIMENTS    = $(shell for ((N=$(FIRST_EXPERIMENT_INDEX);N<=$(LAST_EXPERIMENT_INDEX);N=N+1)) ; do echo test$$N ; done)
MAKEFILE_TST   = test.mk

MAKEFILES      = $(addsuffix /Makefile, $(EXPERIMENTS))
OUTS_SAVED     = $(addsuffix /$(OUT_SAVED), $(EXPERIMENTS))
RELEVANCES     = $(addsuffix /$(RELEVANCE), $(EXPERIMENTS))
RANKINGS       = $(addsuffix /$(RANKING), $(EXPERIMENTS))
TIMES          = $(addsuffix /$(TIME), $(EXPERIMENTS))
KAPPA_BESTS    = $(addsuffix /$(KAPPA_BEST), $(EXPERIMENTS))

KAPPA_BESTS_KNN    = $(addsuffix /kappa_best_knn.data, $(EXPERIMENTS))
KAPPA_BESTS_LDAKNN = $(addsuffix /kappa_best_ldaknn.data, $(EXPERIMENTS))
KAPPA_BESTS_NBC    = $(addsuffix /kappa_best_nbc.data, $(EXPERIMENTS))
KAPPA_BESTS_LDANBC = $(addsuffix /kappa_best_ldanbc.data, $(EXPERIMENTS))


ERROR_BESTS    = $(addsuffix /$(ERROR_BEST), $(EXPERIMENTS))
SVM_PARAMSS    = $(addsuffix /$(SVM_PARAMS), $(EXPERIMENTS))

RELEVANCE_T    = relevance_t.data
RELEVANCE_M    = relevances_m.data
RANKING_M      = rankings_m.data
SPEARMAN_SCORE = spearman_score.data
KAPPA_ANALYSIS = kappa_analysis.data

.SECONDARY: $(MAKEFILES) $(OUTS_SAVED) $(SVM_PARAMSS) $(RELEVANCES) $(RANKINGS) $(TIMES) $(KAPPA_BESTS) $(ERROR_BESTS)
.INTERMEDIATE: $(ECJ_FS_POPS_PARAMS) $(ECJ_RUN_PARAMS) $(RELEVANCE_T) transpose.awk

.PHONY: stats
stats: $(SPEARMAN_SCORE) $(RANKING_M) $(SVM_PARAMS) $(TIME) $(KAPPA_BEST) $(ERROR_BEST)

$(TRAINING_DATA): $(TRAINING_DATA_ORIG) $(TEST_DATA_ORIG)
ifeq ($(RND_FEATS),)
	@echo "Copying training data ..."
	@echo
	@ln -s $< $@
else 
	@echo "Appending $(RND_FEATS) random features to $(TRAINING_DATA_ORIG) and $(TEST_DATA_ORIG) ..."
	@$(JAVA) $(RND_ADDER) $(TRAINING_DATA_ORIG) $(TEST_DATA_ORIG) $(CLASS_INDEX_ORIG) $(RND_FEATS) $(WRITE_CLASS) 1> $(TRAINING_DATA).tmp 2> $(TEST_DATA).tmp
	@cat $(TRAINING_DATA).tmp | column -t > $(TRAINING_DATA)
	@cat $(TEST_DATA).tmp | column -t > $(TEST_DATA)
	$(RM) *.tmp
	@echo
endif

$(TEST_DATA): $(TEST_DATA_ORIG) $(TRAINING_DATA)
ifeq ($(RND_FEATS),)
	@echo "Copying test data ..."
	@echo
	@ln -s $< $@
endif

.PHONY: parallel
parallel: $(MAKEFILES) $(TRAINING_DATA) $(TEST_DATA)
	@ssh -q -o BatchMode=yes -o ConnectTimeout=10 $(word 1, $(COMPUTE_NODES)) exit ; \
	if [ $$? -eq 0 ] ;\
	then \
		n_experiments=$(words $(EXPERIMENTS)); \
		n_nodes=$(words $(COMPUTE_NODES)); \
		n=$$n_nodes; \
		if [ $$n_experiments -lt $$n ] ; \
		then \
			n=$$n_experiments; \
		fi; \
		experiments=($(EXPERIMENTS)); \
		nodes=($(COMPUTE_NODES)); \
		for (( i=0 ; i<$$n ; i++ )) ; \
		do \
			current_dir=`pwd`; \
			xterm -e "ssh -X $${nodes[$$i]} 'cd $$current_dir/$${experiments[$$i]}; make'" >/dev/null &disown; \
		done; \
	else \
		echo "Can't connect to compute nodes" ; \
	fi;

%/$(RANKING): %/$(RELEVANCE)
	@cd $(dir $@) ; make -s $(RANKING) ; cd ..

%/$(RELEVANCE): %/$(OUT_SAVED)
	@cd $(dir $@) ; make -s $(RELEVANCE) ; cd ..

%/$(OUT_SAVED): %/Makefile $(TRAINING_DATA) $(TEST_DATA)
	@cd $(dir $@) ; make -s $(OUT_SAVED) ; cd ..

%/Makefile: $(MAKEFILE_TST)
	@mkdir -p $(dir $@)
	@cd $(dir $@) ; ln -s ../$(MAKEFILE_TST) Makefile ; cd ..

%/$(SVM_PARAMS) : %/$(OUT_SAVED)
	@cd $(dir $@) ; make -s $(SVM_PARAMS) ; cd ..

%/$(BEST) : %/$(OUT_SAVED) %/$(SVM_PARAMS) $(TRAINING_DATA)
	@cd $(dir $@) ; make -s $(BEST) ; cd ..
	
%/$(KAPPA_BEST) : %/$(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@cd $(dir $@) ; make -s $(KAPPA_BEST) ; cd ..

%/$(ERROR_BEST) : %/$(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@cd $(dir $@) ; make -s $(ERROR_BEST) ; cd ..

$(TIME): $(TIMES)
	@echo "Combining the execution time of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		time="$$(eval tail -n 1 $$i/$(TIME))"; \
		echo "$$i $$time" >> time.tmp ; \
	done
	@awk '{ sumTime += $$2; sumTimeX2 += (($$2)^2); } END \
		  { avgTIME=sumTime/NR; \
		    stdDevTime=sqrt(sumTimeX2/(NR-1) - 2*avgTIME*(sumTime/(NR-1)) + ((NR*(avgTIME^2))/(NR-1))); \
		    printf "Avg %f\nStd %f\n", avgTIME, stdDevTime}' time.tmp > stats.tmp
	@echo  "Test Time" > header.tmp
	@cat header.tmp time.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

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

$(RELEVANCE_M) : $(RELEVANCE_T) transpose.awk
	@cat $< | awk 'NR==1 {print; exit}' > $@.tmp
	@cat $< | awk 'NR>1 {for (i=2;i<=NF;i++){acc[i-1]+=$$i;}} END {printf "Relevance\t" ; for (i=1;i<NF;i++){printf "%f", acc[i]/(NR-1); printf "\t"};printf "\n"}' >> $@.tmp
	@cat $@.tmp | awk -f transpose.awk | column -t | awk 'NR == 1; NR > 1 {print $$0 | "sort -k2,2r -k1,1"}' > $@
	@rm $@.tmp

$(SPEARMAN_SCORE): $(RANKINGS)
	@echo "Obtaining the stability score based on the Spearman Metric ..."
	@$(JAVA) $(SPEARMAN_SCORER) $(N_FEATURES) $^ > $@
	@echo

$(RANKING_M): $(RELEVANCE_M)
	@echo "Ranking the features ..."
	@$(JAVA) $(FS_RANKER) $(N_FEATURES) $< > $@
	@echo

$(KAPPA_BEST): $(KAPPA_BESTS)
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
	
kappa_best_knn.data : $(KAPPA_BESTS_KNN)
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

kappa_best_ldaknn.data : $(KAPPA_BESTS_LDAKNN)
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
	
kappa_best_nbc.data : $(KAPPA_BESTS_NBC)
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
	
kappa_best_ldanbc.data : $(KAPPA_BESTS_LDANBC)
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

$(ERROR_BEST): $(ERROR_BESTS)
	@echo "Combining the solutions with best error rate of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumErrorTr  += $$2; sumErrorTrX2  += (($$2)^2); \
	        sumErrorTst += $$3; sumErrorTstX2 += (($$3)^2); \
			sumNF       += $$4; sumNFX2       += (($$4)^2); } END \
		  { avgErrorTr=sumErrorTr/NR; \
		    avgErrorTst=sumErrorTst/NR; \
		    avgNF=sumNF/NR; \
		    stdDevErrorTr=sqrt(sumErrorTrX2/(NR-1) - 2*avgErrorTr*(sumErrorTr/(NR-1)) + ((NR*(avgErrorTr^2))/(NR-1))); \
		    stdDevErrorTst=sqrt(sumErrorTstX2/(NR-1) - 2*avgErrorTst*(sumErrorTst/(NR-1)) + ((NR*(avgErrorTst^2))/(NR-1))); \
		    stdDevNF=sqrt(sumNFX2/(NR-1) - 2*avgNF*(sumNF/(NR-1)) + ((NR*(avgNF^2))/(NR-1))); \
		    printf "Avg %f %f %f\nStd %f %f %f\n", avgErrorTr, avgErrorTst, avgNF, stdDevErrorTr, stdDevErrorTst, stdDevNF}' best.tmp > stats.tmp
	@echo  "Test ErrorTr ErrorTst nFeats" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo

$(SVM_PARAMS): $(SVM_PARAMSS)
	@echo "Combining the SVM parameters of all experiments ..."
	@for i in $(EXPERIMENTS) ; do \
		best="$$(eval tail -n 1 $$i/$@)"; \
		echo "$$i $$best" >> best.tmp ; \
	done
	@awk '{ sumC  += $$2; sumCX2  += (($$2)^2); \
	        sumGamma += $$3; sumGammaX2 += (($$3)^2); } END \
		  { avgC=sumC/NR; \
		    avgGamma=sumGamma/NR; \
		    stdDevC=sqrt(sumCX2/(NR-1) - 2*avgC*(sumC/(NR-1)) + ((NR*(avgC^2))/(NR-1))); \
		    stdDevGamma=sqrt(sumGammaX2/(NR-1) - 2*avgGamma*(sumGamma/(NR-1)) + ((NR*(avgGamma^2))/(NR-1))); \
		    printf "Avg %f %f\nStd %f %f\n", avgC, avgGamma, stdDevC, stdDevGamma}' best.tmp > stats.tmp
	@echo  "Test C Gamma" > header.tmp
	@cat header.tmp best.tmp stats.tmp | column -t > $@
	@rm *.tmp
	@echo


# Hay que ver cómo encontrar unos parámetros adecuados para SVM para cada iteración del bucle ...
$(KAPPA_ANALYSIS): $(RELEVANCE_M) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Kappa analysis of the best solutions ..."
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
	
.PHONY: restore
restore: $(MAKEFILES)
	@for e in $(EXPERIMENTS) ; do                 \
		make --no-print-directory -C $$e restore; \
	done

.PHONY: touch
touch: $(MAKEFILES) restore
	@for e in $(EXPERIMENTS) ; do                  \
		$(MAKE) --no-print-directory -C $$e touch; \
	done
	@touch -c $(TIME)
	@touch -c $(SVM_PARAMS) 
	@touch -c $(RELEVANCE)
	@touch -c $(RELEVANCE_T)
	@touch -c $(RELEVANCE_M)
	@touch -c $(RANKING_M)
	@touch -c $(SPEARMAN_SCORE)
	@touch -c $(KAPPA_BEST)
	@touch -c $(ERROR_BEST)
	@touch -c $(KAPPA_ANALYSIS)

.PHONY: clean
clean: stats_clean $(MAKEFILES)
	@for e in $(EXPERIMENTS) ; do               \
		make --no-print-directory -C $$e clean; \
	done
	
.PHONY: stats_clean
stats_clean:
	@$(RM) $(ECJ_FS_POPS_PARAMS) $(ECJ_RUN_PARAMS) *.tmp $(TIME) $(RELEVANCES) $(RELEVANCE) $(RELEVANCE_T) $(RELEVANCE_M) $(RANKING_M) $(SPEARMAN_SCORE) $(KAPPA_BEST) $(ERROR_BEST) $(KAPPA_ANALYSIS)

.PHONY: distclean
distclean: stats_clean
	@$(RM) $(EXPERIMENTS) $(TRAINING_DATA) $(TEST_DATA) $(TIME) $(SVM_PARAMS)

