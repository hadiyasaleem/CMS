package com.mbd.cmsadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mbd.cmsadmin.feature.root.AppRoot
import com.mbd.cmscommon.ui.theme.CmsApp
import com.mbd.cmscommon.ui.theme.CmsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CmsTheme(app = CmsApp.ADMIN) {
                AppRoot()
            }
        }
    }
}
