package com.github.mrshan23.pytestguard.psi

import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.TypeEvalContext

class PsiFunctionContext(private val psiFunction: PyFunction) {

    val name: String? get() = psiFunction.name

    val text: String? = psiFunction.text

    val containingFile: PsiFileContext = PsiFileContext(psiFunction.containingFile as PyFile)

    val parameterNames: List<String>
        get() = psiFunction.parameterList.parameters.map { it.name ?: "" }

    val parameterTypes: List<String>?
        get() = psiFunction.typeParameterList?.typeParameters?.map {it.name?: "Any"}

    val returnType: String
        get() = TypeEvalContext.codeAnalysis(psiFunction.project, psiFunction.containingFile).getReturnType(psiFunction)?.name ?: "Any"

    val containingClass: PsiClassContext? = psiFunction.containingClass?.let { PsiClassContext(it) }

}