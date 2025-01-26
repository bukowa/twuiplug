package com.github.bukowa.intellijstructureview1

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.xml.XmlTagTreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.xml.XmlFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.jetbrains.rd.util.remove


class MyStructureViewExtension : StructureViewExtension {
    private var project: Project? = null

    override fun getType(): Class<out PsiElement> {
        println("getType() called")
        return XmlFile::class.java
    }

    override fun getChildren(parent: PsiElement?): Array<StructureViewTreeElement> {
        println("getChildren() called with parent: ${parent?.javaClass?.simpleName}")
        // If parent is null, we can log that as well
        if (parent == null) {
            println("Parent is null")
        }
        if (parent != null) {
            project = parent.project
        }
        return arrayOf() // Return an empty array
    }

    override fun getCurrentEditorElement(editor: Editor?, parent: PsiElement?): Any? {
        println("getCurrentEditorElement() called with editor: $editor and parent: ${parent?.javaClass?.simpleName}")
        return null
    }

    override fun filterChildren(
        baseChildren: MutableCollection<StructureViewTreeElement>,
        extensionChildren: MutableList<StructureViewTreeElement>
    ) {
        println("filterChildren() called")
        println("Base children count: ${baseChildren.size}")
        println("Extension children count: ${extensionChildren.size}")

        // Function to recursively filter children and collect elements to remove
        fun filterChildElements(root: XmlTagTreeElement) {
            // Use an iterator to safely remove elements while iterating
            AppExecutorUtil.getAppExecutorService().submit {
                ApplicationManager.getApplication().runReadAction {

                    val iterator = root.children.iterator()

                    while (iterator.hasNext()) {
                        val element = iterator.next()

                        if (element is XmlTagTreeElement) {
                            val key = element.key
                            if (key == "XmlTag:yes" || key == "XmlTag:no") {
                                println("Removing child with key: $key")
                                AppExecutorUtil.getAppExecutorService().submit {
                                    WriteCommandAction.runWriteCommandAction(project) {
                                        (root.element as CompositeElement).removeChild(element.element!!.node)
                                    }
                                }
                            } else {
                                // Recursively filter the children if not removed
                                filterChildElements(element) // Pass mutable list for safe removal
                            }
                        }
                    }
                }
                // Filter the baseChildren directly and remove unwanted elements
                val iterator = baseChildren.iterator()
                while (iterator.hasNext()) {
                    val element = iterator.next()
                    if (element is XmlTagTreeElement) {
                        filterChildElements(element)
                    }
                }
            }
        }
    }
}
