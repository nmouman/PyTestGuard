package com.github.mrshan23.pytestguard.display.editor

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.ui.LanguageTextField
import com.intellij.util.LocalTimeCounter
import java.io.File


class TestCaseDocumentCreator(private val testCase: TestCase) : LanguageTextField.DocumentCreator {
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
                "${testCase.uniqueTestName}." + fileType.defaultExtension,
                fileType,
                "",
                stamp,
                true,
                false,
            )

            val path = File(FileUtils.getPyTestGuardResultsDirectoryPath(notNullProject))
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(path)

            val psiDirectory = ReadAction.compute<PsiDirectory, Throwable> {
                PsiManager.getInstance(notNullProject).findDirectory(virtualFile!!)
            }

            var document: Document? = null
            ApplicationManager.getApplication().runWriteAction {
                val addedFile = psiDirectory.add(psiFile) as PsiFile
                document = PsiDocumentManager.getInstance(notNullProject).getDocument(addedFile)
                document?.setText(value)
            }
            return document ?: EditorFactory.getInstance().createDocument(value)
        } else {
            return EditorFactory.getInstance().createDocument(value)
        }
    }
}
