package programmersbox.com.cards

data class Frame internal constructor(
    val top: String = "", val bottom: String = "",
    val left: String = "", val right: String = "",
    val topLeft: String = "", val topRight: String = "",
    val bottomLeft: String = "", val bottomRight: String = "",
    val topFillIn: String = "", val bottomFillIn: String = ""
)

enum class FrameType(frame: Frame) {
    /**
     * BOX Frame
     * Will look like this
     *
    ```
    ╔==========================╗
    ║ Hello World              ║
    ╚==========================╝
    or
    If the top if modified
    ╔==========Hello===========╗
    ║ World                    ║
    ╚==========================╝
    or
    If the bottom is modified
    ╔==========================╗
    ║ World                    ║
    ╚==========Hello===========╝
    ```
     */
    BOX(Frame("=", "=", "║", "║", "╔", "╗", "╚", "╝", "=", "=")),

    /**
     * ASTERISK Frame
     * Will look like this
     *
    ```
    1.   ****************************
    2.   * Hello World              *
    3.   ****************************
    or
    If the top if modified
    1.   ***********Hello************
    2.   * World                    *
    3.   ****************************
    or
    If the bottom is modified
    1.   ****************************
    2.   * World                    *
    3.   ***********Hello************
    ```
     */
    ASTERISK(Frame("*", "*", "*", "*", "*", "*", "*", "*", "*", "*")),

    /**
     * Plus Frame
     * Will look like this
     *
    ```
    ++++++++++++++++++++++++++++
    + Hello World              +
    ++++++++++++++++++++++++++++
    or
    If the top if modified
    +++++++++++Hello++++++++++++
    + World                    +
    ++++++++++++++++++++++++++++
    or
    If the bottom is modified
    ++++++++++++++++++++++++++++
    + World                    +
    +++++++++++Hello++++++++++++
    ```
     */
    PLUS(Frame("+", "+", "+", "+", "+", "+", "+", "+", "+", "+")),

    /**
     * DIAGONAL Frame
     * Will look like this
     *
    ```
    ╱--------------------------╲
    | Hello World              |
    ╲--------------------------╱
    or
    If the top if modified
    ╱----------Hello-----------╲
    | World                    |
    ╲--------------------------╱
    or
    If the bottom is modified
    ╱--------------------------╲
    | World                    |
    ╲----------Hello-----------╱
    ```
     */
    DIAGONAL(Frame("-", "-", "│", "│", "╱", "╲", "╲", "╱", "-", "-")),

    /**
     * OVAL Frame
     * Will look like this
     *
    ```
    ╭--------------------------╮
    | Hello World              |
    ╰--------------------------╯
    or
    If the top if modified
    ╭----------Hello-----------╮
    | World                    |
    ╰--------------------------╯
    or
    If the bottom is modified
    ╭--------------------------╮
    | World                    |
    ╰----------Hello-----------╯
    ```
     */
    OVAL(Frame("-", "-", "│", "│", "╭", "╮", "╰", "╯", "-", "-")),

    /**
     * BOXED Frame
     * Will look like this
     *
    ```
    ▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ Hello World              ▐
    ▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟
    or
    If the top if modified
    ▛▀▀▀▀▀▀▀▀▀▀Hello▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ World                    ▐
    ▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟
    or
    If the bottom is modified
    ▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ World                    ▐
    ▙▄▄▄▄▄▄▄▄▄▄Hello▄▄▄▄▄▄▄▄▄▄▄▟
    ```
     */
    BOXED(Frame("▀", "▄", "▌", "▐", "▛", "▜", "▙", "▟", "▀", "▄")),

    /**
     * CUSTOM Frame
     * You decide how all of it looks
     */
    CUSTOM(Frame());

    var frame: Frame = frame
        private set

    fun copy(
        top: String = frame.top, bottom: String = frame.bottom,
        left: String = frame.left, right: String = frame.right,
        topLeft: String = frame.topLeft, topRight: String = frame.topRight,
        bottomLeft: String = frame.bottomLeft, bottomRight: String = frame.bottomRight,
        topFillIn: String = frame.topFillIn, bottomFillIn: String = frame.bottomFillIn
    ) = CUSTOM.apply {
        frame = frame.copy(
            top = top,
            bottom = bottom,
            left = left,
            right = right,
            topLeft = topLeft,
            topRight = topRight,
            bottomLeft = bottomLeft,
            bottomRight = bottomRight,
            topFillIn = topFillIn,
            bottomFillIn = bottomFillIn
        )
    }

    fun copy(frame: Frame) = with(frame) {
        this@FrameType.copy(
            top = top,
            bottom = bottom,
            left = left,
            right = right,
            topLeft = topLeft,
            topRight = topRight,
            bottomLeft = bottomLeft,
            bottomRight = bottomRight,
            topFillIn = topFillIn,
            bottomFillIn = bottomFillIn
        )
    }

    class FrameBuilder internal constructor(
        var top: String = "", var bottom: String = "",
        var left: String = "", var right: String = "",
        var topLeft: String = "", var topRight: String = "",
        var bottomLeft: String = "", var bottomRight: String = "",
        var topFillIn: String = "", var bottomFillIn: String = ""
    ) {
        internal fun build() =
            Frame(top, bottom, left, right, topLeft, topRight, bottomLeft, bottomRight, topFillIn, bottomFillIn)
    }

    private fun copy(frame: FrameBuilder) = copy(frame.build())

    companion object {
        /**
         * Use this to create a custom [FrameType]
         */
        @Suppress("FunctionName")
        fun CUSTOM(frame: FrameBuilder.() -> Unit) = CUSTOM.copy(FrameBuilder().apply(frame))
    }
}

fun String.frame(frameType: FrameType, rtl: Boolean = false) = split("\n").frame(
    top = frameType.frame.top, bottom = frameType.frame.bottom,
    left = frameType.frame.left, right = frameType.frame.right,
    topLeft = frameType.frame.topLeft, topRight = frameType.frame.topRight,
    bottomLeft = frameType.frame.bottomLeft, bottomRight = frameType.frame.bottomRight,
    topFillIn = frameType.frame.topFillIn, bottomFillIn = frameType.frame.bottomFillIn, rtl = rtl
)

fun <T> Iterable<T>.frame(frameType: FrameType, rtl: Boolean = false, transform: (T) -> String = { it.toString() }) =
    frame(
        top = frameType.frame.top, bottom = frameType.frame.bottom,
        left = frameType.frame.left, right = frameType.frame.right,
        topLeft = frameType.frame.topLeft, topRight = frameType.frame.topRight,
        bottomLeft = frameType.frame.bottomLeft, bottomRight = frameType.frame.bottomRight,
        topFillIn = frameType.frame.topFillIn, bottomFillIn = frameType.frame.bottomFillIn,
        rtl = rtl, transform = transform
    )

fun <T> Iterable<T>.frame(
    top: String, bottom: String,
    left: String, right: String,
    topLeft: String, topRight: String,
    bottomLeft: String, bottomRight: String,
    topFillIn: String = "", bottomFillIn: String = "",
    rtl: Boolean = false, transform: (T) -> String = { it.toString() }
): String {
    val fullLength =
        mutableListOf(top, bottom).apply { addAll(this@frame.map(transform)) }.maxByOrNull { it.length }!!.length + 2
    val space: (String) -> String = { " ".repeat(fullLength - it.length - 1) }
    val mid = map(transform).joinToString(separator = "\n") {
        "$left${if (rtl) space(it) else " "}$it${
            if (rtl) " " else space(it)
        }$right"
    }
    val space2: (String, Boolean) -> String =
        { spacing, b -> (if (b) topFillIn else bottomFillIn).repeat((fullLength - spacing.length) / 2) }
    val topBottomText: (String, Boolean) -> String = { s, b ->
        if (s.length == 1) s.repeat(fullLength)
        else space2(
            s,
            b
        ).let { spaced -> "$spaced$s${if ((fullLength - s.length) % 2 == 0) "" else (if (b) topFillIn else bottomFillIn)}$spaced" }
    }
    return "$topLeft${topBottomText(top, true)}$topRight\n$mid\n$bottomLeft${topBottomText(bottom, false)}$bottomRight"
}

private var logedFrame = FrameType.BOX