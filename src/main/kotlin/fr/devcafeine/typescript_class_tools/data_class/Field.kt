package fr.devcafeine.typescript_class_tools.data_class

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType

/**
 * Développé par Stéphane BALASSE
 */
data class Field(val name: String, val type: JSType?, val isOptional: Boolean, val defaultValue: String? )
