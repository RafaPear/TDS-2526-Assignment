package pt.isel.reversi.utils

interface Config {
    fun getDefaultConfigFileEntries(): Map<String, String>
}