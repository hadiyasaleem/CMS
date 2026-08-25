package com.mbd.cmsdesktop.platform

import androidx.compose.ui.awt.ComposeWindow
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

object AwtDesktopPlatformServices : DesktopPlatformServices {

    override fun pickFile(parent: ComposeWindow, title: String): File? = chooseFile(parent, title, FileDialog.LOAD, null)

    override fun chooseSaveFile(parent: ComposeWindow, title: String, defaultName: String): File? =
        chooseFile(parent, title, FileDialog.SAVE, defaultName)

    override fun open(file: File): Result<Unit> = desktopAction { open(file) }

    override fun print(file: File): Result<Unit> = desktopAction { print(file) }

    override fun share(file: File): Result<Unit> = open(file)

    private fun chooseFile(parent: ComposeWindow, title: String, mode: Int, defaultName: String?): File? {
        val dialog = FileDialog(parent as Frame, title, mode)
        if (defaultName != null) dialog.file = defaultName
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val fileName = dialog.file ?: return null
        return File(directory, fileName)
    }

    private fun desktopAction(action: Desktop.() -> Unit): Result<Unit> = runCatching {
        check(Desktop.isDesktopSupported()) { "Desktop integration is not supported on this system" }
        Desktop.getDesktop().action()
    }
}
