package com.github.mrshan23.pytestguard.display.generatedTests

import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import javax.swing.JPanel

class GeneratedTestsTabData {
    val testCaseIdToPanel: HashMap<Int, JPanel> = HashMap()
    val testCaseIdToEditorTextField: HashMap<Int, EditorTextField> = HashMap()
    val testCasePanelFactories: ArrayList<TestCasePanelBuilder> = arrayListOf()
    var allTestCasePanel: JPanel = JPanel()
    var scrollPane: JBScrollPane = JBScrollPane(
        allTestCasePanel,
        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
    )
    var topButtonsPanelBuilder = TopButtonsPanelBuilder()
    var contentManager: ContentManager? = null
    var content: Content? = null
}
