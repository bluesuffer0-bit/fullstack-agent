package com.fullstackagent.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fullstackagent.launcher.databinding.ActivityMainBinding

/**
 * One screen, four buttons. This is the Android counterpart of the
 * desktop's four shortcuts, and the button set mirrors the wizard's
 * Phase 6 exactly: Chat, Talk, barehands, Update -- with "everything"
 * and an open-the-face action for convenience.
 *
 * Buttons whose pieces are not installed are hidden rather than greyed:
 * a greyed button is a question the person cannot answer, and a hidden
 * one is a setup that finished correctly.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var config: AgentConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        config = AgentConfig(this)

        wireButtons()
    }

    override fun onResume() {
        super.onResume()
        // Settings may have changed the name or the installed set, so the
        // labels and visibility are recomputed on every return to the
        // screen rather than only at first creation.
        applyConfig()
    }

    private fun applyConfig() {
        val name = config.agentName
        binding.textAgentName.text = name
        binding.textHome.text = config.homeDir

        binding.buttonTalk.text = getString(R.string.button_talk, name)
        binding.buttonHands.text = getString(R.string.button_hands, name)
        binding.buttonChat.text = getString(R.string.button_chat, name)
        binding.buttonUpdate.text = getString(R.string.button_update, name)

        // Talk and barehands both need the voice line, which is the one
        // piece that may not have built on this phone. Everything else
        // stands on its own.
        binding.buttonTalk.visibility = visible(config.hasVoice && config.hasFace)
        binding.buttonHands.visibility = visible(config.hasVoice && config.hasHands)
        binding.buttonFace.visibility = visible(config.hasFace)
        binding.buttonEverything.visibility =
            visible(config.hasFace || config.hasHands || config.hasVoice)

        if (!config.hasVoice) {
            binding.textVoiceNote.text = getString(R.string.note_voice_off)
            binding.textVoiceNote.visibility = android.view.View.VISIBLE
        } else {
            binding.textVoiceNote.visibility = android.view.View.GONE
        }
    }

    private fun visible(on: Boolean) =
        if (on) android.view.View.VISIBLE else android.view.View.GONE

    private fun wireButtons() {
        // Each command is relative to a working directory Termux is
        // placed in, so no absolute path is repeated and a moved agent
        // folder needs only one settings change.
        binding.buttonChat.setOnClickListener {
            TermuxLauncher.runCommand(
                activity = this,
                command = config.agentCommand,
                workdir = config.homeDir,
                sessionName = getString(R.string.session_chat, config.agentName)
            )
        }

        binding.buttonTalk.setOnClickListener {
            TermuxLauncher.runCommand(
                activity = this,
                command = "./start.android.sh voice",
                workdir = config.toolboxDir,
                sessionName = getString(R.string.session_talk, config.agentName)
            )
        }

        binding.buttonHands.setOnClickListener {
            TermuxLauncher.runCommand(
                activity = this,
                command = "./start.android.sh hands",
                workdir = config.toolboxDir,
                sessionName = getString(R.string.session_hands, config.agentName)
            )
        }

        binding.buttonEverything.setOnClickListener {
            TermuxLauncher.runCommand(
                activity = this,
                command = "./start.android.sh",
                workdir = config.toolboxDir,
                sessionName = getString(R.string.session_all, config.agentName)
            )
        }

        binding.buttonUpdate.setOnClickListener {
            TermuxLauncher.runCommand(
                activity = this,
                command = "./update.android.sh",
                workdir = config.toolboxDir,
                sessionName = getString(R.string.session_update, config.agentName)
            )
        }

        // The face is a web page the browser shows; the button is the
        // shortcut to it when the voice session is not the point.
        binding.buttonFace.setOnClickListener {
            val face = config.face.ifBlank { AgentConfig.DEFAULT_FACE }
            TermuxLauncher.openUrl(this, "http://127.0.0.1:8790/faces/$face/")
        }

        binding.buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
