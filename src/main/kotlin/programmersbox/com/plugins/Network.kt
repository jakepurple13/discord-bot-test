package programmersbox.com.plugins

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val REPO_URL_PREFIX = "https://raw.githubusercontent.com/jakepurple13/OtakuWorldSources/repo/"
private const val REPO_URL = "${REPO_URL_PREFIX}index.min.json"

class Network {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient {
            install(ContentNegotiation) { json(json) }
        }
    }

    suspend fun sources() = client.get(REPO_URL)
        .bodyAsText()
        .let { json.decodeFromString<List<ExtensionJsonObject>>(it) }
}

@Serializable
data class ExtensionJsonObject(
    val name: String,
    val pkg: String,
    val apk: String,
    val lang: String,
    val code: Long,
    val version: String,
    val feature: String,
    val sources: List<ExtensionSourceJsonObject>?,
)

@Serializable
data class ExtensionSourceJsonObject(
    val id: String,
    val lang: String,
    val name: String,
    val baseUrl: String,
    val versionId: Int,
)

val ExtensionJsonObject.apkUrl get() = "${REPO_URL_PREFIX}apk/$apk"