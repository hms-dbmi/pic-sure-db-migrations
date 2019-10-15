pipeline {
 
  agent any 
  
  
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def DOCKER_IMAGE_NAME  = "cic-pic-sure-db-authschema-migration-image.tar.gz"  
     def S3_CONTAINER_NAME="main_auth_image_container"
     def CONTAINER_NAME="main_authschema_orchestrator" 
  }  
  
  stages { 
    stage('Prechecks'){ 
        steps {
            sh 'aws s3 ls'
        } 
    }   
    
    stage('Copy Image from S3 to Local'){ 
        steps {
            sh 'aws s3 cp s3://ad381-datastage/"$S3_CONTAINER_NAME".tar.gz . '
        } 
    }
     
    stage('Import Docker Image'){ 
        steps {
            sh ''' 
            	docker import "$S3_CONTAINER_NAME".tar.gz  picsure-db-migrations:authschema-migration-image
			'''
        } 
    }
    
    stage('Clean and Start the Container'){ 
        steps {   
        	 sh ''' 
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi 
			''' 

			sh '''
				docker run --name auth_migration_orchestrator_container -v /flyway-configs:/flyway-configs -d --entrypoint /usr/bin/python3  picsure-db-migrations:authschema-migration-image /app/index.py
				
			'''
			
            sleep(time:15,unit:"SECONDS")
        } 
    }
     
    stage('Export Docker Container'){ 
        steps {   
            sh '''
 				CONTAINER_ID="$(docker inspect --format="{{.Id}}" auth_migration_orchestrator_container)"  
 				CONTAINER_ID_WITH_EXTENSION=auth_migration_orchestrator_container.tar.gz
 				docker export "$CONTAINER_NAME" | gzip > "$CONTAINER_ID_WITH_EXTENSION"  
 			''' 
        } 
    }   
    
    stage('Push Docker Image to S3'){  
        steps {   
 			sh '''
 				CONTAINER_ID="$(docker inspect --format="{{.Id}}" auth_migration_orchestrator_container)"  
 				CONTAINER_ID_WITH_EXTENSION=auth_migration_orchestrator_container.tar.gz
 				aws s3 --sse=AES256 cp "$CONTAINER_ID_WITH_EXTENSION" s3://ad381-datastage/"$CONTAINER_ID_WITH_EXTENSION" 
 			''' 
        } 
    }     
     
    stage('Run DB Migration'){ 
        steps {  
        	 sh '''
        	 	docker exec -i auth_migration_orchestrator_container bash -c \"/picsure-db-migrations/scripts/main/auth/auth-migration-orchestrator.sh\"
			'''  
        } 
    }     
    
    stage('Clean up resources'){ 
        steps {  
        	 sh '''
        	 	CONTAINER_NAME="auth_migration_orchestrator_container"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi 
			''' 

        } 
    }    
    
         
     
  }
  
  post { 
        always { 
            cleanWs()
        }
  }
  
}