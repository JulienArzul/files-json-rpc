package com.julienarzul.files_json_rpc.service

data class FileInfo(
    val fileName: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
)
