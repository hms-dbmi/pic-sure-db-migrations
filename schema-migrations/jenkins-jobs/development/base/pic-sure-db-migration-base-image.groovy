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
        steps {  
            sh "docker login -u username -p password"
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