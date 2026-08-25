package com.mbd.cmsdesktop.platform

import androidx.compose.ui.awt.ComposeWindow
import java.io.File

/**
 * AWT-backed file/OS integration desktop screens need but that has no portable equivalent in
 * Compose Multiplatform: native file pickers and "open/print/share with the OS default app".
 */
interface DesktopPlatformServices {
    fun pickFile(parent: ComposeWindow, title: String): File?
    fun chooseSaveFile(parent: ComposeWindow, title: String, defaultName: String): File?
    fun open(file: File): Result<Unit>
    fun print(file: File): Result<Unit>
    fun share(file: File): Result<Unit>
}
