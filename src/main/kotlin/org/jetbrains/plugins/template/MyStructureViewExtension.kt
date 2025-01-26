package org.jetbrains.plugins.template

import com.intellij.ide.structureView.StructureViewExtension
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class MyStructureViewExtension : StructureViewExtension {

    override fun getType(): Class<out PsiElement> {
        println("getType() called")
        return PsiFile::class.java
    }

    override fun getChildren(parent: PsiElement?): Array<StructureViewTreeElement> {
        println("getChildren() called with parent: ${parent?.javaClass?.simpleName}")
        // If parent is null, we can log that as well
        if (parent == null) {
            println("Parent is null")
        }
        return arrayOf() // Return an empty array
    }

    override fun getCurrentEditorElement(editor: Editor?, parent: PsiElement?): Any? {
        println("getCurrentEditorElement() called with editor: $editor and parent: ${parent?.javaClass?.simpleName}")
        return null
    }
}
