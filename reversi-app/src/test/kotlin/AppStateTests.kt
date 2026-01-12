//class AppStateTests {
//    fun cleanup(func: suspend () -> Unit) {
//        val conf = loadCoreConfig()
//        File(conf.savesPath).deleteRecursively()
//        runBlocking { func() }
//        File(conf.savesPath).deleteRecursively()
//    }
//    @Test
//    fun `setPage update the same page does not change the state`() {
//        cleanup {
//            val expectedAppState = mutableStateOf(
//                value = AppState(
//                    page = Page.JOIN_GAME,
//                    game = runBlocking {
//                        startNewGame(
//                            side = 8,
//                            players = listOf(Player(type = PieceType.BLACK)),
//                            firstTurn = PieceType.BLACK,
//                        )
//                    },
//                    error = null,
//                    audioPool = AudioPool(emptyList())
//                )
//            )
//
//            val uut = setPage(expectedAppState, Page.JOIN_GAME)
//            assertEquals(expectedAppState.value, uut)
//        }
//    }
//
//    @Test
//    fun `setAppState the same page does not change the backPageState`() {
//        cleanup {
//            val expectedAppState = mutableStateOf(
//                value = AppState(
//                    page = Page.JOIN_GAME,
//                    game = runBlocking {
//                        startNewGame(
//                            side = 8,
//                            players = listOf(Player(type = PieceType.BLACK)),
//                            firstTurn = PieceType.BLACK,
//                        )
//                    },
//                    error = GameNotStartedYet(),
//                    audioPool = AudioPool(emptyList()),
//                    backPage = Page.MAIN_MENU
//                )
//            )
//
//            val uut = setAppState(expectedAppState, page = Page.JOIN_GAME, error = GameNotStartedYet())
//            assertEquals(expectedModuleappState.value.backPage, uut.backPage)
//        }
//    }
//}