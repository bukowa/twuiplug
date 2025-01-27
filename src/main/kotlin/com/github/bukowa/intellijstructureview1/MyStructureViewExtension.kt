package com.github.bukowa.intellijstructureview1

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewExtension
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.xml.XmlTagTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.ItemPresentationProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ide.structureView.xml.XmlStructureViewElementProvider
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.xml.XmlElementImpl
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.concurrency.AppExecutorUtil
import javax.swing.Icon


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
        // return array of custom tree elements with 10 elements
//        return Array(10) { MyStructureViewTreeElement() }
        return arrayOf()
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
            val iterator = root.children.iterator()

            while (iterator.hasNext()) {
                val element = iterator.next()

                if (element is XmlTagTreeElement) {
                    val key = element.key
                    if (key == "XmlTag:yes" || key == "XmlTag:no") {
                        println("Removing child with key: $key")
                    } else if (element.children.size > 0) {
                        // Recursively filter the children if not removed
                        filterChildElements(element) // Pass mutable list for safe removal
                    }
                }
            }
        }

        // Filter the baseChildren directly and remove unwanted elements
        AppExecutorUtil.getAppExecutorService().submit {
            WriteCommandAction.runWriteCommandAction(project) {
                val iterator = baseChildren.iterator()

                while (iterator.hasNext()) {
                    val element = iterator.next()

                    if (element is XmlTagTreeElement) {
                        filterChildElements(element)
                    }

                }
            }

        }
        // Call the super method if needed
//    super.filterChildren(baseChildren, extensionChildren)
    }
}

class MyStructureViewTreeElement : StructureViewTreeElement {
    override fun getPresentation(): ItemPresentation {
        return MyItemPresentation()
    }

    override fun getChildren(): Array<TreeElement> {
        return arrayOf()
    }

    override fun getValue(): Any {
        return this
    }

}

class MyItemPresentation : ItemPresentation {
    override fun getIcon(unused: Boolean) = AllIcons.Nodes.Test
    override fun getPresentableText(): String {
        return ""
    }
}

class MyXmlTagImpl : ItemPresentationProvider<XmlTagImpl> {

    override fun getPresentation(item: XmlTagImpl): ItemPresentation {

        return PresentationData(
            "asd",
            item.containingFile.name,
            item.getIcon(0),
            null
        )
    }
}

class MyCustomXmlStructureViewElementProvider : XmlStructureViewElementProvider {
    override fun createCustomXmlTagTreeElement(p0: XmlTag): StructureViewTreeElement? {
        // Apply custom logic for specific tags
        if (p0.name == "uientry") {
            return MyCustomXmlTagTreeElement(p0)
        }
        // Return null for default handling of other tags
        return null
    }
}

class MyCustomXmlTagTreeElement(private val xmlTag: XmlTag) : StructureViewTreeElement {
    override fun getValue(): Any = xmlTag

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                // Customize how the tag appears in the Structure View
                return "<${xmlTag.name}> (i added this shit)"
            }

            override fun getLocationString(): String? = xmlTag.containingFile.name
            override fun getIcon(unused: Boolean): Icon? = null // Use a custom icon if desired
        }
    }

    override fun getChildren(): Array<StructureViewTreeElement> {
        // Recursively process children if needed
        return xmlTag.subTags.map { MyCustomXmlTagTreeElement(it) }.toTypedArray()
    }

}
