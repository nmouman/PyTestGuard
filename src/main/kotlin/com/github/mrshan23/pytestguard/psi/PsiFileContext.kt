package com.github.mrshan23.pytestguard.psi

import com.github.mrshan23.pytestguard.utils.getFunctionSignature
import com.jetbrains.python.psi.PyFile

class PsiFileContext(private val psiFile: PyFile) {

    val name: String get() = psiFile.name

    val methodSignatures: List<String>
        get() = psiFile.topLevelFunctions.map { it.getFunctionSignature((psiFile)) }

    val importStatements: List<String>?
        get() = psiFile.importBlock?.map { it.text?: "" }

}