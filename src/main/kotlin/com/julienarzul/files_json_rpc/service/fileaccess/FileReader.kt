package com.julienarzul.files_json_rpc.service.fileaccess

import com.julienarzul.files_json_rpc.model.FileInfo
import org.springframework.stereotype.Component
import java.io.RandomAccessFile
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

@Component
class FileReader {

    fun readFile(path: Path, offset: Long, limit: Int): String {
        require(path.exists() && path.isRegularFile()) {
            "No file at path ${path.pathString}"
        }

        val file = RandomAccessFile(path.toFile(), "r")
        val fileLength = file.length()
        require(offset < fileLength) {
            "Impossible to read at offset $offset: the file at ${path.pathString} is not long enough"
        }
        require(offset + limit <= fileLength) {
            "Impossible to read $limit bytes: the file at ${path.pathString} is not long enough"
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

    fun getFileInfo(path: Path): FileInfo {
        require(path.exists()) {
            "No file or directory at path ${path.pathString}"
        }

        return FileInfo(
            fileName = path.name,
            path = path.pathString,
            size = path.fileSize(),
            isDirectory = path.isDirectory()
        )
    }

    fun getDirectoryChildren(path: Path): List<FileInfo> {
        require(path.exists() && path.isDirectory()) {
            "No directory at path ${path.pathString}"
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
}