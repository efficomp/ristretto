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

% Loads the results of some experiments
%
% Parameters
% resultsFile Results of the experiments
%
% Returns
% expNames    Experiments names
% expResults  Experiments results

function [expNames, expResults] = loadResults(resultsFile)
file = fopen(resultsFile);

if (file<0)
    error('Could not open the results file');
end

% Obtain the data from file
line = fgetl(file);
expNames = [];
expResultsCell = [];
while ischar(line)
    tokens = strsplit(strtrim(line));
    expNames = [expNames; tokens(1)];
    data = tokens(2:length(tokens));
    expResultsCell = [expResultsCell, {data}];
    line = fgetl(file);
end
fclose(file);

% obtain the maximum number of results  por group
m = 0;
nExperiments = length(expResultsCell);
for i=1:nExperiments
    l=length(expResultsCell{i});
    if l>m
        m=l;
    end
end

% Conver the cell array in a matrix anf fill missing values with Nan
expResults = NaN(m,nExperiments);
for i=1:nExperiments
    data = str2double(expResultsCell{i})';
    expResults(1:length(data),i)=data;
end

