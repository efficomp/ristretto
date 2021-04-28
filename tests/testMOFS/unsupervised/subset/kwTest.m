%
% This file is part of Ristretto.
%
% Ristretto is free software: you can redistribute it and/or modify it under
% the terms of the GNU General Public License as published by the Free
% Software Foundation, either version 3 of the License, or (at your option)
% any later version.
%
% Ristretto is distributed in the hope that it will be useful, but WITHOUT
% ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
% FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
% more details.
%
% You should have received a copy of the GNU General Public License along with
% Ristretto. If not, see <http://www.gnu.org/licenses/>.
%
% This work was supported by project TIN2015-67020-P (Spanish "Ministerio de
% Economía y Competitividad"), and by the European Regional Development Fund
% (ERDF).
%
% Copyright (c) 2018, EFFICOMP
%

% Applies a 1-way ANOVA test to check the homoscedasticity of some
% experiments results
%
% Parameters
% resultsFile Results of the experiments
% outputFile  File to output the results of the test

function anovaTest(resultsFile, outputFile)

[expNames, expResults] = loadResults(resultsFile);

[pValue, ~, stats] = kruskalwallis(expResults);

file = fopen(outputFile,'w');
if (file<0)
    error('Could not open the output file');
end

% prints the p-value
fprintf(file,'p-value\t%.6f\n\n',pValue);

% multiple comparisons
c = multcompare(stats);
comparison= [expNames(c(:,1)), expNames(c(:,2)), num2cell(c(:,3:6))];

fprintf(file, 'Opt1\tOpt2\tp-value\n');
for i=1:size(comparison,1)
    fprintf(file, '%s\t%s\t%.6f\n', comparison{i,1}, comparison{i,2}, comparison{i,size(c,2)});
end

fclose(file);
