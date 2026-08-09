package com.paperscreen.android.feedback

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FeedbackManagerTest {

    @Test
    fun buildIssueUri_withDiagnosticInfo() {
        val uri = FeedbackManager.buildIssueUri("bug_report.yml", "[Bug]: ", true)
        
        val urlString = uri.toString()
        assertTrue(urlString.startsWith("https://github.com/Ram-Dev-tech/PaperScreen/issues/new"))
        assertEquals("bug_report.yml", uri.getQueryParameter("template"))
        assertEquals("[Bug]: ", uri.getQueryParameter("title"))
        assertEquals("1.0", uri.getQueryParameter("paperscreen-version"))
        assertEquals("Android ${Build.VERSION.RELEASE}", uri.getQueryParameter("android-version"))
        assertEquals("${Build.MANUFACTURER} ${Build.MODEL}", uri.getQueryParameter("device"))
    }

    @Test
    fun buildIssueUri_withoutDiagnosticInfo() {
        val uri = FeedbackManager.buildIssueUri("feature_request.yml", "[Feature]: ", false)
        
        val urlString = uri.toString()
        assertTrue(urlString.startsWith("https://github.com/Ram-Dev-tech/PaperScreen/issues/new"))
        assertEquals("feature_request.yml", uri.getQueryParameter("template"))
        assertEquals("[Feature]: ", uri.getQueryParameter("title"))
        
        // Assert diagnostic fields are not present
        assertEquals(null, uri.getQueryParameter("paperscreen-version"))
        assertEquals(null, uri.getQueryParameter("android-version"))
        assertEquals(null, uri.getQueryParameter("device"))
    }
}
