% Calculate the resulting force for a given mass and accelleration
function f = force (m, a)
    
    % check the input data
    if nargin < 2; a = 9.81; end

    % calculation
    f = m * a;
end