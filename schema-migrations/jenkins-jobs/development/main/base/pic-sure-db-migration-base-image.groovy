pipeline {
 
  agent any 
  
  stages { 
    stage('Checkout code'){ 
        steps {
            sh  'git clone https://github.com/hms-dbmi/pic-sure-db-migrations.git'
        } 
    }  
   
    
    stage('Build Base Docker Image'){ 
        steps { 
            dir('pic-sure-db-migrations/schema-migrations/docker/baseimage') {  
                sh "docker build -f DockerFile -t dbmi/pic-sure-db-migrations:base_image ." 
            } 
        } 
    }
    
    stage('Push Base Docker Image to Docker Hub'){ 
        
        environment {
            DOCKER_HUB_CREDENTIALS = credentials('DOCKER_HUB_CREDENTIALS')
        }
  

        steps {  
            
            sh "docker login -u $DOCKER_HUB_CREDENTIALS_USR -p $DOCKER_HUB_CREDENTIALS_PSW"
            sh "docker push dbmi/pic-sure-db-migrations:base_image"  
        } 
    }     
    
    stage('Clean up'){ 
        steps {  
            cleanWs()
        } 
    }      
    
  }
  
}