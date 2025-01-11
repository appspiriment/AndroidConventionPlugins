package com.appspiriment.utils.extensions

fun Int.toPaddedString(padStart:Int = 2, padChar:Char = '0'): String {
    return this.toString().padStart(padStart, padChar)
}

fun String.toPaddedString(padStart:Int = 2,  padChar:Char = '0'): String {
    return this.padStart(padStart, padChar)
}

fun String.takeIfNotBlank() = takeIf { it.isNotBlank() }
fun String.takeIfNotEmpty() = takeIf { it.isNotEmpty() }
fun String.takeIfNotBlankAnd(predicate: (String)->Boolean) = takeIf { it.isNotBlank() && predicate(this)}
fun String.takeIfNotEmptyAnd(predicate: (String)->Boolean) = takeIf { it.isNotEmpty() && predicate(this)}

fun String.ifNotBlank(block: (String)->String) = takeIfNotBlank()?.let(block)
fun String.ifNotEmpty(block: (String)->String) = takeIfNotEmpty()?.let(block)
fun String.ifNotBlankAnd(predicate: (String)->Boolean, block: (String)->String) = takeIfNotBlankAnd(predicate)?.let(block)
fun String.ifNotEmptyAnd(predicate: (String)->Boolean, block: (String)->String) = takeIfNotEmptyAnd(predicate)?.let(block)