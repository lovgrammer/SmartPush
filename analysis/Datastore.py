import pandas as pd
import numpy as np
import os
import os.path

ROOT_PATH = './'
DATASET_PATH = './data/n2/'

class Datastore:
    
    data = {}
    
    def __init__(self):
        self.data['noti'] = pd.read_csv(DATASET_PATH + 'noti.csv', header=0, low_memory=False)
        self.data['seendecision'] = pd.read_csv(DATASET_PATH + 'seendecision.csv', header=0, low_memory=False)
        
        self.data['noti_on'] = self.data['noti'][self.data['noti'].schedulingon == 1]
        self.data['noti_off'] = self.data['noti'][self.data['noti'].schedulingon == 0]
        
        self.data['noti_imm'] = self.data['noti'][(self.data['noti'].immediate == 1) & (self.data['noti']).schedulingon == 0]
        
        self.data['seen_on'] = self.data['seendecision'][(self.data['seendecision'].type == 0) & (self.data['seendecision'].schedulingon == 1)].drop_duplicates(subset=['posttime', 'uid'], keep='first')
        self.data['seen_off'] = self.data['seendecision'][(self.data['seendecision'].type == 0) & (self.data['seendecision'].schedulingon == 0)].drop_duplicates(subset=['posttime', 'uid'], keep='first')

        self.data['decision_on'] = self.data['seendecision'][(self.data['seendecision'].type == 1) & (self.data['seendecision'].schedulingon == 1)].drop_duplicates(subset=['posttime', 'uid'], keep='first')
        self.data['decision_off'] = self.data['seendecision'][(self.data['seendecision'].type == 1) & (self.data['seendecision'].schedulingon == 0)].drop_duplicates(subset=['posttime', 'uid'], keep='first')

        
    
    def get_imm_packages(self):
        print(self.data['noti'][(self.data['noti'].immediate == 1) & (self.data['noti']).schedulingon == 1].drop_duplicates(subset=['package', 'uid'], keep='first'))

    def get_seen_info(self):
        print('seen_on mean : ' , np.mean(self.data['seen_on'].time - self.data['seen_on'].posttime))
        print('seen_on stdv : ' , np.std(self.data['seen_on'].time - self.data['seen_on'].posttime))
        print('seen_off mean : ' , np.mean(self.data['seen_off'].time - self.data['seen_off'].posttime))
        print('seen_off stdv : ' , np.std(self.data['seen_off'].time - self.data['seen_off'].posttime))

    def get_decision_info(self):
        print('decision_on mean : ' , np.mean(self.data['decision_on'].time - self.data['decision_on'].posttime))
        print('decision_on stdv : ' , np.std(self.data['decision_on'].time - self.data['decision_on'].posttime))
        print('decision_off mean : ' , np.mean(self.data['decision_off'].time - self.data['decision_off'].posttime))
        print('decision_off stdv : ' , np.std(self.data['decision_off'].time - self.data['decision_off'].posttime))
        
    def save(self):
        self.data['seen_on'].to_csv('./output/seen_on.csv', index=False, header=True)
        self.data['seen_off'].to_csv('./output/seen_off.csv', index=False, header=True)
        self.data['decision_on'].to_csv('./output/decision_on.csv', index=False, header=True)
        self.data['decision_off'].to_csv('./output/decision_off.csv', index=False, header=True)
        
        
        
