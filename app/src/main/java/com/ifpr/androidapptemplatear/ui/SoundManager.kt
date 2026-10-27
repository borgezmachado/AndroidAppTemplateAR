package com.ifpr.androidapptemplatear.ui

import android.content.Context
import android.media.MediaPlayer

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAssetSound(fileName: String, isLooping: Boolean = false) {
        try {
            stopSound() // Para qualquer áudio em reprodução antes de iniciar outro

            val descriptor = context.assets.openFd(fileName)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}