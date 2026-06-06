package com.planify.planifyspring.main.common.utils.redis

import com.planify.planifyspring.main.common.utils.ObjectMapHelper
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Suppress("unused", "SpellCheckingInspection")
@Component
class RedisHelper(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapHelper: ObjectMapHelper
) {
    fun setTTL(key: String, ttl: Duration) {
        stringRedisTemplate.expire(key, ttl)
    }

    fun del(key: String): Boolean {
        return stringRedisTemplate.delete(key)
    }

    fun del(keys: Collection<String>): Long {
        if (keys.isEmpty()) return 0
        return stringRedisTemplate.delete(keys)
    }

    fun exists(key: String): Boolean {
        return stringRedisTemplate.hasKey(key)
    }

    fun getTTL(key: String): Duration? {
        val seconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS)
        return if (seconds < 0) null else Duration.ofSeconds(seconds)
    }

    fun scan(pattern: String, count: Long = 100): List<String> {
        val options = ScanOptions.scanOptions().match(pattern).count(count).build()
        val keys = ArrayList<String>()

        stringRedisTemplate.scan(options).use { cursor ->
            while (cursor.hasNext()) keys.add(cursor.next())
        }

        return keys
    }

    fun <T : Any> set(key: String, value: T) {
        stringRedisTemplate.opsForValue().set(key, objectMapHelper.convertToString(value))
    }

    fun <T : Any> set(key: String, value: T, ttl: Duration) {
        stringRedisTemplate.opsForValue().set(key, objectMapHelper.convertToString(value), ttl)
    }

    fun <T : Any> setIfAbsent(key: String, value: T, ttl: Duration? = null): Boolean {
        val json = objectMapHelper.convertToString(value)
        val ops = stringRedisTemplate.opsForValue()
        val result = if (ttl != null) ops.setIfAbsent(key, json, ttl) else ops.setIfAbsent(key, json)
        return result ?: false
    }

    fun <T : Any> get(key: String, clazz: Class<T>): T? {
        val value = stringRedisTemplate.opsForValue().get(key) ?: return null
        return objectMapHelper.convertFromString(value, clazz)
    }

    fun increment(key: String, by: Long = 1): Long {
        return stringRedisTemplate.opsForValue().increment(key, by) ?: 0
    }

    fun <T : Any> hsetObject(key: String, value: T) {
        val data = objectMapHelper.convertToStringsMap(value)
        stringRedisTemplate.opsForHash<String, String>().putAll(key, data)
    }

    fun <T : Any> hsetField(key: String, field: String, value: T) {
        val jsonString = objectMapHelper.convertToString(value)
        stringRedisTemplate.opsForHash<String, String>().put(key, field, jsonString)
    }

    fun <T : Any> hgetObject(key: String, clazz: Class<T>): T? {
        val raw = stringRedisTemplate.opsForHash<String, String>().entries(key)
        if (raw.isEmpty()) return null
        return objectMapHelper.convertFromStringsMap(raw, clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> hgetObjects(keys: List<String>, clazz: Class<T>): List<T> {
        if (keys.isEmpty()) return emptyList()

        val rawResults = stringRedisTemplate.executePipelined(RedisCallback<Any?> { connection ->
            val hashCommands = connection.hashCommands()
            keys.forEach { key -> hashCommands.hGetAll(key.toByteArray(Charsets.UTF_8)) }
            null
        })

        return rawResults.mapNotNull { raw ->
            val map = raw as? Map<String, String>
            if (map.isNullOrEmpty()) null else objectMapHelper.convertFromStringsMap(map, clazz)
        }
    }

    fun <T : Any> hgetField(key: String, field: String, clazz: Class<T>): T? {
        val value = stringRedisTemplate.opsForHash<String, String>().get(key, field) ?: return null
        return objectMapHelper.convertFromString(value, clazz)
    }

    fun hexistsField(key: String, field: String): Boolean {
        return stringRedisTemplate.opsForHash<String, String>().hasKey(key, field)
    }

    fun hFields(key: String): Set<String> {
        return stringRedisTemplate.opsForHash<String, String>().keys(key)
    }

    fun hdelFields(key: String, vararg fields: String): Long {
        if (fields.isEmpty()) return 0
        return stringRedisTemplate.opsForHash<String, String>().delete(key, *fields)
    }

    fun <T : Any> hgetObjectsByPattern(pattern: String, clazz: Class<T>): List<T> {
        return scan(pattern).mapNotNull { hgetObject(it, clazz) }
    }

    fun <T : Any> addToSet(key: String, value: T) {
        stringRedisTemplate.opsForSet().add(key, objectMapHelper.convertToString(value))
    }

    fun <T : Any> addToSet(key: String, values: Collection<T>) {
        if (values.isEmpty()) return
        val serialized = values.map { objectMapHelper.convertToString(it) }.toTypedArray()
        stringRedisTemplate.opsForSet().add(key, *serialized)
    }

    fun <T : Any> removeFromSet(key: String, value: T): Long {
        return stringRedisTemplate.opsForSet().remove(key, objectMapHelper.convertToString(value)) ?: 0
    }

    fun <T : Any> isMember(key: String, value: T): Boolean {
        return stringRedisTemplate.opsForSet().isMember(key, objectMapHelper.convertToString(value)) ?: false
    }

    fun scard(key: String): Long {
        return stringRedisTemplate.opsForSet().size(key) ?: 0
    }

    fun <T : Any> getSet(key: String, clazz: Class<T>): List<T> {
        val values = stringRedisTemplate.opsForSet().members(key) ?: return emptyList()
        return values.mapNotNull { value -> objectMapHelper.convertFromString(value, clazz) }
    }

    fun <T : Any> lpush(key: String, value: T): Long {
        return stringRedisTemplate.opsForList().leftPush(key, objectMapHelper.convertToString(value)) ?: 0
    }

    fun <T : Any> rpush(key: String, value: T): Long {
        return stringRedisTemplate.opsForList().rightPush(key, objectMapHelper.convertToString(value)) ?: 0
    }

    fun <T : Any> lpop(key: String, clazz: Class<T>): T? {
        val value = stringRedisTemplate.opsForList().leftPop(key) ?: return null
        return objectMapHelper.convertFromString(value, clazz)
    }

    fun <T : Any> rpop(key: String, clazz: Class<T>): T? {
        val value = stringRedisTemplate.opsForList().rightPop(key) ?: return null
        return objectMapHelper.convertFromString(value, clazz)
    }

    fun <T : Any> lrange(key: String, start: Long, end: Long, clazz: Class<T>): List<T> {
        val values = stringRedisTemplate.opsForList().range(key, start, end) ?: return emptyList()
        return values.mapNotNull { objectMapHelper.convertFromString(it, clazz) }
    }

    fun llen(key: String): Long {
        return stringRedisTemplate.opsForList().size(key) ?: 0
    }

    fun <T : Any> zadd(key: String, score: Double, value: T): Boolean {
        return stringRedisTemplate.opsForZSet().add(key, objectMapHelper.convertToString(value), score) ?: false
    }

    fun <T : Any> zrem(key: String, value: T): Long {
        return stringRedisTemplate.opsForZSet().remove(key, objectMapHelper.convertToString(value)) ?: 0
    }

    fun <T : Any> zscore(key: String, value: T): Double? {
        return stringRedisTemplate.opsForZSet().score(key, objectMapHelper.convertToString(value))
    }

    fun <T : Any> zincrby(key: String, delta: Double, value: T): Double {
        return stringRedisTemplate.opsForZSet().incrementScore(key, objectMapHelper.convertToString(value), delta) ?: 0.0
    }

    fun zcard(key: String): Long {
        return stringRedisTemplate.opsForZSet().zCard(key) ?: 0
    }

    fun zcount(key: String, min: Double, max: Double): Long {
        return stringRedisTemplate.opsForZSet().count(key, min, max) ?: 0
    }

    fun <T : Any> zrange(key: String, start: Long, end: Long, clazz: Class<T>): List<T> {
        val values = stringRedisTemplate.opsForZSet().range(key, start, end) ?: return emptyList()
        return values.mapNotNull { objectMapHelper.convertFromString(it, clazz) }
    }

    fun <T : Any> zrevrange(key: String, start: Long, end: Long, clazz: Class<T>): List<T> {
        val values = stringRedisTemplate.opsForZSet().reverseRange(key, start, end) ?: return emptyList()
        return values.mapNotNull { objectMapHelper.convertFromString(it, clazz) }
    }

    fun <T : Any> zrevrangeWithScores(key: String, start: Long, end: Long, clazz: Class<T>): List<Pair<T, Double>> {
        val tuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end) ?: return emptyList()
        return tuples.mapNotNull { tuple ->
            val raw = tuple.value ?: return@mapNotNull null
            val obj = objectMapHelper.convertFromString(raw, clazz)
            obj to (tuple.score ?: 0.0)
        }
    }

    fun <T : Any> zrangeByScore(key: String, min: Double, max: Double, clazz: Class<T>): List<T> {
        val values = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max) ?: return emptyList()
        return values.mapNotNull { objectMapHelper.convertFromString(it, clazz) }
    }

    fun <T : Any> zrangeByScore(
        key: String,
        min: Double,
        max: Double,
        offset: Long,
        count: Long,
        clazz: Class<T>
    ): List<T> {
        val values = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count) ?: return emptyList()
        return values.mapNotNull { objectMapHelper.convertFromString(it, clazz) }
    }

    fun zremRangeByScore(key: String, min: Double, max: Double): Long {
        return stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max) ?: 0
    }

    fun zremRangeByRank(key: String, start: Long, end: Long): Long {
        return stringRedisTemplate.opsForZSet().removeRange(key, start, end) ?: 0
    }

    fun createStreamGroup(
        key: String,
        group: String,
        readOffset: ReadOffset,
        ignoreBusyGroup: Boolean = true
    ) {
        try {
            stringRedisTemplate.opsForStream<String, String>().createGroup(key, readOffset, group)
        } catch (e: Exception) {
            val message = e.cause?.message ?: e.message
            if (message?.contains("BUSYGROUP") == true && ignoreBusyGroup) return  // Group already exists
            throw e
        }
    }

    fun <T : Any> addToStream(key: String, value: T): RecordId {
        return stringRedisTemplate.opsForStream<String, String>().add(key, objectMapHelper.convertToStringsMap(value))
    }

    fun deleteFromStream(key: String, recordId: RecordId) {
        stringRedisTemplate.opsForStream<String, String>().delete(key, recordId)
    }

    fun <T : Any> readStream(
        key: String,
        offset: ReadOffset,
        count: Long,
        timeout: Long,
        clazz: Class<T>
    ): List<Pair<RecordId, T>> {
        val redis = stringRedisTemplate.opsForStream<String, String>()

        val readOptions = StreamReadOptions
            .empty()
            .count(count)
            .block(Duration.ofSeconds(timeout))

        val offset = StreamOffset.create(key, offset)
        val records = redis.read(readOptions, offset) ?: return emptyList()

        return records.mapNotNull { record ->
            record.id to objectMapHelper.convertFromStringsMap(record.value, clazz)
        }
    }

    fun <T : Any> readAsConsumer(
        key: String,
        group: String,
        consumer: String,
        offset: ReadOffset,
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
            StreamOffset.create(key, offset),
        ) ?: return emptyList<T>()

        return records.mapNotNull { record ->
            objectMapHelper.convertFromStringsMap(record.value, clazz)
        }
    }

    fun acknowledge(
        key: String,
        group: String,
        recordId: RecordId,
    ) {
        stringRedisTemplate.opsForStream<String, String>().acknowledge(key, group, recordId)
    }
}
