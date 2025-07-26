package com.julienarzul.files_json_rpc.service

interface FilesRpcService {
    fun appendToFile(filePath: String, textToAppend: String)
}