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
# Test of the Feature Selection problem for ECJ
#

# A bash shell will be used for the recipes
SHELL          = /bin/bash

# Include the parameters for the experiments
include ../../../../params.mk
include ../../params.mk
include ../params.mk

# Adapt paths of files in upper folders
TRAINING_DATA := ../../../../$(TRAINING_DATA)
TEST_DATA     := ../../../../$(TEST_DATA)

PARAMETERS = ../../../../params.mk ../../params.mk ../params.mk
ECJ_PARAMETERS = ../../../../ecj.params ../../../ecj.params ../../ecj.params ../ecj.params


# Apps
ECJ            = ec.Evolve
ECJ_MASTER     = ec.Evolve
ECJ_SLAVE      = ec.eval.Slave

# Results
FRONT          = front.stat
FRONT_STATS    = front_stats.data
TIME           = time.stat
BEST           = best.data
KAPPA_FRONT_BEST = kappa_front_best.data
SENS_FRONT_BEST = sens_front_best.data
SPEC_FRONT_BEST = spec_front_best.data
FRONT_XVAL     = front_xval.data
RELEVANCE      = relevance.data
RANKING        = ranking.data
SEL_FEATS      = selected.data
FRONT_PLOT     = $(FRONT:.stat=.png)
RELEVANCE_PLOT = $(RELEVANCE:.data=.png)
PLOTS          = $(FRONT_PLOT) $(RELEVANCE_PLOT)

# ParetoFront analysis
FRONT_ANALYZER = ristretto.tools.FSParetoFrontAnalyzer

# Ranking of features
FS_RANKER = ristretto.tools.FSRanker

# Feature Selection Validators
FS_VALIDATOR    = ristretto.tools.FSCrossValidator
KAPPA_VALIDATOR = ristretto.tools.FSKappaValidator
SENS_VALIDATOR = ristretto.tools.FSSensitivityValidator
SPEC_VALIDATOR = ristretto.tools.FSSpecificityValidator

# Tools
JAVA  = java
RM    = rm -rf
TIMER = /usr/bin/time -f "%e" -o $(TIME)

.PHONY: all
all : front stats

.PHONY: front
front: $(FRONT_STATS)

.PHONY: stats
stats: $(KAPPA_ANALYSIS) $(FRONT_XVAL) $(BEST) $(RANKING) $(KAPPA_FRONT_BEST)


../../../../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/generations.*/generations = $(GENERATIONS)/g" $@
	@sed -i "s/pop.subpop.0.size.*/pop.subpop.0.size = $(POP_SIZE)/g" $@
	@sed -i "s/eval.problem.dataset.class-index.*/eval.problem.dataset.class-index = $(CLASS_INDEX)/g" $@
		
../../../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/eval.problem.clusterer.num-centroids.*/eval.problem.clusterer.num-centroids = $(NUM_CENTROIDS)/g" $@
	@sed -i "s/eval.problem.perc-validation.*/eval.problem.perc-validation = $(PERC_VALIDATION)/g" $@

../../ecj.params: $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/fs.subset.species.n-features.*/fs.subset.species.n-features = $(N_FEATURES)/g" $@
	@sed -i "s/fs.subset.species.min-size.*/fs.subset.species.min-size = $(MIN_N_FEATS)/g" $@
	@sed -i "s/fs.subset.species.max-size.*/fs.subset.species.max-size = $(MAX_N_FEATS)/g" $@

$(SLAVE_PARAMS): ../../../../../common/ecj/$(SLAVE_PARAMS)
	@cp $< $@
	@sed -i "s/evalthreads.*/evalthreads = $(THREADS_PER_CPU)/g" $@

$(MASTER_PARAMS): ../../../../../common/ecj/$(MASTER_PARAMS) $(PARAMETERS)
	@cp $< $@
	@echo "Parametrizing $@ ..."
	@ping -c 1 $(SERVER_NODE) > /dev/null 2>&1 ;              \
	if [ $$? -eq 0 ] ;                                        \
	then                                                      \
		maxjobsize=$$(( $(POP_SIZE) / ($(N_SLAVES)*$(CPUS_PER_NODE)) ));         \
	else                                                      \
		maxjobsize=$(POP_SIZE);                               \
	fi ;                                                      \
	if [ $$maxjobsize -lt 1 ] ; then                          \
		maxjobsize=1;                                         \
	fi;                                                       \
	jobsize=$$(( $(NETWORK_PACKET_SIZE) / $(IND_MAX_SIZE) )); \
	if [ $$jobsize -lt 1 ] ; then                             \
		jobsize=1;                                            \
	elif [ $$jobsize -gt $$maxjobsize ] ; then                \
		jobsize=$$maxjobsize;                                 \
	fi;                                                       \
	maxjobsperslave=3;                                        \
	jobsize=$$(( $$jobsize / $$maxjobsperslave ));            \
	sed -i "s/eval.masterproblem.job-size.*/eval.masterproblem.job-size = $$jobsize/g" $@ ; \
	sed -i "s/eval.masterproblem.max-jobs-per-slave.*/eval.masterproblem.max-jobs-per-slave = $$maxjobsperslave/g" $@

.PHONY: plot
plot: $(PLOTS)
#	@for plot in $(PLOTS); do (display $$plot &) ; done

$(RELEVANCE_PLOT): $(RELEVANCE)
	@echo 'set term png' > relevance.plot
	@echo 'set xlabel "Features"' >> relevance.plot
	@echo 'set ylabel "Relevance"' >> relevance.plot
	@echo 'set output "$@"' >> relevance.plot
	@echo 'set boxwidth 0.75' >> relevance.plot
	@echo 'set style data histogram' >> relevance.plot
	@echo 'set style fill solid border -1' >> relevance.plot
#	@echo 'set title "Features relevance"' >> relevance.plot
	@echo 'unset key' >> relevance.plot
	@echo 'plot "$<" using 2:xtic(1) with boxes lc rgb "grey" notitle, $(RELEVANCE_TH) with lines lc rgb "black"' >> relevance.plot
	@gnuplot relevance.plot
	@rm relevance.plot

$(RELEVANCE): $(FRONT)
	@echo "Analyzing the Pareto front ..."
	@sort $< | uniq > $<.tmp
	@$(JAVA) $(FRONT_ANALYZER) $(IND_TYPE) $<.tmp | column -t | awk 'NR == 1; NR > 1 {print $$0 | "sort -k2,2r -k1,1"}' > $@
	@rm *.tmp
	@echo
	
$(RANKING): $(RELEVANCE)
	@echo "Ranking the features ..."
	@$(JAVA) $(FS_RANKER) $(N_FEATURES) $< > $@
	@echo

$(FRONT_PLOT): $(FRONT)
	@echo 'set term png' > front.plot
	@echo 'set xlabel "CVI 1"' >> front.plot
	@echo 'set ylabel "CVI 2"' >> front.plot
	@echo 'set output "$@"' >> front.plot
#	@echo 'set xrange [0:1]' >> front.plot
#	@echo 'set yrange [0:1]' >> front.plot
	@echo 'set key bmargin' >> front.plot
	@echo 'plot "$<" using 2:3 with points notitle' >> front.plot
	@gnuplot front.plot
	@rm front.plot

.PHONY: frontSeq
frontSeq: $(ECJ_PARAMETERS) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Starting ECJ in localhost ..."
	@$(TIMER) java $(ECJ) -file $(SEQ_PARAMS)

.PHONY: frontMS
frontMS: $(ECJ_PARAMETERS) $(TRAINING_DATA) $(TEST_DATA)
	@make -s slave.params master.params
	$(eval TESTPATH=$(shell pwd))
	@for node in $(SLAVE_NODES) ; do \
		for cpu in {1..$(CPUS_PER_NODE)} ; do \
			echo "Starting slave in cpu $$cpu of node $$node ..." ; \
			ssh -f $$node "cd $(TESTPATH) ; java $(ECJ_SLAVE) -file $(SLAVE_PARAMS) -p eval.master.host=$(SERVER_NODE) -p eval.slave.name=slave-$$node-$$cpu" ; \
		done ; \
	done
	echo "Starting server in node $(SERVER_NODE) ..."
	ssh $(SERVER_NODE) "cd $(TESTPATH) ; $(TIMER) java $(ECJ_MASTER) -file $(MASTER_PARAMS)"

$(FRONT): $(ECJ_PARAMETERS) $(TRAINING_DATA) $(TEST_DATA)
	@$(RM) slave.params master.params
	@ping -c 1 $(SERVER_NODE) > /dev/null 2>&1 ; \
	if [ $$? -eq 0 ] ;                           \
	then                                         \
		make -s frontMS;                         \
	else                                         \
		make -s frontSeq;                        \
	fi
	@echo

$(SEL_FEATS) : $(RELEVANCE)
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

$(TH_ANALYSIS): $(RELEVANCE) $(TEST_DATA)
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

$(KAPPA_ANALYSIS): $(RELEVANCE) $(TRAINING_DATA) $(TEST_DATA)
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

$(SENS_ANALYSIS): $(RELEVANCE) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Sensitivity analysis of the best pareto front solutions ..."
	@echo  "Relevance SensTr SensTst nFeats" > $@.tmp
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

$(SPEC_ANALYSIS): $(RELEVANCE) $(TRAINING_DATA) $(TEST_DATA)
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

$(FRONT_STATS): $(FRONT)
	@echo "Kappa front statistics ..."
	@sort $< | uniq > $<.tmp
	@line=$$(head -n 1 $<.tmp) ; \
	n_words=$$(eval wc -w <<< "$$line"); \
	n_obj=$$(eval echo $$n_words - 1 | bc); \
	header="" ; \
	for ((i=0;i < n_obj;i++)) ; do header+="Obj$$i " ; done ; \
	header+="nFeats"; \
	echo $$header > $@.tmp;
	@while IFS='' read -r line || [[ -n "$$line" ]]; do \
		l=$$(eval "echo '$$line' | tr '|i' ' '") ; \
		vector=($$l) ; \
		sel_feats=""; \
		type=$$(eval "echo $(IND_TYPE) | tr [a-z] [A-Z]") ; \
		if [ $$type = "SUBSET" ] ; then \
			n_feats=$$(eval "echo $${vector[0]}") ; \
			for ((i=1;i <= n_feats;i++)) ; do \
				sel_feats+=$$(eval "echo $${vector[i]}") ; \
				sel_feats+=' '; \
			done ; \
		else \
			n_bits=$$(eval "echo $${vector[0]}") ; \
			bits=$$(eval "echo $${vector[1]}") ; \
			for ((i=0;i<n_bits;i++)) ; do \
				c="$${bits:i:1}"; \
				if [ $$c = "T" ] ; then \
					sel_feats+=$$i ; \
					sel_feats+=' '; \
				fi ; \
			done ; \
		fi ; \
		if [ -n "$$sel_feats" ] ; then \
			objs=$$(eval 'echo "$$line" | cut -f1 --complement'); \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			echo -e $$objs $$n_feats $$sel_feats >> $@.tmp; \
		fi \
	done < $<.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(FRONT_XVAL): $(FRONT) $(TRAINING_DATA)
	@echo "Applying cross-valitation to the solutions in the Pareto front..."
	@echo  "XValError nFeats" > $@.tmp
	@sort $< | uniq > $<.tmp
	@while IFS='' read -r line || [[ -n "$$line" ]]; do \
		l=$$(eval "echo '$$line' | tr '|i' ' '") ; \
		vector=($$l) ; \
		sel_feats=""; \
		type=$$(eval "echo $(IND_TYPE) | tr [a-z] [A-Z]") ; \
		if [ $$type = "SUBSET" ] ; then \
			n_feats=$$(eval "echo $${vector[0]}") ; \
			for ((i=1;i <= n_feats;i++)) ; do \
				sel_feats+=$$(eval "echo $${vector[i]}") ; \
				sel_feats+=' '; \
			done ; \
		else \
			n_bits=$$(eval "echo $${vector[0]}") ; \
			bits=$$(eval "echo $${vector[1]}") ; \
			for ((i=0;i<n_bits;i++)) ; do \
				c="$${bits:i:1}"; \
				if [ $$c = "T" ] ; then \
					sel_feats+=$$i ; \
					sel_feats+=' '; \
				fi ; \
			done ; \
		fi ; \
		if [ -n "$$sel_feats" ] ; then \
			xValError="$$(eval $(JAVA) $(FS_VALIDATOR) $(TRAINING_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			echo -e $$xValError $$n_feats $$sel_feats >> $@.tmp; \
		fi \
	done < $<.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(BEST): $(FRONT_XVAL)
	@echo "Selecting the best solution in the Pareto front..."
	@sort -k1,1 -k2,2 -n front_xval.data | head -n2 > $@

$(KAPPA_FRONT): $(FRONT) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Kappa analysis of the Pareto front..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@sort $< | uniq > $<.tmp
	@while IFS='' read -r line || [[ -n "$$line" ]]; do \
		l=$$(eval "echo '$$line' | tr '|i' ' '") ; \
		vector=($$l) ; \
		sel_feats=""; \
		type=$$(eval "echo $(IND_TYPE) | tr [a-z] [A-Z]") ; \
		if [ $$type = "SUBSET" ] ; then \
			n_feats=$$(eval "echo $${vector[0]}") ; \
			for ((i=1;i <= n_feats;i++)) ; do \
				sel_feats+=$$(eval "echo $${vector[i]}") ; \
				sel_feats+=' '; \
			done ; \
		else \
			n_bits=$$(eval "echo $${vector[0]}") ; \
			bits=$$(eval "echo $${vector[1]}") ; \
			for ((i=0;i<n_bits;i++)) ; do \
				c="$${bits:i:1}"; \
				if [ $$c = "T" ] ; then \
					sel_feats+=$$i ; \
					sel_feats+=' '; \
				fi ; \
			done ; \
		fi ; \
		if [ -n "$$sel_feats" ] ; then \
			kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp; \
		fi \
	done < $<.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp


$(SENS_FRONT): $(FRONT) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Sensitivity analysis of the Pareto front..."
	@echo  "SensTr SensTst nFeats" > $@.tmp
	@sort $< | uniq > $<.tmp
	@while IFS='' read -r line || [[ -n "$$line" ]]; do \
		l=$$(eval "echo '$$line' | tr '|i' ' '") ; \
		vector=($$l) ; \
		sel_feats=""; \
		type=$$(eval "echo $(IND_TYPE) | tr [a-z] [A-Z]") ; \
		if [ $$type = "SUBSET" ] ; then \
			n_feats=$$(eval "echo $${vector[0]}") ; \
			for ((i=1;i <= n_feats;i++)) ; do \
				sel_feats+=$$(eval "echo $${vector[i]}") ; \
				sel_feats+=' '; \
			done ; \
		else \
			n_bits=$$(eval "echo $${vector[0]}") ; \
			bits=$$(eval "echo $${vector[1]}") ; \
			for ((i=0;i<n_bits;i++)) ; do \
				c="$${bits:i:1}"; \
				if [ $$c = "T" ] ; then \
					sel_feats+=$$i ; \
					sel_feats+=' '; \
				fi ; \
			done ; \
		fi ; \
		if [ -n "$$sel_feats" ] ; then \
			sens="$$(eval $(JAVA) $(SENS_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			echo -e $$sens $$n_feats $$sel_feats >> $@.tmp; \
		fi \
	done < $<.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp


$(SPEC_FRONT): $(FRONT) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Specificity analysis of the Pareto front..."
	@echo  "SpecTr SpecTst nFeats" > $@.tmp
	@sort $< | uniq > $<.tmp
	@while IFS='' read -r line || [[ -n "$$line" ]]; do \
		l=$$(eval "echo '$$line' | tr '|i' ' '") ; \
		vector=($$l) ; \
		sel_feats=""; \
		type=$$(eval "echo $(IND_TYPE) | tr [a-z] [A-Z]") ; \
		if [ $$type = "SUBSET" ] ; then \
			n_feats=$$(eval "echo $${vector[0]}") ; \
			for ((i=1;i <= n_feats;i++)) ; do \
				sel_feats+=$$(eval "echo $${vector[i]}") ; \
				sel_feats+=' '; \
			done ; \
		else \
			n_bits=$$(eval "echo $${vector[0]}") ; \
			bits=$$(eval "echo $${vector[1]}") ; \
			for ((i=0;i<n_bits;i++)) ; do \
				c="$${bits:i:1}"; \
				if [ $$c = "T" ] ; then \
					sel_feats+=$$i ; \
					sel_feats+=' '; \
				fi ; \
			done ; \
		fi ; \
		if [ -n "$$sel_feats" ] ; then \
			specs="$$(eval $(JAVA) $(SPEC_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
			n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
			echo -e $$specs $$n_feats $$sel_feats >> $@.tmp; \
		fi \
	done < $<.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(KAPPA_FRONT_BEST) : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	

$(SENS_FRONT_BEST) : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the sensitivity indexes for the best solution..."
	@echo  "SensTr SensTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	sens="$$(eval $(JAVA) $(SENS_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
	echo -e $$sens $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	

$(SPEC_FRONT_BEST) : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the specifificity indexes for the best solution..."
	@echo  "SpecTr SpecTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	specs="$$(eval $(JAVA) $(SPEC_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(TP_LABEL) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
	echo -e $$specs $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	

$(KAPPA_FRONT_BEST_TR) : $(KAPPA_FRONT)
	@echo "Selecting the solution with best training kappa..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@sort -k1 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(SENS_FRONT_BEST_TR) : $(SENS_FRONT)
	@echo "Selecting the solution with best training sensitivity..."
	@echo  "SensTr SensTst nFeats" > $@.tmp
	@sort -k1 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(SPEC_FRONT_BEST_TR) : $(SPEC_FRONT)
	@echo "Selecting the solution with best training specificity..."
	@echo  "SpecTr SpecTst nFeats" > $@.tmp
	@sort -k1 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(KAPPA_FRONT_BEST_TST) : $(KAPPA_FRONT)
	@echo "Selecting the solution with best test kappa..."
	@echo  "KappaTr KappaTst SensTr SensTst SpecTr SpecTst nFeats" > $@.tmp
	@sort -k2 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(SENS_FRONT_BEST_TST) : $(SENS_FRONT)
	@echo "Selecting the solution with best test sensitivity..."
	@echo  "SensTr SensTst nFeats" > $@.tmp
	@sort -k2 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

$(SPEC_FRONT_BEST_TST) : $(SPEC_FRONT)
	@echo "Selecting the solution with best test specificity..."
	@echo  "SpecTr SpecTst nFeats" > $@.tmp
	@sort -k2 -n $<  | tail -1 >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp

.PHONY: touch
touch:
	@touch -c $(FRONT)
	@touch -c $(FRONT_STATS)
	@touch -c $(RELEVANCE)
	@touch -c $(RANKING)
	@touch -c $(SEL_FEATS)
	@touch -c $(PLOTS)
	@touch -c $(FRONT_XVAL) $(BEST) $(KAPPA_FRONT_BEST)
	@touch -c $(KAPPA_FRONT) $(SENS_FRONT) $(SPEC_FRONT)
	@touch -c $(KAPPA_FRONT_BEST_TR) $(SENS_FRONT_BEST_TR) $(SPEC_FRONT_BEST_TR)
	@touch -c $(KAPPA_FRONT_BEST_TST) $(SENS_FRONT_BEST_TST)$(SPEC_FRONT_BEST_TST)
	@touch -c $(KAPPA_ANALYSIS) $(SENS_ANALYSIS) $(SPEC_ANALYSIS)
	@touch -c $(TIME)

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~ *.plot out.stat *.log $(FRONT_STATS) $(RELEVANCE) $(RANKING) $(SEL_FEATS) $(PLOTS) $(FRONT_XVAL) $(BEST) $(KAPPA_FRONT_BEST) $(KAPPA_FRONT) $(SENS_FRONT) $(SPEC_FRONT) $(KAPPA_FRONT_BEST_TR) $(SENS_FRONT_BEST_TR) $(SPEC_FRONT_BEST_TR) $(KAPPA_FRONT_BEST_TST) $(SENS_FRONT_BEST_TST)$(SPEC_FRONT_BEST_TST) $(KAPPA_ANALYSIS) $(SENS_ANALYSIS) $(SPEC_ANALYSIS) $(TIME)
	@echo

.PHONY: distclean
distclean: clean
	@$(RM) *.stat

