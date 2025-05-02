package api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object SecurityConfiguration {
    private const val SECRET = "secret_key"
    private const val ISSUER = "Egor Kuznetsov"
    private const val AUDIENCE = "all"
    private val algorithm = Algorithm.HMAC256(SECRET)

    fun AuthenticationConfig.configureAuth() {
        val jwtRealm = "ktor.io"
        val jwtVerifier = JWT
            .require(algorithm)
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .build()

        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(jwtVerifier)
            validate { credential ->
                if (credential.payload.getClaim("role").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    fun generateToken(userId: Int, role: String): String = JWT.create()
        .withSubject(userId.toString())
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
        .sign(algorithm)
}
