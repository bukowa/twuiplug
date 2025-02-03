package com.github.bukowa.twuiplug

import com.intellij.openapi.components.service
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.bukowa.twuiplug.services.MyProjectService
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Paths

@TestDataPath("\$CONTENT_ROOT/testdata")
class MyPluginTest : BasePlatformTestCase() {

    fun testProjectService() {
        val projectService = project.service<MyProjectService>()
        assertNotSame(projectService.getRandomNumber(), projectService.getRandomNumber())
    }

    override fun getTestDataPath(): String {
        return Paths.get("src/test/testdata").toAbsolutePath().normalize().toString()
    }

    fun writeVirtualFileToTempFile(virtualFile: VirtualFile): File {
        require(virtualFile.isValid) { "The virtual file is not valid." }

        // Create a temporary file
        val tempFile = File.createTempFile("virtual_file", ".tmp")
        tempFile.deleteOnExit() // Ensure the temp file is deleted when the program exits

        // Write the content of the VirtualFile to the temp file
        virtualFile.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("Virtual file written to: ${tempFile.absolutePath}")
        return tempFile
    }

    fun getNewTestFile(path: String): File {
        return writeVirtualFileToTempFile(myFixture.copyFileToProject(path))
    }

    fun test_rubyScriptExecution() {
        val original_binary_file = getNewTestFile("options_mods")
        val original_xml_file = getNewTestFile("options_mods.xml")

        if (original_binary_file.length() < 1024 || original_xml_file.length() < 1024) {
            throw Exception("invalid files")
        }

        val xml_from_binary = File.createTempFile("file", "")
        RubyScriptExecutor.executeUi2XmlScript(original_binary_file.path, xml_from_binary.path)
        assert(areFilesEqual(xml_from_binary, original_xml_file))

        val binary_from_xml = File.createTempFile("file2", "")
        RubyScriptExecutor.executeXml2UiScript(xml_from_binary.path, binary_from_xml.path)
        assert(areFilesEqual(binary_from_xml, original_binary_file))
    }


    fun areFilesEqual(file1: File, file2: File): Boolean {
        val file1Size = file1.length()
        val file2Size = file2.length()

        // Check file sizes first
        if (file1Size != file2Size) {
            println("File sizes are different:")
            println("- ${file1.absolutePath}: $file1Size bytes")
            println("- ${file2.absolutePath}: $file2Size bytes")
            return false
        }

        // Read file contents
        val file1Bytes = file1.readBytes()
        val file2Bytes = file2.readBytes()

        // Compare byte by byte and log differences
        for (i in file1Bytes.indices) {
            if (file1Bytes[i] != file2Bytes[i]) {
                println("Files differ at byte $i:")
                println("- File 1: ${file1Bytes[i]} (${file1.absolutePath})")
                println("- File 2: ${file2Bytes[i]} (${file2.absolutePath})")
                return false
            }
        }

        println("Files are equal: ${file1.absolutePath} and ${file2.absolutePath}")
        return true
    }

    fun assertFilesEqual(xmlFromBinary: File, originalXmlFile: File) {
        require(areFilesEqual(xmlFromBinary, originalXmlFile)) {
            "Files are not equal:\n" +
                    "- File 1: ${xmlFromBinary.absolutePath} (size: ${xmlFromBinary.length()} bytes)\n" +
                    "- File 2: ${originalXmlFile.absolutePath} (size: ${originalXmlFile.length()} bytes)"
        }
    }

}
