 pipeline { 
  agent any
  stages {
    
    
    
     stage('Pull Base Container'){ 
        steps { 
        	 sh "docker pull dbmi/pic-sure-db-migrations:base_image" 
        } 
     }    
    
     stage('Clean and Start the Base Container'){ 
        steps { 
        
        	 sh '''
        	 	CONTAINER_NAME="picsuredb_schema_migrations_base_container"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run --name picsuredb_schema_migrations_base_container -d dbmi/pic-sure-db-migrations:base_image		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
     }    

    stage('Prepare db changes for pic-sure-auth-microapp in the container'){ 
        steps {  
        	sh "docker exec -i picsuredb_schema_migrations_base_container bash -c \"/configs/get-picsure-schema-from-repo.sh https://github.com/hms-dbmi/pic-sure.git ${env.PIC_SURE_AUTH_BRANCH_NAME}\"" 
        } 
    } 
    
    stage('Commit changes'){ 
        steps { 
            sh "docker commit -m 'jenkins job commit' picsuredb_schema_migrations_base_container dbmi/pic-sure-db-migrations:picsuredb_image_v1.0"
        } 
    }    
    
    
    stage('Push Base Docker Image to Docker Hub'){ 
        steps {  
            sh "docker login -u username -p password"
            sh "docker push dbmi/pic-sure-db-migrations:picsuredb_image_v1.0"  
        } 
    }     
    
    stage('Clean up'){ 
        steps {  
        	 sh '''
        	 	CONTAINER_NAME="picsuredb_schema_migrations_base_container"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi 
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