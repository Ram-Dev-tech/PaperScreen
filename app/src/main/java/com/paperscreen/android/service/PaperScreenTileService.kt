package com.paperscreen.android.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class PaperScreenTileService : TileService() {

    // Simple in-memory toggle state for demonstration
    // Real implementation should read from SharedPreferences / DataStore
    private var isPaperModeEnabled = false

    override fun onStartListening() {
        super.onStartListening()
        // Here we would sync isPaperModeEnabled with our actual DataStore/Preferences
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        
        // Toggle state
        isPaperModeEnabled = !isPaperModeEnabled
        
        // Broadcast the change so the rest of the app can react (e.g. going into Paper Mode)
        val toggleIntent = Intent("com.paperscreen.android.ACTION_TOGGLE_PAPER_MODE")
        toggleIntent.putExtra("enabled", isPaperModeEnabled)
        sendBroadcast(toggleIntent)
        
        // Update UI of the tile
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        
        if (isPaperModeEnabled) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Paper Mode" // Usually comes from strings.xml
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Paper Mode"
        }
        
        tile.updateTile()
    }
}
