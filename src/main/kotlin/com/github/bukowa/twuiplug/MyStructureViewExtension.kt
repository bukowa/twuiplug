package com.github.bukowa.twuiplug

import com.intellij.ide.structureView.StructureViewExtension
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.xml.XmlTagTreeElement
import com.intellij.ide.structureView.xml.XmlStructureViewElementProvider
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.concurrency.AppExecutorUtil
import javax.swing.Icon
import kotlinx.collections.immutable.toImmutableSet

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
    println(
      "getCurrentEditorElement() called with editor: $editor and parent: ${parent?.javaClass?.simpleName}"
    )
    return null
  }

  override fun filterChildren(
    baseChildren: MutableCollection<StructureViewTreeElement>,
    extensionChildren: MutableList<StructureViewTreeElement>,
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

class MyCustomXmlStructureViewElementProvider : XmlStructureViewElementProvider {
  override fun createCustomXmlTagTreeElement(p0: XmlTag): StructureViewTreeElement? {
    // Apply custom logic for specific tags
    //        if (p0.name == "uientry") {
    return MyCustomXmlTagTreeElement(p0)
    //        }
    // Return null for default handling of other tags
  }
}

class MyCustomXmlTagTreeElement(private val xmlTag: XmlTag) : XmlTagTreeElement(xmlTag) {
  override fun getValue(): XmlTag? = xmlTag

  private companion object {
    private val countableElements =
      listOf(
        "children",
        "funcs",
        "states",
        "mouse_states",
        "image_uses",
        "images",
        "properties"
      )
  }

  override fun getPresentation(): ItemPresentation {
    return object : ItemPresentation {

      override fun getPresentableText(): String {
        val childrenCount = xmlTag.subTags.size

        // Customize how the tag appears in the Structure View
        if (xmlTag.name == "s") {
          return "<${xmlTag.value.text}>"
        }

        // if tag named in countable print with count
        if (xmlTag.name in countableElements) {
          return "($childrenCount): <${xmlTag.name}>"
        }

        // TITLE FILTER
        // if tag named uientry get first child "s" value if not empty
        if (xmlTag.name == "uientry") {
          return "uientry: <${xmlTag.subTags.firstOrNull { it.name == "s" }?.value?.text ?: ""}>"
        }

        // if element named image and first child is <u> show its id
        if (xmlTag.name == "image" && xmlTag.subTags.firstOrNull()?.name == "u") {
          return "image: <${xmlTag.subTags.firstOrNull()?.value?.text ?: ""}>"
        }

        // if element named state and first child is <u> show its id
        if (xmlTag.name == "state" && xmlTag.subTags.firstOrNull()?.name == "u") {
          return "state: <${xmlTag.subTags.firstOrNull()?.value?.text ?: ""}>"
        }

        // if element is additional_data and has type other than none
        if (xmlTag.name == "additional_data") {
             return "additional_data: <${xmlTag.getAttributeValue("type")}>"
        }

        return "<${xmlTag.name}>"
      }

      override fun getIcon(p0: Boolean): Icon? {
        val element: PsiElement? = xmlTag
        if (element == null) {
          return null
        } else {
          var flags = 2
          if (element !is PsiFile || !element.isWritable) {
            flags = flags or 1
          }
          return element.getIcon(flags)
        }
      }
    }
  }

  override fun getChildrenBase(): Collection<StructureViewTreeElement?> {
    val children = super.getChildrenBase()
    //        val notAllowedNames = listOf("yes", "no")

    val notAllowedNames2 =
      listOf("i", "unicode", "byte", "u", "color", "flt", "yes", "no", "normal_t0")

    // Create a new collection to hold the removed children
    //        val removedChildren = mutableListOf<TreeElement>()
    //
    //        // Filter and collect elements with XmlTagImpl name "yes" or "no", and remove them
    // from the original list
    //        val filteredChildren = children.filterNot { child ->
    //            (
    //                    child as? MyCustomXmlTagTreeElement)?.xmlTag?.name in notAllowedNames &&
    // removedChildren.add(
    //                child
    //            )
    //        }.toMutableList()  // Convert filteredChildren to a mutable list

    // Now 'filteredChildren' contains all elements except those with the names "yes" or "no".
    // 'removedChildren' contains all the elements that were removed.

    //        if (removedChildren.isNotEmpty()) {
    //            val x = MyTreeElement()
    //            x.addChildren(removedChildren)
    //            filteredChildren.add(0, x)
    //        }

    fun filterByNameAndValue(element: MyCustomXmlTagTreeElement): Boolean {
      return element.xmlTag.name == "s" &&
        (element.xmlTag.value.text.isEmpty() || element.xmlTag.value.text == "normal_t0")
    }

    fun filterByEmptyCountable(element: MyCustomXmlTagTreeElement): Boolean {
      return element.xmlTag.name in countableElements && element.xmlTag.subTags.size == 0
    }

    // TITLE FILTER
    fun filterTitleIfNotEmptyAndFirstchild(element: MyCustomXmlTagTreeElement): Boolean {
      return element.xmlTag.name == "s" &&
        element.xmlTag.parentTag?.subTags?.get(1) == element.xmlTag
    }

    // if additional data prop type == none
    fun filterAdditionalData(element: MyCustomXmlTagTreeElement): Boolean {
      return element.xmlTag.name == "additional_data" &&
        element.xmlTag.getAttributeValue("type") == "none"
    }

    // todo use xml comment
    // filter fonts (tag s and value match)
    val fontValues =
      listOf(
        "Default Font Category",
        "fe_section_heading_yellow",
        "fe_tooltip_text",
        "fe_page_heading",
        "ingame_tooltip",
        "la_gioconda_uppercase",
        "georgia_italic",
        "fe_italic",
        "la_gioconda"
      )

    fun filterFont(element: MyCustomXmlTagTreeElement): Boolean {
      return element.xmlTag.name == "s" && element.xmlTag.value.text in fontValues
    }

    val customFilters: List<(MyCustomXmlTagTreeElement) -> Boolean> =
      listOf(
        ::filterByNameAndValue,
        ::filterByEmptyCountable,
        ::filterTitleIfNotEmptyAndFirstchild,
        ::filterAdditionalData,
        ::filterFont,
      )

    return children
      .filterNot { child ->
        // Cast once and store the result
        val customElement = child as? MyCustomXmlTagTreeElement ?: return@filterNot false

        // Base check - filter by name
        if (customElement.xmlTag.name in notAllowedNames2) return@filterNot true

        // Check against all additional predicates
        customFilters.any { filter -> filter(customElement) }
      }
      .toImmutableSet()
  }

  class MyTreeElement : StructureViewTreeElement {

    // Initialize removedChildren with a mutable list
    private var removedChildren: MutableCollection<TreeElement> = mutableListOf()

    // Function to add new children to removedChildren
    fun addChildren(children: Collection<TreeElement>) {
      removedChildren.addAll(children) // Adds all elements from 'children' collection
    }

    override fun getValue(): Any? {
      // Return the value that represents this element
      return "Some Value" // For example, this could be any data you'd like to associate with this
      // element
    }

    override fun getChildren(): Array<out TreeElement?> {
      // Return a collection of child elements (or empty if none)
      return removedChildren
        .toTypedArray() // No children in this example, but you could return a list of child
      // elements
    }

    override fun getPresentation(): ItemPresentation {
      return object : ItemPresentation {
        override fun getPresentableText(): @NlsSafe String? {
          return "flags"
        }

        override fun getIcon(p0: Boolean): Icon? {
          return null
        }
      }
    }

    override fun navigate(requestFocus: Boolean) {
      // Define how this element should be navigated to, if necessary
    }

    override fun canNavigate(): Boolean {
      return true // This element is navigable
    }

    override fun canNavigateToSource(): Boolean {
      return true // This element can navigate to its source
    }
  }
}
