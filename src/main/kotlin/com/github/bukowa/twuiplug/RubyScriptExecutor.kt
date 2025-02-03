package com.github.bukowa.twuiplug

import com.intellij.util.ResourceUtil
import org.jruby.embed.ScriptingContainer

object RubyScriptExecutor {

    private fun String.clearPath() = replace("\\", "/")

    // path where jruby will find gems
    fun getJrubyGemsPath(): String {
        Thread.currentThread().contextClassLoader = this.javaClass.classLoader;
        val p = listOf<String>(Thread.currentThread().contextClassLoader.getResource("gemsjruby").path).first()
        if (p == "") {
            throw Exception("cannot find gems path for jruby")
        }
        return p
    }

    // read file and return string content of it
    private fun readResourceFileToString(fileName: String): String {
        val basePath = "etwng/ui/final"

        val inputStream = ResourceUtil.getResourceAsStream(
            this.javaClass.classLoader,
            basePath.clearPath(),
            fileName.clearPath()
        )

        if (inputStream == null) {
            throw Exception("cannot open $fileName in $basePath")
        }

        return inputStream.bufferedReader().use { it.readText() }

    }

    private fun runScriptlet(container : ScriptingContainer, rubyScript: String) {
        try {
            val result = container.runScriptlet(rubyScript)
            if (result != "OK0") {
                throw Exception("Ruby script returned $result")
            }
        } catch (e: Exception) {
            println("Error executing Ruby script: $e")
            throw e
        }
    }

    private fun executeRubyScript(argv1: String, argv2: String, path: String) {
        val rubyScript = readResourceFileToString(path)

        val container = ScriptingContainer()
        // environment has to be set before interacting with `put`
        container.environment.set("GEM_PATH", getJrubyGemsPath())
        container.put("java_input_path", argv1)
        container.put("java_output_path", argv2)
        return runScriptlet(container, rubyScript)
    }

    fun executeXml2UiScript(argv1: String, argv2: String) {
        return executeRubyScript(argv1, argv2, "xml2ui.rb")
    }

    fun executeUi2XmlScript(argv1: String, argv2: String) {
        return executeRubyScript(argv1, argv2, "ui2xml.rb")
    }

}

