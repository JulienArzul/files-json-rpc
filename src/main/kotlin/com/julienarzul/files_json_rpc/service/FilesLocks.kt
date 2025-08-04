package com.julienarzul.files_json_rpc.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.absolutePathString

private data class LockWrapper(
    private val lock: Lock = ReentrantLock(),
    private val _numberOfThreadsInQueue: AtomicInteger = AtomicInteger(0),
) {
    val numberOfThreadsInQueue: Int = _numberOfThreadsInQueue.get()

    fun lock(): LockWrapper {
        // Increment the number of threads in queue before potentially blocking to acquire the lock
        _numberOfThreadsInQueue.incrementAndGet()

        lock.lock()

        return this
    }

    /**
     * Returns the number of threads still in the queue for the lock
     */
    fun unlock(): LockWrapper {
        lock.unlock()

        _numberOfThreadsInQueue.decrementAndGet()

        return this
    }
}

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class FilesLocks {

    companion object {
        val log: Logger = LoggerFactory.getLogger("FilesLocks")
    }

    private val locks: ConcurrentHashMap<String, LockWrapper> =
        ConcurrentHashMap<String, LockWrapper>()

    fun lockFile(filePath: Path) {
        locks.compute(
            getKeyFromPath(filePath),
            { key, value -> if (value == null) LockWrapper().lock() else value.lock() }
        )

        log.info("Lock acquired for path: $filePath")
    }

    fun unlock(filePath: Path) {
        val key = getKeyFromPath(filePath)
        val lock = locks[key]
        require(lock != null) {
            "Trying to unlock a key ($key) where we didn't acquire the lock previously"
        }

        val lockWrapper = lock.unlock()

        if (lockWrapper.numberOfThreadsInQueue == 0) {
            // Removes the lock from the Map only if no other threads
            // has tried to acquire the lock in the meantime
            locks.compute(
                key,
                { key, value ->
                    if (value?.numberOfThreadsInQueue == 0) null else value
                }
            )
        }

        log.info("Lock released for path: $filePath")
    }

    private fun getKeyFromPath(path: Path) = path.normalize().absolutePathString()
}