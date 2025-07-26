package com.julienarzul.files_json_rpc.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "files")
data class FilesProperties(val rootPath: String)