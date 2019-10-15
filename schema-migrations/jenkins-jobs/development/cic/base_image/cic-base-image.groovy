def propsJSON = ''

pipeline {
 
  agent any 
  
  
  environment {
     def BUILD_TIMESTAMP = sh(script: "echo `date +%Y%m%d%H%M%S`", returnStdout: true).trim()
     def DOCKER_IMAGE_NAME  = "cic-pic-sure-db-migrations-baseimage.tar.gz"
     
     def CONTAINER_NAME="base_image_container" 
     
     def S3_PROFILE_NAME  = "datastage-prod"
     def S3_BUCKET_NAME  = "ad381-datastage"
     def S3_BUCKET_PROPERTIES_FILE_NAME  = "dataSTAGE-properties.json"
     
     
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
            sh 'git clone https://github.com/hms-dbmi/pic-sure-db-migrations.git'
        } 
    }  
    
    stage('Build Base Docker Image'){ 
        steps { 
            dir('pic-sure-db-migrations/schema-migrations/docker/baseimage') {  
                sh "docker build -f DockerFile -t dbmi/pic-sure-db-migrations:cic-pic-sure-db-migrations-baseimage ." 
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
				docker run --name "$CONTAINER_NAME" -d dbmi/pic-sure-db-migrations:cic-pic-sure-db-migrations-baseimage		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
     }    
    
    stage('Copy S3 Properties to Container and Save Image'){ 
        steps {
            sh '''
            	docker cp $S3_BUCKET_PROPERTIES_FILE_NAME "$CONTAINER_NAME":/$S3_BUCKET_PROPERTIES_FILE_NAME 
            	docker exec -i $CONTAINER_NAME bash -c \"python /picsure-db-migrations/scripts/build_properties.py\"
            	docker commit $CONTAINER_NAME dbmi/pic-sure-db-migrations:cic-pic-sure-db-migrations-baseimage
            ''' 
        } 
    }           
     
     
	stage('Save Docker Image'){ 
        steps {   
            sh "echo ${env.DOCKER_IMAGE_NAME}"
 			sh "docker save dbmi/pic-sure-db-migrations:cic-pic-sure-db-migrations-baseimage | gzip > ${env.DOCKER_IMAGE_NAME}"  
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