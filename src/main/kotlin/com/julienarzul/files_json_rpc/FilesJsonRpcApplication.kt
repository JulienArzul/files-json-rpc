package com.julienarzul.files_json_rpc

import com.julienarzul.files_json_rpc.config.FilesProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FilesProperties::class)
class FilesJsonRpcApplication

fun main(args: Array<String>) {
	runApplication<FilesJsonRpcApplication>(*args)
}
