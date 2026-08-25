package com.mbd.cmsstudent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mbd.cmsstudent.feature.root.AppRoot
import com.mbd.cmscommon.ui.theme.CmsApp
import com.mbd.cmscommon.ui.theme.CmsTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SupabaseEntryPoint {
    fun supabaseClient(): SupabaseClient
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private fun supabaseClient(context: Context): SupabaseClient =
        EntryPointAccessors.fromApplication(context.applicationContext, SupabaseEntryPoint::class.java).supabaseClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supabaseClient(this).handleDeeplinks(intent)
        setContent {
            CmsTheme(app = CmsApp.STUDENT) {
                AppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabaseClient(this).handleDeeplinks(intent)
    }
}
