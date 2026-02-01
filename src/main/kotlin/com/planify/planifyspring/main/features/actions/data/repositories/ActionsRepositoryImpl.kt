package com.planify.planifyspring.main.features.actions.data.repositories

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ActionsRepositoryImpl(
    val redisHelper: RedisHelper
) : ActionsRepository {
    private fun generateActionUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getActionScopeKey(scope: String): String {
        return "actions:scope:$scope"
    }

    override fun createAction(scope: String, type: String, data: Any): Action {
        val action = Action(
            uuid = generateActionUuid(),
            type = type,
            data = data
        )

        redisHelper.addToStream(getActionScopeKey(scope), action)
        return action
    }

    override fun getIncomingActions(
        scope: String,
        group: String,
        consumer: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        val streamKey = getActionScopeKey(scope)

        redisHelper.createStreamGroup(streamKey, group, ReadOffset.from("0"))

        return redisHelper.readAsConsumer(
            key = streamKey,
            group = group,
            consumer = consumer,
            offset = ReadOffset.lastConsumed(),
            count = count,
            timeout = timeout,
            clazz = Action::class.java
        )
    }
}
