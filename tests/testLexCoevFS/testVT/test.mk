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

# Parameters
BASE           = ..
PARAMETERS     = ../../params.mk

.PHONY: all
all : out stats

# Include the parameters for the experiments
include $(PARAMETERS)

# Adapt paths of files in upper folders
TRAINING_DATA := ../$(TRAINING_DATA)
TEST_DATA     := ../$(TEST_DATA)

# Results
BEST       = best.data

.SECONDARY: $(OUT) $(OUT_SAVED) $(TIME) $(SVM_PARAMS) $(RELEVANCE) $(RANKING) $(KAPPA_BEST) $(ERROR_BEST)
.INTERMEDIATE: $(ECJ_FS_POPS_PARAMS) $(ECJ_RUN_PARAMS) $(BEST)

.PHONY: out
out: $(OUT) $(OUT_SAVED) $(SVM_PARAMS)

$(OUT_SAVED): $(OUT)
	cp $< $@

.PHONY: stats
stats: $(RANKING) $(KAPPA_BEST) $(ERROR_BEST) kappa_best_knn.data kappa_best_ldaknn.data kappa_best_nbc.data kappa_best_ldanbc.data

$(OUT): $(ECJ_RUN_PARAMS) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Starting ECJ in localhost ..."
	@$(TIMER) java $(ECJ) -file $(ECJ_RUN_PARAMS)
	@echo

$(SVM_PARAMS): $(OUT_SAVED)
	@echo "Extracting the SVM parameters ..."
	@echo "C Gamma" > $@.tmp
	@SVM_C=$$(eval "tail -3 $< | head -1 | cut -d' ' -f2-"); \
	SVM_gamma=$$(eval "tail -2 $< | head -1 | cut -d' ' -f2-"); \
	echo $$SVM_C $$SVM_gamma >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp
	@echo

$(RELEVANCE): $(OUT_SAVED)
	@echo "Extracting the relevant features ..."
	@echo  "Feature  Relevance" > $@.tmp
	@sel_feats=$$(eval "tail -1 $< | cut -d' ' -f2-"); \
	for feat in $$sel_feats; do \
		echo $$feat 1.0 >> $@.tmp ; \
	done
	@cat $@.tmp | column -t > $@
	@rm *.tmp
	@echo

$(RANKING): $(RELEVANCE)
	@echo "Ranking the features ..."
	@$(JAVA) $(FS_RANKER) $(N_FEATURES) $< > $@
	@echo

$(BEST): $(OUT_SAVED) $(SVM_PARAMS) $(TRAINING_DATA)
	@echo "Applying cross-valitation to the best solution found ..."
	@echo  "XValError nFeats" > $@.tmp
	@sel_feats=$$(eval "tail -1 $< | cut -d' ' -f2-"); \
	if [ -n "$$sel_feats" ] ; then \
		xValError="$$(eval $(JAVA) $(FS_VALIDATOR) $(TRAINING_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
		n_feats=$$(eval "echo $$sel_feats | wc -w") ; \
		echo -e $$xValError $$n_feats $$sel_feats >> $@.tmp; \
	fi
	@cat $@.tmp | column -t > $@
	@rm *.tmp
	@echo

$(KAPPA_BEST) : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo

kappa_best_knn.data : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution using knn ..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) knn $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo

kappa_best_ldaknn.data : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution using knn ..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) applyLDA knn $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo

kappa_best_nbc.data : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution using knn ..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) nbc $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo

kappa_best_ldanbc.data : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the Kappa indexes for the best solution using knn ..."
	@echo  "KappaTr KappaTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	kappas="$$(eval $(JAVA) $(KAPPA_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) applyLDA nbc $$sel_feats)"; \
	echo -e $$kappas $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo


$(ERROR_BEST) : $(BEST) $(TRAINING_DATA) $(TEST_DATA)
	@echo "Calculating the error rate for the best solution..."
	@echo  "ErrorTr ErrorTst nFeats" > $@.tmp
	@n_feats=$$(eval "cat $< | tail -n1 | tr -s ' ' | cut -d ' ' -f 2") ; \
	sel_feats=$$(eval "cat best.data | tail -n1 | tr -s ' ' | cut -d ' ' -f 1,2 --complement") ; \
	errors="$$(eval $(JAVA) $(ERROR_VALIDATOR) $(TRAINING_DATA) $(TEST_DATA) $(CLASS_INDEX) $(APPLY_LDA) $(CLASSIFIER) $$sel_feats)"; \
	echo -e $$errors $$n_feats $$sel_feats >> $@.tmp
	@cat $@.tmp | column -t > $@
	@rm *.tmp	
	@echo

.PHONY: restore
restore:
	@cp $(OUT_SAVED) $(OUT)

.PHONY: touch
touch: restore
	@touch -c $(OUT)
	@touch -c $(OUT_SAVED)
	@touch -c $(SVM_PARAMS)
	@touch -c $(RELEVANCE)
	@touch -c $(TIME)
	@touch -c $(BEST)
	@touch -c $(RANKING)
	@touch -c $(KAPPA_BEST)
	@touch -c kappa*
	@touch -c $(ERROR_BEST)

# Cleaning
.PHONY: clean
clean:
	@echo "Cleaning ..."
	@$(RM) *~ $(ECJ_FS_POPS_PARAMS) $(ECJ_RUN_PARAMS) $(BEST) $(RELEVANCE) $(SVM_PARAMS) $(RANKING) $(KAPPA_BEST) $(ERROR_BEST)
	@echo

.PHONY: distclean
distclean: clean
	@$(RM) $(OUT) $(OUT_SAVED) $(TIME)

