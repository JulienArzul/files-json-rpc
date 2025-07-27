package com.julienarzul.files_json_rpc.model

data class FileInfo(
    val fileName: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
)