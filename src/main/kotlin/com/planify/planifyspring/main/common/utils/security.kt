package com.planify.planifyspring.main.common.utils

import io.jsonwebtoken.security.Keys
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import javax.crypto.SecretKey

object SecurityHelper {
    val secretString = System.getenv("JWT_SECRET")!!
    val secretKey: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretString))
    val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder(12)

    fun calculateAccessTokenExpiresAt(): Date {
        return Date(Date().time + (1000 * 60 * 60 * 1))
    }

    fun calculateRefreshTokenExpiresAt(): Date {
        return Date(Date().time + (1000 * 60 * 60 * 12))
    }

    fun calculateSessionExpiresAt(): Date {
        return Date(Date().time + (1000 * 60 * 60 * 12))
    }

    fun hashPassword(password: String): String {
        return passwordEncoder.encode(password)!!
    }

    fun isPasswordsMatch(password: String, hashedPassword: String): Boolean {
        return passwordEncoder.matches(password, hashedPassword)
    }
}
