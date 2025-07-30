package com.julienarzul.files_json_rpc.service

import com.julienarzul.files_json_rpc.config.FilesProperties
import com.julienarzul.files_json_rpc.model.FileInfo
import com.julienarzul.files_json_rpc.service.fileaccess.FileMovement
import com.julienarzul.files_json_rpc.service.fileaccess.FileReader
import com.julienarzul.files_json_rpc.service.fileaccess.FileWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute

@Service
class DefaultFilesRpcService(
    val filesProperties: FilesProperties,
    val fileWriter: FileWriter,
    val fileReader: FileReader,
    val fileMovement: FileMovement,
) : FilesRpcService {

    companion object {
        val log: Logger = LoggerFactory.getLogger("DefaultFilesRpcService")
    }

    override fun createFile(filePath: String): String {
        val path = validateAndGetPath(filePath)

        return fileWriter.createFile(path)
    }

    override fun createDirectory(filePath: String): String {
        val path = validateAndGetPath(filePath)

        return fileWriter.createDirectory(path)
    }

    override fun appendToFile(filePath: String, textToAppend: String) {
        val path = validateAndGetPath(filePath)

        fileWriter.appendToFile(path, textToAppend)
    }

    override fun readFile(filePath: String): String {
        return readFile(filePath, 0, -1)
    }

    override fun readFile(filePath: String, offset: Long, limit: Int): String {
        val path = validateAndGetPath(filePath)

        return fileReader.readFile(path, offset, limit)
    }

    override fun deleteFile(filePath: String) {
        val path = validateAndGetPath(filePath)

        return fileWriter.deleteFile(path)
    }

    override fun getFileInfo(filePath: String): FileInfo {
        val path = validateAndGetPath(filePath)

        return fileReader.getFileInfo(path)
    }

    override fun getDirectoryChildren(filePath: String): List<FileInfo> {
        val path = validateAndGetPath(filePath)

        return fileReader.getDirectoryChildren(path)
    }

    override fun moveFile(source: String, destination: String) {
        fileMovement.moveFile(
            validateAndGetPath(source),
            validateAndGetPath(destination)
        )
    }

    override fun copyFile(source: String, destination: String) {
        fileMovement.copyFile(
            validateAndGetPath(source),
            validateAndGetPath(destination)
        )
    }

    private fun validateAndGetPath(filePath: String): Path {
        val rootPath = Paths.get(filesProperties.rootPath)

        val path = rootPath.resolve(filePath)

        require(path.normalize().absolute().startsWith(rootPath.normalize().absolute())) {
            "Impossible to interact with a file outside of the application's root path"
        }

        return path
    }
}