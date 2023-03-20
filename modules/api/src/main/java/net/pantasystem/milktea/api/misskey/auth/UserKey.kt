package net.pantasystem.milktea.api.misskey.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserKey(
    @SerialName("appSecret")
    val appSecret: String,

    @SerialName("token")
    val token: String,
)