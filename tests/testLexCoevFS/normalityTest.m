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

% Applies a Kolmogorov-Smirnov test to check the normality of some
% experiments results
%
% Parameters
% resultsFile Results of the experiments
% outputFile  File to output the results of the test

function normalityTest(resultsFile, outputFile)

[expNames, expResults] = loadResults(resultsFile);

file = fopen(outputFile,'w');

if (file<0)
    error('Could not open the output file');
end
fprintf(file,'Experiment\tp-value\n');
for i=1:length(expNames)
    data = expResults(:,i);
    name = expNames{i};
    m = nanmean(data);
    sd = nanstd(data);
    normData = data-m;
    if sd ~=0
        normData = normData/sd;
    end
    [~,pValue] = kstest(normData);
    fprintf(file,'%s\t%.6f\n',name,pValue);
end

fclose(file);
