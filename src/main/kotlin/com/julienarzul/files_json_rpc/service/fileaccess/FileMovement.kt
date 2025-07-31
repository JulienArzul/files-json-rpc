package com.julienarzul.files_json_rpc.service.fileaccess

import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.notExists
import kotlin.io.path.pathString

@Component
class FileMovement {

    /**
     * ⚠️ The current implementation doesn't allow to move non-empty directories
     */
    fun moveFile(sourcePath: Path, destinationPath: Path) {
        require(sourcePath.exists()) {
            "No file or directory at source ${sourcePath.pathString}"
        }
        require(destinationPath.notExists()) {
            "Already existing file or directory at destination ${destinationPath.pathString}"
        }

        sourcePath.moveTo(destinationPath)
    }

    /**
     * ⚠️ The current implementation doesn't allow to move non-empty directories
     */
    fun copyFile(sourcePath: Path, destinationPath: Path) {
        require(sourcePath.exists()) {
            "No file or directory at source ${sourcePath.pathString}"
        }
        require(destinationPath.notExists()) {
            "Already existing file or directory at destination ${destinationPath.pathString}"
        }

        sourcePath.copyTo(destinationPath)
    }
}