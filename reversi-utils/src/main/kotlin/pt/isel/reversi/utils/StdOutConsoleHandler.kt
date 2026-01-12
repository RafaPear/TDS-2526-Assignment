package pt.isel.reversi.utils

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

class StdOutConsoleHandler : Handler() {

    override fun publish(record: LogRecord) {
        if (!isLoggable(record)) return

        val color = when (record.level) {
            Level.SEVERE -> RED
            Level.WARNING -> YELLOW
            Level.INFO -> GREEN
            Level.CONFIG -> CYAN
            Level.FINE, Level.FINER, Level.FINEST -> BLUE
            else -> RESET
        }

        val msg = formatter.format(record)
        print("$color$msg$RESET")
    }

    override fun flush() = System.out.flush()
    override fun close() {}

    companion object {
        private const val RESET = "\u001B[0m"
        private const val RED = "\u001B[31m"
        private const val GREEN = "\u001B[32m"
        private const val YELLOW = "\u001B[33m"
        private const val BLUE = "\u001B[34m"
        private const val CYAN = "\u001B[36m"
    }
}
