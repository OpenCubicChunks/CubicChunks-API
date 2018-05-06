import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.DescribeOp
import org.gradle.api.internal.HasConvention

// Gradle repositories and dependencies
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            setUrl("http://files.minecraftforge.net/maven")
        }
        maven {
            setUrl("http://repo.spongepowered.org/maven")
        }
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.ajoberstar:grgit:2.0.0-milestone.1")
        classpath("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.13.1")
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    }
}

plugins {
    base
    java
    maven
}

apply {
    plugin<ForgePlugin>()
    plugin<LicensePlugin>()
}

// TODO: Reduce duplication of buildscript code between CC projects?
version = getModVersion()

val theForgeVersion: String by project
val versionSuffix: String  by project
val versionMinorFreeze: String  by project
val theMappingsVersion: String by project

val licenseYear: String by project
val projectName: String by project

configure<ForgeExtension> {
    version = theForgeVersion
    runDir = "run"
    mappings = theMappingsVersion
}

configure<LicenseExtension> {
    val ext = (this as HasConvention).convention.extraProperties
    ext["project"] = projectName
    ext["year"] = licenseYear
    exclude("**/*.info")
    exclude("**/package-info.java")
    exclude("**/*.json")
    exclude("**/*.xml")
    exclude("assets/*")
    header = file("HEADER.txt")
    ignoreFailures = false
    strictCheck = true
    mapping(mapOf("java" to "SLASHSTAR_STYLE"))
}

// based on:
// https://github.com/Ordinastie/MalisisCore/blob/30d8efcfd047ac9e9bc75dfb76642bd5977f0305/build.gradle#L204-L256
// https://github.com/gradle/kotlin-dsl/blob/201534f53d93660c273e09f768557220d33810a9/samples/maven-plugin/build.gradle.kts#L10-L44
tasks {
    "uploadArchives"(Upload::class) {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    // Sign Maven POM
                    //beforeDeployment {
                    //    signing.signPom(this)
                    //}

                    val username = if (project.hasProperty("sonatypeUsername")) project.properties["sonatypeUsername"] else System.getenv("sonatypeUsername")
                    val password = if (project.hasProperty("sonatypePassword")) project.properties["sonatypePassword"] else System.getenv("sonatypePassword")

                    withGroovyBuilder {
                        "snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots") {
                            "authentication"("userName" to username, "password" to password)
                        }

                        "repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2") {
                            "authentication"("userName" to username, "password" to password)
                        }
                    }

                    // Maven POM generation
                    pom.project {
                        withGroovyBuilder {

                            "name"(projectName)
                            "artifactId"(base.archivesBaseName.toLowerCase())
                            "packaging"("jar")
                            "url"("https://github.com/OpenCubicChunks/CubicChunks")
                            "description"("Unlimited world height mod for Minecraft")


                            "scm" {
                                "connection"("scm:git:git://github.com/OpenCubicChunks/CubicChunks.git")
                                "developerConnection"("scm:git:ssh://git@github.com:OpenCubicChunks/CubicChunks.git")
                                "url"("https://github.com/OpenCubicChunks/RegionLib")
                            }

                            "licenses" {
                                "license" {
                                    "name"("The MIT License")
                                    "url"("http://www.tldrlegal.com/license/mit-license")
                                    "distribution"("repo")
                                }
                            }

                            "developers" {
                                "developer" {
                                    "id"("Barteks2x")
                                    "name"("Barteks2x")
                                }
                                // TODO: add more developers
                            }

                            "issueManagement" {
                                "system"("github")
                                "url"("https://github.com/OpenCubicChunks/CubicChunks/issues")
                            }
                        }
                    }
                }
            }
        }
    }
}
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !!!!!!!!!!!VERSIONS!!!!!!!!!!!!!
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

fun getMcVersion(): String {
    return theForgeVersion.split("-")[0]
}

//returns version string according to this: http://mcforge.readthedocs.org/en/latest/conventions/versioning/
//format: MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH(-final/rcX/betaX)
//rcX and betaX are not implemented yet
fun getModVersion(): Any {
    return object {
        val v: String by lazy {
            try {
                val git = Grgit.open()
                val describe = org.ajoberstar.grgit.operation.DescribeOp(git.repository).call()
                val branch = getGitBranch(git)
                val snapshotSuffix = if (project.hasProperty("doRelease")) "" else "-SNAPSHOT"
                getModVersion(describe, branch) + snapshotSuffix;
            } catch (ex: RuntimeException) {
                logger.error("Unknown error when accessing git repository! Are you sure the git repository exists?", ex)
                String.format("%s-%s.%s.%s%s%s", getMcVersion(), "9999", "9999", "9999", "", "NOVERSION")
            }
        }
        override fun toString() = v
    }
}

fun getGitBranch(git: org.ajoberstar.grgit.Grgit): String {
    var branch: String = git.branch.current.name
    if (branch.equals("HEAD")) {
        branch = when {
            System.getenv("TRAVIS_BRANCH")?.isEmpty() == false -> // travis
                System.getenv("TRAVIS_BRANCH")
            System.getenv("GIT_BRANCH")?.isEmpty() == false -> // jenkins
                System.getenv("GIT_BRANCH")
            System.getenv("BRANCH_NAME")?.isEmpty() == false -> // ??? another jenkins alternative?
                System.getenv("BRANCH_NAME")
            else -> throw RuntimeException("Found HEAD branch! This is most likely caused by detached head state! Will assume unknown version!")
        }
    }

    if (branch.startsWith("origin/")) {
        branch = branch.substring("origin/".length)
    }
    return branch
}

fun getModVersion(describe: String, branch: String): String {
    if (branch.startsWith("MC_")) {
        val branchMcVersion = branch.substring("MC_".length)
        if (branchMcVersion != getMcVersion()) {
            logger.warn("Branch version different than project MC version! MC version: " +
                    getMcVersion() + ", branch: " + branch + ", branch version: " + branchMcVersion)
        }
    }

    //branches "master" and "MC_something" are not appended to version sreing, everything else is
    //only builds from "master" and "MC_version" branches will actually use the correct versioning
    //but it allows to distinguish between builds from different branches even if version number is the same
    val branchSuffix = if (branch == "master" || branch.startsWith("MC_")) "" else ("-" + branch.replace("[^a-zA-Z0-9.-]", "_"))

    val baseVersionRegex = "v[0-9]+\\.[0-9]+"
    val unknownVersion = String.format("%s-UNKNOWN_VERSION%s%s", getMcVersion(), versionSuffix, branchSuffix)
    if (!describe.contains('-')) {
        //is it the "vX.Y" format?
        if (describe.matches(Regex(baseVersionRegex))) {
            return String.format("%s-%s.0.0%s%s", getMcVersion(), describe, versionSuffix, branchSuffix)
        }
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    //Describe format: vX.Y-build-hash
    val parts = describe.split("-")
    if (!parts[0].matches(Regex(baseVersionRegex))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    if (!parts[1].matches(Regex("[0-9]+"))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    val mcVersion = getMcVersion()
    val modAndApiVersion = parts[0].substring(1)
    //next we have commit-since-tag
    val commitSinceTag = Integer.parseInt(parts[1])

    val minorFreeze = if ((versionMinorFreeze as String).isEmpty()) -1 else Integer.parseInt(versionMinorFreeze as String)

    val minor = if (minorFreeze < 0) commitSinceTag else minorFreeze
    val patch = if (minorFreeze < 0) 0 else (commitSinceTag - minorFreeze)

    return String.format("%s-%s.%d.%d%s%s", mcVersion, modAndApiVersion, minor, patch, versionSuffix, branchSuffix)
}

fun extractForgeMinorVersion(): String {
    // version format: MC_VERSION-MAJOR.MINOR.?.BUILD
    return (theForgeVersion as String).split(Regex("-")).getOrNull(1)?.split(Regex("\\."))?.getOrNull(1)
            ?: throw RuntimeException("Invalid forge version format: $theForgeVersion")
}
