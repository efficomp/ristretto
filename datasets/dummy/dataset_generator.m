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

% number of samples in each cluster
n = 150;

% number of clusters
n_clusters = 3;

% number of features
n_features = 2;

% non-repeatability for random numbers
rng shuffle;

% parameters for the clusters
Mu = {[13 ; 18], [8 ; 7], [20 ; 11]};
Cov = {[5 -1; -2 2] , [3 0; 0 3] , [1 3 ; 0 5]};

train = zeros(n_features, n*n_clusters);
test = zeros(n_features, n*n_clusters);
labels = zeros(1, n*n_clusters);
for i=1:n_clusters
    train(:,(i-1)*n+1:i*n) = generate_cluster(Mu{i}, Cov{i}, n);
    test(:,(i-1)*n+1:i*n) = generate_cluster(Mu{i}, Cov{i}, n);
    labels(:,(i-1)*n+1:i*n) = ones(1,n).*i;
end

trainFile = fopen ('train.data','w');
testFile = fopen ('test.data','w');

for i=1:size(train,2)
    for j=1:size(train,1)
        fprintf(trainFile,'%.6f\t', train(j,i));
        fprintf(testFile,'%.6f\t', test(j,i));
    end
    fprintf(trainFile,'%d\n', labels(i));
    fprintf(testFile,'%d\n', labels(i));
end
fclose(trainFile);
fclose(testFile);

fileID = fopen ('dataset.info','w');
fprintf(fileID,'Synthetic dataset\n\n');
fprintf(fileID,'Number of features: %d\n', n_features);
fprintf(fileID,'Number of clusters: %d\n', n_clusters);

fprintf(fileID,'\nCentroids of the clusters:\n');

for i=1:n_clusters
    fprintf(fileID,'Cluster %d:', i);
    for j = 1:n_features
        fprintf(fileID, ' %2.6f', Mu{i}(j));
    end
    fprintf(fileID,'\n');
end

fprintf(fileID,'\nNumber of train patterns: %d', n);
fprintf(fileID,'\nNumber of test patterns: %d\n', n);
fclose(fileID);

save('dataset.mat','train','test','labels');
