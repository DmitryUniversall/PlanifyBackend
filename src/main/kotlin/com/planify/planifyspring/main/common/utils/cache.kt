package com.planify.planifyspring.main.common.utils

import org.springframework.cache.Cache
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.CompletableFuture

class JsonCacheWrapper(
    val delegate: Cache,
    val objectMapper: ObjectMapper
) : Cache by delegate {

    inline fun <reified T> getAs(key: String): T? {
        val wrapper: Cache.ValueWrapper? = delegate.get(key)
        return wrapper?.let {
            objectMapper.convertValue(it.get(), T::class.java)
        }
    }

    fun put(key: String, value: Any?) {
        delegate.put(key, value)
    }

    override fun retrieve(key: Any): CompletableFuture<*>? {
        return delegate.retrieve(key)
    }

//    override fun <T : Any> retrieve(key: Any, valueLoader: Supplier<CompletableFuture<T>>): CompletableFuture<T?> {
//        return delegate.retrieve(key, valueLoader)
//    }

    override fun putIfAbsent(key: Any, value: Any?): Cache.ValueWrapper? {
        return delegate.putIfAbsent(key, value)
    }

    override fun evictIfPresent(key: Any): Boolean {
        return delegate.evictIfPresent(key)
    }

    override fun invalidate(): Boolean {
        return delegate.invalidate()
    }
}