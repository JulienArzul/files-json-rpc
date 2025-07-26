package com.julienarzul.files_json_rpc.service

import com.julienarzul.files_json_rpc.config.FilesProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.io.path.bufferedReader
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Service
class DefaultFilesRpcService(val filesProperties: FilesProperties, val filesLocks: FilesLocks) : FilesRpcService {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DefaultFilesRpcService")
    }

    override fun createFile(filePath: String): String {
        val path = getPath(filePath)

        require(!path.exists()) {
            "A file or folder already exists at path $filePath"
        }

        val createdPath = path.createFile()

        log.info("Created file at path $filePath")

        return createdPath.pathString
    }

    override fun appendToFile(filePath: String, textToAppend: String) {
        val path = getPath(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        filesLocks.lockFile(path)
        try {
            path.appendText(textToAppend)
        } finally {
            filesLocks.unlock(path)
        }

        log.info("Appended $textToAppend to file at path $filePath")
    }

    override fun readFile(filePath: String): String {
        val path = getPath(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        return path.bufferedReader().use { it.readText() }
    }

    override fun deleteFile(filePath: String) {
        val path = getPath(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        path.deleteExisting()

        log.info("Deleted file at path $filePath")
    }

    private fun getPath(filePath: String): Path =
        Paths.get(filesProperties.rootPath, filePath)
}