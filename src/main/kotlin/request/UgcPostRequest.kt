import com.google.gson.annotations.SerializedName

data class UgcPostRequest(
    val author: String,
    val lifecycleState: String = "PUBLISHED",
    val specificContent: SpecificContent,
    val visibility: Visibility
)

data class SpecificContent(
    @SerializedName("com.linkedin.ugc.ShareContent") val shareContent: ShareContent
)

data class ShareContent(
    val shareCommentary: ShareCommentary,
    val shareMediaCategory: String = "NONE"
)

data class ShareCommentary(
    val text: String
)

data class Visibility(
    @SerializedName("com.linkedin.ugc.MemberNetworkVisibility")
    val memberNetworkVisibility: String = "PUBLIC" // ENUM değerlerinden biri olmalı)
)
