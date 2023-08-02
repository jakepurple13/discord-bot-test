package programmersbox.com.plugins

import io.realm.kotlin.types.RealmObject
import kotlin.time.Duration.Companion.minutes

class Settings : RealmObject {
    var delayInMillis: Long = 60.minutes.inWholeMilliseconds
}