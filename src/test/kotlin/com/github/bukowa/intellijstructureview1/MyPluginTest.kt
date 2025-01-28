package com.github.bukowa.intellijstructureview1

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.components.service
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.github.bukowa.intellijstructureview1.services.MyProjectService
import com.github.bukowa.intellijstructureview1.utils.RubyScriptExecutor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.PsiTestUtil.addContentRoot
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
        if (file1.length() != file2.length()) {
            return false  // Files are different if their lengths don't match
        }

        return file1.readBytes().contentEquals(file2.readBytes())
    }
}
