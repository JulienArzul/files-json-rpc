package com.julienarzul.files_json_rpc.service

import com.julienarzul.files_json_rpc.config.FilesProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.RandomAccessFile
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.pathString

@Service
class DefaultFilesRpcService(val filesProperties: FilesProperties, val filesLocks: FilesLocks) : FilesRpcService {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DefaultFilesRpcService")
    }

    override fun createFile(filePath: String): String {
        val path = getPath(filePath)

        require(path.parent.exists()) {
            "The file must be created in an existing directory"
        }
        require(!path.exists()) {
            "A file or directory already exists at path $filePath"
        }

        val createdPath = path.createFile()

        log.info("Created file at path $filePath")

        return createdPath.pathString
    }

    override fun createDirectory(filePath: String): String {
        val path = getPath(filePath)

        require(path.parent.exists()) {
            "The directory must be created in an existing directory"
        }
        require(!path.exists()) {
            "A file or directory already exists at path $filePath"
        }

        val createdPath = path.createDirectory()

        log.info("Created directory at path $filePath")

        return createdPath.pathString
    }

    override fun appendToFile(filePath: String, textToAppend: String) {
        val path = getPath(filePath)

        require(path.exists() && path.isRegularFile()) {
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
        return readFile(filePath, 0, -1)
    }

    override fun readFile(filePath: String, offset: Long, limit: Int): String {
        val path = getPath(filePath)

        require(path.exists() && path.isRegularFile()) {
            "No file at path $filePath"
        }

        val file = RandomAccessFile(path.toFile(), "r")
        val fileLength = file.length()
        require(offset < fileLength) {
            "Impossible to read at offset $offset: the file is not long enough"
        }
        require(offset + limit <= fileLength) {
            "Impossible to read $limit bytes: the file is not long enough"
        }

        val bytesSize = if (limit > 0) limit else (fileLength - offset).toInt()
        val stringBytes = ByteArray(bytesSize)
        file.seek(offset)
        file.use {
            if (limit > 0)
                it.readFully(stringBytes, 0, limit)
            else
                it.readFully(stringBytes)
        }
        return String(stringBytes)
    }

    override fun deleteFile(filePath: String) {
        val path = getPath(filePath)

        require(path.exists()) {
            "No file or directory at path $filePath"
        }

        path.deleteExisting()

        log.info("Deleted file at path $filePath")
    }

    override fun getFileInfo(filePath: String): FileInfo {
        val path = getPath(filePath)

        require(path.exists()) {
            "No file or directory at path $filePath"
        }

        return FileInfo(
            fileName = path.name,
            path = path.pathString,
            size = path.fileSize(),
            isDirectory = path.isDirectory()
        )
    }

    override fun getDirectoryChildren(filePath: String): List<FileInfo> {
        val path = getPath(filePath)

        require(path.exists() && path.isDirectory()) {
            "No directory at path $filePath"
        }

        return path.listDirectoryEntries().map { child ->
            FileInfo(
                fileName = child.name,
                path = child.pathString,
                size = child.fileSize(),
                isDirectory = child.isDirectory()
            )
        }.sortedWith(
            compareByDescending<FileInfo> { it.isDirectory }.thenBy { it.fileName }
        )
    }

    override fun moveFile(source: String, destination: String) {
        val sourcePath = getPath(source)
        val destinationPath = getPath(destination)

        require(sourcePath.exists()) {
            "No file or directory at source $source"
        }
        require(destinationPath.notExists()) {
            "Already existing file or directory at destination $destination"
        }

        sourcePath.moveTo(destinationPath)
    }

    override fun copyFile(source: String, destination: String) {
        val sourcePath = getPath(source)
        val destinationPath = getPath(destination)

        require(sourcePath.exists()) {
            "No file or directory at source $source"
        }
        require(destinationPath.notExists()) {
            "Already existing file or directory at destination $destination"
        }

        sourcePath.copyTo(destinationPath)
    }

    private fun getPath(filePath: String): Path =
        Paths.get(filesProperties.rootPath, filePath)
}