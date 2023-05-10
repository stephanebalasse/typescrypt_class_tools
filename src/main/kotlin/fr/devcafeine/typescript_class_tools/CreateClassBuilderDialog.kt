package fr.devcafeine.typescript_class_tools

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBInsets
import java.awt.*
import com.intellij.openapi.ui.ComboBox
import javax.swing.ButtonGroup


import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.JRadioButtonMenuItem
import javax.swing.event.DocumentEvent

/**
 * Développé par Stéphane Balasse
 */
class CreateClassBuilderDialog(
    project: Project,
    title: String,
    private val targetClassName: String?,
    private val canonicalPath: String?
) : DialogWrapper(project, true) {
    private val textFieldClassName: JTextField = MyTextField()
    private val textFieldCanonicalPath = TextFieldWithBrowseButton(MyTextField())
    private val comboBoxChoiceOfPrefix = ComboBox<String>(arrayOf("with", "set", "none"))

    init {
        init()
        setTitle(title)
        textFieldCanonicalPath.text = canonicalPath ?: ""
        textFieldClassName.text = targetClassName ?: ""
        textFieldCanonicalPath.addBrowseFolderListener(
            "Choose Destination Directory",
            "",
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }

    override fun createCenterPanel(): JComponent {
        return JPanel(BorderLayout())
    }

    override fun createNorthPanel(): JComponent {

        val panel = JPanel(GridBagLayout()).apply {
            val gbConstraints = GridBagConstraints().apply {
                insets = JBInsets.create(4, 8)
                fill = GridBagConstraints.HORIZONTAL
                weightx = 0.0
                gridwidth = 1
                anchor = GridBagConstraints.WEST
            }
            add(JBLabel("Create class : "), gbConstraints)
            gbConstraints.apply {
                gridx = 1
                weightx = 1.0
            }
            add(textFieldClassName, gbConstraints)
            gbConstraints.apply {
                gridy = 3
                gridx = 0
                gridwidth = 2
                insets.top = 12
                fill = GridBagConstraints.NONE
            }
            add(JBLabel("Choice prefix : "), gbConstraints)
            gbConstraints.apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
                gridwidth = 8
            }
            add(comboBoxChoiceOfPrefix, gbConstraints)
            gbConstraints.apply {
                gridy = 5
                gridx = 0
                gridwidth = 2
                insets.top = 12
                fill = GridBagConstraints.NONE
            }
            add(JBLabel("To directory : "), gbConstraints)
            gbConstraints.apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
            }
            add(textFieldCanonicalPath, gbConstraints)
        }

        updateOkButtonState()

        addEventListeners()

        return panel
    }

    private fun addEventListeners() {
        textFieldClassName.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                updateOkButtonState()
            }
        })
        textFieldCanonicalPath.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                updateOkButtonState()
            }
        })
    }

    private fun updateOkButtonState() {
        okAction.isEnabled = textFieldClassName.text.isNotBlank() && textFieldCanonicalPath.text.isNotBlank()
    }


    private class MyTextField : JTextField() {
        override fun getPreferredSize(): Dimension {
            val size = super.getPreferredSize()
            val fontMetrics = getFontMetrics(getFont())
            size.width = fontMetrics.charWidth('a') * 80
            return size
        }
    }

    fun getBaseDir(): String = textFieldCanonicalPath.text
    fun getClassName(): String = textFieldClassName.text
    fun getPrefix(): String = comboBoxChoiceOfPrefix.selectedItem as String

}