package jku.multimediasysteme.analytics.service.huffman

import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class HuffmanService(
    private val smartSessionRepository: SmartSessionRepository
) {
    fun generateHuffmanCode(userId: UUID): Map<Char, String>? {
        val text = smartSessionRepository.findAllByUserId(userId)
            .mapNotNull { it.transcription?.text }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }

        return text?.let { buildHuffmanCode(it) }
    }

    fun generateHuffmanCodes(userId: UUID, ids: List<UUID>): Map<Char, String>? {
        val text = smartSessionRepository.findAllById(ids)
            .filter { it.userId == userId }
            .mapNotNull { it.transcription?.text }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }

        return text?.let { buildHuffmanCode(it) }
    }

    private fun buildHuffmanCode(text: String): Map<Char, String> {
        val freq = text.groupingBy { it }.eachCount()
        val pq = PriorityQueue<Node>(compareBy { it.freq })

        freq.forEach { (char, count) -> pq.add(Leaf(char, count)) }

        while (pq.size > 1) {
            val left = pq.remove()
            val right = pq.remove()
            pq.add(Internal(left, right))
        }

        val root = pq.remove()
        val codeMap = mutableMapOf<Char, String>()
        generateCode(root, "", codeMap)
        return codeMap
    }

    private fun generateCode(node: Node, code: String, map: MutableMap<Char, String>) {
        when (node) {
            is Leaf -> map[node.char] = code
            is Internal -> {
                generateCode(node.left, code + "0", map)
                generateCode(node.right, code + "1", map)
            }
        }
    }

    private abstract class Node(val freq: Int)
    private class Leaf(val char: Char, freq: Int) : Node(freq)
    private class Internal(val left: Node, val right: Node) : Node(left.freq + right.freq)
}