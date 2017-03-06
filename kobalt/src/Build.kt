import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.application.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val kobaltVersionToDocument="1.0.3"

fun getVersion() = DateTimeFormatter.ofPattern("ymd").format(LocalDateTime.now())

val p = project {

    name = "kobalt-dsl-doc"
    group = "com.guatec.kdd"
    artifactId = "kobalt-dsl-doc"
    version = getVersion()

    sourceDirectories {
        path("src/main/kotlin")
    }

    sourceDirectoriesTest {
        path("src/test/kotlin")
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-reflect:jar:1.1.0",
                "io.github.lukehutch:fast-classpath-scanner:",
                file(System.getProperty("user.home") + "/.kobalt/wrapper/dist/kobalt-$kobaltVersionToDocument/kobalt/wrapper/kobalt-$kobaltVersionToDocument.jar"))
    }

    dependenciesTest {
        compile("org.testng:testng:6.10")

    }

    assemble {
        jar {
            fatJar=true
            manifest {
                attributes("Main-Class", "com.guatec.kdd.MainKt")
            }
        }
    }

    application {
        mainClass = "com.guatec.kdd.MainKt"
    }
}
