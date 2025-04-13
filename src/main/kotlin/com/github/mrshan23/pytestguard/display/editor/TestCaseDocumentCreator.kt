package com.github.mrshan23.pytestguard.display.editor

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.LanguageTextField
import com.intellij.util.LocalTimeCounter

class TestCaseDocumentCreator(private val testName: String) : LanguageTextField.DocumentCreator {
    override fun createDocument(
        value: String,
        language: Language?,
        project: Project?,
    ): Document {

        if (language != null) {
            val notNullProject = project ?: ProjectManager.getInstance().defaultProject
            val factory = PsiFileFactory.getInstance(notNullProject)
            val fileType: FileType = language.associatedFileType!!
            val stamp = LocalTimeCounter.currentTime()
            val psiFile = factory.createFileFromText(
                "$testName." + fileType.defaultExtension,
                fileType,
                "",
                stamp,
                true,
                false,
            )

            val document = PsiDocumentManager.getInstance(notNullProject).getDocument(psiFile)!!
            ApplicationManager.getApplication().runWriteAction {
                document.setText(value)
            }
            return document
        } else {
            return EditorFactory.getInstance().createDocument(value)
        }
    }
}
