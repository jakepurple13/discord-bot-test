@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions.games

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import programmersbox.com.cards.Card
import programmersbox.com.cards.Deck
import programmersbox.com.cards.compareTo
import programmersbox.com.plugins.Games

class PokerExtension(
    private val games: Games
) : Extension() {
    override val name: String = "poker"

    override suspend fun setup() {
        val deck = Deck.defaultDeck()
        deck.shuffle()
        deck.addDeckListener {
            onDraw { _, size ->
                if (size == 0) {
                    deck.addDeck(Deck.defaultDeck())
                    deck.shuffle()
                }
            }
        }

        publicSlashCommand(::PokerArgs) {
            name = "poker"
            description = "Play Poker"
            action {
                val player = games.getOrPut(user.id)
                var currentState = PokerState.Playing
                val playerHand = mutableListOf<Card>()
                val removeFrom = arrayOf(false, false, false, false, false)
                playerHand.addAll(deck.draw(5))
                respondEphemeral {
                    content = pokerHandValues
                    embed {
                        title = "You currently have a ${PokerHand.entries.first { it.check(playerHand) }}"
                        description = playerHand.joinToString { it.toSymbolString() }
                    }

                    components {
                        repeat(5) { index ->
                            ephemeralButton {
                                label = "${index + 1}"
                                action {
                                    removeFrom[index] = !removeFrom[index]

                                    edit {
                                        embed {
                                            title =
                                                "You currently have a ${PokerHand.entries.first { it.check(playerHand) }}"
                                            description = playerHand.joinToString { it.toSymbolString() }
                                            field("Discarding") {
                                                playerHand
                                                    .filterIndexed { index, _ -> removeFrom[index] }
                                                    .joinToString { it.toSymbolString() }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ephemeralButton {
                            label = "Done"
                            action {
                                if (currentState == PokerState.Playing) {
                                    this@components.cancel()
                                    currentState = PokerState.Stayed
                                    removeFrom.forEachIndexed { index, b ->
                                        if (b) {
                                            playerHand[index] = deck.draw()
                                        }
                                    }

                                    edit {
                                        components {}
                                        embed {
                                            when (val hand = PokerHand.entries.first { it.check(playerHand) }) {
                                                PokerHand.HighCard -> {
                                                    title = "You have nothing! -\$${arguments.bet}"

                                                    description = playerHand.joinToString { it.toSymbolString() }

                                                    footer {
                                                        text = "You current have \$${player.money - arguments.bet}"
                                                    }

                                                    color = DISCORD_RED

                                                    games[user.id] = player.copy(
                                                        money = player.money - arguments.bet,
                                                    )
                                                }

                                                else -> {
                                                    title =
                                                        "You have a $hand! +\$${hand.initialWinning * arguments.bet}"
                                                    description = playerHand.joinToString { it.toSymbolString() }
                                                    footer {
                                                        text =
                                                            "You current have \$${player.money + (hand.initialWinning * arguments.bet)}"
                                                    }
                                                    color = DISCORD_GREEN

                                                    games[user.id] = player.copy(
                                                        money = player.money + (hand.initialWinning * arguments.bet),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class PokerState {
    Playing, Stayed
}

class PokerArgs : Arguments() {
    val bet by int {
        name = "bet"
        description = "How much you want to bet"
        minValue = 1
        maxValue = 5
    }
}

data class PokerPlayer(
    val money: Int = 500
)

val pokerHandValues: String
    get() {
        var values = ""

        var value = 1.0
        var next: Int

        for (i in 9 downTo 1) {
            when (i) {
                9 -> {
                    values += "Royal Flush:    |"
                    value = 250 / 9.0
                }

                8 -> {
                    values += "Straight Flush: |"
                    value = 6.25
                }

                7 -> {
                    values += "4 of a Kind:    |"
                    value = 25 / 7.0
                }

                6 -> {
                    values += "Full House:     |"
                    value = 9 / 6.0
                }

                5 -> {
                    values += "Flush:          |"
                    value = 6 / 5.0
                }

                4 -> {
                    values += "Straight:       |"
                    value = 4 / 4.0
                }

                3 -> {
                    values += "3 of a Kind:    |"
                    value = 3 / 3.0
                }

                2 -> {
                    values += "Two Pair:       |"
                    value = 2 / 2.0
                }

                1 -> {
                    values += "Pair:           |"
                    value = 1 / 1.0
                }

                else -> {
                }
            }
            for (j in 1..5) {
                next = (i.toDouble() * j.toDouble() * value).toInt()
                values += "$next|"
            }
            values += "\n"
        }

        return values
    }

enum class PokerHand(val rank: Int, val initialWinning: Int) {
    RoyalFlush(9, 250) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            if (h[1].value == 10) {
                if (h[2].value == 11) {
                    if (h[3].value == 12) {
                        if (h[4].value == 13) {
                            if (h[0].value == 1) {
                                if (Straight.check(h) && Flush.check(h)) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
            return false
        }
    },
    StraightFlush(8, 50) {
        override fun check(hand: List<Card>): Boolean = Straight.check(hand) && Flush.check(hand)
    },
    FourOfAKind(7, 25) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 0
            val numberCount = h[3].value
            for (element in h) {
                if (element.value == numberCount) {
                    count++
                }
            }
            if (count == 4) {
                acceptable = true
            }

            return acceptable
        }
    },
    FullHouse(6, 9) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 1
            var found = false
            var found1 = false
            for (i in 1 until h.size) {
                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                } else {
                    if (count == 3) {
                        found1 = true
                    } else if (count == 2) {
                        found = true
                    }
                    count = 1
                }

            }

            if (count == 3) {
                found1 = true
            } else if (count == 2) {
                found = true
            }

            return found && found1
        }
    },
    Flush(5, 6) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            for (i in 1 until h.size) {
                if (h[i].suit != h[i - 1].suit) {
                    return false
                }
            }
            return true
        }
    },
    Straight(4, 4) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 0
            var value: Int
            for (i in 0 until h.size - 1) {
                value = h[i].value
                if (value == 1) {
                    if (h[i + 1].value == 2) {
                        value = 1
                    } else if (h[i + 1].value == 10) {
                        value = 9
                    }
                }
                if (value + 1 == h[i + 1].value) {
                    count++
                }
            }

            return count == 4
        }
    },
    ThreeOfAKind(3, 3) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 1
            var hold = false
            for (i in 1 until h.size) {

                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                    hold = true
                } else if (hold) {
                    break
                }

            }

            if (count == 3) {
                acceptable = true
            }

            return acceptable
        }
    },

    @Suppress("KotlinConstantConditions")
    TwoPair(2, 2) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 1
            var found = false
            var found1 = false
            var i = 1
            while (i < h.size) {
                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                    i++
                }
                if (count == 2 && found1) {
                    found = true
                    count = 1
                } else if (count == 2) {
                    found1 = true
                    count = 1
                }
                i++


            }

            if (count == 2) {
                found1 = true
            } else if (count == 2 && found1) {
                found = true
            }

            return found && found1
        }
    },
    Pair(1, 1) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 0
            for (i in 1 until h.size) {
                //if (h[i].compareTo(h[i - 1]) == 0) {
                val valueMin = if (jacksOrBetter) 11 else 1
                if (h[i].compareTo(h[i - 1]) == 0 && h[i].value > valueMin || h[i].value == 1) {
                    count++
                }

            }

            if (count == 1) {
                acceptable = true
            }

            return acceptable
        }
    },
    HighCard(0, 0) {
        override fun check(hand: List<Card>): Boolean = true
    };

    abstract fun check(hand: List<Card>): Boolean

    companion object {
        var jacksOrBetter = false
    }
}