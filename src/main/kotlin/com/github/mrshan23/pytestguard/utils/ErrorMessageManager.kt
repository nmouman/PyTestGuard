package com.github.mrshan23.pytestguard.utils

/**
 * The ErrorMessageNormalizer class is responsible for normalizing error messages by inserting "<br/>" tags after every block size characters.
 * This file uses code from TestSpark (https://github.com/JetBrains-Research/TestSpark)
 */
object ErrorMessageManager {
    private const val BLOCK_SIZE = 100

    private const val SEPARATOR = "<br/>"

    /**
     * Normalizes an error message by inserting "<br/>" tags after every block size characters,
     * except if there is already a "<br/>" tag within the blockSize characters.
     * If the string length is a multiple of blockSize and the last character is a "<br/>" tag,
     * it is removed from the result.
     *
     * @param error The error message to be normalized.
     * @return The normalized error message.
     */
    fun normalize(error: String): String {
        // init variables
        val builder = StringBuilder()
        var lastIndex = 0

        // string separating
        while (lastIndex < error.length) {
            val nextIndex = (lastIndex + BLOCK_SIZE).coerceAtMost(error.length)
            val substring = error.substring(lastIndex, nextIndex)

            if (!substring.contains(SEPARATOR)) {
                builder.append(substring).append(SEPARATOR)
            } else {
                builder.append(substring)
            }

            lastIndex = nextIndex
        }

        // remove the last <br/> if the string length is a multiple of the block size, and it didn't have <br/>
        if (builder.endsWith(SEPARATOR) && (error.length % BLOCK_SIZE == 0)) {
            builder.deleteRange(builder.length - SEPARATOR.length, builder.length)
        }

        return builder.toString()
    }
}
