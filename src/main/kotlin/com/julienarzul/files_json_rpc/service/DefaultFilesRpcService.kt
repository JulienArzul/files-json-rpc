package com.julienarzul.files_json_rpc.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultFilesRpcService : FilesRpcService {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DefaultFilesRpcService")
    }

    override fun appendToFile(filePath: String, textToAppend: String) {
        log.info("appendToFile called with params: filePath = $filePath, textToAppend = $textToAppend")
    }
}