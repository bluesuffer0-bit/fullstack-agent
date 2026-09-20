package com.fullstackagent.launcher

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.fullstackagent.launcher.databinding.ActivitySettingsBinding

/**
 * The four knobs the launcher reads. Nothing here reaches into Termux --
 * the app cannot see Termux's private folder, so these are the launcher's
 * own copy of values the wizard wrote into each piece's config file.
 *
 * Editing a value here changes what the buttons point at; editing the
 * piece's own config file changes what the piece does. Both are true at
 * once and neither overwrites the other.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var config: AgentConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        config = AgentConfig(this)

        load()

        binding.buttonSave.setOnClickListener {
            save()
            finish()
        }
    }

    private fun load() {
        binding.editName.setText(config.agentName)
        binding.editHome.setText(config.homeDir)
        binding.editCommand.setText(config.agentCommand)

        // The four shipped faces, as a dropdown, so the face is picked
        // from the gallery's real contents rather than typed and mistyped.
        val faces = resources.getStringArray(R.array.faces)
        binding.editFace.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, faces)
        )
        binding.editFace.setText(config.face, false)
        binding.editFace.setOnItemClickListener { parent, _, position, _ ->
            binding.editFace.setText(parent.getItemAtPosition(position) as String, false)
        }

        binding.switchMemory.isChecked = config.hasMemory
        binding.switchVoice.isChecked = config.hasVoice
        binding.switchFace.isChecked = config.hasFace
        binding.switchHands.isChecked = config.hasHands
    }

    private fun save() {
        config.agentName = binding.editName.text.toString().trim().ifBlank { AgentConfig.DEFAULT_NAME }
        config.homeDir = binding.editHome.text.toString().trim().ifBlank { AgentConfig.DEFAULT_HOME }
        config.agentCommand = binding.editCommand.text.toString().trim().ifBlank { AgentConfig.DEFAULT_COMMAND }
        config.face = binding.editFace.text.toString().trim().ifBlank { AgentConfig.DEFAULT_FACE }
        config.hasMemory = binding.switchMemory.isChecked
        config.hasVoice = binding.switchVoice.isChecked
        config.hasFace = binding.switchFace.isChecked
        config.hasHands = binding.switchHands.isChecked
    }
}
