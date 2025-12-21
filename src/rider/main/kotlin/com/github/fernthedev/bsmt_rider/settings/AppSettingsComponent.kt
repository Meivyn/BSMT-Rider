package com.github.fernthedev.bsmt_rider.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListModel

fun <E> ListModel<E>.toList() : List<E> {
    if (this.size == 0)
        emptyList<E>()

    val list = mutableListOf<E>()

    try {
        for (i in 0 until this.size) {
            list[i] = this.getElementAt(i)
        }
    } catch (_: IndexOutOfBoundsException) {
        // Why is this thrown if size is being checked?
    }

    return list
}

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val _beatSaberFolders = JBList<String>(CollectionListModel())
    private val _beatSaberFoldersToolbar = ToolbarDecorator.createDecorator(_beatSaberFolders)

    private val _useDefaultFolder = JBCheckBox("Use a default beat saber directory?")
    private var _defaultFolder = JBTextField()
    private val _getBeatSaberDirs = JButton("Locate Beat Saber Directories")
    private val _refreshOnProjectOpen = JBCheckBox("Prompt/Regenerate the user.csproj file on project open")

    val preferredFocusedComponent: JComponent
        get() = _beatSaberFolders

    var beatSaberFolders: Array<String>
        get() = (_beatSaberFolders.model as CollectionListModel<String>).items.toTypedArray()
        set(newText) {
            _beatSaberFolders.model = CollectionListModel (*newText)
        }

    var useDefaultFolder: Boolean
        get() = _useDefaultFolder.isSelected
        set(newStatus) {
            _useDefaultFolder.isSelected = newStatus
            _defaultFolder.isEnabled = newStatus
        }

    var refreshOnProjectOpen: Boolean
        get() = _refreshOnProjectOpen.isSelected
        set(newStatus) {
            _refreshOnProjectOpen.isSelected = newStatus
        }

    var defaultBeatSaberFolder: String?
        get() = if (_defaultFolder.text.isNullOrEmpty()) {
            null
        } else _defaultFolder.text
        set(newText) {
            _defaultFolder.text = newText
        }

    init {
        _beatSaberFolders.model = CollectionListModel()

        _beatSaberFoldersToolbar.setAddAction {
            FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), null, null) {
                // TODO: Validate it's a beat saber folder using protocol

                // Theoretically this should never crash
                (_beatSaberFolders.model as CollectionListModel<String>).add(it.path)

            }

        }
        _beatSaberFoldersToolbar.setRemoveAction {
            (_beatSaberFolders.model as CollectionListModel<String>).remove(_beatSaberFolders.selectedIndex)
        }

        val selectDefault = object : AnAction(
            "Set as default beat saber directory",
            null,
            AllIcons.Actions.Checked_selected
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                _defaultFolder.text = _beatSaberFolders.selectedValue
            }
        }

        selectDefault.templatePresentation.isEnabled = useDefaultFolder

        _useDefaultFolder.addActionListener {
            selectDefault.templatePresentation.isEnabled = useDefaultFolder
        }

        _useDefaultFolder.addChangeListener {
            selectDefault.templatePresentation.isEnabled = useDefaultFolder
        }

        _beatSaberFoldersToolbar.addExtraAction(selectDefault)


        _defaultFolder.isEditable = false

        _getBeatSaberDirs.isEnabled = false // TODO: Find a way to get beat saber dirs without a solution


        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Beat Saber game directories: "), _beatSaberFoldersToolbar.createPanel(), 1, true)
            .addComponent(_useDefaultFolder, 1)
            .addLabeledComponent(JBLabel("Default Beat Saber Directory:"), _defaultFolder, 1, false)
            .addComponent(_refreshOnProjectOpen, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}
