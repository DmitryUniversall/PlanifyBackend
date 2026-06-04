package com.planify.planifyspring.main.features.profiles.data.specifications

import com.planify.planifyspring.main.features.auth.data.models.UserModel
import com.planify.planifyspring.main.features.profiles.data.models.ProfileModel
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object ProfileSearchSpecification {
    fun searchProfile(input: String): Specification<ProfileModel> {  // TODO: Use raw sql?
        val tokens = input.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        return Specification { root, query, cb ->
            if (tokens.isEmpty()) return@Specification cb.conjunction()

            val perTokenPredicates = tokens.map { token ->
                val pattern = "%$token%"

                val usernameSubquery = query.subquery(Long::class.java)
                val userRoot = usernameSubquery.from(UserModel::class.java)
                usernameSubquery
                    .select(userRoot.get("id"))
                    .where(
                        cb.and(
                            cb.equal(userRoot.get<Long>("id"), root.get<Long>("userId")),
                            cb.like(cb.lower(userRoot.get("username")), pattern)
                        )
                    )

                cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("position")), pattern),
                    cb.like(cb.lower(root.get("department")), pattern),
                    cb.exists(usernameSubquery)
                )
            }

            cb.and(*perTokenPredicates.toTypedArray<Predicate>())
        }
    }
}
