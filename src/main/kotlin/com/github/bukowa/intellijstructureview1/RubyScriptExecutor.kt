package com.github.bukowa.intellijstructureview1.utils
import com.intellij.util.ResourceUtil
import org.jruby.Ruby
import org.jruby.RubyArray
import org.jruby.RubyFile
import org.jruby.embed.ScriptingContainer
import org.jruby.javasupport.JavaEmbedUtils
import org.jruby.runtime.builtin.IRubyObject
import java.io.File
import java.util.ResourceBundle

object RubyScriptExecutor {

    fun _loadRubyScript(basePath: String, fileName: String): String? {
        val basePath = basePath.replace("\\", "/")
        val fileName = fileName.replace("\\", "/")
        val inputStream = ResourceUtil.getResourceAsStream(this.javaClass.classLoader, basePath, fileName)
        // to get full path
        // container.loadPaths = listOf<String>(ResourceUtil.getResource(this.javaClass.classLoader, "etwng/ui", "").path);
        return inputStream?.bufferedReader()?.use {
            val scriptContent = it.readText()
            // Remove the shebang line (if it exists) at the start of the script
            scriptContent.replaceFirst("#!/usr/bin/env ruby", "")  // Remove the first occurrence
        }
    }

    fun executeLoadUi2XmlScript(uiFile: String, outputXmlFile: String) {
        val rubyScript = _loadRubyScript("etwng/ui/final", "ui2xml.rb")
        if (rubyScript == null) {
            println("Failed to load Ruby script")
            // throw error showing what script was attempted to be loaded
            throw IllegalArgumentException("Failed to load Ruby script : etwng/ui/final/ui2xml.rb")
        }
        val container = ScriptingContainer()
        container.put("java_input_path", uiFile)
        container.classloaderDelegate = false
        container.put("java_output_path", outputXmlFile)
        try {
            val result = container.runScriptlet(rubyScript)
            println("Result from Ruby script: $result")
        }   catch (e: Exception) {
            println("Error executing Ruby script: $e")
            throw e
        }
    }

    fun executeLoadXmlToUiScript(xmlFile: String, outputUiFile: String) {
        val rubyScript = _loadRubyScript("etwng/ui/final", "xml2ui.rb")
        if (rubyScript == null) {
            println("Failed to load Ruby script")
            // throw error showing what script was attempted to be loaded
            throw IllegalArgumentException("Failed to load Ruby script")
        }
        val container = ScriptingContainer()
//        container.put("ARGV", arrayOf(xmlFile, outputUiFile))
        // get jruby path
        val pathToJrubyGems = listOf<String>(ResourceUtil.getResource(this.javaClass.classLoader, "gemsjruby", "").path).first()
        container.environment.set("GEM_PATH", pathToJrubyGems)
//        container.environment.set("GEM_HOME", "C:\\Users\\buk\\IdeaProjects\\intellij_structureview1\\gems")
//        container.environment.set("CLASSPATH", "C:\\Users\\buk\\IdeaProjects\\intellij_structureview1\\src\\main\\resources")
//        container.loadPaths.add("C:\\Users\\buk\\IdeaProjects\\intellij_structureview1\\src\\main\\resources\\etwng\\ui")
        container.put("java_input_path", xmlFile)
        container.put("java_output_path", outputUiFile)
        try {
            val result = container.runScriptlet(rubyScript)
//            val result = container.runScriptlet(org.jruby.embed.PathType.ABSOLUTE, "C:\\Users\\buk\\IdeaProjects\\intellij_structureview1\\src\\main\\resources\\etwng\\ui\\bin\\xml2ui")
            println("Result from Ruby script: $result")
        }   catch (e: Exception) {
            println("Error executing Ruby script: $e")
            throw e
        }
    }
}

