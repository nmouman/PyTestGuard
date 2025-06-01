package com.github.mrshan23.pytestguard.display.generatedTests

import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle

import java.awt.Dimension

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class TopButtonsPanelBuilder {

    private var testsGeneratedText: String = "${PluginLabelsBundle.get("testsGenerated")}: %d"
    private var testsGeneratedLabel: JLabel = JLabel(testsGeneratedText)

    /**
     * Updates the labels.
     */
    fun update(generatedTestsTabData: GeneratedTestsTabData) {
        testsGeneratedLabel.text = String.format(
            testsGeneratedText,
            generatedTestsTabData.testCaseIdToPanel.size,
        )
    }

    fun getPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.preferredSize = Dimension(0, 30)
        panel.add(Box.createRigidArea(Dimension(10, 0)))
        panel.add(testsGeneratedLabel)
        panel.add(Box.createHorizontalGlue())

        return panel
    }

    fun clear(generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.testCasePanelFactories.clear()
    }
}
