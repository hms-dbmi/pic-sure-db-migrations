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

def jobName4 = "pic-sure-db-migration-authschema-orchestrator"  
def job4 = Jenkins.instance.getItem(jobName4)  
job4.getBuilds().each { it.delete() }  
job4.nextBuildNumber = 1   
job4.save()


def jobName5 = "pic-sure-db-migration-authschema-custom-image"  
def job5 = Jenkins.instance.getItem(jobName5)  
job5.getBuilds().each { it.delete() }  
job5.nextBuildNumber = 1   
job5.save()


def jobName6 = "pic-sure-db-migration-authschema-custom-orchestrator"  
def job6 = Jenkins.instance.getItem(jobName6)  
job6.getBuilds().each { it.delete() }  
job6.nextBuildNumber = 1   
job6.save()

