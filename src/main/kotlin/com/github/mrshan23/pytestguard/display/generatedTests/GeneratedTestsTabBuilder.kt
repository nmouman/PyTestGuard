package com.github.mrshan23.pytestguard.display.generatedTests

import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle
import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.display.CoverageVisualisationTabBuilder
import com.github.mrshan23.pytestguard.settings.PluginSettingsService
import com.github.mrshan23.pytestguard.test.TestFramework
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.serviceContainer.AlreadyDisposedException
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants


class GeneratedTestsTabBuilder(
    private val project: Project,
    private val testFramework: TestFramework,
    private val report: Report,
    private val coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
) {
    private val generatedTestsTabData: GeneratedTestsTabData = GeneratedTestsTabData()

    private var mainPanel: JPanel = JPanel()

    /**
     * Displays the generated tests tab in the tool window.
     * This method initializes necessary components based on the selected language and shows the tab.
     */
    fun show(contentManager: ContentManager) {
        generatedTestsTabData.allTestCasePanel.removeAll()
        generatedTestsTabData.allTestCasePanel.layout =
            BoxLayout(generatedTestsTabData.allTestCasePanel, BoxLayout.Y_AXIS)
        generatedTestsTabData.testCaseIdToPanel.clear()

        generatedTestsTabData.contentManager = contentManager

        fillMainPanel()

        fillAllTestCasePanel()

        createToolWindowTab()
    }

    /**
     * Initializes and fills the main panel with subcomponents.
     */
    private fun fillMainPanel() {
        mainPanel.layout = BorderLayout()

        mainPanel.add(
            generatedTestsTabData.topButtonsPanelBuilder.getPanel(),
            BorderLayout.NORTH,
        )
        mainPanel.add(generatedTestsTabData.scrollPane, BorderLayout.CENTER)
    }

    /**
     * Initializes and fills the main panel with subcomponents.
     */
    private fun fillAllTestCasePanel() {
        val testCasePanelFactories = arrayListOf<TestCasePanelBuilder>()

        report.testCaseList.values.forEach {
            val testCase = it
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            val testCasePanelBuilder =
                TestCasePanelBuilder(
                    project,
                    testCase,
                    report,
                    testFramework,
                    generatedTestsTabData,
                    coverageVisualisationTabBuilder
                )

            val settingsState = project.service<PluginSettingsService>().state
            val enableSimpleMode = settingsState.enableSimpleMode

            if (!enableSimpleMode) {
                testCasePanel.add(testCasePanelBuilder.getUpperPanel(), BorderLayout.NORTH)
            }

            testCasePanel.add(testCasePanelBuilder.getMiddlePanel(), BorderLayout.CENTER)

            testCasePanelFactories.add(testCasePanelBuilder)

            testCasePanel.add(Box.createRigidArea(Dimension(12, 0)), BorderLayout.EAST)
            testCasePanel.add(Box.createRigidArea(Dimension(12, 0)), BorderLayout.WEST)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            generatedTestsTabData.allTestCasePanel.add(testCasePanel)
            addSeparator()

            generatedTestsTabData.testCaseIdToPanel[testCase.id!!] = testCasePanel
            generatedTestsTabData.testCaseIdToEditorTextField[testCase.id!!] =
                testCasePanelBuilder.getEditorTextField()
        }
        generatedTestsTabData.testCasePanelFactories.addAll(testCasePanelFactories)
        generatedTestsTabData.topButtonsPanelBuilder.update(generatedTestsTabData)
    }

    /**
     * Adds a visual separator component to the panel to distinguish sections.
     */
    private fun addSeparator() {
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
        generatedTestsTabData.allTestCasePanel.add(JSeparator(SwingConstants.HORIZONTAL))
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
    }

    /**
     * Creates a new tab in the tool window for displaying the generated tests.
     */
    private fun createToolWindowTab() {
        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("PyTestGuard")
        generatedTestsTabData.contentManager = toolWindowManager!!.contentManager
        if (generatedTestsTabData.content != null) {
            generatedTestsTabData.contentManager!!.removeContent(generatedTestsTabData.content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        generatedTestsTabData.content = contentFactory.createContent(
            mainPanel,
            PluginLabelsBundle.get("testsGenerated"),
            true,
        )
        generatedTestsTabData.contentManager!!.addContent(generatedTestsTabData.content!!)
        generatedTestsTabData.contentManager!!.setSelectedContent(generatedTestsTabData.content!!)

        toolWindowManager.show()
    }

    /**
     * Closes the tool window by removing the content and hiding the window.
     */
    private fun closeToolWindow() {
        try {
            generatedTestsTabData.contentManager?.removeContent(generatedTestsTabData.content!!, true)
            ToolWindowManager.getInstance(project).getToolWindow("PyTestGuard")?.hide()
            coverageVisualisationTabBuilder.closeToolWindowTab()
        } catch (_: AlreadyDisposedException) {
        } // Make sure the process continues if the tool window is already closed
    }

    /**
     * Clears all the generated test cases from the UI and the internal cache.
     */
    fun clear() {
        generatedTestsTabData.testCaseIdToPanel.toMap()
            .forEach { GenerateTestsTabHelper.removeTestCase(it.key, generatedTestsTabData) }
        generatedTestsTabData.testCasePanelFactories.clear()
        generatedTestsTabData.topButtonsPanelBuilder.clear(generatedTestsTabData)

        closeToolWindow()
    }
}
