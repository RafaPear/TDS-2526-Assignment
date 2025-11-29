package pt.isel.reversi.utils.audio

import pt.isel.reversi.utils.LOGGER

/**
 * Represents a pool of audio tracks that can be managed together.
 * @param pool The list of AudioWrapper instances in the pool.
 */
data class AudioPool(val pool: List<AudioWrapper>) {
    init {
        LOGGER.info("AudioPool created with ${pool.size} audio tracks")
    }

    /**
     * Plays the audio track with the specified ID.
     * @param id The ID of the audio track to play.
     */
    fun play(id: String) {
        val track = pool.find { it.id == id } ?: return
        track.play()
    }

    /**
     * Stops the audio track with the specified ID.
     * @param id The ID of the audio track to stop.
     */
    fun stop(id: String) {
        val track = pool.find { it.id == id } ?: return
        track.stop()
    }

    /**
     * Pauses the audio track with the specified ID.
     * @param id The ID of the audio track to pause.
     */
    fun pause(id: String) {
        val track = pool.find { it.id == id } ?: return
        track.pause()
    }

    /**
     * Gets the audio track with the specified ID.
     * @param id The ID of the audio track to retrieve.
     * @return The AudioWrapper instance if found, null otherwise.
     */
    fun getAudioTrack(id: String): AudioWrapper? {
        return pool.find { it.id == id }
    }

    /**
     * Plays all audio tracks in the pool.
     */
    fun playAll() {
        pool.forEach { it.play() }
    }

    /**
     * Stops all audio tracks in the pool.
     */
    fun stopAll() {
        pool.forEach { it.stop() }
    }

    /**
     * Pauses all audio tracks in the pool.
     */
    fun pauseAll() {
        pool.forEach { it.pause() }
    }

    /**
     * Destroys the audio pool by closing all audio clips.
     */
    fun destroy() {
        pool.forEach { it.clip.close() }
    }

    /**
     * Checks if the audio track with the specified ID is currently playing.
     * @param id The ID of the audio track to check.
     * @return True if the track is playing, false otherwise.
     */
    fun isPlaying(id: String): Boolean {
        val track = pool.find { it.id == id }
        return track?.isPlaying() ?: false
    }

    /**
     * Executes a function while any audio track in the pool is still playing.
     * @param func The function to execute.
     */
    fun whileNotFinished(func: () -> Unit = {}) {
        while (pool.any { it.isPlaying() }) {
            func()
        }
    }

    /**
     * Executes a function while the audio track with the specified ID is still playing.
     * @param id The ID of the audio track to monitor.
     * @param func The function to execute.
     */
    fun whileNotFinished(id: String, func: () -> Unit = {}) {
        val audio = pool.find { it.id == id } ?: return
        while (audio.isPlaying()) {
            func()
        }
    }

    /**
     * Changes the master volume of all audio tracks in the pool by the specified amount.
     * @param volume The amount to change the master volume by.
     */
    fun changeMasterVolume(volume: Float) {
        pool.forEach { it.masterGainControl.addValue(volume) }
    }

    /**
     * Sets the master volume of all audio tracks in the pool to the specified value.
     * @param volume The value to set the master volume to.
     */
    fun setMasterVolume(volume: Float) {
        pool.forEach { it.masterGainControl.updateValue(volume) }
    }

    /**
     * Gets the master volume of the first audio track in the pool.
     * @return The master volume if available, null otherwise.
     */
    fun getMasterVolume(): Float? {
        return pool.firstOrNull()?.masterGainControl?.getValue()
    }

    /**
     * Changes the balance of all audio tracks in the pool by the specified amount.
     * @param balance The amount to change the balance by.
     */
    fun changeBalance(balance: Float) {
        pool.forEach { it.balanceControl.addValue(balance) }
    }

    /**
     * Sets the balance of all audio tracks in the pool to the specified value.
     * @param balance The value to set the balance to.
     */
    fun setBalance(balance: Float) {
        pool.forEach { it.balanceControl.updateValue(balance) }
    }

    /**
     * Gets the balance of the first audio track in the pool.
     * @return The balance if available, null otherwise.
     */
    fun getBalance(): Float? {
        return pool.firstOrNull()?.balanceControl?.getValue()
    }

    /**
     * Mutes or unmutes all audio tracks in the pool.
     * @param mute True to mute, false to unmute.
     */
    fun mute(mute: Boolean) {
        pool.forEach { it.muteControl.updateValue(mute) }
    }

    /**
     * Checks if all audio tracks in the pool are stopped.
     * @return True if all tracks are stopped, false otherwise.
     */
    fun isPoolStopped(): Boolean = pool.all { !it.isPlaying() }

    companion object {
        /**
         * Builds an AudioPool using the provided builder action.
         * @param builderAction The action to build the list of AudioWrapper instances.
         * @return The constructed AudioPool.
         */
        fun buildAudioPool(builderAction: MutableList<AudioWrapper>.() -> Unit): AudioPool {
            val audioList = mutableListOf<AudioWrapper>()
            audioList.builderAction()
            return AudioPool(audioList)
        }
    }
}
