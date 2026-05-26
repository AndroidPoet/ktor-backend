package com.ranbirsingh.ktorbackend.chat

import kotlinx.serialization.json.Json

val ChatJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
