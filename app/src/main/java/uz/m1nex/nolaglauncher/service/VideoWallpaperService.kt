// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.service

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var exoPlayer: ExoPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            // Initialize ExoPlayer and bind it to the system's SurfaceHolder
            exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                setVideoSurface(holder.surface)
                repeatMode = Player.REPEAT_MODE_ALL
                // Load your video asset here
                playWhenReady = true
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) exoPlayer?.play() else exoPlayer?.pause()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            exoPlayer?.release()
            exoPlayer = null
        }
    }
}