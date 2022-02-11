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
# Documentation generation
#

# A bash shell will be used for the recipes
SHELL = /bin/bash

# Tools
RM    = rm -rf

WINDOWTITLE = 'Ristretto API'
DOCTITLE = 'Ristretto API'
HEADER = '<b>Ristretto API'
BOTTOM = '<font size="-1"><div align=right> \
           Copyright &copy; 2016-2018 <a \
           href="https://efficomp.ugr.es">EFFICOMP</a>. \
           All rights reserved. <br><br>\
          </div><div align=justify> \
           This work was supported by project <i>Energy-aware High Performance \
           Multi-objective Optimization in Heterogeneous Computer Architectures. \
           Applications on Biomedical Engineering</i> (<a \
           href="https://atcproyectos.ugr.es/ehpmobe/">e-hpMOBE</a>), with \
           reference TIN2015-67020-P, funded by the Spanish <i> \
           <a href="https://www.ciencia.gob.es/">Ministerio de Economía y Competitividad</a></i>, \
           and by the <a \
           href="https://ec.europa.eu/regional_policy/en/funding/erdf/">\
           European Regional Development Fund (ERDF)</a>. <br> <br>\
          </div><div align=center> \
           <a href="https://www.ciencia.gob.es/"><img height="75" \
           src="https://raw.githubusercontent.com/efficomp/ristretto/main/docs/resources/mineco.png" \
           alt="Ministerio de Economía y Competitividad"></a> \
           &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; \
           <a href="https://ec.europa.eu/regional_policy/en/funding/erdf/">\
           <img height="75" src="https://raw.githubusercontent.com/efficomp/ristretto/main/docs/resources/erdf.png" \
           alt="European Regional Development Fund (ERDF)"></a> \
          </div></font>'

SRCDIR = ..
RISTRETTO = ristretto
TESTS = tests

.PHONY: all
all : $(SRC)
	@echo "Generating the documentation ..."
	@javadoc -sourcepath $(SRCDIR) \
	-subpackages $(RISTRETTO) -subpackages $(TESTS)       \
	-overview $(SRCDIR)/$(RISTRETTO)/overview.html \
        -splitIndex                   \
        -windowtitle $(WINDOWTITLE)   \
        -doctitle $(DOCTITLE)         \
        -header $(HEADER)             \
        -bottom $(BOTTOM)
	@echo

clean:
	@echo "Cleaning ..."
	@ls | grep -v Makefile | grep -v resources | xargs $(RM)
	@echo




