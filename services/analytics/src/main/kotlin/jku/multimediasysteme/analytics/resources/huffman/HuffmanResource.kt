package jku.multimediasysteme.analytics.resources.huffman

import jku.multimediasysteme.analytics.service.huffman.HuffmanService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/huffman")
class HuffmanResource(private val huffmanService: HuffmanService) {

    @GetMapping
    fun generate(@AuthenticationPrincipal userId: String): ResponseEntity<Map<Char, String>> {
        return huffmanService.generateHuffmanCode(UUID.fromString(userId))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}