package com.planify.planifyspring.main.common.utils.redis

import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import kotlin.reflect.full.memberProperties

@Component
class RedisJsonHelper(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private fun <T : Any> convertToStringsMap(value: T): Map<String, String> {
        return value::class.memberProperties.associate { prop ->
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
    }

    private fun <T : Any> convertFromStringsMap(value: Map<String, String>, clazz: Class<T>): T {
        val parsedMap = value.mapValues { (_, v) ->
            try {
                objectMapper.readValue(v, Any::class.java)
            } catch (_: Exception) {
                v
            }
        }

        return objectMapper.convertValue(parsedMap, clazz)
    }

    fun <T> hsetField(key: String, field: String, value: T) {
        val jsonSting = objectMapper.writeValueAsString(value)
        stringRedisTemplate.opsForHash<String, String>().put(key, field, jsonSting)
    }

    fun hdel(key: String) {
        stringRedisTemplate.opsForHash<String, String>().delete(key)
    }

    fun <T : Any> hset(key: String, value: T) {
        val hash = convertToStringsMap(value)
        stringRedisTemplate.opsForHash<String, String>().putAll(key, hash)
    }

    fun <T : Any> hget(key: String, clazz: Class<T>): T? {
        val raw = stringRedisTemplate.opsForHash<String, String>().entries(key)
        if (raw.isEmpty()) return null
        return convertFromStringsMap(raw, clazz)
    }

    fun <T : Any> hgetAllSubkeys(base: String, clazz: Class<T>): List<T> {
        val keys = stringRedisTemplate.keys(base)

        val result = ArrayList<T>()
        for (key in keys) {
            val obj = hget(key, clazz)
            result.add(obj!!)
        }

        return result
    }

    fun <T : Any> set(key: String, value: T) {
        stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value))
    }

    fun <T : Any> get(key: String, clazz: Class<T>): T? {
        return objectMapper.convertValue(stringRedisTemplate.opsForValue().get(key), clazz)
    }

    fun createStreamGroup(
        key: String,
        group: String
    ) {
        try {
            stringRedisTemplate.opsForStream<String, String>().createGroup(key, ReadOffset.latest(), group)
        } catch (_: Exception) {
            // group already exists
        }
    }

    fun <T : Any> addToStream(key: String, value: T): RecordId {
        return stringRedisTemplate.opsForStream<String, String>().add(key, convertToStringsMap(value))
    }

    fun <T : Any> readObjectFromStream(key: String, streamId: String, clazz: Class<T>): T? {
        val records = stringRedisTemplate.opsForStream<String, String>().range(key, Range.closed(streamId, streamId))
        val record = records.firstOrNull() ?: return null
        return convertFromStringsMap(record.value, clazz)
    }

    fun <T : Any> readAsConsumer(
        key: String,
        group: String,
        consumer: String,
        count: Long,
        timeout: Long,
        clazz: Class<T>
    ): List<T> {
        val redis = stringRedisTemplate.opsForStream<String, String>()

        val records = redis.read(
            Consumer.from(group, consumer),
            StreamReadOptions
                .empty()
                .count(count)
                .block(Duration.ofSeconds(timeout)),
            StreamOffset.create(key, ReadOffset.lastConsumed()),
        ) ?: return emptyList<T>()

        return records.mapNotNull { record ->
            convertFromStringsMap(record.value, clazz).also {
                redis.acknowledge(key, group, record.id)
            }
        }
    }
}
