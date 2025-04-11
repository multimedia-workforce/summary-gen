package jku.multimediasysteme.analytics.service.huffman

import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class HuffmanService(private val transcriptionRepository: TranscriptionRepository) {
    fun generateHuffmanCode(userId: UUID): Map<Char, String> {
        val text = transcriptionRepository.findAllByUserId(userId).mapNotNull { it.text }.joinToString(" ")

        return buildHuffmanCode(text)
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