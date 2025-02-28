package org.graphqlncs.type

import org.springframework.stereotype.Component

@Component
class TriangleClassification {

    fun classify(a: Int, b: Int, c: Int): Int {
        if (a <= 0 || b <= 0 || c <= 0) {
            return 0
        }
        if (a == b && b == c) {
            return 3
        }
        val max = Math.max(a, Math.max(b, c))
        if (max == a && max - b - c >= 0 ||
            max == b && max - a - c >= 0 ||
            max == c && max - a - b >= 0
        ) {
            return 0
        }
        return if (a == b || b == c || a == c) {
            2
        } else {
            1
        }
    }
}