package pt.isel.reversi.core.storage

import pt.isel.reversi.core.storage.serializers.GameStateSerializer
import pt.isel.reversi.storage.FileStorage
import pt.isel.reversi.storage.Storage

enum class GameStorageType(val storage: (String) -> Storage<String, GameState, String>) {
    FILE_STORAGE({ folder ->
                     FileStorage(
                         folder = folder,
                         serializer = GameStateSerializer()
                     )
                 });

    companion object {
        fun fromConfigValue(value: String): GameStorageType =
            entries.firstOrNull { it.name == value } ?: FILE_STORAGE
    }
}