package com.github.mrshan23.pytestguard.display

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.serviceContainer.AlreadyDisposedException
import com.intellij.ui.content.ContentManager
import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.display.generatedTests.GeneratedTestsTabBuilder
import com.github.mrshan23.pytestguard.test.TestFramework

class PyTestGuardDisplayManager {
    private var toolWindow: ToolWindow? = null

    private var contentManager: ContentManager? = null

    private var generatedTestsTabBuilder: GeneratedTestsTabBuilder? = null

    /**
     * Fill the panel with the generated test cases.
     */
    fun display(
        report: Report,
        testFramework: TestFramework,
        project: Project,
    ) {
        this.toolWindow = ToolWindowManager.getInstance(project).getToolWindow("PyTestGuard")
        this.contentManager = toolWindow!!.contentManager

        clear()

        generatedTestsTabBuilder = GeneratedTestsTabBuilder(
            project,
            testFramework,
            report,
        )
        generatedTestsTabBuilder!!.show(contentManager!!)

        toolWindow!!.show()
    }

    fun clear() {
        generatedTestsTabBuilder?.clear()

        if (contentManager != null) {
            for (content in contentManager!!.contents) {
                contentManager?.removeContent(content, true)
            }
        }

        try {
            toolWindow?.hide()
        } catch (_: AlreadyDisposedException) {} // Make sure the process continues if the tool window is already closed
    }
}