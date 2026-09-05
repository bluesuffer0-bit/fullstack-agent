package com.fullstackagent.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fullstackagent.android.data.Memory
import com.fullstackagent.android.data.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(val messages: List<Message> = emptyList(), val busy: Boolean = false, val error: String? = null)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val root = app as AgentApplication; private val dao = root.database.dao()
    private val status = MutableStateFlow(UiState())
    val state = combine(dao.messages(), status) { messages, s -> s.copy(messages = messages) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
    val memories = dao.memories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun send(text: String, onReply: (String) -> Unit = {}) { if (text.isBlank() || status.value.busy) return; viewModelScope.launch {
        status.value = UiState(busy = true)
        try { dao.addMessage(Message(role="user", content=text)); val history = state.value.messages + Message(role="user", content=text)
            val memory = dao.recentMemories().joinToString("\n") { "${it.title}: ${it.body}" }
            val reply = root.api.complete(history, memory); dao.addMessage(Message(role="assistant", content=reply)); onReply(reply); status.value = UiState()
        } catch (e: Exception) { status.value = UiState(error=e.message ?: "Request failed") }
    } }
    fun remember(title: String, body: String) = viewModelScope.launch { if(title.isNotBlank() && body.isNotBlank()) dao.addMemory(Memory(title=title, body=body)) }
    fun forget(memory: Memory) = viewModelScope.launch { dao.deleteMemory(memory) }
    fun clearChat() = viewModelScope.launch { dao.clearMessages() }
}
