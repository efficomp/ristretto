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

% Generates a cluster of points
% Parameters
%  Mu  Mean of ehr cluster
%  Cov Covariance matrix
%  n   Number of points   
function cluster = generate_cluster(Mu, Cov, n)

if ~isvector(Mu)
    error('Mu must be a vector');
end

[rowsMu colsMu] = size(Mu);

if rowsMu < colsMu
    error('Mu must be a column vector');
end

if ~ismatrix(Cov)
    error('Cov must be a matrix');
end

[rowsCov colsCov] = size(Cov);

if rowsCov ~= colsCov
    error('Cov must be a square matrix');
end

if rowsCov ~= rowsMu
    error('Mu and Cov must have the same number of rows');
end

if ~isscalar(n)
    error('n must be a scalar integer');
end

% Number of features
nFeatures = size(Mu,1);

% Generarting feature vectors using Box-Muller approach
% Generate a random variable following uniform (0,1) having nFeatures
% and 1000 feature vectors
U = rand (nFeatures,2*n);

% Extracting from a generated uniform random variable two independent
% uniform random variables
u1 = U(:,1:2:end);
u2 = U(:,2:2:end);

% Using u1 and u2, we will use Box-Muller method to generate the feature
% vectors to follow standard normal
X = sqrt((-2).*log(u1)) .* (cos(2*pi.*u2));
clear u1 u2 U;

% Now ... Manipulating the generated Features N(0,1) to following certain
% mean and covariance other than the standard normal

% First we wil change its variance then we will change its mean

%Getting the eigen vectors and values of the covariance matrix
[V,D] = eig(Cov); % D is the eigen values matrix and V is the eigen Vectors Matrix
cluster = X;
for j = 1 : size(X,2)
    cluster(:,j) = V * sqrt(D) * X(:,j);
end

% changing its mean
cluster = cluster + repmat(Mu,1,size(cluster,2));
