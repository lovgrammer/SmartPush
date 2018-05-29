seenOn = dataset('File', strcat('seen_on.csv'), 'ReadVarNames', true, 'ReadObsNames', false, 'Delimiter', ',');
seenOff = dataset('File', strcat('seen_off.csv'), 'ReadVarNames', true, 'ReadObsNames', false, 'Delimiter', ',');
decisionOn = dataset('File', strcat('decision_on.csv'), 'ReadVarNames', true, 'ReadObsNames', false, 'Delimiter', ',');
decisionOff = dataset('File', strcat('decision_off.csv'), 'ReadVarNames', true, 'ReadObsNames', false, 'Delimiter', ',');

figure(1)
histogram((seenOn.time - seenOn.posttime)/(1000), [-1:100000])
title('Seen Time / Scheduling On')
ylabel('Number of Notifications')
xlabel('Time (sec)')

figure(2)
histogram((seenOff.time - seenOff.posttime)/(1000), [-1:100000])
title('Seen Time / Scheduling Off')
ylabel('Number of Notifications')
xlabel('Time (sec)')

figure(3)
histogram((decisionOff.time - decisionOff.posttime)/(1000*60), [-1:700])
title('Decision Time / Scheduling Off')
ylabel('Number of Notifications')
xlabel('Time (1min)')

figure(4)
histogram((decisionOn.time - decisionOn.posttime)/(1000*60), [-1:700])
title('Decision Time / Scheduling On')
ylabel('Number of Notifications')
xlabel('Time (1min)')