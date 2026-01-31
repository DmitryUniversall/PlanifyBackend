package com.planify.planifyspring.main.features.actions.data.repositories

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ActionsRepositoryImpl(
    val redisHelper: RedisHelper
) : ActionsRepository {
    private final val REDIS_CONSUMER_GROUP_NAME = "actions"

    private fun generateActionUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getRedisConsumerName(targetUserId: Long): String {
        return "consumer-user-$targetUserId"
    }

    private fun getTargetStreamKey(targetUserId: Long): String {
        return "actions:target:$targetUserId"
    }

    private fun getActionUuidIndexKey(actionUuid: String): String {
        return "actions:uuid:$actionUuid"
    }

    override fun <T : Any> createAction(type: String, targetUserId: Long, data: T): Action<T> {
        val action = Action(
            uuid = generateActionUuid(),
            type = type,
            targetUserId = targetUserId,
            data = data
        )

        val record = redisHelper.addToStream(getTargetStreamKey(targetUserId), action)  // Write to target user stream
        redisHelper.set(getActionUuidIndexKey(action.uuid), "${action.targetUserId}==${record.value}")  // Save uuid index for faster lookup

        return action
    }

    override fun getActionByUuid(uuid: String): Action<out Any>? {
        val actionInfoString = redisHelper.get(getActionUuidIndexKey(uuid), String::class.java) ?: return null
        val actionInfo = actionInfoString.split("==")
        if (actionInfo.size != 2) return null

        return redisHelper.readObjectFromStream(getTargetStreamKey(actionInfo[0].toLong()), actionInfo[1], Action::class.java)
    }

    override fun getUserIncomingActions(userId: Long, count: Long, timeout: Long): List<Action<out Any>> {
        redisHelper.createStreamGroup(getTargetStreamKey(userId), REDIS_CONSUMER_GROUP_NAME)
        return redisHelper.readAsConsumer(
            key = getTargetStreamKey(userId),
            group = REDIS_CONSUMER_GROUP_NAME,
            consumer = getRedisConsumerName(userId),
            count = count,
            timeout = timeout,
            clazz = Action::class.java
        )
    }
}
