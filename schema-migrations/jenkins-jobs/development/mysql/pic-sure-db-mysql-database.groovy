pipeline {
 
  agent any
  stages {
  
  
    stage('Build/ReBuild MySQL Database'){ 
        steps { 
        
        	 sh '''
        	 	CONTAINER_NAME="pic_sure_mysqldb"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run -d -p 3306:3306 -v /tmp/docker/mySql:/var/lib/mysql -v /flyway-configs:/flyway-configs --name=pic_sure_mysqldb -e MYSQL_ROOT_PASSWORD=mysql mysql:5.7
			'''
			
            sleep(time:25,unit:"SECONDS")
            
        	 sh '''  
        	    CONTAINER_NAME="pic_sure_mysqldb"
				docker exec $CONTAINER_NAME  bash -c ' mysql -u root -p$MYSQL_ROOT_PASSWORD -e \"drop database IF EXISTS auth; create database auth\" '
				docker exec $CONTAINER_NAME  bash -c ' mysql -u root -p$MYSQL_ROOT_PASSWORD -e \"drop database IF EXISTS irct; create database irct\" '
				docker exec $CONTAINER_NAME  bash -c ' mysql -u root -p$MYSQL_ROOT_PASSWORD -e \"drop database IF EXISTS picsure; create database picsure\" '
				
				docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $CONTAINER_NAME
			'''            
            
            
             
        }  
    } 
    
    stage('Clean Workspace'){ 
        steps {  
            cleanWs()
        } 
    }      
         
    
  } 
  
} 