package com.planify.planifyspring.main.features.actions.data.repositories

import com.planify.planifyspring.core.exceptions.AlreadyInUseAppError
import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.locks.ReentrantLock

@Repository
class ActionsRepositoryImpl(
    val redisHelper: RedisHelper
) : ActionsRepository {
    private val actionsReadLocks: MutableMap<String, ReentrantLock> = mutableMapOf()

    private fun generateActionUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getActionScopeKey(scope: String): String {
        return "actions:scope:$scope"
    }

    private fun getLockForConsumer(streamKey: String, group: String, consumer: String): ReentrantLock {
        return actionsReadLocks.getOrPut("lock:$streamKey:$group:$consumer") { ReentrantLock() }
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

        val lock = getLockForConsumer(streamKey, group, consumer)

        if (!lock.tryLock()) throw AlreadyInUseAppError("Consumer is already reading stream")  // TODO: Release on request cancel

        return try {
            redisHelper.readAsConsumer(
                key = streamKey,
                group = group,
                consumer = consumer,
                offset = ReadOffset.lastConsumed(),
                count = count,
                timeout = timeout,
                clazz = Action::class.java
            )
        } finally {
            lock.unlock()
        }
    }
}
