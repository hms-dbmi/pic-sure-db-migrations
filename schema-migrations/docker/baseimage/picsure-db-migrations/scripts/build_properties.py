import pymysql
import time
import configparser
import json 

class DBProps():
    def __init__(self, **db_params):
        self.db_user = db_params['db_user']
        self.db_password = db_params['db_password']
        self.db_host = db_params['db_host']
        self.db_port = db_params['db_port'] 
        
    def create_db_properties(self):
        with open('/flyway-configs/db.properties', mode = 'w') as file:
            file.write('[mysql]\n')
            file.write('mysql.user='+self.db_user+'\n')
            file.write('mysql.password='+self.db_password+'\n')
            file.write('mysql.host='+self.db_host+'\n')
            file.write('mysql.port='+str(self.db_port)+'\n') 
            
class FlywayConf():
    def __init__(self, **flyway_params):
        self.db_user = flyway_params['db_user']
        self.db_password = flyway_params['db_password']
        self.db_host = flyway_params['db_host']
        self.db_port = flyway_params['db_port']
        self.db_schema = flyway_params['db_schema']
        self.locations = flyway_params['locations']
                 
        
    def create_flyway_conf(self, **args):
        with open('/flyway-configs/flyway-'+ args['file_name']+'.conf', mode = 'w') as file:  
            flyway_url = 'mysql://'+self.db_host+':'+str(self.db_port)+'/'+self.db_schema
            file.write('flay.url='+flyway_url+'\n')
            file.write('flyway.user='+self.db_user+'\n')
            file.write('flyway.password='+self.db_password+'\n')
            file.write('flyway.locations='+self.db_host+'\n')  
            file.write('flyway.schemas='+self.db_schema+'\n')        
     
class PropertyFileCreator():
    
    def __init__(self):
        self.data = {}
        with open('/dataSTAGE-properties.json', 'r') as input_file:
            self.data = json.load(input_file)
    
    def create_files(self): 
        for d in self.data['databases']: 
            if d['connection_name'] == 'primary_db_connection':  
                db_params = {
                    'db_user': d['db_username'],
                    'db_password': d['db_password'], 
                    'db_host': d['db_host'], 
                    'db_port': d['db_port'] 
                }
                auth = DBProps(**db_params)
                auth.create_db_properties() 
            elif d['connection_name'] == 'auth_db_connection':
                flyway_params = {
                    'db_user': d['db_username'],
                    'db_password': d['db_password'], 
                    'db_host': d['db_host'], 
                    'db_port': d['db_port'],
                    'db_schema': 'auth', 
                    'locations': 'filesystem:./sql'
                }
                auth = FlywayConf(**flyway_params)
                auth.create_flyway_conf(file_name = 'auth') 
            elif d['connection_name'] == 'picsure_db_connection':  
                flyway_params = {
                    'db_user': d['db_username'],
                    'db_password': d['db_password'], 
                    'db_host': d['db_host'], 
                    'db_port': d['db_port'],
                    'db_schema': 'picsure', 
                    'locations': 'filesystem:./sql'
                }
                auth = FlywayConf(**flyway_params)
                auth.create_flyway_conf(file_name = 'picsure') 
        
        
if __name__ == '__main__':
      pfc = PropertyFileCreator()
      pfc.create_files()      


