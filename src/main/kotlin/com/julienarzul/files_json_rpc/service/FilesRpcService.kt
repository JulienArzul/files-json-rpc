package com.julienarzul.files_json_rpc.service

interface FilesRpcService {
    fun createFile(filePath: String): String

    fun appendToFile(filePath: String, textToAppend: String)

    fun readFile(filePath: String): String

    fun deleteFile(filePath: String)
}