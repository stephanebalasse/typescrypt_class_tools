package fr.devcafeine.typescript_class_tools.fileTemplates

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.project.Project
import com.intellij.util.ArrayUtil
import fr.devcafeine.typescript_class_tools.extension.toUpperCamelCase

/**
 * Développé par stephane BALASSE
 */
class TypescriptCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {
    val CLASS_NAME_UPPER = "CLASS_NAME_UPPER"

    override fun handlesTemplate(template: FileTemplate): Boolean {
        val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.extension)
        return fileType == TypeScriptFileType.INSTANCE;
    }

    override fun prepareProperties(props: MutableMap<String?, Any?>,
                                   filename: String,
                                   template: FileTemplate,
                                   project: Project) {
        props[CLASS_NAME_UPPER] = filename.toUpperCamelCase()
    }
}