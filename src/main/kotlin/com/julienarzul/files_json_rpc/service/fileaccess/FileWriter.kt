package com.julienarzul.files_json_rpc.service.fileaccess

import com.julienarzul.files_json_rpc.service.DefaultFilesRpcService
import com.julienarzul.files_json_rpc.service.FilesLocks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.appendText
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString

@Component
class FileWriter(private val filesLocks: FilesLocks) {

    companion object {
        val log: Logger = LoggerFactory.getLogger("FileWriter")
    }

    fun createFile(path: Path): String {
        require(path.parent.exists()) {
            "The file must be created in an existing directory"
        }
        require(!path.exists()) {
            "A file or directory already exists at path ${path.pathString}"
        }

        val createdPath = path.createFile()

        DefaultFilesRpcService.Companion.log.info("Created file at path ${path.pathString}")

        return createdPath.pathString
    }

    fun createDirectory(path: Path): String {
        require(path.parent.exists()) {
            "The directory must be created in an existing directory"
        }
        require(!path.exists()) {
            "A file or directory already exists at path ${path.pathString}"
        }

        val createdPath = path.createDirectory()

        DefaultFilesRpcService.Companion.log.info("Created directory at path ${path.pathString}")

        return createdPath.pathString
    }

    fun appendToFile(path: Path, textToAppend: String) {
        require(path.exists() && path.isRegularFile()) {
            "No file at path ${path.pathString}"
        }

        filesLocks.lockFile(path)
        try {
            path.appendText(textToAppend)
        } finally {
            filesLocks.unlock(path)
        }

        log.info("Appended $textToAppend to file at path ${path.pathString}")
    }

    fun deleteFile(path: Path) {
        require(path.exists()) {
            "No file or directory at path ${path.pathString}"
        }

        path.deleteExisting()

        DefaultFilesRpcService.Companion.log.info("Deleted file at path ${path.pathString}")
    }
}