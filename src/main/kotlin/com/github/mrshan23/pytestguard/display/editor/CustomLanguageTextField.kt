package com.github.mrshan23.pytestguard.display.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.lang.Language
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.EditorMarkupModel
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import javax.swing.ScrollPaneConstants

class CustomLanguageTextField(
    language: Language?,
    project: Project?,
    value: String,
    documentCreator: DocumentCreator,
    oneLineMode: Boolean,
) : LanguageTextField(language, project, value, documentCreator, oneLineMode) {

    override fun createEditor(): EditorEx {
        val editor = super.createEditor()

        // Set virtual file for editor
        val fileFromDoc = FileDocumentManager.getInstance().getFile(editor.document)
        editor.setFile(fileFromDoc!!)

        addErrorStripeInspection(editor)

        // Enable inspections
        editor.putUserData(AutoPopupController.ALWAYS_AUTO_POPUP, true)

        return editor
    }

    private fun addErrorStripeInspection(editor: EditorEx) {
        (editor.markupModel as EditorMarkupModel).isErrorStripeVisible = true
        editor.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
    }
}