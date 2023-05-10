package fr.devcafeine.typescript_class_tools.extension

/**
 * Développé par Stéphane BALASSE
 */
fun String.toUpperCamelCase(): String {
    val words = this.split("[\\W_]+".toRegex())
    val builder = StringBuilder()
    for (word in words) {
        if (word.isNotEmpty()) {
            builder.append(word[0].uppercase())
            builder.append(word.substring(1).lowercase())
        }
    }
    return builder.toString()
}

fun String.camelToKebabCase(): String {
    return this.fold(StringBuilder()) { acc, c ->
        acc.let {
            val lowerC = c.lowercase()
            acc.append(if (acc.isNotEmpty() && c.isUpperCase()) "-$lowerC" else lowerC)
        }
    }.toString()
}