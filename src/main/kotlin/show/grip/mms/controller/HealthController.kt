package show.grip.mms.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.ivs.IvsClient

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

    @Value("\${aws.ivs.account-id}")
    private val ivsAccountId: String,

    private val ivsClient: IvsClient
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

    @GetMapping("/ivscheck")
    fun checkIvs(): Map<String, Any> {
        val healthStatus = mutableMapOf<String, Any>(
            "status" to "UP",
            "service" to "grip-media-system",
            "db-url" to dbUrl
        )

        // IVS 연결 확인
        try {
            // 가장 가볍게 1개만 조회해서 응답이 오는지 확인
            val response = ivsClient.listChannels { it.maxResults(1) }
            healthStatus["ivs"] = mapOf(
                "status" to "CONNECTED",
                "request_id" to response.responseMetadata().requestId()
            )
        } catch (e: Exception) {
            healthStatus["ivs"] = mapOf(
                "status" to "DISCONNECTED",
                "error" to (e.message ?: "Unknown Error")
            )
            // 필요하다면 여기서 전체 status를 "DOWN"으로 변경할 수도 있습니다.
            // healthStatus["status"] = "DOWN"
        }

        return healthStatus
    }
}
