package com.julienarzul.files_json_rpc.service

import com.julienarzul.files_json_rpc.config.FilesProperties
import com.julienarzul.files_json_rpc.model.FileInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Paths
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class DefaultFilesRpcServiceTest {

    @Autowired
    private lateinit var filesProperties: FilesProperties

    @Autowired
    private lateinit var underTest: DefaultFilesRpcService

    @OptIn(ExperimentalPathApi::class)
    @BeforeEach
    fun emptyRootTestDirectory() {
        Paths.get(filesProperties.rootPath)
            .apply {
                if (notExists()) {
                    createDirectory()
                }
            }
            .listDirectoryEntries()
            .forEach { it.deleteRecursively() }
    }

    @Test
    fun createFile() {
        underTest.createFile("test.txt")

        val expectedPath = Paths.get(filesProperties.rootPath, "test.txt")
        assertTrue { expectedPath.exists() && expectedPath.isRegularFile() }
    }

    @Test
    fun createFile_failsForExistingFile() {
        underTest.createFile("test.txt")

        assertTrue { Paths.get(filesProperties.rootPath, "test.txt").exists() }

        assertThrows<IllegalArgumentException> {
            underTest.createFile("test.txt")
        }
    }

    @Test
    fun createDirectory() {
        underTest.createDirectory("directory")

        val expectedPath = Paths.get(filesProperties.rootPath, "directory")
        assertTrue { expectedPath.exists() && expectedPath.isDirectory() }
    }

    @Test
    fun getFileInfo() {
        val filePath = "test.txt"
        underTest.createFile(filePath)
        underTest.appendToFile(filePath, "my text")

        val result = underTest.getFileInfo(filePath)

        val expectedFilePath = Paths.get(filesProperties.rootPath, filePath).pathString
        val expected = FileInfo(fileName = filePath, path = expectedFilePath, size = 7, isDirectory = false)
        assertEquals(expected, result)
    }

    @Test
    fun getDirectoryInfo() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val result = underTest.getFileInfo(directoryPath)

        val expectedFilePath = Paths.get(filesProperties.rootPath, directoryPath).pathString
        val expected = FileInfo(fileName = "directory", path = expectedFilePath, size = 192, isDirectory = true)
        assertEquals(expected, result)
    }

    @Test
    fun getDirectoryChildren() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val result = underTest.getDirectoryChildren(directoryPath)

        val expectedDirectoryPath = Paths.get(filesProperties.rootPath, directoryPath).pathString
        val expected = listOf(
            FileInfo(
                fileName = "subdirectory1",
                path = "$expectedDirectoryPath/subdirectory1",
                size = 64,
                isDirectory = true
            ),
            FileInfo(
                fileName = "subdirectory2",
                path = "$expectedDirectoryPath/subdirectory2",
                size = 96,
                isDirectory = true
            ),
            FileInfo(fileName = "file1", path = "$expectedDirectoryPath/file1", size = 4, isDirectory = false),
            FileInfo(fileName = "file3", path = "$expectedDirectoryPath/file3", size = 11, isDirectory = false),
        )
        assertEquals(expected, result)
    }

    @Test
    fun moveFile() {
        val directoryPath = "directory"
        withDirectory(directoryPath)
        val actualDirectoryPath = Paths.get(filesProperties.rootPath, directoryPath).pathString

        assertTrue { Paths.get(actualDirectoryPath, "file1").exists() }
        assertTrue { Paths.get(actualDirectoryPath, "subdirectory1/newName").notExists() }

        underTest.moveFile("$directoryPath/file1", "$directoryPath/subdirectory1/newName")

        assertTrue { Paths.get(actualDirectoryPath, "file1").notExists() }
        assertTrue { Paths.get(actualDirectoryPath, "subdirectory1/newName").exists() }
    }

    @Test
    fun copyFile() {
        val directoryPath = "directory"
        withDirectory(directoryPath)
        val actualDirectoryPath = Paths.get(filesProperties.rootPath, directoryPath).pathString

        assertTrue { Paths.get(actualDirectoryPath, "file3").exists() }
        assertTrue { Paths.get(actualDirectoryPath, "subdirectory1/newName").notExists() }

        underTest.copyFile("$directoryPath/file3", "$directoryPath/subdirectory1/newName")

        val sourceFilePath = Paths.get(actualDirectoryPath, "file3")
        val destinationPath = Paths.get(actualDirectoryPath, "subdirectory1/newName")
        assertTrue { sourceFilePath.exists() }
        assertTrue { destinationPath.exists() }
        assertTrue { sourceFilePath.fileSize() == destinationPath.fileSize() }
    }

    @Test
    fun deleteFile() {
        val directoryPath = "directory"
        withDirectory(directoryPath)
        val actualDirectoryPath = Paths.get(filesProperties.rootPath, directoryPath).pathString

        assertTrue { Paths.get(actualDirectoryPath, "file3").exists() }

        underTest.deleteFile("$directoryPath/file3")

        assertTrue { Paths.get(actualDirectoryPath, "file3").notExists() }
    }

    @Test
    fun deleteDirectory() {
        val directoryPath = "directory"
        withDirectory(directoryPath)
        val actualDirectoryPath = Paths.get(filesProperties.rootPath, directoryPath).pathString

        assertTrue { Paths.get(actualDirectoryPath, "subdirectory1").exists() }

        underTest.deleteFile("$directoryPath/subdirectory1")

        assertTrue { Paths.get(actualDirectoryPath, "subdirectory1").notExists() }
    }

    @Test
    fun readFile__fully() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val textFile1 = underTest.readFile("$directoryPath/file1")

        assertEquals("text", textFile1)
    }

    @Test
    fun readFile__with_offset() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val textFile1 = underTest.readFile("$directoryPath/file3", 4, -1)

        assertEquals("er text", textFile1)
    }

    @Test
    fun readFile__with_limit() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val textFile1 = underTest.readFile("$directoryPath/file3", 0, 5)

        assertEquals("longe", textFile1)
    }

    @Test
    fun readFile__with_offset_and_limit() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        val textFile1 = underTest.readFile("$directoryPath/file3", 5, 3)

        assertEquals("r t", textFile1)
    }

    @Test
    fun readFile__with_offset_too_high() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        assertThrows<java.lang.IllegalArgumentException> {
            underTest.readFile("$directoryPath/file3", 11, -1)
        }
    }

    @Test
    fun readFile__with_limit_too_high() {
        val directoryPath = "directory"
        withDirectory(directoryPath)

        assertThrows<java.lang.IllegalArgumentException> {
            underTest.readFile("$directoryPath/file3", 9, 3)
        }
    }

    @Test
    fun appendToFile() {
        val filePath = "test.txt"
        underTest.createFile(filePath)
        val actualFilePath = Paths.get(filesProperties.rootPath, filePath)
        assertEquals(0, actualFilePath.fileSize())
        assertEquals("", actualFilePath.bufferedReader().use { it.readText() })

        underTest.appendToFile(filePath, "first_text")

        assertEquals(10, actualFilePath.fileSize())
        assertEquals("first_text", actualFilePath.bufferedReader().use { it.readText() })

        underTest.appendToFile(filePath, " with more")

        assertEquals(20, actualFilePath.fileSize())
        assertEquals("first_text with more", actualFilePath.bufferedReader().use { it.readText() })
    }

    @Test
    fun createFileOutsideOfRootDirectory() {
        val relativePath = "../test.txt"
        assertThrows<java.lang.IllegalArgumentException> {
            underTest.createFile(relativePath)
        }

        val absolutePath = "${FileSystemView.getFileSystemView().homeDirectory.absolutePath}/test.txt"
        assertThrows<java.lang.IllegalArgumentException> {
            underTest.createFile(absolutePath)
        }
    }

    @Test
    fun canCeateFilesWithAbsolutePath() {
        val relativePath = "../test.txt"
        assertThrows<java.lang.IllegalArgumentException> {
            underTest.createFile(relativePath)
        }

        val absolutePath = "${FileSystemView.getFileSystemView().homeDirectory.absolutePath}/test.txt"
        assertThrows<java.lang.IllegalArgumentException> {
            underTest.createFile(absolutePath)
        }
    }

    private fun withDirectory(directoryPath: String) {
        underTest.createDirectory(directoryPath)
        underTest.createDirectory("$directoryPath/subdirectory1")
        underTest.createDirectory("$directoryPath/subdirectory2")
        underTest.createFile("$directoryPath/subdirectory2/file2")
        underTest.createFile("$directoryPath/file1")
        underTest.appendToFile("$directoryPath/file1", "text")
        underTest.createFile("$directoryPath/file3")
        underTest.appendToFile("$directoryPath/file3", "longer text")
    }
}