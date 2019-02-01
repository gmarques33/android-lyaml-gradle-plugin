package com.hubspot.android.localization

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.hubspot.android.localization.I18nKotlinGeneratorTask
import com.hubspot.android.localization.I18nXmlGeneratorTask

class HubSpotLocalizationPlugin implements Plugin<Project> {

    void apply(Project parentProject) {
        parentProject.subprojects {
            afterEvaluate { project ->

                def baseVariant = null
                if (project.plugins.hasPlugin("com.android.library")) {
                    baseVariant = project.android.libraryVariants
                } else if (project.plugins.hasPlugin("com.android.application")) {
                    baseVariant = project.android.applicationVariants
                }
                File inputFolder = new File("${project.projectDir}/lang")
                if (baseVariant == null || !inputFolder.exists()) return

                String projectPackageName = (new XmlSlurper().parse(file(android.sourceSets.main.manifest.srcFile))).@package.text()

                baseVariant.all { variant ->

                    //Register task to generate the Kotlin code for attributed strings
                    File srcOutputDir = new File(project.buildDir.path, "/generated/source/i18n/${variant.dirName}/${projectPackageName}")
                    variant.registerJavaGeneratingTask(tasks.create("i18nKotlin${variant.dirName.capitalize()}", I18nKotlinGeneratorTask) {
                        langFolder = inputFolder
                        outputFolder = srcOutputDir
                        packageName = projectPackageName
                    }, srcOutputDir)

                    //Register task to generate the strings xml file
                    File resOutputDir = new File(project.buildDir.path, "/generated/res/i18n/${variant.dirName}")
                    variant.registerResGeneratingTask(tasks.create("i18nXml${variant.dirName.capitalize()}", I18nXmlGeneratorTask) {
                        langFolder = inputFolder
                        outputFolder = resOutputDir
                    }, resOutputDir)
                    if (variant.name == "debug") {
                        android.sourceSets.debug.res.srcDirs += resOutputDir
                    }
                }
            }
        }

    }
}
