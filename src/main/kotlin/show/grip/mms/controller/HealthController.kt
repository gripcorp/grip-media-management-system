package show.grip.mms.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/monitor")
class HealthController (
    @Value("\${spring.datasource.hikari.jdbc-url}")
    private val dbUrl: String,

    @Value("\${spring.datasource.hikari.username}")
    private val dbUsername: String,

    @Value("\${spring.datasource.hikari.password}")
    private val dbPassword: String,

    @Value("\${spring.data.redis.host}")
    private val redisHost: String,

    @Value("\${google.cloud.firestore.api.key}")
    private val firestoreApiKey: String,

    @Value("\${google.cloud.client.id}")
    private val googleClientId: String,

    @Value("\${aws.ivs.region}")
    private val ivsRegion: String,

    @Value("\${aws.ivs.accountId}")
    private val ivsAccountId: String
){

    @GetMapping("/healthCheck")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "grip-media-system",
            "version" to "1.0.0",
            "dbUrl" to "$dbUrl",
            "dbUsername" to "$dbUsername",
            "dbPassword" to "$dbPassword",
            "redisHost" to "$redisHost",
            "firestoreApiKey" to "$firestoreApiKey",
            "googleClientId" to "$googleClientId",
            "ivsRegion" to "$ivsRegion",
            "ivsAccountId" to "$ivsAccountId"
        )
    }
}
