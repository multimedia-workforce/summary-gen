package jku.multimediasysteme.analytics.resources.huffman

import jku.multimediasysteme.analytics.data.IdsRequest
import jku.multimediasysteme.analytics.service.huffman.HuffmanService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/huffman")
class HuffmanResource(private val huffmanService: HuffmanService) {

    @GetMapping
    fun generate(@AuthenticationPrincipal userId: String): ResponseEntity<Map<Char, String>> {
        return huffmanService.generateHuffmanCode(UUID.fromString(userId))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun generateSelected(
        @AuthenticationPrincipal userId: String,
        @RequestBody body: IdsRequest
    ): ResponseEntity<Map<Char, String>> {
        return huffmanService.generateHuffmanCodes(UUID.fromString(userId), body.ids)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}