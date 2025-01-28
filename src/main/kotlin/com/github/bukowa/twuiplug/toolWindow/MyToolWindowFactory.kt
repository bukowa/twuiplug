package com.github.bukowa.twuiplug.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.bukowa.twuiplug.MyBundle
import com.github.bukowa.twuiplug.services.MyProjectService
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.ui.components.JBTextField
import com.jetbrains.JBRFileDialog
import javax.swing.JButton


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()
        val project = toolWindow.project;

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val label = JBLabel(MyBundle.message("randomLabel", "?"))

            add(label)
            add(JButton(MyBundle.message("shuffle")).apply {
                addActionListener {
                    val descriptor = FileChooserDescriptor(
                        true,  // chooseFiles
                        false, // chooseFolders
                        false, // chooseJars
                        false, // chooseJarsAsFiles
                        false, // chooseJarContents
                        false  // chooseMultiple
                    )
                    label.text = MyBundle.message("randomLabel", service.getRandomNumber())
                    val chooser = FileChooser.chooseFile(descriptor, project, null)
                }
            })
        }
    }
}
