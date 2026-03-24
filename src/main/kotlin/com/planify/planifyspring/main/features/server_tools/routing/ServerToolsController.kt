package com.planify.planifyspring.main.features.server_tools.routing

import com.planify.planifyspring.main.common.entities.ApplicationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tools")
class ServerToolsController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<ApplicationResponse<Nothing>> {
        return ResponseEntity.ok(ApplicationResponse.success())
    }
}
