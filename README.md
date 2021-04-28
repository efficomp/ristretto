# Ristretto

Ristretto is a [Java](https://www.java.com) package intended to solve feature selection problems. It is based on [ECJ](https://cs.gmu.edu/~eclab/projects/ecj/) and [Java-ML](http://java-ml.sourceforge.net/).

It provides an individual representation based on subsets of selected features and specific mutation and crossover operators for this representation. It also provides [NSGA-2](https://ieeexplore.ieee.org/document/996017)-based multi-objective algorithms for supervised and unsupervised problems and a lexicographic co-evolutionary many-objective algorithm for supervised problems, able to optimize simultaneously the parameters of a classifier while the most relevant features are also being selected.

## Requirements

Ristretto requires Java SE 7 or above. It also depends on the following Java libraries:

* [ECJ](https://cs.gmu.edu/~eclab/projects/ecj/),
* [Java-ML](http://java-ml.sourceforge.net/),
* [Apache Commons Math](https://commons.apache.org/proper/commons-math/), and
* [LIBSVM](https://www.csie.ntu.edu.tw/~cjlin/libsvm/), if SVM classifiers are desired

Some tests also make use of [gnuplot](http://www.gnuplot.info/) to show their results graphically and [MATLAB](https://www.mathworks.com/products/matlab.html) to make some statistics.

## Documentation

Ristretto is fully documented in its [github-pages](https://efficomp.github.io/ristretto/). You can also generate its docs from the source code. Simply change directory to the `docs` subfolder and type in `make`.

## Usage

The `tests` subfolder contains several examples that show the basic usage of Ristretto.

## Publications

* J. González, J. Ortega, M. Damas, P. Martín-Smith, J. Q. Gan. *A new multi-objective wrapper method for feature selection – Accuracy and stability analysis for BCI*, **Neurocomputing**, 333:407-418, 2019. https://doi.org/10.1016/j.neucom.2019.01.017
* J. González, J. Ortega, M. Damas, P. Martín-Smith. *Many-Objective Cooperative Co-evolutionary Feature Selection: A Lexicographic Approach*.  In: I. Rojas, G. Joya, A. Catala (eds) **Advances in Computational Intelligence. IWANN 2019**. Lecture Notes in Computer Science, vol 11507. Springer, Cham. https://doi.org/10.1007/978-3-030-20518-8_39

## Acknowledgments

This work was supported by project *Energy-aware High Performance Multi-objective Optimization in Heterogeneous Computer Architectures. Applications on Biomedical Engineering* ([e-hpMOBE](https://atcproyectos.ugr.es/ehpmobe/)), with reference TIN2015-67020-P, funded by the Spanish *[Ministerio de Economía y Competitividad](https://www.ciencia.gob.es/)*, and by the [European Regional Development Fund (ERDF)](https://ec.europa.eu/regional_policy/en/funding/erdf/)**.**

<div style="text-align: center">
  <a href="https://www.ciencia.gob.es/">
    <img height="75" src="https://raw.githubusercontent.com/efficomp/ristretto/main/docs/resources/mineco.png" alt="Ministerio de Economía y Competitividad">
  </a> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; 
  <a href="https://ec.europa.eu/regional_policy/en/funding/erdf/">
    <img height="75" src="https://raw.githubusercontent.com/efficomp/ristretto/main/docs/resources/erdf.png" alt="European Regional Development Fund (ERDF)">
  </a>
</div>

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.md) © 2015-2018 [EFFICOMP](https://atcproyectos.ugr.es/efficomp/).