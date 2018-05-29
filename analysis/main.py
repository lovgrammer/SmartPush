from Datastore import Datastore

def main():
    ds = Datastore()
    print('immediate packages')
    ds.get_imm_packages()
    ds.get_seen_info()
    ds.get_decision_info()
    ds.save()

if __name__ == '__main__':
    main()    
    

