package com.julienarzul.files_json_rpc.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.absolutePathString

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class FilesLocks {

    companion object {
        val log: Logger = LoggerFactory.getLogger("FilesLocks")
    }

    private val locks: ConcurrentHashMap<String, ReentrantLock> =
        ConcurrentHashMap<String, ReentrantLock>()

    fun lockFile(filePath: Path) {
        val lock = locks.computeIfAbsent(getKeyFromPath(filePath), { key -> ReentrantLock() })
        lock.lock()

        log.info("Lock acquired for path: $filePath")
    }

    fun unlock(filePath: Path) {
        val key = getKeyFromPath(filePath)
        val lock = locks[key]
        require(lock != null) {
            "Trying to unlock a key ($key) where we didn't acquire the lock previously"
        }
        lock.unlock()

        log.info("Lock released for path: $filePath")
    }

    private fun getKeyFromPath(path: Path) = path.normalize().absolutePathString()
}