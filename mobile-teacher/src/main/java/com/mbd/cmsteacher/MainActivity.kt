package com.mbd.cmsteacher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mbd.cmscommon.ui.theme.CmsApp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsteacher.feature.root.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CmsTheme(app = CmsApp.TEACHER) {
                AppRoot()
            }
        }
    }
}
