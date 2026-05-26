package com.ranbirsingh.ktorbackend.users

import java.util.UUID

class UserNotFoundException(id: UUID) : RuntimeException("User not found: $id")
