def jobName1 = "pic-sure-db-mysql-database"  
def job1 = Jenkins.instance.getItem(jobName1)  
job1.getBuilds().each { it.delete() }  
job1.nextBuildNumber = 1   
job1.save()


def jobName2 = "pic-sure-db-migration-base-image"  
def job2 = Jenkins.instance.getItem(jobName2)  
job2.getBuilds().each { it.delete() }  
job2.nextBuildNumber = 1   
job2.save()

def jobName3 = "pic-sure-db-migration-authschema-image"  
def job3 = Jenkins.instance.getItem(jobName3)  
job3.getBuilds().each { it.delete() }  
job3.nextBuildNumber = 1   
job3.save()

def jobName4 = "pic-sure-db-migration-auth-orchestrator"  
def job4 = Jenkins.instance.getItem(jobName4)  
job4.getBuilds().each { it.delete() }  
job4.nextBuildNumber = 1   
job4.save()

def jobName5 = "pic-sure-db-migration-picsureschema-image"  
def job5 = Jenkins.instance.getItem(jobName5)  
job5.getBuilds().each { it.delete() }  
job5.nextBuildNumber = 1   
job5.save()

def jobName6 = "pic-sure-db-migration-picsureschema-orchestrator"  
def job6 = Jenkins.instance.getItem(jobName6)  
job6.getBuilds().each { it.delete() }  
job6.nextBuildNumber = 1   
job6.save()

def jobName7 = "pic-sure-db-migration-irctschema-image"  
def job7 = Jenkins.instance.getItem(jobName7)  
job7.getBuilds().each { it.delete() }  
job7.nextBuildNumber = 1   
job7.save()

def jobName8 = "pic-sure-db-migration-irctschema-orchestrator"  
def job8 = Jenkins.instance.getItem(jobName8)  
job8.getBuilds().each { it.delete() }  
job8.nextBuildNumber = 1   
job8.save()











# Jobs


pic-sure-db-mysql-database

pic-sure-db-migration-base-image

pic-sure-db-migration-authschema-image	
pic-sure-db-migration-auth-orchestrator  

pic-sure-db-migration-picsureschema-image
pic-sure-db-migration-picsureschema-orchestrator
	 
pic-sure-db-migration-irctschema-image  	 
pic-sure-db-migration-irctschema-orchestrator	

 

	
