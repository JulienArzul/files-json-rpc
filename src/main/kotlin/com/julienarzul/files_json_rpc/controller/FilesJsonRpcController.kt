package com.julienarzul.files_json_rpc.controller

import com.googlecode.jsonrpc4j.JsonRpcServer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping


@Controller
class FilesJsonRpcController(private val jsonRpcServer: JsonRpcServer) {

    @PostMapping("/rpc/files")
    fun handleFilesJsonRpcRequests(req: HttpServletRequest, resp: HttpServletResponse) {
        jsonRpcServer.handle(req, resp)
    }
}