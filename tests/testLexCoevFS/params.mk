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
# Parameters for the experiments
#

# Parameters
TRAINING_DATA_ORIG   = ../../datasets/seeds/seeds_train.data
TEST_DATA_ORIG       = ../../datasets/seeds/seeds_test.data
CLASS_INDEX_ORIG     = 7
RND_FEATS            = 20
WRITE_CLASS          = true
CLASS_INDEX        = $(shell echo $(CLASS_INDEX_ORIG) + $(RND_FEATS) | bc)
N_FEATURES         = $(CLASS_INDEX)
TRAINING_DATA      = training.data
TEST_DATA          = test.data
CLASSIFIER         = svm
RELEVANCE_LIMIT    = 0.05

# Results
OUT        = out.stat
OUT_SAVED  = out.stat.saved
TIME       = time.stat
SVM_PARAMS = svm_params.data
RELEVANCE  = relevance.data
RANKING    = ranking.data
KAPPA_BEST = kappa_best.data
ERROR_BEST = error_best.data

# ECJ
ECJ                = ec.Evolve
ECJ_PARAMS         = $(BASE)/ecj.params
ECJ_SVM_PARAMS     = $(BASE)/svm.params
ECJ_FS_PARAMS      = $(BASE)/fs.params
ECJ_RUN_PARAMS     = $(BASE)/run.params
ECJ_FS_POPS_PARAMS = $(shell for ((pop=1;pop<$(SUBPOPS);pop=pop+1)) ; do echo $(BASE)/fs$$pop.params ; done)

# Feature Selection Validators
FS_VALIDATOR    = ristretto.tools.FSCrossValidator
KAPPA_VALIDATOR = ristretto.tools.FSKappaValidator
ERROR_VALIDATOR = ristretto.tools.FSErrorRateValidator

# Relevances analysis
RELEVANCES_ANALYZER = ristretto.tools.FSRelevancesAnalyzer

# Ranking of features
FS_RANKER = ristretto.tools.FSRanker

# Stability scores
SPEARMAN_SCORER  = ristretto.tools.FSStabilitySpearmanScorer

# Random feature adder
RND_ADDER      = ristretto.tools.RandomFeatureAdder

# Tools
JAVA  = java
RM    = rm -rf
TIMER = /usr/bin/time -f "%e" -o $(TIME)

# Experiments
FIRST_EXPERIMENT_INDEX = 0
LAST_EXPERIMENT_INDEX = 9

FIRST_COMPUTE_NODE = 0
LAST_COMPUTE_NODE = 17
ALL_NODES  = $(shell for ((N=$(FIRST_COMPUTE_NODE);N<=$(LAST_COMPUTE_NODE);N=N+1)) ; do echo compute-0-$$N ; done)

BUSY_NODES = compute-0-0 compute-0-4

COMPUTE_NODES = $(filter-out $(BUSY_NODES), $(ALL_NODES))

# ECJ
GENERATIONS = 300
POP_SIZE = 150
CLASSIFIER_POP_SIZE = 150
SUBPOPS = 4

# Rules tu update the ecj parameters file

$(ECJ_PARAMS): $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/generations.*/generations = $(GENERATIONS)/g" $@
	@sed -i "s/pop.subpops.*/pop.subpops = $(SUBPOPS)/g" $@
	@sed -i "s/eval.problem.dataset.class-index.*/eval.problem.dataset.class-index = $(CLASS_INDEX)/g" $@
	@echo
	
$(ECJ_SVM_PARAMS): $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/pop.subpop.0.size.*/pop.subpop.0.size = $(CLASSIFIER_POP_SIZE)/g" $@
	@echo

$(ECJ_FS_PARAMS): $(PARAMETERS)
	@echo "Parametrizing $@ ..."
	@sed -i "s/pop.subpop.1.size.*/pop.subpop.1.size = $(POP_SIZE)/g" $@
	@sed -i "s/pop.subpop.1.species.n-features.*/pop.subpop.1.species.n-features = $(N_FEATURES)/g" $@
	@sed -i "s/pop.subpop.1.species.min-size.*/pop.subpop.1.species.min-size = 0/g" $@
	@sed -i "s/pop.subpop.1.species.max-size.*/pop.subpop.1.species.max-size = $(N_FEATURES)/g" $@
	@sed -i "s/pop.subpop.1.species.min-feature.*/pop.subpop.1.species.min-feature = 0/g" $@
	@sed -i "s/pop.subpop.1.species.max-feature.*/pop.subpop.1.species.max-feature = $(N_FEATURES)/g" $@
	@echo

$(BASE)/fs%.params : $(ECJ_FS_PARAMS)
	@echo "Generating $@ ..."
	@pop=$(patsubst fs%.params,%,$(notdir $@)); \
	cp $< $@ ; \
	sed -i "s/pop.subpop.1/pop.subpop.$$pop/g" $@ ; \
	max_n_feats=$$(( $(N_FEATURES) / ($(SUBPOPS) - 1) )); \
	remaining=$$(( $(N_FEATURES) % ($(SUBPOPS) - 1) )); \
	if [ $$remaining -gt 0 ] ; then \
		max_n_feats=$$((max_n_feats+1)); \
	fi; \
	min_feat=$$((max_n_feats * (pop -1))); \
	max_feat=$$((min_feat + max_n_feats - 1)); \
	if [ $$max_feat -ge $(N_FEATURES) ] ; then \
		 max_feat=$$(( $(N_FEATURES) - 1 )); \
		 max_n_feats=$$(( $(N_FEATURES) - min_feat )); \
	fi; \
	sed -i "s/pop.subpop.$$pop.species.min-size.*/pop.subpop.$$pop.species.min-size = 0/g" $@ ; \
	sed -i "s/pop.subpop.$$pop.species.max-size.*/pop.subpop.$$pop.species.max-size = $$max_n_feats/g" $@ ; \
	sed -i "s/pop.subpop.$$pop.species.min-feature.*/pop.subpop.$$pop.species.min-feature = $$min_feat/g" $@ ; \
	sed -i "s/pop.subpop.$$pop.species.max-feature.*/pop.subpop.$$pop.species.max-feature = $$max_feat/g" $@ ;
	@echo

$(ECJ_RUN_PARAMS): $(ECJ_PARAMS) $(ECJ_SVM_PARAMS) $(ECJ_FS_POPS_PARAMS)
	@echo "Generating $@ ..."
	@cp $(ECJ_PARAMS) $@
	@echo >> $@
	@echo "# The first subpopulation will evolve the classifier" >> $@
	@cat $(ECJ_SVM_PARAMS) >> $@
	@echo >> $@
	@echo "# The remaining subpopulations will evolve the features" >> $@
	@for file in $(ECJ_FS_POPS_PARAMS) ; do \
		cat $$file >> $@ ; \
	done
	@echo

