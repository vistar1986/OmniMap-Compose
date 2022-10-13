package com.melody.map.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.melody.map.myapplication.ui.OverlayScreen

/**
 * OverlayActivity
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 14:11
 */
class OverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OverlayScreen()
        }
    }
}