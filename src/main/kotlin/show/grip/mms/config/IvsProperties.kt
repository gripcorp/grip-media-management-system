package show.grip.mms.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "aws.ivs")
@Validated
data class IvsProperties(
    @field:NotBlank(message = "AWS Region은 필수입니다")
    var region: String = "ap-northeast-2",
    @field:NotBlank(message = "AWS Account ID는 필수입니다")
    var accountId: String = "",
    var channelType: String = "ADVANCED_HD",
    var recording: RecordingConfig = RecordingConfig()
) {
    data class RecordingConfig(
        var enabled: Boolean = true,
        var s3: S3 = S3(),
        var thumbnail: Thumbnail = Thumbnail()
    ) {
        data class S3(
            var bucket: String = ""
        )

        data class Thumbnail(
            var interval: Int = 60,
            var storage: String = "LATEST"
        )
    }

    /**
     * Channel ARN 생성
     * 형식: arn:aws:ivs:ap-northeast-2:123456789012:channel/abcd1234EFGH
     */
    fun buildChannelArn(channelId: String): String {
        return "arn:aws:ivs:$region:$accountId:channel/$channelId"
    }

    /**
     * Recording Configuration ARN 생성
     */
    fun buildRecordingConfigArn(configId: String): String {
        return "arn:aws:ivs:$region:$accountId:recording-configuration/$configId"
    }
}
