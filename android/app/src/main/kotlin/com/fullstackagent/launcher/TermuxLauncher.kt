package com.fullstackagent.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Hands a command to Termux through its RunCommandService.
 *
 * This is the same public entry point Termux:Widget uses to run a script
 * from the home screen, which is the strongest evidence it is meant for
 * third-party callers: Termux:Widget is a separate app that does exactly
 * this and nothing else.
 *
 * Why a service and not an activity: Termux's session has to outlive this
 * app, and the person should be able to leave the launcher and have the
 * agent keep running. The service starts a Termux session in the
 * foreground notification, which is also the thing that keeps Android from
 * reaping it.
 *
 * On any failure the person is never left looking at a silent button:
 * missing Termux gets an install offer, and a refused start gets the URL
 * or the command printed so it can be run by hand.
 */
object TermuxLauncher {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val EXTRA_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_SESSION_NAME = "com.termux.RUN_COMMAND_SESSION_NAME"

    private const val TERMUX_BASH = "/data/data/com.termux/files/usr/bin/bash"

    const val FDROID_TERMUX = "https://f-droid.org/packages/com.termux/"
    const val GITHUB_TERMUX = "https://github.com/termux/termux-app/releases/latest"

    /**
     * True when Termux is installed and able to receive the command.
     * Requires the <queries> block in the manifest on Android 11+;
     * without it this returns false even when Termux is right there,
     * which is the one silent failure this class exists to avoid.
     */
    fun termuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** One command, run in a named Termux session. */
    fun runCommand(
        activity: Activity,
        command: String,
        workdir: String,
        sessionName: String
    ) {
        if (!termuxInstalled(activity)) {
            offerInstallTermux(activity)
            return
        }

        // argv is passed as an array, so the command needs no shell
        // quoting: bash receives "-c" and the string as two clean
        // arguments, which means a path with a space in it cannot break
        // here. It is the difference between a launcher and a launcher
        // that works for a folder named "my agent".
        val intent = Intent().apply {
            setClassName(TERMUX_PACKAGE, TERMUX_SERVICE)
            action = ACTION_RUN_COMMAND
            putExtra(EXTRA_PATH, TERMUX_BASH)
            putExtra(EXTRA_ARGUMENTS, arrayOf("-c", command))
            putExtra(EXTRA_WORKDIR, workdir)
            putExtra(EXTRA_BACKGROUND, false)
            putExtra(EXTRA_SESSION_NAME, sessionName)
        }

        try {
            // Foreground start: the receiving service belongs to Termux,
            // and the FGS type it declares is its own business. This app
            // runs no service and needs no foreground permission.
            ContextCompat.startForegroundService(activity, intent)
        } catch (e: ActivityNotFoundException) {
            offerInstallTermux(activity)
        } catch (e: SecurityException) {
            // A phone that refuses a cross-app service start. Rare, but
            // recoverable by hand, so say the command out loud.
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_start_refused, command),
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_start_failed, e.message ?: "?"),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Open a localhost URL in the browser; the face and the board both use it. */
    fun openUrl(activity: Activity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_no_browser),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Termux is the whole stack's foundation, so its absence is the one
     * dialog worth making friendly: both install sources are offered,
     * because the Play Store copy is abandoned and people who look for it
     * there land on a broken package that then gets blamed on us.
     */
    private fun offerInstallTermux(activity: Activity) {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_termux_missing_title)
            .setMessage(R.string.dialog_termux_missing_message)
            .setPositiveButton(R.string.dialog_install_fdroid) { _, _ ->
                openUrl(activity, FDROID_TERMUX)
            }
            .setNeutralButton(R.string.dialog_install_github) { _, _ ->
                openUrl(activity, GITHUB_TERMUX)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
}
