package fr.devcafeine.typescript_class_tools

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons.CLASS_ICON
import com.intellij.util.PlatformIcons.INTERFACE_ICON
import com.intellij.util.PlatformIcons.INTERFACE_ICON


/**
 * Développé par Stéphane Balasse
 */
class CreateTypescriptClassFileAction:
        CreateFileFromTemplateAction("TypeScript Class", "Create TypeScript Class", TypeScriptFileType.INSTANCE.getIcon()){

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Typescript File ${newName}"
    }

    public override fun createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory): PsiFile?{
        return CreateFileFromTemplateAction.createFileFromTemplate(name, template, dir, defaultTemplateProperty, true, emptyMap())
    }

    override fun createFile(name: String?, templateName: String?, dir: PsiDirectory?): PsiFile? {
        return super.createFile(name, templateName, dir)
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("New Typescript Class")
                .addKind("Class", CLASS_ICON, "TypeScript Class File")
                .addKind("Interface", INTERFACE_ICON, "TypeScript Interface File")
                .addKind("Type", CLASS_ICON, "TypeScript Type File")
    }

}