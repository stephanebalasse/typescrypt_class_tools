package fr.devcafeine.typescript_class_tools

import ai.grazie.utils.capitalize
import com.intellij.CommonBundle
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSStubElementTypes.*
import com.intellij.lang.javascript.completion.JSImportCompletionUtil
import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFieldImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSWidenType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import fr.devcafeine.typescript_class_tools.data_class.Field
import fr.devcafeine.typescript_class_tools.data_class.InfoFromTemplate
import fr.devcafeine.typescript_class_tools.extension.camelToKebabCase
import io.ktor.util.reflect.*
import java.io.IOException
import java.util.function.Predicate

/**
 * Développé par Stéphane Balasse
 */
class ImplementBuilderClass : PsiElementBaseIntentionAction(), IntentionAction {

    override fun startInWriteAction(): Boolean = false

    override fun getText(): String = "Create a builder pattern for Typescript"

    override fun getFamilyName(): String = "Create a builder pattern for Typescript"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val parent = element.parent ?: return false
        return parent.language.id == "TypeScript" &&
                (parent.node.elementType.toString() == TYPESCRIPT_CLASS.toString() ||
                        parent.node.elementType.toString() == TYPESCRIPT_INTERFACE.toString() ||
                        parent.node.elementType.toString() == TYPESCRIPT_TYPE_ALIAS.toString())
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val tsClass: TypeScriptClass? = PsiTreeUtil.getParentOfType(element, TypeScriptClass::class.java)
        val tsInterface: TypeScriptInterface? = PsiTreeUtil.getParentOfType(element, TypeScriptInterface::class.java)
        val tsType: TypeScriptTypeAlias? = PsiTreeUtil.getParentOfType(element, TypeScriptTypeAlias::class.java)


        val (name, canonicalPath, parent, fields, parameters) = when {
            tsClass != null -> InfoFromTemplate(
                tsClass.name,
                tsClass.containingFile.virtualFile.parent.canonicalPath,
                tsClass.parent,
                getFieldsByClass(tsClass),
                getParameterOfConstructor(tsClass)
            )

            tsInterface != null -> InfoFromTemplate(
                tsInterface.name,
                tsInterface.containingFile.virtualFile.parent.canonicalPath,
                tsInterface.parent,
                getFieldsByInterface(tsInterface),
                null
            )

            tsType != null -> InfoFromTemplate(
                tsType.name,
                tsType.containingFile.virtualFile.parent.canonicalPath,
                tsType.parent,
                getFieldsByType(tsType),
                null
            )

            else -> return
        }

        val dialog = CreateClassBuilderDialog(
            project,
            "Generate builder class",
            name + "Builder",
            canonicalPath
        )

        if (!dialog.showAndGet()) return
        try {
            val className: String = dialog.getClassName()
            val prefix: String = when (dialog.getPrefix()) {
                "with" -> "with"
                "set" -> "set"
                else -> ""
            }
            val dirPath: String = dialog.getBaseDir()
            val virtualFile = VfsUtil.createDirectories(dirPath)
            val psiManager = PsiManager.getInstance(project)
            val psiDirectory = psiManager.findDirectory(virtualFile)
            if (psiDirectory == null) return

            val templateManager = TemplateManager.getInstance(project)
            val template = name?.let { templateManager.createTemplate(it, "TypeScript") }
            if (template == null) return


            val imports: List<PsiElement?> = ES6ImportPsiUtil.getImportDeclarations(parent)
            template.run {
                imports.forEach { addTextSegment("${it?.text}") }
                addTextSegment("export class $className {\n")

                (parameters.orEmpty() + fields).forEach {
                    addTextSegment(
                        "private _${it.name}${if (it.isOptional) "?" else "!"}${
                            if (!it.type!!.instanceOf(
                                    JSWidenType::class
                                )
                            ) ": ${it.type}" else ""
                        };\n"
                    )
                }

                (parameters.orEmpty() + fields).forEach {
                    val prefixName =
                        if (prefix.isBlank()) it.name.replaceFirstChar { it.lowercase() } else it.name.capitalize()
                    addTextSegment(
                        "$prefix${prefixName}(value ${
                            if (!it.type!!.instanceOf(
                                    JSWidenType::class
                                )
                            ) ": ${it.type}" else ""
                        }): $className {\n"
                    )
                    addTextSegment("this._${it.name} = value;\nreturn this;\n}\n")
                }

                addTextSegment("build(): ${name} {\n")
                if (parameters != null) {
                    val nameLowerCase = name.lowercase();
                    addTextSegment("const ${nameLowerCase} = new ${name}(");
                    parameters.forEach {
                        addTextSegment("this._${it.name}")
                        if (it.defaultValue != null) {
                            addTextSegment(" ?? ${it.defaultValue}")
                        } else {
                            addTextSegment(",")
                        }
                    }
                    addTextSegment(");\n");
                    fields.forEach {
                        addTextSegment("${nameLowerCase}.${it.name} = this._${it.name} ${if (it.defaultValue != null) " ?? ${it.defaultValue}; \n" else "\n"}")
                    }
                    addTextSegment("return ${nameLowerCase};")
                    addTextSegment("\n}\n}")
                } else {
                    addTextSegment("return {\n")
                    fields.forEach {
                        addTextSegment("${it.name}: this._${it.name} ${if (it.defaultValue != null) " ?? ${it.defaultValue}, \n" else ", \n"}")
                    }
                    addTextSegment("};\n}\n}")
                }

            }
            val fileTemplate = FileTemplateUtil.createTemplate("template", "ts", template.templateText, arrayOf())
            CreateTypescriptClassFileAction().createFileFromTemplate(
                className.camelToKebabCase(),
                fileTemplate,
                psiDirectory
            )
                ?.let { tsFile ->
                    val candidates = ArrayList<JSImportCandidate>()
                    val keyFilter = Predicate { n: String? -> n == name }
                    val info = JSImportPlaceInfo(tsFile)
                    val providers = JSImportCandidatesProvider.getProviders(info)
                    JSImportCompletionUtil.processExportedElements(
                        tsFile,
                        providers,
                        keyFilter
                    ) { elements: Collection<JSImportCandidate?>, _: String? ->
                        candidates.addAll(elements.filterNotNull())
                    }
                    candidates.forEach {
                        JSImportCandidateWithExecutor(it, ES6AddImportExecutor(tsFile)).execute()
                    }
                }

        } catch (e: IncorrectOperationException) {
            Messages.showMessageDialog(
                project,
                e.localizedMessage,
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        } catch (e: IOException) {
            Messages.showMessageDialog(
                project,
                e.localizedMessage,
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }
    }

    private fun getFieldsByType(tsType: TypeScriptTypeAlias?): List<Field> {
        if (tsType == null) return listOf();
        return (tsType.parsedTypeDeclaration as JSSimpleRecordTypeImpl).properties.toList().map {
            Field(it.memberName, it.jsType, it.isOptional, null)
        }
    }

    private fun getFieldsByInterface(ts: TypeScriptInterface): List<Field> {
        return ts.fields.map {
            TypeScriptFieldImpl(it.node)
        }.map {
            Field(it.name ?: "", it.jsType, it.isOptional, it.initializer?.text)
        }
    }

    private fun getFieldsByClass(ts: TypeScriptClass): List<Field> {

        return ts.fields.map { TypeScriptFieldImpl(it.node) }.map { field ->
            Field(
                field.name ?: "",
                field.jsType,
                field.isOptional,
                defaultValue = field.initializer?.text
            )
        }
    }


    private fun getParameterOfConstructor(ts: TypeScriptClass): List<Field>? {
        return ts.constructor?.parameterList?.parameterVariables?.map {
            Field(
                it.name ?: "",
                it.jsType,
                it.isOptional,
                it.initializer?.text
            )
        }
    }
}