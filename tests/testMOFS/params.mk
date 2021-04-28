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
CLASS_INDEX          = $(shell echo $(CLASS_INDEX_ORIG) + $(RND_FEATS) | bc)
TP_LABEL             = 1
N_FEATURES           = $(CLASS_INDEX)
TRAINING_DATA        = training.data
TEST_DATA            = test.data
RELEVANCE_TH         = 0.5
RELEVANCE_LIMIT      = 0.05
SEL_FEATS            = selected.data
CLASS_ERROR          = classification_error.data
KAPPA                = kappa.data
SENSITIVITY          = sensitivity.data
SPECIFICITY          = specificity.data
TH_ANALYSIS          = th_analysis.data
KAPPA_ANALYSIS       = kappa_analysis.data
KAPPA_FRONT          = kappa_front.data
KAPPA_FRONT_BEST_TR  = kappa_front_best_tr.data
KAPPA_FRONT_BEST_TST = kappa_front_best_tst.data
SENS_ANALYSIS        = sens_analysis.data
SENS_FRONT           = sens_front.data
SENS_FRONT_BEST_TR   = sens_front_best_tr.data
SENS_FRONT_BEST_TST  = sens_front_best_tst.data
SPEC_ANALYSIS        = spec_analysis.data
SPEC_FRONT           = spec_front.data
SPEC_FRONT_BEST_TR   = spec_front_best_tr.data
SPEC_FRONT_BEST_TST  = spec_front_best_tst.data
SPEARMAN_SCORE       = spearman_score.data
SPEARMAN_BEST_MEAN   = spearman_best_mean.data

# Experiments
FIRST_EXPERIMENT_INDEX = 0
LAST_EXPERIMENT_INDEX = 9

FIRST_COMPUTE_NODE = 0
LAST_COMPUTE_NODE = 17
ALL_NODES  = $(shell for ((N=$(FIRST_COMPUTE_NODE);N<=$(LAST_COMPUTE_NODE);N=N+1)) ; do echo compute-0-$$N ; done)

BUSY_NODES = compute-0-1  compute-0-5

COMPUTE_NODES = $(filter-out $(BUSY_NODES), $(ALL_NODES))

SERVER_NODE = $(word 1, $(COMPUTE_NODES))
SLAVE_NODES = $(filter-out $(SERVER_NODE), $(COMPUTE_NODES))
N_SLAVES = $(words $(SLAVE_NODES))
CPUS_PER_NODE = 16
THREADS_PER_CPU = 4

# ECJ
GENERATIONS = 100
POP_SIZE = 200
MIN_N_FEATS = 1
MAX_N_FEATS = 10
NETWORK_PACKET_SIZE = 1500

# Supervised
PERC_VALIDATION = 0.33

# Unsupervised
NUM_CENTROIDS = 3

