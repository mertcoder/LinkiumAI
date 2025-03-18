package response

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("sub") val id: String
)
