package com.github.mrshan23.pytestguard.psi

import com.jetbrains.python.psi.*

class PsiFileContext(private val psiFile: PyFile) {

    val name: String get() = psiFile.name

    val importStatements: List<String>?
        get() = psiFile.importBlock?.map { it.text?: "" }

}