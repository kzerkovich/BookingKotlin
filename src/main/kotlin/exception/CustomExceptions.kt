package exception

sealed class CustomExceptions {
    class AuthenticationException(message: String) : RuntimeException(message)
    class ForbiddenException(message: String) : RuntimeException(message)
}