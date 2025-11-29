package pt.isel.reversi.utils

/**
 * A plain formatter for logging that outputs the log level, source method name, and message.
 */
class PlainFormatter : java.util.logging.Formatter() {
    override fun format(record: java.util.logging.LogRecord): String {
        return "[${record.level}] (${record.sourceMethodName}) " + record.message + System.lineSeparator()
    }
}