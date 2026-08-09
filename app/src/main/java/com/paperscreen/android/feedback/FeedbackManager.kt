package com.paperscreen.android.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast

object FeedbackManager {
    private const val GITHUB_REPO_URL = "https://github.com/Ram-Dev-tech/PaperScreen"
    private const val NEW_ISSUE_URL = "$GITHUB_REPO_URL/issues/new"
    
    fun reportBug(context: Context) {
        val uri = buildIssueUri(
            template = "bug_report.yml",
            title = "[Bug]: ",
            includeDiagnosticInfo = true
        )
        launchUri(context, uri)
    }

    fun suggestFeature(context: Context) {
        val uri = buildIssueUri(
            template = "feature_request.yml",
            title = "[Feature]: ",
            includeDiagnosticInfo = false
        )
        launchUri(context, uri)
    }

    fun giveFeedback(context: Context) {
        val uri = buildIssueUri(
            template = "feedback.yml",
            title = "[Feedback]: ",
            includeDiagnosticInfo = false
        )
        launchUri(context, uri)
    }

    fun openGitHubProject(context: Context) {
        launchUri(context, Uri.parse(GITHUB_REPO_URL))
    }

    internal fun buildIssueUri(template: String, title: String, includeDiagnosticInfo: Boolean): Uri {
        val builder = Uri.parse(NEW_ISSUE_URL).buildUpon()
            .appendQueryParameter("template", template)
            .appendQueryParameter("title", title)

        // Best-effort prefill logic. GitHub issue forms can prefill fields using query params matching the input IDs.
        if (includeDiagnosticInfo) {
            val versionName = "1.0" // We can hardcode or get from BuildConfig.VERSION_NAME if exposed
            builder.appendQueryParameter("paperscreen-version", versionName)
            builder.appendQueryParameter("android-version", "Android ${Build.VERSION.RELEASE}")
            builder.appendQueryParameter("device", "${Build.MANUFACTURER} ${Build.MODEL}")
        }
        
        return builder.build()
    }

    private fun launchUri(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            // Add flags to ensure it opens as a new task outside of our launcher stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Unable to resolve ACTION_VIEW intent for $uri", e)
            Toast.makeText(context, "Unable to open GitHub.", Toast.LENGTH_SHORT).show()
        }
    }
}
