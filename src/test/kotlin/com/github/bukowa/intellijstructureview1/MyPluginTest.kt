package com.github.bukowa.intellijstructureview1

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.components.service
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.github.bukowa.intellijstructureview1.services.MyProjectService
import com.github.bukowa.intellijstructureview1.utils.RubyScriptExecutor
import com.intellij.testFramework.TestDataFile
import org.jruby.embed.ScriptingContainer
import java.io.File
import java.nio.file.Paths
import java.time.Instant

@TestDataPath("\$CONTENT_ROOT/testdata")
class MyPluginTest : BasePlatformTestCase() {

    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))
        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }

    fun testProjectService() {
        val projectService = project.service<MyProjectService>()

        assertNotSame(projectService.getRandomNumber(), projectService.getRandomNumber())
    }

    fun testJRuby() {
//        myFixture.copyFileToProject("options_mods.xml")
        RubyScriptExecutor.executeLoadUi2XmlScript("${testDataPath}/options_mods", "${testDataPath}/options_mods.xml")
    }
    override fun getTestDataPath(): String {
        return Paths.get("src/test/testdata").toAbsolutePath().normalize().toString()
    }

    fun testJRuby2() {

//        myFixture.copyFileToProject("options_mods.xml")
        RubyScriptExecutor.executeLoadXmlToUiScript("${testDataPath}\\options_mods.xml", "${testDataPath}\\options_mods")
    }
}
