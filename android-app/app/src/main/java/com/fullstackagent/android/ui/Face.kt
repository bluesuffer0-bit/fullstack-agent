package com.fullstackagent.android.ui
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable fun AgentFace(name:String, speaking:Boolean, modifier:Modifier=Modifier) {
    val inf=rememberInfiniteTransition(label="face"); val pulse by inf.animateFloat(0.8f, if(speaking)1.35f else 1.05f, infiniteRepeatable(tween(if(speaking)350 else 1800), RepeatMode.Reverse), label="pulse")
    Box(modifier, contentAlignment=Alignment.Center) { Canvas(Modifier.fillMaxSize()) { val c=center; val r=size.minDimension*.2f*pulse; repeat(3){ i->drawCircle(Color(0xFF4DE4C1).copy(alpha=.65f-i*.17f),r+i*28, c,style=Stroke(3f))}; repeat(8){i-> val x=(i%4+1)*size.width/5; drawLine(Color(0xFF21495A),Offset(x,0f),Offset(x,size.height),2f)}}; Column(horizontalAlignment=Alignment.CenterHorizontally){Text(name.uppercase(),color=Color(0xFF4DE4C1),fontSize=28.sp,letterSpacing=5.sp); Text(if(speaking)"SPEAKING" else "ONLINE",textAlign=TextAlign.Center,color=Color.White.copy(.65f),fontSize=12.sp)} }
}
