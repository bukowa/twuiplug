package com.github.bukowa.twuiplug.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.ui.components.JBTextField
import javax.swing.JButton
import javax.swing.BoxLayout
import com.github.bukowa.twuiplug.settings.MyPluginSettings
import com.intellij.openapi.components.service

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val project = toolWindow.project
        private val settings = project.service<MyPluginSettings>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

            // Path to rpfm_cli.exe
            add(JBLabel("Path to rpfm_cli.exe:"))
            val exeTextField = JBTextField(settings.getExePath())
            add(exeTextField)

            add(JButton("Browse...").apply {
                addActionListener {
                    val exeFileChooser = FileChooserDescriptor(true, false, false, false, false, false)
                    val exeFile = FileChooser.chooseFile(exeFileChooser, project, null)
                    exeFile?.let {
                        exeTextField.text = it.path
                        settings.setExePath(it.path)  // Save to settings
                    }
                }
            })

            // Path to mods folder
            add(JBLabel("Build '.pack' file from directory:"))
            val modsTextField = JBTextField(settings.getModsFolderPath())
            add(modsTextField)

            add(JButton("Browse...").apply {
                addActionListener {
                    val modsFolderChooser = FileChooserDescriptor(false, true, false, false, false, false)
                    val modsFolder = FileChooser.chooseFile(modsFolderChooser, project, null)
                    modsFolder?.let {
                        modsTextField.text = it.path
                        settings.setModsFolderPath(it.path)  // Save to settings
                    }
                }
            })

            // Path to output folder
            add(JBLabel("Save 'pack' file to:"))
            val outputTextField = JBTextField(settings.getOutPutPath())
            add(outputTextField)

            add(JButton("Browse...").apply {
                addActionListener {
                    val outputFolderChooser = FileChooserDescriptor(false, true, false, false, false, false)
                    val outputFolder = FileChooser.chooseFile(outputFolderChooser, project, null)
                    outputFolder?.let {
                        outputTextField.text = it.path
                        settings.setOutPutPath(it.path)  // Save to settings
                    }
                }
            })

        }
    }
}
