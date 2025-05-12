package jku.multimediasysteme.analytics.resources.huffman

import jku.multimediasysteme.analytics.data.IdsRequest
import jku.multimediasysteme.analytics.service.huffman.HuffmanService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller that provides endpoints for generating Huffman codes
 * based on a user's transcriptions.
 *
 * Supports both global and session-specific Huffman encoding.
 *
 * @property huffmanService Business logic for Huffman code generation.
 */
@RestController
@RequestMapping("/huffman")
class HuffmanResource(private val huffmanService: HuffmanService) {

    /**
     * Generates a Huffman code table for all transcriptions of the authenticated user.
     *
     * @param userId Extracted user ID from the JWT token.
     * @return A map of characters to their Huffman codes, or 404 if no data available.
     */
    @GetMapping
    fun generate(@AuthenticationPrincipal userId: String): ResponseEntity<Map<Char, String>> {
        return huffmanService.generateHuffmanCode(UUID.fromString(userId))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Generates a Huffman code table based on selected SmartSession IDs.
     *
     * @param userId Extracted user ID from the JWT token.
     * @param body Request body containing selected SmartSession IDs.
     * @return A map of characters to Huffman codes, or 404 if no valid sessions are found.
     */
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