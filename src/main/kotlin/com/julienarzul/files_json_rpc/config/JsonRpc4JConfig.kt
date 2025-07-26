package com.julienarzul.files_json_rpc.config

import com.googlecode.jsonrpc4j.JsonRpcServer
import com.julienarzul.files_json_rpc.service.FilesRpcService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JsonRpc4JConfig {

    @Bean
    fun filesJsonRpcServer(filesRpcService: FilesRpcService): JsonRpcServer {
        return JsonRpcServer(filesRpcService, FilesRpcService::class.java)
    }
}