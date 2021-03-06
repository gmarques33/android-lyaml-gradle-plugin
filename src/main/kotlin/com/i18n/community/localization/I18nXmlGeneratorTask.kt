package com.i18n.community.localization

import org.yaml.snakeyaml.Yaml
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory

open class I18nXmlGeneratorTask : DefaultTask() {

  @OutputDirectory
  lateinit var outputFolder: File

  @InputDirectory
  lateinit var langFolder: File

  @TaskAction
  fun generateStringsXml() {
    listOf(
      "en" to "values",
      "pt-br" to "values-pt-br",
      "de" to "values-de",
      "es" to "values-es",
      "fr" to "values-fr",
      "ja" to "values-ja"
    ).forEach { (input, resourcesFolder) -> parse("$input.lyaml", "${outputFolder.absolutePath}/$resourcesFolder") }
  }

  private fun parse(filename: String, outputFolder: String) {
    val lyamlFile = File("${langFolder.absolutePath}/$filename")
    if (!lyamlFile.exists()) return

    val map = Yaml().load<Map<String, Any>>(lyamlFile.inputStream())
    val lang = map.keys.first()
    val strings = parse(map[lang] as LinkedHashMap<String, Any>)
    File(outputFolder).mkdirs()
    File(outputFolder, "generated_strings.xml").printWriter().use { file ->
      file.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
      file.println("<resources>")
      strings.forEach(file::println)
      file.println("</resources>")
    }
  }

  private fun parse(node: LinkedHashMap<String, Any>, parentKey: String? = null): List<String> {
    val strings = mutableListOf<String>()

    node
      .filterValues { (it as? String)?.contains("{{") != true }
      .forEach { key, value ->
        if (value is String) {
          strings.add(
            "<string name=\"${parentKey?.plus("_").orEmpty()}$key\">${value.replace("\'", "\\'").replace(
              "&",
              "&amp;"
            )}</string>"
          )
        } else {
          strings.addAll(parse(value as LinkedHashMap<String, Any>, "${parentKey?.plus("_").orEmpty()}$key"))
        }
      }

    return strings
  }
}
