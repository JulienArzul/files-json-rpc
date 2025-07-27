package com.julienarzul.files_json_rpc.service

interface FilesRpcService {
    fun createFile(filePath: String): String

    fun appendToFile(filePath: String, textToAppend: String)

    fun readFile(filePath: String): String

    fun readFile(filePath: String, offset: Long, limit: Int): String

    fun deleteFile(filePath: String)

    fun getFileInfo(filePath: String): FileInfo
}