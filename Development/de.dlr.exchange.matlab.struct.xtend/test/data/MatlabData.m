%% component Flaps
Flaps = struct;
Flaps.name='Flaps';
Flaps.description='This are the flaps';
Flaps.param{1}.name = 'P1';
Flaps.param{1}.value=0.99;
Flaps.param{1}.unit='kg';
Flaps.param{1}.description='This is P1';
Flaps.param{2}.name = 'P2';
Flaps.param{2}.value=0.88;
Flaps.param{2}.unit='';
Flaps.param{2}.description='';
% Flaps.Flap{1}
Flaps.Flap{1}.name='Flap_IB';
Flaps.Flap{1}.description='';
Flaps.Flap{1}.param{1}.name = 'P1_1';
Flaps.Flap{1}.param{1}.value=0.66;
Flaps.Flap{1}.param{1}.unit='';
Flaps.Flap{1}.param{1}.description='';
Flaps.Flap{1}.param{2}.name = 'P1_2';
Flaps.Flap{1}.param{2}.unit='';
Flaps.Flap{1}.param{2}.description='';
% Flaps.Flap{2}
Flaps.Flap{2}.name='Flap_OB';
Flaps.Flap{2}.description='';
% Flaps.Flap{2}.element{1}
Flaps.Flap{2}.element{1}.name='Supp_Left';
Flaps.Flap{2}.element{1}.description='';
Flaps.Flap{2}.element{1}.param{1}.name = 'P1_1_1';
Flaps.Flap{2}.element{1}.param{1}.value=0.55;
Flaps.Flap{2}.element{1}.param{1}.unit='';
Flaps.Flap{2}.element{1}.param{1}.description='';
% Flaps.Flap{2}.element{2}
Flaps.Flap{2}.element{2}.name='Supp_Right';
Flaps.Flap{2}.element{2}.description='';

%% component Environment
Environment = struct;
Environment.name='Environment';
Environment.description='';
Environment.param{1}.name = 'P3';
Environment.param{1}.value=0.77;
Environment.param{1}.unit='';
Environment.param{1}.description='';

%% component System "Functions"
SystemFunctions = struct;
SystemFunctions.name='SystemFunctions';
SystemFunctions.description='';

