package pt.isel.reversi.app

import pt.isel.reversi.utils.LOGGER
import pt.isel.reversi.utils.generateUniqueTimestampedFileName
import pt.isel.reversi.utils.makePathString
import java.io.File

fun installFatalCrashLogger() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

        val randomFunMessages = listOf(
            "The ancient spirits of the code are displeased.",
            "May the code be with you. This time, it was not.",
            "This is not the bug you were looking for. Or is it?",
            "By the power of debugging, dark forces have been invoked.",
            "The compiler has abandoned us at our darkest hour.",
            "To err is human. To debug is punishment.",
            "Brace yourselves. The bugs are coming.",
            "Keep calm and pretend this was expected.",
            "This is not a bug. It is a very aggressive feature.",
            "Debugging is where hope slowly goes to die.",
            "You have awakened forbidden code.",
            "Nothing about this was supposed to happen.",
            "Congratulations. You have found a legendary error.",
            "Something went terribly right for the bug.",
            "The application has fallen, but the bug has won."
        )

        val msg = randomFunMessages.random() + " A fatal crash has occurred!"

        val text = """
$msg
===============================
CRASH FATAL
Thread: ${thread.name}
Time: ${java.time.Instant.now()}
Type: ${throwable::class.qualifiedName}
Message: ${throwable.message}
               
Stacktrace:
${throwable.stackTraceToString()}
               
===============================
""".trimIndent()

        LOGGER.severe(msg)
        LOGGER.severe("Attempting to write crash log to file...")
        try {
            val baseName = makePathString("crash", "REVERSI-CRASH")
            val name = generateUniqueTimestampedFileName(baseName, ".log")
            val crashFile = File(name)
            crashFile.parentFile?.mkdirs()
            crashFile.writeText(text)
            LOGGER.severe("Crash log written to file: $name")
            LOGGER.severe(text)
        } catch (_: Throwable) {
        }

        // close logger
        LOGGER.info("Shutting down logger before crash...")
        val fileHandler = LOGGER.handlers.find { it is java.util.logging.FileHandler }
        fileHandler?.flush()
        for (handler in LOGGER.handlers) {
            handler.flush()
            handler.close()
        }
        try {
            System.err.println("CRASH FATAL na thread ${thread.name}")
            throwable.printStackTrace()
        } finally {
            Runtime.getRuntime().halt(1)
        }
    }
}

// Ref: https://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Runtime.html#addShutdownHook%28java.lang.Thread%29
fun addShutdownHook(safeExitApplication: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread { safeExitApplication() })
}
