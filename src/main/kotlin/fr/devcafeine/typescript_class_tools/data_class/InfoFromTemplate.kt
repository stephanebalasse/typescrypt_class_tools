package fr.devcafeine.typescript_class_tools.data_class

import com.intellij.psi.PsiElement

/**
 * Développé par Stéphane BALASSE
 */
data class InfoFromTemplate(
    val name: String?,
    val canonicalPath: String?,
    val parent: PsiElement,
    val fields: List<Field>,
    val parameterConstructor: List<Field>?
)
