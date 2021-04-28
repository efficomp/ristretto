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

% Preprocesses a dataset
% Parameters
% datafile textfile with a tab separated dataset
% prop     proportion of the data used tor training (the rest will be for
%          testing)
%
function preprocessData(datafile, prop)

% Init the random generator
rng('shuffle', 'twister');

% Load the data
data = load(datafile);
inputs = data(:,1:size(data,2)-1)';
labels = data(:,size(data,2))';

% Normalize the inputs
minInputs = min(inputs,[],2);
maxInputs = max(inputs,[],2);
diffInputs = maxInputs - minInputs;
inputs = (inputs - repmat(minInputs, [1, size(inputs,2)])) ./ repmat(diffInputs, [1, size(inputs,2)]);

% Apply a random permutation to the dataset
perm = randperm(length(labels));
inputs = inputs(:,perm);
labels = labels(perm);

% Save the whole dataset
save(strrep(datafile,'.data','.mat'),'inputs','labels');
data = [inputs', labels'];
dlmwrite(datafile,data,'\t');

% split the data into training and test
classes = unique(labels);
nClasses = length(classes);
nSamples = size(inputs,2);

nTrain = 0;
for i=1:nClasses
    % obtain the inputs of the i-th class
    nSamplesClass = length(find (labels == 1));
    nTrain = nTrain + round(nSamplesClass*prop);
end

nTest = nSamples - nTrain;

trainInputs = zeros(size(inputs,1),nTrain);
trainLabels = zeros(1,nTrain);
testInputs = zeros(size(inputs,1),nTest);
testLabels = zeros(1,nTest);
offsetTrain = 1;
offsetTest = 1;
for i=1:nClasses
    % obtain the inputs of the i-th class
    classInputs = inputs(:,labels == classes(i));
    % perform a random permutation
    classInputs = classInputs(:,randperm(size(classInputs,2)));
    nSamplesClass = size(classInputs,2);
    nTrainClass = round(nSamplesClass*prop);
    indexes = offsetTrain:(offsetTrain+nTrainClass-1);
    trainInputs(:,indexes)=classInputs(:,1:nTrainClass);
    trainLabels(indexes) = repmat(classes(i),1,nTrainClass);
    offsetTrain = offsetTrain + length(indexes);
    nTestClass = nSamplesClass - nTrainClass;
    indexes = offsetTest:(offsetTest+nTestClass-1);
    testInputs(:,indexes)=classInputs(:,nTrainClass+1:nTrainClass+nTestClass);
    testLabels(indexes) = repmat(classes(i),1,nTestClass);
    offsetTest = offsetTest + length(indexes);
end

% Apply a random permutation to the training and test data
perm = randperm(length(trainLabels));
trainInputs = trainInputs(:,perm);
trainLabels = trainLabels(perm);
perm = randperm(length(testLabels));
testInputs = testInputs(:,perm);
testLabels = testLabels(perm);

% Save the training and test data
inputs = trainInputs;
labels = trainLabels;
save(strrep(datafile,'.data','_train.mat'),'inputs','labels');
data = [inputs', labels'];
dlmwrite(strrep(datafile,'.data','_train.data'),data,'\t');

inputs = testInputs;
labels = testLabels;
save(strrep(datafile,'.data','_test.mat'),'inputs','labels');
data = [inputs', labels'];
dlmwrite(strrep(datafile,'.data','_test.data'),data,'\t');
