package com.github.mrshan23.pytestguard.display.editor

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.utils.FileUtils.getTestCasePath
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.LanguageTextField
import java.io.File


class TestCaseDocumentCreator(private val testCase: TestCase) : LanguageTextField.DocumentCreator {
    override fun createDocument(
        value: String,
        language: Language?,
        project: Project?,
    ): Document {

        if (language != null) {
            val notNullProject = project ?: ProjectManager.getInstance().defaultProject

            val existingFilePath = File(getTestCasePath(testCase, notNullProject))

            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(existingFilePath)

            val psiFile = ReadAction.compute<PsiFile, Throwable> {
                PsiManager.getInstance(notNullProject).findFile(virtualFile!!)
            }

            var document: Document? = null
            ApplicationManager.getApplication().runWriteAction {
                document = PsiDocumentManager.getInstance(notNullProject).getDocument(psiFile)
            }
            return document ?: EditorFactory.getInstance().createDocument(value)
        } else {
            return EditorFactory.getInstance().createDocument(value)
        }
    }
}
