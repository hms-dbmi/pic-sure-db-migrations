 pipeline { 
  agent any
  stages {
  
  
  
 	stage('Get Docker Image Tags'){ 
	        steps {  
	             sh  "curl 'https://registry.hub.docker.com/v2/repositories/dbmi/pic-sure-db-migrations/tags/'|jq '.\"results\"[][\"name\"]' > tags.txt "
	             sh "cat tags.txt" 
	        } 
	 }
     
         
     stage("Read Tags, build List for UI") {
            steps {
                script {
                    env.DOCKER_IMAGE_TAGS = readFile 'tags.txt'
                    env.DOCKER_IMAGE_TAGS = env.DOCKER_IMAGE_TAGS.replace("\"", "")
                    env.DOCKER_IMAGE_RELEASE_TAG = input message: 'Please choose the Docker Release Tag', ok: 'Release!',
                            parameters: [choice(name: 'DOCKER_IMAGE_RELEASE_TAG', choices: env.DOCKER_IMAGE_TAGS, description: 'What is the release scope?')]
                    
                }
                echo "Will build an Container from this tag: ${env.DOCKER_IMAGE_RELEASE_TAG}"
            }
     }   
    
    
     stage('Pull Auth Docker Image by Tag'){ 
        steps { 
        	 sh "docker pull dbmi/pic-sure-db-migrations:${env.DOCKER_IMAGE_RELEASE_TAG}" 
        } 
     }    
    
     stage('Clean and Start the Base Container'){ 
        steps { 
        
             
        
        	 sh '''
        	 	CONTAINER_NAME="auth_migration_orchestrator"
				CONTAINER_FOUND="$(docker ps --all --quiet --filter=name="$CONTAINER_NAME")"
				if [ -n "$CONTAINER_FOUND" ]; then
  					docker stop $CONTAINER_FOUND && docker rm $CONTAINER_FOUND
				fi			
			   
			''' 
			
			sh "docker run --name auth_migration_orchestrator -v /flyway-configs:/flyway-configs  -d dbmi/pic-sure-db-migrations:${env.DOCKER_IMAGE_RELEASE_TAG}"	 
			
            sleep(time:15,unit:"SECONDS")
        } 
     }  
     
    stage('Run DB Migration'){ 
        steps {  
        	 sh '''
        	 	docker exec -i auth_migration_orchestrator bash -c \"/picsure-db-migrations/scripts/custom/auth/auth-custom-migration-orchestrator.sh\"
			''' 

        } 
    }        
 
    stage('Clean up resources'){ 
        steps {  
        	 sh '''
        	 	CONTAINER_NAME="auth_migration_orchestrator"
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