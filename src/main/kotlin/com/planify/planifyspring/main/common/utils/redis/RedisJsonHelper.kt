package com.planify.planifyspring.main.common.utils.redis

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import kotlin.reflect.full.memberProperties

@Component
class RedisJsonHelper(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(RedisJsonHelper::class.java)

    fun <T> hset(key: String, field: String, value: T) {
        val jsonSting = objectMapper.writeValueAsString(value)
        stringRedisTemplate.opsForHash<String, String>().put(key, field, jsonSting)
    }

    fun <T : Any> hsetDataClass(key: String, value: T) {
        val hash = value::class.memberProperties.associate { prop ->
            val fieldName = prop.name
            val fieldValue = prop.getter.call(value)

            val stringValue = when (fieldValue) {
                null -> "null"
                is String -> fieldValue
                is Number, is Boolean -> fieldValue.toString()
                else -> objectMapper.writeValueAsString(fieldValue)
            }

            fieldName to stringValue
        }

        stringRedisTemplate.opsForHash<String, String>().putAll(key, hash)
    }

    fun <T : Any> hgetDataClass(key: String, clazz: Class<T>): T? {
        logger.debug("Getting key $key")

        val raw = stringRedisTemplate.opsForHash<String, String>().entries(key)
        if (raw.isEmpty()) return null

        val parsedMap = raw.mapValues { (_, v) ->
            try {
                objectMapper.readValue(v, Any::class.java)
            } catch (_: Exception) {
                v
            }
        }

        return objectMapper.convertValue(parsedMap, clazz)
    }

    fun <T : Any> hgetAllDataClasses(base: String, clazz: Class<T>): List<T> {
        val keys = stringRedisTemplate.keys(base)

        val result = ArrayList<T>()
        for (key in keys) {
            val obj = hgetDataClass(key, clazz)
            result.add(obj!!)
        }

        return result
    }

    fun hdel(key: String) {
        stringRedisTemplate.opsForHash<String, String>().delete(key)
    }
}
