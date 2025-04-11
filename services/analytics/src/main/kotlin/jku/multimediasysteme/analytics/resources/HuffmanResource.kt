package jku.multimediasysteme.analytics.resources

import jku.multimediasysteme.analytics.service.huffman.HuffmanService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/huffman")
class HuffmanResource(
    private val huffmanService: HuffmanService
) {

    @GetMapping("/{userId}")
    fun generate(@PathVariable userId: UUID): Map<Char, String> {
        return huffmanService.generateHuffmanCode(userId)
    }
}