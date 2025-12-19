package com.example.test.chess

// Énumération pour les types de pièces
enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

// Énumération pour les couleurs
enum class ChessColor {
    WHITE, BLACK
}

// Classe représentant une pièce d'échecs
data class ChessPiece(val type: PieceType, val color: ChessColor)

fun ChessColor.toBoolean(): Boolean = this == ChessColor.WHITE

fun getPieceUnicode(piece: ChessPiece): String = when {
    piece.color == ChessColor.WHITE -> when (piece.type) {
        PieceType.PAWN -> "♙"
        PieceType.ROOK -> "♖"
        PieceType.KNIGHT -> "♘"
        PieceType.BISHOP -> "♗"
        PieceType.QUEEN -> "♕"
        PieceType.KING -> "♔"
    }
    else -> when (piece.type) {
        PieceType.PAWN -> "♟"
        PieceType.ROOK -> "♜"
        PieceType.KNIGHT -> "♞"
        PieceType.BISHOP -> "♝"
        PieceType.QUEEN -> "♛"
        PieceType.KING -> "♚"
    }
}
