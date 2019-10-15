pipeline {
 
  agent any 
   
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def PICSURE_BASE_IMAGE_NAME  = "picsure-db-migration-base-image"  
     def CONTAINER_NAME="main_authschema_image_container" 
     def jsonObj = readJSON file: '/flyway-configs/cic-config.json' 
     def PROJECT_GIT_HASH = sh(script: "echo ${jsonObj.database.auth.git_hash}", returnStdout:true).trim().replaceAll("\\[|\\]", "")
     
     def S3_PROFILE_NAME  = "datastage-prod"
     def S3_BUCKET_NAME  = "ad381-datastage"
     def S3_BUCKET_PROPERTIES_FILE_NAME  = "dataSTAGE-properties.json"
     
     def PSAMA_REPO="https://github.com/hms-dbmi/pic-sure-auth-microapp.git" 
     def AUTH_IMAGE_TAG_NAME="picsure-db-migration-authschema-image"     
  }  
  
  stages {  
    stage('Prechecks'){ 
        steps {
            sh 'aws s3 ls --profile $S3_PROFILE_NAME'
        } 
    }   
    
    stage('Copy Image from S3 to Local'){ 
        steps {
            sh 'aws s3 cp s3://ad381-datastage/"$PICSURE_BASE_IMAGE_NAME".tar.gz .  --profile $S3_PROFILE_NAME'
        } 
    }
     
    stage('Import Docker Image'){ 
        steps {
            sh ''' 
            	docker load < "$PICSURE_BASE_IMAGE_NAME".tar.gz
			'''
        } 
    }       
    
    stage('Clean and Start the Base Container'){ 
        steps {  
        	 sh ''' 
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run --name "$CONTAINER_NAME" -d dbmi/picsure-db-migrations:$PICSURE_BASE_IMAGE_NAME		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
    }   
     
	stage('Prepare db changes for pic-sure-auth-microapp in the container'){ 
        steps {  
             sh '''
             	docker exec -i "$CONTAINER_NAME" bash -c "/picsure-db-migrations/scripts/main/auth/get-auth-schema-from-repo.sh $PSAMA_REPO  "$PROJECT_GIT_HASH"  "             
             '''
        }  
    }     
    
    stage('Save Docker Container'){ 
        steps {   
            sh '''
 				docker commit $CONTAINER_NAME dbmi/picsure-db-migrations:$AUTH_IMAGE_TAG_NAME
 				docker save dbmi/picsure-db-migrations:$AUTH_IMAGE_TAG_NAME | gzip > $AUTH_IMAGE_TAG_NAME
 			''' 
        } 
    }   
    
    stage('Push Docker Image to S3'){  
        steps {   
 			sh "aws s3 --sse=AES256 cp $AUTH_IMAGE_TAG_NAME s3://$S3_BUCKET_NAME/$AUTH_IMAGE_TAG_NAME --profile $S3_PROFILE_NAME" 
        } 
    }      
    
    stage('Remove Container'){ 
        steps {  
        	 sh ''' 
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