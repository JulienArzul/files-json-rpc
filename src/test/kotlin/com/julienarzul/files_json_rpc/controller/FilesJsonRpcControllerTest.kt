package com.julienarzul.files_json_rpc.controller

import com.julienarzul.files_json_rpc.service.fileaccess.FileWriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.file.Path
import kotlin.io.path.pathString

@SpringBootTest
class FilesJsonRpcControllerTest {

    @Autowired
    private lateinit var controllerUnderTest: FilesJsonRpcController

    @MockitoBean
    private lateinit var fileWriter: FileWriter

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).build()
    }

    @Test
    fun createFile() {
        whenever(fileWriter.createFile(any())).thenAnswer { (it.arguments[0] as Path).pathString }

        mockMvc.perform(
            MockMvcRequestBuilders.post("/rpc/files")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "jsonrpc": "2.0",
                      "method": "createFile",
                      "params": ["myrpcfile.txt"],
                      "id": 1
                    }
                """.trimIndent()
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "result": "src/test/resources/testFileSystem/myrpcfile.txt"
                }
            """.trimIndent()
                )
            )
    }
}