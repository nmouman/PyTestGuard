package com.github.mrshan23.pytestguard.display

import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle
import com.github.mrshan23.pytestguard.settings.PluginSettingsService
import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.table.AbstractTableModel

class CoverageVisualisationTabBuilder(
    private val project: Project,
) {

    private var content: Content? = null
    private var contentManager: ContentManager? = null

    private var mainScrollPane: JScrollPane? = null


    fun show(executionResult: ExecutionResult) {
        fillToolWindowContents(executionResult)
        createToolWindowTab()
    }


    private fun fillToolWindowContents(executionResult: ExecutionResult) {
        mainScrollPane = getPanel(
            arrayListOf(
                "${executionResult.statementCoverage}%",
                "${executionResult.statementCoverageChange}%",
            ),
        )
    }

    private fun createToolWindowTab() {
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("PyTestGuard")
        contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            contentManager!!.removeContent(content!!, true)
        }

        val contentFactory: ContentFactory = ContentFactory.getInstance()
        content = contentFactory.createContent(
            mainScrollPane,
            PluginLabelsBundle.get("coverageVisualisation"),
            true,
        )
        contentManager!!.addContent(content!!)
    }

    private fun getPanel(data: ArrayList<String>): JScrollPane {
        val settingsState = project.service<PluginSettingsService>().state
        val enableCoverageChangeMode = settingsState.enableCoverageChangeMode

        val tableModel = object : AbstractTableModel() {

            override fun getRowCount(): Int {
                return 1
            }

            override fun getColumnCount(): Int {
                if (enableCoverageChangeMode) {
                    return 2
                }
                return 1
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                return data[rowIndex * (getColumnCount()) + columnIndex]
            }
        }

        val table = JBTable(tableModel)

        val mainPanel = ScrollPaneFactory.createScrollPane(table)

        val tableColumnModel = table.columnModel
        tableColumnModel.getColumn(0).headerValue = PluginLabelsBundle.get("statementCoverage")

        if (enableCoverageChangeMode) {
            tableColumnModel.getColumn(1).headerValue = PluginLabelsBundle.get("coverageStatementChange")
        }


        table.columnModel = tableColumnModel
        table.minimumSize = Dimension(700, 100)

        return mainPanel
    }

    /**
     * Closes the toolWindow tab for the coverage visualisation
     */
    fun closeToolWindowTab() {
        contentManager?.removeContent(content!!, true)
    }
}
