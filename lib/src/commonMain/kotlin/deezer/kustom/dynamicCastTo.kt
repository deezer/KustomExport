package deezer.kustom

/**
 * This is implemented ONLY for JS.
 * It will throw an error on other platforms.
 * NB : do not use "Any?" as receiver here:
 * it's used on dynamic types and can lead to Typescript runtime errors.
 */
expect fun <T> Any.dynamicCastTo(): T
