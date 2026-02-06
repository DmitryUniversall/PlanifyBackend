package com.planify.planifyspring.main.features.actions.data.repositories

import com.planify.planifyspring.main.common.utils.redis.RedisHelper
import com.planify.planifyspring.main.features.actions.domain.entities.Action
import com.planify.planifyspring.main.features.actions.domain.repositories.ActionsRepository
import com.planify.planifyspring.main.features.actions.domain.schemas.PatchActionScheme
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

    private fun getActionScopeStreamKey(scope: String): String {
        return "actions:scope:$scope:stream"
    }

    private fun getActionScopeKey(scope: String, actionId: String): String {
        return "actions:scope:$scope:actions:$actionId"
    }

    private fun getActionId(actionUuid: String, recordId: String): String {
        return "${actionUuid}==${recordId}"
    }

    private fun getAction(actionId: String): Action? {
        return redisHelper.hget(actionId, Action::class.java)
    }

    override fun createAction(scope: String, type: String, data: Any): Action {
        val actionUuid = generateActionUuid()

        val streamKey = getActionScopeStreamKey(scope)
        val recordId = redisHelper.addToStream(streamKey, actionUuid)

        val action = Action(
            id = getActionId(actionUuid, recordId.value),
            type = type,
            data = data
        )

        val actionKey = getActionScopeKey(scope, action.id)
        redisHelper.hset(actionKey, action)

        return action
    }

    override fun getIncomingActions(
        scope: String,
        lastSeen: String,
        count: Long,
        timeout: Long
    ): List<Action> {
        val streamKey = getActionScopeStreamKey(scope)  // TODO: Async lock and wait

        return redisHelper.readStream(
            key = streamKey,
            offset = ReadOffset.lastConsumed(),
            count = count,
            timeout = timeout,
            clazz = String::class.java,
        ).mapNotNull {  // TODO: Ignore nulls?
            getAction(getActionId(it.second, it.first.value))
        }
    }

    override fun patchAction(scope: String, actionId: String, patch: PatchActionScheme) {
        val streamKey = getActionScopeKey(scope, actionId)
        patch.checked?.let { redisHelper.hsetField(streamKey, "checked", it) }
    }
}
