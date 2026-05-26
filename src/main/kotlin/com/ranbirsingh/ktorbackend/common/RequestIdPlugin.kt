package com.ranbirsingh.ktorbackend.common

import io.ktor.server.application.createApplicationPlugin
import org.slf4j.MDC
import java.util.UUID

const val RequestIdHeader = "X-Request-ID"
private const val RequestIdMdcKey = "request.id"

val RequestIdPlugin = createApplicationPlugin("RequestIdPlugin") {
    onCall { call ->
        val requestId = call.request.headers[RequestIdHeader]
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        MDC.put(RequestIdMdcKey, requestId)
        call.response.headers.append(RequestIdHeader, requestId)
    }

    onCallRespond { _ ->
        MDC.remove(RequestIdMdcKey)
    }
}
