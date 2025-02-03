package com.github.bukowa.twuiplug.actions

import com.github.bukowa.twuiplug.RubyScriptExecutor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

private fun collectFiles(
    file: VirtualFile,
    files: MutableList<VirtualFile>
) {
    @Suppress("UnsafeVfsRecursion")
    if (file.isDirectory) file.children.forEach {
        collectFiles(it, files)
    }
    else files += file
}

class ConvertToXmlAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        if (files.isNullOrEmpty()) Messages.showErrorDialog("Please select some files...", "Error")

        val allFiles = mutableListOf<VirtualFile>()
        files?.forEach { file -> collectFiles(file, allFiles) }

        fun runCommandInBackground(command: String) {
            ApplicationManager.getApplication().executeOnPooledThread {
                println("Executing command: $command")
                // Simulate task execution
                Thread.sleep(1000)  // Simulating work (replace with actual command)
                println("Command completed: $command")
            }
        }

        files?.forEach { file -> runCommandInBackground(file.name) }
        val task = MyBackgroundTask(event.project!!)
        ProgressManager.getInstance().run(task)
        return
    }

}


class MyBackgroundTask(project: Project) : Task.Backgroundable(project, "Running Command") {

    override fun run(indicator: ProgressIndicator) {
        val commands = listOf("command1", "command2", "command3")

        // Simulate running multiple commands with progress feedback
        for ((index, command) in commands.withIndex()) {
            if (indicator.isCanceled) return  // Check if the task was canceled

            indicator.text = "Executing $command"
            println("Executing command: $command")
            Thread.sleep(1000)  // Simulate command execution
            println("Command completed: $command")
            indicator.fraction = (index + 1) / commands.size.toDouble()  // Update progress
            Notifications.Bus.notify(
                Notification("com.github.bukowa.twuiplug", "error1", "error2", NotificationType.ERROR),
            )
        }
    }
}
