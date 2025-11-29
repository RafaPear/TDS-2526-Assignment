package pt.isel.reversi.core.storage

import pt.isel.reversi.core.storage.serializers.GameStateSerializer
import pt.isel.reversi.storage.AsyncFileStorage
import pt.isel.reversi.storage.AsyncStorage

enum class GameStorageType(val storage: (String) -> AsyncStorage<String, GameState, String>) {
    FILE_STORAGE({ folder ->
        AsyncFileStorage(
            folder = folder,
            serializer = GameStateSerializer()
        )
    });

    companion object {
        fun fromConfigValue(value: String): GameStorageType =
            entries.firstOrNull { it.name == value } ?: FILE_STORAGE
    }
}