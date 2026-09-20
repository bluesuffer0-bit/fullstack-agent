package com.fullstackagent.launcher

import android.content.Context
import android.content.SharedPreferences

/**
 * The agent's configuration, held in SharedPreferences.
 *
 * The desktop version writes these same values into the config files of
 * each piece (backtalk.json, ai-visualizer.json, barehands.json); this app
 * keeps its own copy because it cannot read Termux's private folder --
 * /data/data/com.termux/files/home is invisible to every other app on the
 * phone, by design. So the app is a remote control that remembers what it
 * is pointing at, and the values here are set once during setup or edited
 * on the config screen.
 *
 * Every field has a default that matches the wizard's own defaults, so the
 * app works the moment it is opened against a standard install.
 */
class AgentConfig(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** The agent's name. Used in the button labels, nothing else. */
    var agentName: String
        get() = prefs.getString(KEY_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    /** The agent's home folder inside Termux. */
    var homeDir: String
        get() = prefs.getString(KEY_HOME, DEFAULT_HOME) ?: DEFAULT_HOME
        set(value) = prefs.edit().putString(KEY_HOME, value).apply()

    /** The command that starts a typed session ("claude" on a desk). */
    var agentCommand: String
        get() = prefs.getString(KEY_COMMAND, DEFAULT_COMMAND) ?: DEFAULT_COMMAND
        set(value) = prefs.edit().putString(KEY_COMMAND, value).apply()

    /** Which face the browser opens. */
    var face: String
        get() = prefs.getString(KEY_FACE, DEFAULT_FACE) ?: DEFAULT_FACE
        set(value) = prefs.edit().putString(KEY_FACE, value).apply()

    /** Which pieces are installed. Hides the buttons that have nothing to run. */
    var hasMemory: Boolean
        get() = prefs.getBoolean(KEY_MEMORY, true)
        set(value) = prefs.edit().putBoolean(KEY_MEMORY, value).apply()

    var hasVoice: Boolean
        // The voice is the experimental piece on Android (see the wizard),
        // so it is off by default until setup has watched it speak.
        get() = prefs.getBoolean(KEY_VOICE, false)
        set(value) = prefs.edit().putBoolean(KEY_VOICE, value).apply()

    var hasFace: Boolean
        get() = prefs.getBoolean(KEY_FACE_INSTALLED, true)
        set(value) = prefs.edit().putBoolean(KEY_FACE_INSTALLED, value).apply()

    var hasHands: Boolean
        get() = prefs.getBoolean(KEY_HANDS, true)
        set(value) = prefs.edit().putBoolean(KEY_HANDS, value).apply()

    /** Where Termux itself lives. Constant on every install. */
    val termuxHome: String
        get() = "/data/data/com.termux/files/home"

    val toolboxDir: String
        get() = if (homeDir.endsWith("/")) "${homeDir}fullstack-agent" else "$homeDir/fullstack-agent"

    companion object {
        private const val PREFS_NAME = "fullstack_agent_launcher"
        private const val KEY_NAME = "agent_name"
        private const val KEY_HOME = "home_dir"
        private const val KEY_COMMAND = "agent_command"
        private const val KEY_FACE = "face"
        private const val KEY_MEMORY = "has_memory"
        private const val KEY_VOICE = "has_voice"
        private const val KEY_FACE_INSTALLED = "has_face"
        private const val KEY_HANDS = "has_hands"

        const val DEFAULT_NAME = "Jarvis"
        const val DEFAULT_HOME = "/data/data/com.termux/files/home/my-agent"
        const val DEFAULT_COMMAND = "claude"
        const val DEFAULT_FACE = "board"
    }
}
