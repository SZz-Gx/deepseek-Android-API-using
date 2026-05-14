package com.example.deepseekchat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.deepseekchat.navigation.NavGraph
import com.example.deepseekchat.ui.theme.DeepSeekChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContent {
                DeepSeekChatTheme { NavGraph() }
            }
        } catch (e: Throwable) {
            android.util.Log.e("DeepSeekChat", "onCreate crash", e)
            Toast.makeText(this, "启动失败: ${e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
        }
    }
}
