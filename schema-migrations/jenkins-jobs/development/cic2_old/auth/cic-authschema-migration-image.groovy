pipeline {
 
  agent any 
  
  
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def DOCKER_IMAGE_NAME  = "cic-pic-sure-db-authschema-migration-image.tar.gz"  
     def CONTAINER_NAME="main_auth_image_container" 
     def jsonObj = readJSON file: '/flyway-configs/cic-config.json' 
     def PROJECT_GIT_HASH = sh(script: "echo ${jsonObj.database.auth.git_hash}", returnStdout:true).trim().replaceAll("\\[|\\]", "")
  }  
  
  stages { 
    stage('Prechecks'){ 
        steps {
            sh 'aws s3 ls'
        } 
    }   
    
    stage('Clean and Start the Base Container'){ 
        steps {  
        	 sh ''' 
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run --name "$CONTAINER_NAME" -d dbmi/pic-sure-db-migrations:cic-pic-sure-db-migrations-baseimage		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
    }   
     
	stage('Prepare db changes for pic-sure-auth-microapp in the container'){ 
        steps {  
             sh '''
             	docker exec -i "$CONTAINER_NAME" bash -c "/picsure-db-migrations/scripts/main/auth/get-auth-schema-from-repo.sh https://github.com/hms-dbmi/pic-sure-auth-microapp.git  "$PROJECT_GIT_HASH"  "             
             '''
        }  
    }     
    
    stage('Export Docker Container'){ 
        steps {   
            sh '''
 				CONTAINER_ID="$(docker inspect --format="{{.Id}}" "$CONTAINER_NAME")"  
 				CONTAINER_ID_WITH_EXTENSION=$CONTAINER_NAME".tar.gz"
 				docker export "$CONTAINER_NAME" | gzip > "$CONTAINER_ID_WITH_EXTENSION"  
 			''' 
        } 
    }   
    
    stage('Push Docker Image to S3'){  
        steps {   
 			sh '''
 				CONTAINER_ID="$(docker inspect --format="{{.Id}}" "$CONTAINER_NAME")"  
 				CONTAINER_ID_WITH_EXTENSION=$CONTAINER_NAME".tar.gz"
 				aws s3 --sse=AES256 cp "$CONTAINER_ID_WITH_EXTENSION" s3://ad381-datastage/"$CONTAINER_ID_WITH_EXTENSION" 
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