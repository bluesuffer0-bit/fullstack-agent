package com.fullstackagent.android.ui
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val colors = darkColorScheme(primary=Color(0xFF4DE4C1), secondary=Color(0xFF69A7FF), background=Color(0xFF061018), surface=Color(0xFF0D1B26), onBackground=Color(0xFFE6F2F5), onSurface=Color(0xFFE6F2F5))
@Composable fun AgentTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme=colors, content=content)
