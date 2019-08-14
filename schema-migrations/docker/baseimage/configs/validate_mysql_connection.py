import pymysql
import time
import configparser


def validate_mysql_connection(): 
    connection  = None
    connect_check = None
    try:
        config = configparser.ConfigParser()
        config.read('/flyway-configs/conf/db.properties') 
        connection = pymysql.connect(host=config.get('mysql', 'mysql.host'), port=config.get('mysql', 'mysql.port'), user=config.get('mysql', 'mysql.user'), passwd=config.get('mysql', 'mysql.password'), db=config.get('mysql', 'mysql.schema'))  
        if connection != None:
            connect_check = "Passed"  
    except Exception as e: 
         connect_check = e  
    finally:
        # Close connection.
        if connection!=None:
            connection.close()
            
    print(connect_check)  




validate_mysql_connection()