package com.github.mrshan23.pytestguard.display.generatedTests

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseId: Int, generatedTestsTabData: GeneratedTestsTabData) {

        // Remove the test panel from the UI
        generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseIdToPanel[testCaseId])

        // Remove the test panel
        generatedTestsTabData.testCaseIdToPanel.remove(testCaseId)


        // Remove the editorTextField
        generatedTestsTabData.testCaseIdToEditorTextField.remove(testCaseId)
    }

    /**
     * Updates the user interface of the tool window.
     *
     * This method updates the UI of the tool window tab by calling the updateUI
     * method of the allTestCasePanel object and the updateTopLabels method
     * of the topButtonsPanel object. It also checks if there are no more tests remaining
     * and closes the tool window if that is the case.
     */
    fun update(generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.allTestCasePanel.updateUI()
        generatedTestsTabData.topButtonsPanelBuilder.update(generatedTestsTabData)
    }
}
