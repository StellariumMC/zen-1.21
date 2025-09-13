package meowing.zen.api

import meowing.zen.utils.NetworkUtils

object DungeonsAPI {
    fun fetchSecrets(uuid: String, cacheMs: Long, onResult: (Int) -> Unit) {
        if (SecretsCache.isFresh(uuid, cacheMs)) {
            SecretsCache.get(uuid)?.let(onResult)
            return
        }

        NetworkUtils.fetchJson<Int>(
            url = "https://api.tenios.dev/secrets/$uuid",
            headers = mapOf("User-Agent" to "Stella"),
            onSuccess = { secrets ->
                SecretsCache.put(uuid, secrets)
                println("Fetched current secrets for $uuid: $secrets")
                onResult(secrets)
            },
            onError = { error ->
                println("Failed to fetch secrets for $uuid: ${error.message}")
                onResult(0)
            }
        )
    }
}

object SecretsCache {
    private val data = mutableMapOf<String, Pair<Long, Int>>() // UUID → (timestamp, secrets)
    private const val EXPIRY_MS = 5 * 60 * 1000L

    fun cleanup() {
        val now = System.currentTimeMillis()
        data.entries.removeIf { now - it.value.first > EXPIRY_MS }
    }

    fun get(uuid: String): Int? = data[uuid]?.second

    fun put(uuid: String, secrets: Int) {
        data[uuid] = System.currentTimeMillis() to secrets
    }

    fun isFresh(uuid: String, cacheMs: Long): Boolean {
        val timestamp = data[uuid]?.first ?: return false
        return System.currentTimeMillis() - timestamp < cacheMs
    }
}
