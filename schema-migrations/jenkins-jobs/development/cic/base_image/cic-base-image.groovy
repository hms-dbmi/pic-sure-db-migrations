def propsJSON = ''

pipeline {
 
  agent any 
  
  
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def DOCKER_IMAGE_NAME  = "picsure-db-migration-base-image.tar.gz" 
     def CONTAINER_NAME="base_image_container" 
     def BASE_IMAGE_TAG_NAME="picsure-db-migration-base-image" 
     def S3_PROFILE_NAME  = "datastage-prod"
     def S3_BUCKET_NAME  = "ad381-datastage"
     def S3_BUCKET_PROPERTIES_FILE_NAME  = "dataSTAGE-properties.json" 
     def PICSURE_DB_MIGRATIONS_REPO="https://github.com/hms-dbmi/pic-sure-db-migrations.git" 
  }  
  
  stages {
  
    stage('Prechecks'){ 
        steps {
            sh "aws s3 ls --profile $S3_PROFILE_NAME"
        } 
    }  
    
    stage('Dowload S3 Properties'){ 
        steps {
            sh '''
            	aws s3 cp s3://$S3_BUCKET_NAME/$S3_BUCKET_PROPERTIES_FILE_NAME . --profile $S3_PROFILE_NAME
            ''' 
        } 
    }
    
    stage('Checkout code'){ 
        steps {
            sh 'git clone $PICSURE_DB_MIGRATIONS_REPO pic-sure-db-migrations'
        } 
    }  
    
    stage('Build Base Docker Image'){ 
        steps { 
            dir('pic-sure-db-migrations/schema-migrations/docker/baseimage') {  
                sh "docker build -f DockerFile -t dbmi/picsure-db-migrations:$BASE_IMAGE_TAG_NAME ." 
            } 
        } 
    } 
    
    stage('Clean and Start the Base Container'){ 
        steps {  
        	 sh ''' 
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run --name "$CONTAINER_NAME" -d dbmi/picsure-db-migrations:$BASE_IMAGE_TAG_NAME		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
     }    
    
    stage('Copy S3 Properties to Container and Save Image'){ 
        steps {
            sh '''
            	docker cp $S3_BUCKET_PROPERTIES_FILE_NAME "$CONTAINER_NAME":/$S3_BUCKET_PROPERTIES_FILE_NAME 
            	docker exec -i $CONTAINER_NAME bash -c \"python3 /picsure-db-migrations/scripts/build_properties.py\"
            	docker commit $CONTAINER_NAME dbmi/picsure-db-migrations:$BASE_IMAGE_TAG_NAME
            ''' 
        } 
    }           
     
     
	stage('Save Docker Image'){ 
        steps {   
            sh "echo ${env.DOCKER_IMAGE_NAME}"
 			sh "docker save dbmi/picsure-db-migrations:$BASE_IMAGE_TAG_NAME | gzip > ${env.DOCKER_IMAGE_NAME}"  
        } 
    }  
    
    
    stage('Push Docker Image to S3'){  
        steps {   
 			sh "aws s3 --sse=AES256 cp ${env.DOCKER_IMAGE_NAME} s3://$S3_BUCKET_NAME/${env.DOCKER_IMAGE_NAME} --profile $S3_PROFILE_NAME" 
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