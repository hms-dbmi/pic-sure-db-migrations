#!/bin/bash

echo "Migration Orchestrator: Begin"


flyway_conf_location=/flyway-configs/conf/flyway.conf


# Validate Database Connection

db_check=`python3 validate_mysql_connection.py`

if [ "Passed" == "$db_check" ]; then
    echo "Database connection check passed, continue Flyway migration"
    
     flyway -X -configFiles=${flyway_conf_location} migrate 
    
     
    
    
    
    
else

    echo "Database connection check failed, Abort Flyway migration"
    echo "Errors: " ${db_check}
fi
 





echo "Migration Orchestrator: End"