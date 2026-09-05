package com.fullstackagent.android

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fullstackagent.android.network.ProviderConfig
import com.fullstackagent.android.ui.*
import com.fullstackagent.android.vision.CameraGestureView
import com.fullstackagent.android.voice.VoiceController

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels(); private lateinit var voice: VoiceController
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); voice=VoiceController(this); setContent { AgentTheme { App() } } }
    override fun onDestroy(){ voice.close(); super.onDestroy() }
    @Composable private fun App(){ var tab by remember{mutableIntStateOf(0)}; var dictated by remember{mutableStateOf<String?>(null)}
        val speech=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){ r-> dictated=r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() }
        val mic=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it) voice.listen(speech)}
        Scaffold(bottomBar={NavigationBar{ listOf(Icons.Default.Chat to "Chat",Icons.Default.Psychology to "Memory",Icons.Default.Visibility to "Face",Icons.Default.BackHand to "Hands",Icons.Default.Settings to "Settings").forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.first,p.second)},label={Text(p.second)})}}}){pad->Box(Modifier.padding(pad)){when(tab){0->ChatScreen(dictated,{dictated=null},{mic.launch(Manifest.permission.RECORD_AUDIO)}){voice.speak(it)};1->MemoryScreen();2->FaceScreen();3->HandsScreen();else->SettingsScreen()}}}
    }
    @Composable private fun ChatScreen(dictated:String?, consumed:()->Unit, listen:()->Unit, speak:(String)->Unit){val s by vm.state.collectAsState();var text by remember{mutableStateOf("")};LaunchedEffect(dictated){dictated?.let{text=it;consumed()}}
        Column(Modifier.fillMaxSize().padding(12.dp)){LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){items(s.messages,key={it.id}){m->Card(colors=CardDefaults.cardColors(containerColor=if(m.role=="user") Color(0xFF163749) else Color(0xFF102820)),modifier=Modifier.fillMaxWidth()){Text(m.content,Modifier.padding(12.dp))}}};s.error?.let{Text(it,color=MaterialTheme.colorScheme.error)};Row(verticalAlignment=Alignment.CenterVertically){OutlinedTextField(text,{text=it},Modifier.weight(1f),placeholder={Text("Ask anything")});IconButton(listen){Icon(Icons.Default.Mic,"Speak")};IconButton({val q=text;text="";vm.send(q,speak)},enabled=!s.busy){Icon(if(s.busy)Icons.Default.HourglassTop else Icons.Default.Send,"Send")}}}
    }
    @Composable private fun MemoryScreen(){val ms by vm.memories.collectAsState();var title by remember{mutableStateOf("")};var body by remember{mutableStateOf("")};Column(Modifier.padding(16.dp)){Text("Persistent memory",style=MaterialTheme.typography.headlineSmall);OutlinedTextField(title,{title=it},label={Text("Title")},modifier=Modifier.fillMaxWidth());OutlinedTextField(body,{body=it},label={Text("What should I remember?")},modifier=Modifier.fillMaxWidth());Button({vm.remember(title,body);title="";body=""}){Text("Remember")};LazyColumn{items(ms,key={it.id}){m->ListItem(headlineContent={Text(m.title)},supportingContent={Text(m.body)},trailingContent={IconButton({vm.forget(m)}){Icon(Icons.Default.Delete,"Forget")}})}}}}
    @Composable private fun FaceScreen(){val c=(application as AgentApplication).settings.load();AgentFace(c.agentName,vm.state.collectAsState().value.busy,Modifier.fillMaxSize())}
    @Composable private fun HandsScreen(){var granted by remember{mutableStateOf(false)};var gesture by remember{mutableStateOf("Raise a hand")};val camera=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted=it};LaunchedEffect(Unit){camera.launch(Manifest.permission.CAMERA)};Box(Modifier.fillMaxSize()){if(granted)CameraGestureView{gesture=it}else Text("Camera permission is needed",Modifier.align(Alignment.Center));Text(gesture,Modifier.align(Alignment.BottomCenter).padding(24.dp),style=MaterialTheme.typography.headlineSmall)}}
    @Composable private fun SettingsScreen(){val store=(application as AgentApplication).settings;val old=remember{store.load()};var type by remember{mutableStateOf(old.type)};var endpoint by remember{mutableStateOf(old.endpoint)};var model by remember{mutableStateOf(old.model)};var key by remember{mutableStateOf(old.apiKey)};var name by remember{mutableStateOf(old.agentName)};Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("AI provider",style=MaterialTheme.typography.headlineSmall);Row{FilterChip(type=="openai",{type="openai"},{Text("OpenAI-compatible")});Spacer(Modifier.width(8.dp));FilterChip(type=="anthropic",{type="anthropic"},{Text("Anthropic")})};listOf("Agent name" to name,"Endpoint" to endpoint,"Model" to model,"API key" to key).forEach{(label,value)->OutlinedTextField(value,{when(label){"Agent name"->name=it;"Endpoint"->endpoint=it;"Model"->model=it;else->key=it}},label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=true)};Button({store.save(ProviderConfig(type,endpoint,model,key,name))}){Text("Save securely")};Text("Keys are encrypted with Android Keystore and never leave this device except in provider requests.",style=MaterialTheme.typography.bodySmall)}}
}
