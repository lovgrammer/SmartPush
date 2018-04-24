package msnl.unist.smartpushscheduler;

import java.util.ArrayList;

public class SPDay {
    
    ArrayList<SPHour> mHourList = new ArrayList<SPHour>();
    
    public SPDay() {
	for (int i=0; i<24; i++) {
	    mHourList.add(new SPHour());
	}
    }

    public ArrayList<SPHour> getHourList() {
	return mHourList;
    }

    public int getDecisionCount() {
	int count = 0;
	for (SPHour h : mHourList) {
	    count += h.decisionCount;
	}
	return count;
    }
}

