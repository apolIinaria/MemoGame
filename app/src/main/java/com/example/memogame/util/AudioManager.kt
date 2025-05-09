package com.example.memogame.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.memogame.R

class AudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicEnabled = true

    private var flipSoundPlayer: MediaPlayer? = null
    private var matchSoundPlayer: MediaPlayer? = null
    private var winSoundPlayer: MediaPlayer? = null
    private var isSoundEnabled = true

    companion object {
        private var INSTANCE: AudioManager? = null

        fun getInstance(context: Context): AudioManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AudioManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled) return

        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.background_music)
                mediaPlayer?.isLooping = true
                mediaPlayer?.setVolume(0.5f, 0.5f)
            }

            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d("AudioManager", "Background music started")
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Error starting background music: ${e.message}")
        }
    }

    fun pauseBackgroundMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d("AudioManager", "Background music paused")
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Error pausing background music: ${e.message}")
        }
    }

    fun resumeBackgroundMusic() {
        if (!isMusicEnabled) return

        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d("AudioManager", "Background music resumed")
            } else {
                startBackgroundMusic()
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Error resuming background music: ${e.message}")
        }
    }

    fun stopBackgroundMusic() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                Log.d("AudioManager", "Background music stopped")
            }

            releaseAllSounds()
        } catch (e: Exception) {
            Log.e("AudioManager", "Error stopping background music: ${e.message}")
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            resumeBackgroundMusic()
        } else {
            pauseBackgroundMusic()
        }
    }

    fun isMusicEnabled(): Boolean {
        return isMusicEnabled
    }

    fun playCardFlipSound() {
        if (!isSoundEnabled) return

        try {
            if (flipSoundPlayer == null) {
                flipSoundPlayer = MediaPlayer.create(context, R.raw.card_flip)
            }

            flipSoundPlayer?.seekTo(0)
            flipSoundPlayer?.start()
        } catch (e: Exception) {
            Log.e("AudioManager", "Error playing flip sound: ${e.message}")
        }
    }

    fun playMatchSound() {
        if (!isSoundEnabled) return

        try {
            if (matchSoundPlayer == null) {
                matchSoundPlayer = MediaPlayer.create(context, R.raw.card_match)
            }

            matchSoundPlayer?.seekTo(0)
            matchSoundPlayer?.start()
        } catch (e: Exception) {
            Log.e("AudioManager", "Error playing match sound: ${e.message}")
        }
    }

    fun playWinSound() {
        if (!isSoundEnabled) return

        try {
            if (winSoundPlayer == null) {
                winSoundPlayer = MediaPlayer.create(context, R.raw.win_sound)
            }

            winSoundPlayer?.seekTo(0)
            winSoundPlayer?.start()
        } catch (e: Exception) {
            Log.e("AudioManager", "Error playing win sound: ${e.message}")
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun isSoundEnabled(): Boolean {
        return isSoundEnabled
    }

    fun releaseAllSounds() {
        try {
            flipSoundPlayer?.release()
            flipSoundPlayer = null

            matchSoundPlayer?.release()
            matchSoundPlayer = null

            winSoundPlayer?.release()
            winSoundPlayer = null
        } catch (e: Exception) {
            Log.e("AudioManager", "Error releasing sound players: ${e.message}")
        }
    }
}