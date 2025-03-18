data class UgcPostResponse(
    val id: String,
    val specificContent: SpecificContent,
    val author: String,
    val created: CreateInfo? = null,
    val visibility: Visibility,
    val distribution: Distribution? = null
)

data class CreateInfo(
    val actor: String,
    val time: Long
)

data class Distribution(
    val feedDistribution: String? = null,
    val targetEntities: List<String>? = null,
    val thirdPartyDistributionChannels: List<String>? = null
)
