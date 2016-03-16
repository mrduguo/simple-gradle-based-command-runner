package buildscript

import buildscript.utils.Env
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class BuildscriptGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        applyScripts(project,'00-')
        if(Env.config('buildscriptDisableAutoConfig','false')=='false'){
            registerTasks(project)
        }
        applyScripts(project,'!00-')
    }



    def registerTasks(Project project) {
        project.defaultTasks('clean', 'build')

        def taskNames=collectDefaultTaskNames(project)
        if(taskNames){
            tryToRegisterTasks( project, taskNames,false)
        }else{
            def jobName=Env.config('JOB_NAME')
            if(jobName){
                taskNames=jobName.split('-')
                tryToRegisterTasks( project, taskNames,true)
            }
        }

        if (project.getTasksByName('clean', true).empty) {
            project.getPlugins().apply(BasePlugin)
        }
    }




    def tryToRegisterTasks(Project project,def taskNames,def setupDefaultTasks) {
        def foundTasks=[]
        taskNames.each { String taskName ->
            if (project.getTasksByName(taskName, true).empty) {
                try {
                    def taskClass = Class.forName("buildscript.tasks.${taskName.split('_').first()}.${taskName.split('_').collect { it.capitalize() }.join()}Task")
                    project.getTasks().create(taskName, taskClass)
                    foundTasks << taskName
                } catch (ClassNotFoundException noTaskFound) {
                    if(!(setupDefaultTasks || taskName=='clean')){
                        throw noTaskFound
                    }
                }
            }
        }
        if(foundTasks && setupDefaultTasks){
            def gradleDefaultTasks=new ArrayList<String>()
            gradleDefaultTasks.add('clean')
            gradleDefaultTasks.addAll(foundTasks)
            project.setDefaultTasks(gradleDefaultTasks)
            println("set default tasks: ${gradleDefaultTasks}")
        }
    }

    def collectDefaultTaskNames(Project project) {
        def taskNames=[]
        project.gradle.startParameter?.taskNames.each { String taskName ->
            taskNames << taskName
        }
        taskNames
    }

    def applyScripts(Project project,String prefix) {
        def isReversedExclude=prefix.startsWith('!')
        if(isReversedExclude){
            prefix=prefix.substring(1)
        }
        project.file('gradle').list()?.findAll { it.endsWith('.gradle') }.sort().each {def fileName->
            if(
            (fileName.startsWith(prefix) &&  !isReversedExclude) ||
            (!fileName.startsWith(prefix) &&  isReversedExclude)
            ){
                project.apply from: project.file("gradle/$fileName")
            }
        }
    }
}