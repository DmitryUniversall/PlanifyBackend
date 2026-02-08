package com.planify.planifyspring.main.features.actions.data.repositories

import com.planify.planifyspring.core.exceptions.InvalidArgumentAppError
import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.exceptions.BadActionIdHttpException
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import io.lettuce.core.RedisCommandExecutionException
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ActionsRepositoryImpl(
    val redisHelper: RedisHelper
) : ActionsRepository {
    private fun generateActionUuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun getActionScopeStreamKey(scope: String): String {
        return "actions:scope:$scope:stream"
    }

    private fun getActionId(actionUuid: String, recordId: String): String {
        return "${actionUuid}===${recordId}"
    }

    override fun createAction(scope: String, type: String, data: Any): Action {
        val actionUuid = generateActionUuid()

        val action = Action(
            id = actionUuid,
            type = type,
            data = data
        )

        val streamKey = getActionScopeStreamKey(scope)
        val recordId = redisHelper.addToStream(streamKey, action)

        return action.copy(
            id = getActionId(actionUuid, recordId.value)
        )
    }

    override fun deleteAction(scope: String, actionId: String) {
        val streamKey = getActionScopeStreamKey(scope)

        val idParts = actionId.split("===")
        if (idParts.size != 2) throw InvalidArgumentAppError("Invalid action id: $actionId")

        redisHelper.deleteFromStream(streamKey, RecordId.of(idParts[1]))
    }

    override fun getIncomingActions(  // TODO: Async lock and wait
        scope: String,
        lastSeen: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        val streamKey = getActionScopeStreamKey(scope)

        try {
            return redisHelper.readStream(
                key = streamKey,
                offset = ReadOffset.from(lastSeen),
                count = count,
                timeout = timeout,
                clazz = Action::class.java,
            ).map { it.second.copy(id = getActionId(it.second.id, it.first.value)) }
        } catch (error: RedisSystemException) {
            val cause = error.cause
            if (cause?.message?.contains("Invalid stream ID") == true ) throw InvalidArgumentAppError("Invalid lastSeen specified");
            throw error
        }
    }
}
