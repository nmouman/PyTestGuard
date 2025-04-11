package com.github.mrshan23.pytestguard.psi

import com.github.mrshan23.pytestguard.utils.ToolUtils
import com.jetbrains.python.psi.*

class PsiFileContext(private val psiFile: PyFile) {

    val name: String get() = psiFile.name

    val methodSignatures: List<String>
        get() = psiFile.topLevelFunctions.map { ToolUtils.getFunctionSignature(it, psiFile) }

    val importStatements: List<String>?
        get() = psiFile.importBlock?.map { it.text?: "" }

}