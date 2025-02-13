package com.github.bukowa.twuiplug.actions

import com.github.bukowa.twuiplug.RubyScriptExecutor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

data class FilePathMapping(
  val inputFile: VirtualFile,
  val inputFilePath: String,
  val outputFilePath: String,
)

open class ConvertUiFilesAction(
  private var destinationPathFunc: (VirtualFile) -> String,
  private var filterFileFunc: (VirtualFile) -> Boolean = { true },
  private var action: (FilePathMapping) -> Unit,
) : AnAction() {

  private fun collectFiles(
    file: VirtualFile,
    files: MutableList<VirtualFile>,
    filter: (VirtualFile) -> Boolean = { true },
  ) {
    @Suppress("UnsafeVfsRecursion")
    if (file.isDirectory) file.children.forEach { collectFiles(it, files) }
    else if (filter(file)) files += file
  }

  override fun actionPerformed(event: AnActionEvent) {

    val userSelectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

    // nothing selected, return
    if (userSelectedFiles.isNullOrEmpty()) {
      Messages.showErrorDialog("Please select some files...", "Error")
      return
    }

    // collect files that are about to be processed
    val allFiles = mutableListOf<VirtualFile>()
    userSelectedFiles.forEach { collectFiles(it, allFiles, filterFileFunc) }
    var filesToProcess = mutableListOf<FilePathMapping>()

    // perform checks and collect properly
    fileIteration@ for (file in allFiles) {
      val destinationPath = destinationPathFunc(file)
      val fileExists = file.fileSystem.findFileByPath(destinationPath) != null

      when (fileExists) {
        true -> {
          val choice =
            Messages.showDialog(
              "$destinationPath already exists. Override?",
              "Override?",
              arrayOf("Override", "Override All", "Skip", "Cancel"),
              0,
              Messages.getWarningIcon(),
              null,
            )

          when (choice) {
            0 -> {
              filesToProcess += FilePathMapping(file, file.path, destinationPath)
            }
            1 -> {
              filesToProcess =
                allFiles
                  .map { FilePathMapping(it, it.path, destinationPathFunc(it)) }
                  .toMutableList()
              break@fileIteration
            }
            2 -> continue
            else -> return
          }
        }
        false -> {
          filesToProcess += FilePathMapping(file, file.path, destinationPath)
        }
      }
    }

    ProgressManager.getInstance()
      .run(FileProcessingTask(event.project!!, filesToProcess, action))
    return
  }
}

class ConvertXmlToUIAction :
  ConvertUiFilesAction(
    destinationPathFunc = { file ->
      "${file.parent.path}/${file.nameWithoutExtension}"
    },
    filterFileFunc = { file -> file.extension == "xml" },
    action = { fileMapping ->
      RubyScriptExecutor.executeXml2UiScript(
        fileMapping.inputFilePath,
        fileMapping.outputFilePath,
      )
    },
  )

class ConvertUIToXmlAction :
  ConvertUiFilesAction(
    destinationPathFunc = { file ->
      "${file.parent.path}/${file.nameWithoutExtension}.xml"
    },
    filterFileFunc = { file -> file.extension == null },
    action = { fileMapping ->
      RubyScriptExecutor.executeUi2XmlScript(
        fileMapping.inputFilePath,
        fileMapping.outputFilePath,
      )
    },
  )

class FileProcessingTask(
  project: Project,
  private val files: List<FilePathMapping>,
  private val action: (FilePathMapping) -> Unit,
) : Task.Backgroundable(project, "Processing files") {

  override fun run(indicator: ProgressIndicator) {
    if (files.isEmpty()) return

    for ((index, file) in files.withIndex()) {
      if (indicator.isCanceled) return // Check if the task was canceled

      indicator.text = "Processing file: ${file.inputFilePath}"
      try {
        action(file)
        Notifications.Bus.notify(
          Notification(
            "com.github.bukowa.twuiplug",
            "File processed",
            "File ${file.inputFilePath} processed successfully",
            NotificationType.INFORMATION,
          )
        )
      } catch (e: Exception) {
        Notifications.Bus.notify(
          Notification(
            "com.github.bukowa.twuiplug",
            "File processing error",
            "Error processing file: ${file.inputFilePath}. Details: ${e.message}",
            NotificationType.ERROR,
          )
        )
      }
      indicator.fraction = (index + 1) / files.size.toDouble() // Update progress
    }

    VirtualFileManager.getInstance().asyncRefresh()
  }
}
