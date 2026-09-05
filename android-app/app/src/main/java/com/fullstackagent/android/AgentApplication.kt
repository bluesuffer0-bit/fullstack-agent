package com.fullstackagent.android

import android.app.Application
import androidx.room.Room
import com.fullstackagent.android.data.AgentDatabase
import com.fullstackagent.android.network.AgentApi
import com.fullstackagent.android.network.SecureSettings

class AgentApplication : Application() {
    val database by lazy { Room.databaseBuilder(this, AgentDatabase::class.java, "agent.db").build() }
    val settings by lazy { SecureSettings(this) }
    val api by lazy { AgentApi(settings) }
}
