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
        	 	CONTAINER_NAME="main_auth_image_container"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
				docker run --name main_auth_image_container -d dbmi/pic-sure-db-migrations:base_image		 
			''' 
			
            sleep(time:15,unit:"SECONDS")
        } 
     }    

    stage('Prepare db changes for pic-sure-auth-microapp in the container'){ 
        steps {  
        	sh "docker exec -i main_auth_image_container bash -c \"/picsure-db-migrations/scripts/main/auth/get-auth-schema-from-repo.sh https://github.com/hms-dbmi/pic-sure-auth-microapp.git ${env.PIC_SURE_AUTH_BRANCH_NAME}\"" 
        } 
    } 
    
    stage('Commit changes'){ 
        steps { 
            sh "docker commit -m 'jenkins job commit' main_auth_image_container dbmi/pic-sure-db-migrations:authdb_image_v1.0"
        } 
    }    
    
    
    stage('Push Base Docker Image to Docker Hub'){ 
        environment {
            DOCKER_HUB_CREDENTIALS = credentials('DOCKER_HUB_CREDENTIALS')
            GIT_REPO_URL = "https://github.com/hms-dbmi/pic-sure-auth-microapp.git" 
             
        }
        
        
        steps {  
            
            git credentialsId: 'GITHUB_CREDENTIALS', url: "$GIT_REPO_URL", branch: "${env.PIC_SURE_AUTH_BRANCH_NAME}"  
            git log -n 1 --pretty=format:'%H'
            
            sh "echo 'Printing GIT HASH'"
            sh "echo ${env.shortCommit}"
            sh "echo ${shortCommit}"
            sh "echo $shortCommit"
            sh "docker login -u $DOCKER_HUB_CREDENTIALS_USR -p $DOCKER_HUB_CREDENTIALS_PSW"
            sh "docker push dbmi/pic-sure-db-migrations:authdb_image_v1.0"  
        } 
    }     
    
    stage('Clean up'){ 
        steps {  
        	 sh '''
        	 	CONTAINER_NAME="main_auth_image_container"
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