pipeline {
 
  agent any 
  
  
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def AUTHSCHEMA_IMAGE_NAME  = "picsure-db-migration-authschema-image"  
     def CONTAINER_NAME="main_authschema_orchestrator_container" 
     
     def S3_PROFILE_NAME  = "datastage-prod"
     def S3_BUCKET_NAME  = "ad381-datastage" 
  }   
  
  stages { 
    stage('Prechecks'){ 
        steps {
            sh 'aws s3 ls  --profile $S3_PROFILE_NAME'
        } 
    }   
    
    stage('Copy Image from S3 to Local'){ 
        steps {
            sh '''
            	aws s3 cp s3://$S3_BUCKET_NAME/$AUTHSCHEMA_IMAGE_NAME . --profile $S3_PROFILE_NAME 
            '''
        } 
    }
     
    stage('Load Docker Image'){ 
        steps {
            sh ''' 
            	docker load < $AUTHSCHEMA_IMAGE_NAME
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
				docker run --name $CONTAINER_NAME  -d --entrypoint /usr/bin/python3  dbmi/picsure-db-migrations:$AUTHSCHEMA_IMAGE_NAME /app/index.py
				
			'''
			
            sleep(time:15,unit:"SECONDS")
        } 
    }
      
    stage('Run DB Migration'){ 
        steps {  
        	 sh '''
        	 	docker exec -i $CONTAINER_NAME bash -c \"/picsure-db-migrations/scripts/main/auth/auth-migration-orchestrator.sh\"
			'''  
        } 
    }     
    
    stage('Clean up resources'){ 
        steps {  
        	 sh '''
        	 	CONTAINER_NAME=$CONTAINER_NAME
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