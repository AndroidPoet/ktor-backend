package com.ranbirsingh.ktorbackend.common

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging

fun Application.configureObservability() {
    install(RequestIdPlugin)
    install(CallLogging)
}
