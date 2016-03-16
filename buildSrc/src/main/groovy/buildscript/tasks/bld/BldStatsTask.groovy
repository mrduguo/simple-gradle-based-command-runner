package buildscript.tasks.bld

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BldStatsTask extends DefaultTask {

    @TaskAction
    def run() {
        println "\n\n\nSystem Properties:"
        System.properties.sort().each { k, v ->
            println "$k = $v"
        }

        println "\n\n\nEnvrionment Variables:"
        System.getenv().sort().each { k, v ->
            println "$k = $v"
        }


        println "\n\n\nProject Ext:"
        project.getExtensions().extraProperties.properties.sort().each { def k, v ->
            println "$k = $v"
        }
    }

}
