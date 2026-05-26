package com.ranbirsingh.ktorbackend.users

import com.ranbirsingh.ktorbackend.common.UsersRoute
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import java.util.UUID

fun Route.userRoutes(users: UserService) {
    post<UsersRoute> {
        val request = call.receive<CreateUserRequest>()
        val user = users.create(request.toCommand())
        call.respond(HttpStatusCode.Created, user.toResponse())
    }

    get<UsersRoute.ById> { route ->
        call.respond(users.findById(UUID.fromString(route.id)).toResponse())
    }
}
