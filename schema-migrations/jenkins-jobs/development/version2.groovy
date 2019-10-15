GITHUB_PROJECT = {{GITHUB_PROJECT}}
GITHUB_BRANCH = '${env.BRANCH_NAME}'

 pipeline { 
  agent any
  stages { 
  
	stage ("Listing Branches") { 
		git url: GITHUB_PROJECT 
		sh 'git branch -r | awk \'{print $1}\' ORS=\'\\n\' > branches.txt'
		sh "cut -d '/' -f 2 branches.txt > branch.txt"
		
	}
	
	stage('get build branch Parameter User Input') {
		liste = readFile 'branch.txt'
		echo "please click on the link here to chose the branch to build"
		env.BRANCH_SCOPE = input message: 'Please choose the branch to build ', ok: 'Choose this',
		parameters: [choice(name: 'BRANCH_NAME', choices: "${liste}", description: 'Branch to build?')]
	}
	
	stage('Capture GIT_HASH') { 
		git url: GITHUB_PROJECT, branch: "${env.BRANCH_SCOPE}"  
        env.GIT_HASH = sh returnStdout: true, script: "git log -n 1 --pretty=format:'%H'"
	}  
  
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
        }
        
        
        steps {  
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