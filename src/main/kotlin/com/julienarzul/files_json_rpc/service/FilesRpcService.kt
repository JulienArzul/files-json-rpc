package com.julienarzul.files_json_rpc.service

interface FilesRpcService {
    fun createFile(filePath: String): String

    fun createDirectory(filePath: String): String

    fun deleteFile(filePath: String)

    fun getFileInfo(filePath: String): FileInfo

    fun getDirectoryChildren(filePath: String): List<FileInfo>

    fun appendToFile(filePath: String, textToAppend: String)

    fun readFile(filePath: String): String

    fun readFile(filePath: String, offset: Long, limit: Int): String

    fun moveFile(source: String, destination: String)

    fun copyFile(source: String, destination: String)
}