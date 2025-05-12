package jku.multimediasysteme.analytics.service.huffman

import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service responsible for generating Huffman codes from transcription text data
 * stored in SmartSessions.
 *
 * Uses character frequency analysis and a binary tree to construct
 * an optimal prefix-free Huffman coding.
 *
 * @property smartSessionRepository Repository to access user SmartSessions.
 */
@Service
class HuffmanService(
    private val smartSessionRepository: SmartSessionRepository
) {

    /**
     * Builds a Huffman code map for all transcriptions of the given user.
     *
     * @param userId The user's UUID.
     * @return A map of characters to Huffman codes or null if no content is available.
     */
    fun generateHuffmanCode(userId: UUID): Map<Char, String>? {
        val text = smartSessionRepository.findAllByUserId(userId)
            .mapNotNull { it.transcription?.text }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }

        return text?.let { buildHuffmanCode(it) }
    }

    /**
     * Builds a Huffman code map based on selected SmartSession IDs of a user.
     *
     * @param userId The user's UUID.
     * @param ids A list of selected SmartSession UUIDs.
     * @return A map of characters to Huffman codes or null if no content is available.
     */
    fun generateHuffmanCodes(userId: UUID, ids: List<UUID>): Map<Char, String>? {
        val text = smartSessionRepository.findAllById(ids)
            .filter { it.userId == userId }
            .mapNotNull { it.transcription?.text }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }

        return text?.let { buildHuffmanCode(it) }
    }

    /**
     * Internal method to build a Huffman code map based on character frequency.
     *
     * @param text Input text to analyze.
     * @return A map of characters to their assigned Huffman code.
     */
    private fun buildHuffmanCode(text: String): Map<Char, String> {
        val freq = text.groupingBy { it }.eachCount()
        val pq = PriorityQueue<Node>(compareBy { it.freq })

        // Add all characters as leaf nodes
        freq.forEach { (char, count) -> pq.add(Leaf(char, count)) }

        // Build the Huffman tree
        while (pq.size > 1) {
            val left = pq.remove()
            val right = pq.remove()
            pq.add(Internal(left, right))
        }

        // Traverse the tree to assign binary codes
        val root = pq.remove()
        val codeMap = mutableMapOf<Char, String>()
        generateCode(root, "", codeMap)
        return codeMap
    }

    /**
     * Recursively generates the binary code for each character from the Huffman tree.
     *
     * @param node Current node in the tree.
     * @param code Accumulated binary path string.
     * @param map Target map where character-to-code mapping is stored.
     */
    private fun generateCode(node: Node, code: String, map: MutableMap<Char, String>) {
        when (node) {
            is Leaf -> map[node.char] = code
            is Internal -> {
                generateCode(node.left, code + "0", map)
                generateCode(node.right, code + "1", map)
            }
        }
    }
    // Base node class
    private abstract class Node(val freq: Int)

    // Leaf node represents a character
    private class Leaf(val char: Char, freq: Int) : Node(freq)

    // Internal node represents a combination of two nodes
    private class Internal(val left: Node, val right: Node) : Node(left.freq + right.freq)
}