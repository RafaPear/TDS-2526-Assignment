package pt.isel.reversi.core.game.data

import pt.rafap.ktflag.style.Colors.ERROR_COLOR
import pt.rafap.ktflag.style.Colors.INFO_COLOR
import pt.rafap.ktflag.style.Colors.colorText

/**
 * Generic immutable container representing the outcome of a data access operation. Encodes
 * intent (success, absence, validation issue, IO fault) without relying on exceptions for
 * ordinary domain states.
 *
 * Typical usage:
 *  val r = GDACodes.SUCCESS("Saved move", true)
 *  if (r.code == GDACodes.SUCCESS && r.data == true) { /* proceed */ }
 *  else log(r.toString())
 *
 * Use [toStringColored] only for CLI / console contexts; keep plain [toString] for logs.
 *
 * @param T Type of the optional payload returned on success (or occasionally on error for context).
 * @property code Structured result code describing the outcome category.
 * @property message Optional human-readable detail about the outcome.
 * @property data Optional payload (present mostly on success).
 */
open class GDAResult<T>(
    val code: GDACodes,
    val message: String? = null,
    val data: T? = null
) {
    /**
     * Converts this result to the same outcome typed to another payload type by dropping the data.
     * Useful when propagating an error or success status where the specific payload type changes.
     * Keeps message & code intact, intentionally discards original payload to avoid unsafe casts.
     */
    fun <U> toOtherType(): GDAResult<U> {
        return GDAResult(this.code, this.message, null)
    }

    override fun toString(): String {
        return "GameDataAccessResult(code=$code, message=$message, data=$data)"
    }

    /**
     * Pretty colored representation (intended for CLI diagnostics) using SUCCESS as info color and
     * every other code as error color.
     */
    fun toStringColored(): String {
        val colorCode = when (code) {
            GDACodes.SUCCESS -> INFO_COLOR
            else             -> ERROR_COLOR
        }
        val prefix = "[$code]"
        val message = message ?: ""
        return colorText("$prefix: $message: Data: $data", colorCode)
    }
}