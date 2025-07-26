package com.julienarzul.files_json_rpc.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.io.path.bufferedReader
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Service
class DefaultFilesRpcService : FilesRpcService {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DefaultFilesRpcService")
    }

    override fun createFile(filePath: String): String {
        val path = Paths.get(filePath)

        require(!path.exists()) {
            "A file or folder already exists at path $filePath"
        }

        val createdPath = path.createFile()

        log.info("Created file at path $filePath")

        return createdPath.pathString
    }

    override fun appendToFile(filePath: String, textToAppend: String) {
        val path = Paths.get(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        path.appendText(textToAppend)

        log.info("Appended $textToAppend to file at path $filePath")
    }

    override fun readFile(filePath: String): String {
        val path = Paths.get(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        return path.bufferedReader().use { it.readText() }
    }

    override fun deleteFile(filePath: String) {
        val path = Paths.get(filePath)

        require(path.exists()) {
            "No file at path $filePath"
        }

        path.deleteExisting()

        log.info("Deleted file at path $filePath")
    }
}