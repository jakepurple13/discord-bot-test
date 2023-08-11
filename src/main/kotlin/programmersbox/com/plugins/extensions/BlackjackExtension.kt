@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package programmersbox.com.plugins.extensions

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
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
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.delay
import programmersbox.com.cards.Card
import programmersbox.com.cards.Deck
import programmersbox.com.cards.toSum
import programmersbox.com.cards.valueTen

class BlackjackExtension : Extension() {
    override val name: String = "blackjack"

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
        val blackjackPlayers = mutableMapOf<Snowflake, BlackjackPlayer>()
        publicSlashCommand(::BlackjackArgs) {
            name = "blackjack"
            description = "Play Blackjack"
            action {
                val dealer = BlackjackPlayer()
                dealer.currentHand.add(deck.draw())
                var player = blackjackPlayers.getOrPut(user.id) { BlackjackPlayer() }
                respondEphemeral {
                    content = "Dealer is showing ${dealer.currentHand.joinToString { it.toSymbolString() }}"
                    embed {
                        title = "Your cards"
                        player.currentHand.addAll(deck.draw(2))
                        player.currentHand.forEach {
                            field(it.toSymbolString(), true) { it.valueTen.toString() }
                        }
                        footer { text = "You have ${player.currentHand.toSum()}" }
                    }
                    components {
                        ephemeralButton {
                            label = "Stay"
                            action {
                                if (player.state != BlackjackState.Stayed) {
                                    this@components.cancel()
                                    player = player.copy(state = BlackjackState.Stayed)
                                    while (dealer.currentHand.toSum() < 17) {
                                        dealer.currentHand.add(deck.draw())
                                        delay(1000)
                                        val dealerHand = dealer.currentHand.joinToString { it.toSymbolString() }
                                        edit {
                                            content =
                                                "Dealer is showing $dealerHand which totals to ${dealer.currentHand.toSum()}"
                                        }
                                    }
                                    val pSum = player.currentHand.toSum()
                                    val dSum = dealer.currentHand.toSum()

                                    val state = when {
                                        pSum > 21 && dSum <= 21 -> WinningState.Lose
                                        dSum > 21 && pSum <= 21 -> WinningState.Win
                                        pSum in (dSum + 1)..21 -> WinningState.Win
                                        dSum in (pSum + 1)..21 -> WinningState.Lose
                                        else -> WinningState.Draw
                                    }

                                    respondEphemeral {
                                        embed {
                                            when (state) {
                                                WinningState.Win -> {
                                                    title = "You win! +\$${arguments.bet}"
                                                    field("You have") { "\$${player.money + arguments.bet}" }
                                                    color = DISCORD_GREEN

                                                    blackjackPlayers[user.id] = player.copy(
                                                        state = BlackjackState.Playing,
                                                        currentHand = mutableListOf(),
                                                        money = player.money + arguments.bet
                                                    )
                                                }

                                                WinningState.Lose -> {
                                                    title = "You Lost! -\$${arguments.bet}"
                                                    field("You have") { "\$${player.money - arguments.bet}" }
                                                    color = DISCORD_RED

                                                    blackjackPlayers[user.id] = player.copy(
                                                        state = BlackjackState.Playing,
                                                        currentHand = mutableListOf(),
                                                        money = player.money - arguments.bet
                                                    )
                                                }

                                                WinningState.Draw -> {
                                                    title = "You Drew!"
                                                    field("You have") { "\$${player.money}" }
                                                    color = DISCORD_BLURPLE

                                                    blackjackPlayers[user.id] = player.copy(
                                                        state = BlackjackState.Playing,
                                                        currentHand = mutableListOf(),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ephemeralButton {
                            label = "Hit"
                            action {
                                if (player.state == BlackjackState.Playing) {
                                    player.currentHand.add(deck.draw())
                                    val sum = player.currentHand.toSum()
                                    edit {
                                        embed {
                                            title = "Your cards"
                                            player.currentHand.forEach {
                                                field(it.toSymbolString(), true) { it.valueTen.toString() }
                                            }
                                            color = if (sum > 21) DISCORD_RED else DISCORD_GREEN
                                            footer {
                                                text = "You have $sum"
                                            }
                                        }

                                        if (sum > 21) {
                                            this@components.cancel()
                                            blackjackPlayers[user.id] = player.copy(
                                                state = BlackjackState.Playing,
                                                currentHand = mutableListOf(),
                                                money = player.money - arguments.bet
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

enum class BlackjackState {
    Playing, Stayed
}

enum class WinningState {
    Win, Lose, Draw
}

class BlackjackArgs : Arguments() {
    val bet by int {
        name = "bet"
        description = "How much you want to bet"
        minValue = 1
        maxValue = 5
    }
}

data class BlackjackPlayer(
    val money: Int = 500,
    val state: BlackjackState = BlackjackState.Playing,
    val currentHand: MutableList<Card> = mutableListOf()
)
