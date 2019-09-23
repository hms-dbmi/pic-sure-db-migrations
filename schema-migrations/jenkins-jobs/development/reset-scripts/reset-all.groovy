Jenkins.instance.getAllItems(Job.class).each({
 job->job.getBuilds().each ({
   it-> it.delete()
 })
job.nextBuildNumber = 1
job.save()

});