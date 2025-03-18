package data

data class UserCredentials(
    val geminiApiString: String,
    val linkedinClientID: String,
    val linkedinSecretCode: String,
    var userUrn : String?
)
