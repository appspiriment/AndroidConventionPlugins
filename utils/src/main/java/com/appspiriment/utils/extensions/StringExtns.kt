package com.appspiriment.utils.extensions
/**
 * Extension functions for padding strings and integers, and for conditionally operating on strings.
 */

/**
 * Converts an [Int] to a [String] and pads it with a specified character to reach a given length.
 *
 * <p>If the string representation of the integer is shorter than [padStart], it will be padded
 * on the left with the specified [padChar].
 *
 * @param padStart The minimum length of the resulting string. Defaults to 2.
 * @param padChar The character to use for padding. Defaults to '0'.
 * @return The padded string representation of the integer.
 */
fun Int.toPaddedString(padStart: Int = 2, padChar: Char = '0'): String {
    return this.toString().padStart(padStart, padChar)
}

/**
 * Pads a [String] with a specified character to reach a given length.
 *
 * <p>If the string's length is shorter than [padStart], it will be padded on the left with the
 * specified [padChar].
 *
 * @param padStart The minimum length of the resulting string. Defaults to 2.
 * @param padChar The character to use for padding. Defaults to '0'.
 * @return The padded string.
 */
fun String.toPaddedString(padStart: Int = 2, padChar: Char = '0'): String {
    return this.padStart(padStart, padChar)
}

/**
 * Returns this [String] if it is not blank (i.e., not empty and contains at least one non-whitespace character),
 * otherwise returns `null`.
 *
 * @return This string if it is not blank, otherwise `null`.
 */
fun String.takeIfNotBlank() = takeIf { it.isNotBlank() }

/**
 * Returns this [String] if it is not empty, otherwise returns `null`.
 *
 * @return This string if it is not empty, otherwise `null`.
 */
fun String.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

/**
 * Returns this [String] if it is not blank and satisfies the given [predicate], otherwise returns `null`.
 *
 * @param predicate The predicate to test the string against.
 * @return This string if it is not blank and satisfies the predicate, otherwise `null`.
 */
fun String.takeIfNotBlankAnd(predicate: (String) -> Boolean) = takeIf { it.isNotBlank() && predicate(this) }

/**
 * Returns this [String] if it is not empty and satisfies the given [predicate], otherwise returns `null`.
 *
 * @param predicate The predicate to test the string against.
 * @return This string if it is not empty and satisfies the predicate, otherwise `null`.
 */
fun String.takeIfNotEmptyAnd(predicate: (String) -> Boolean) = takeIf { it.isNotEmpty() && predicate(this) }

/**
 * Executes the given [block] with this [String] as its argument if the string is not blank,
 * and returns the result of the [block]. Otherwise, returns `null`.
 *
 * @param block The block to execute with the string if it is not blank.
 * @return The result of the block if the string is not blank, otherwise `null`.
 */
fun String.ifNotBlank(block: (String) -> String) = takeIfNotBlank()?.let(block)

/**
 * Executes the given [block] with this [String] as its argument if the string is not empty,
 * and returns the result of the [block]. Otherwise, returns `null`.
 *
 * @param block The block to execute with the string if it is not empty.
 * @return The result of the block if the string is not empty, otherwise `null`.
 */
fun String.ifNotEmpty(block: (String) -> String) = takeIfNotEmpty()?.let(block)

/**
 * Executes the given [block] with this [String] as its argument if the string is not blank and
 * satisfies the given [predicate], and returns the result of the [block]. Otherwise, returns `null`.
 *
 * @param predicate The predicate to test the string against.
 * @param block The block to execute with the string if it is not blank and satisfies the predicate.
 * @return The result of the block if the string is not blank and satisfies the predicate, otherwise `null`.
 */
fun String.ifNotBlankAnd(predicate: (String) -> Boolean, block: (String) -> String) = takeIfNotBlankAnd(predicate)?.let(block)

/**
 * Executes the given [block] with this [String] as its argument if the string is not empty and
 * satisfies the given [predicate], and returns the result of the [block]. Otherwise, returns `null`.
 *
 * @param predicate The predicate to test the string against.
 * @param block The block to execute with the string if it is not empty and satisfies the predicate.
 * @return The result of the block if the string is not empty and satisfies the predicate, otherwise `null`.
 */
fun String.ifNotEmptyAnd(predicate: (String) -> Boolean, block: (String) -> String) = takeIfNotEmptyAnd(predicate)?.let(block)