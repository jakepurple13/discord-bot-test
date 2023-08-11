package programmersbox.com.cards

fun List<Card>.asciiCards(
    padding: String = "",
    colorRed: Int = 0xff0000,
    colorBlack: Int = 0xffffff,
    equalText: (List<Card>) -> String
): String {
    val lines = asciiCards(padding, colorRed, colorBlack).lines().toMutableList()
    lines[lines.size / 2 - 1] += equalText(this)
    return lines.joinToString("\n")
}

fun List<Card>.asciiCards(padding: String = "", colorRed: Int = 0xff0000, colorBlack: Int = 0xffffff): String {
    val m = map { it.color to it.asciiCard().lines() }
    val size = m.random().second.size - 1
    val s = StringBuilder()
    fun CardColor.asciiColor() = when (this) {
        CardColor.Red -> colorRed
        CardColor.Black -> colorBlack
    }
    for (i in 0..size) s.append(m.joinToString(padding) { it.second[i].color(it.first.asciiColor()) }).append("\n")
    return s.toString()
}

fun Card.asciiCard(): String {
    val spaceLength = 10
    val symbol = toSymbolString()
    val spaces = fun(num: Int) = " ".repeat(num)
    return listOf(
        " $symbol${spaces(spaceLength - symbol.length)}",
        "",
        "",
        "${spaces(spaceLength / 2)}${suit.unicodeSymbol}${spaces(spaceLength / 2)}",
        "",
        "",
        "${spaces(spaceLength - symbol.length)}$symbol "
    ).frame(FrameType.OVAL)
}

val Card.valueTen: Int get() = if (value > 10) 10 else value

fun List<Card>.toSum() = sortedByDescending { if (it.value > 10) 10 else it.value }
    .fold(0) { v, c -> v + if (c.value == 1 && v + 11 < 22) 11 else if (c.value == 1) 1 else if (c.value > 10) 10 else c.value }