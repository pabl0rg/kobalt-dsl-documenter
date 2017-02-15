import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.kotlin.*

val bs = buildScript {
    repos()
}


val p = project {

    name = "dsl-doc"
    group = "com.example"
    artifactId = name
    version = "0.1"

    sourceDirectories {
        path("src/main/kotlin")
    }

    sourceDirectoriesTest {
        path("src/test/kotlin")
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-reflect:jar:1.1.0-beta-22",
                "io.github.lukehutch:fast-classpath-scanner:",
                "org.yaml:snakeyaml:jar:1.17",
                file("/Users/juanliska/Documents/startrack/code/kobalt/kobaltBuild/libs/kobalt-0.931.jar"))
    }

    dependenciesTest {
        compile("org.testng:testng:6.10")

    }

    assemble {
        jar {
            fatJar=true
        }
    }

    application {
        mainClass = "com.guatec.MainKt"
    }


}
