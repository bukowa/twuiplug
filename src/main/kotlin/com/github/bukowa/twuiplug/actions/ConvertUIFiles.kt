import com.github.bukowa.twuiplug.utils.RubyScriptExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Paths

class ConvertToXmlAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file == null) {
            return
        }

        if (file.extension == "xml") {
            val outputPath = Paths.get(file.parent.path).resolve(file.nameWithoutExtension)  // Remove "xml" from name
            RubyScriptExecutor.executeXml2UiScript(file.path, outputPath.toString())
        }
    }

    private fun handleFileConversion(file: VirtualFile, project: Project) {
        // Call your custom function to convert the file to/from XML
    }

    private fun handleFolderConversion(folder: VirtualFile, project: Project) {
        // Call your custom function to convert the folder contents to/from XML
    }
}