package com.example.test.chess

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// État du jeu d'échecs avec état réactif Compose
class ChessGameState {
    private val board = Array(8) { Array<ChessPiece?>(8) { null } }
    
    var selectedPosition by mutableStateOf<Position?>(null)
        private set
    var validMoves by mutableStateOf<List<Position>>(emptyList())
        private set
    var whiteToMove by mutableStateOf(true)
        private set
    var gameOver by mutableStateOf(false)
        private set
    var winner by mutableStateOf<ChessColor?>(null)
        private set
    var capturedPieces by mutableStateOf<List<ChessPiece>>(emptyList())
        private set
    
    // Compteur de version pour forcer la recomposition du plateau
    var boardVersion by mutableStateOf(0)
        private set

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        // Placement des pièces noires
        board[0][0] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        board[0][1] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        board[0][2] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        board[0][3] = ChessPiece(PieceType.QUEEN, ChessColor.BLACK)
        board[0][4] = ChessPiece(PieceType.KING, ChessColor.BLACK)
        board[0][5] = ChessPiece(PieceType.BISHOP, ChessColor.BLACK)
        board[0][6] = ChessPiece(PieceType.KNIGHT, ChessColor.BLACK)
        board[0][7] = ChessPiece(PieceType.ROOK, ChessColor.BLACK)
        for (col in 0 until 8) {
            board[1][col] = ChessPiece(PieceType.PAWN, ChessColor.BLACK)
        }

        // Placement des pièces blanches
        for (col in 0 until 8) {
            board[6][col] = ChessPiece(PieceType.PAWN, ChessColor.WHITE)
        }
        board[7][0] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)
        board[7][1] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)
        board[7][2] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)
        board[7][3] = ChessPiece(PieceType.QUEEN, ChessColor.WHITE)
        board[7][4] = ChessPiece(PieceType.KING, ChessColor.WHITE)
        board[7][5] = ChessPiece(PieceType.BISHOP, ChessColor.WHITE)
        board[7][6] = ChessPiece(PieceType.KNIGHT, ChessColor.WHITE)
        board[7][7] = ChessPiece(PieceType.ROOK, ChessColor.WHITE)
    }

    fun getPiece(row: Int, col: Int): ChessPiece? = board[row][col]

    fun selectSquare(pos: Position) {
        val piece = getPiece(pos.row, pos.col)

        if (selectedPosition == null) {
            if (piece != null && piece.color.toBoolean() == whiteToMove) {
                selectedPosition = pos
                validMoves = calculateValidMoves(pos)
            }
        } else if (selectedPosition == pos) {
            selectedPosition = null
            validMoves = emptyList()
        } else if (pos in validMoves) {
            val from = selectedPosition!!
            movePiece(from, pos)
            selectedPosition = null
            validMoves = emptyList()
            whiteToMove = !whiteToMove
        } else if (piece != null && piece.color.toBoolean() == whiteToMove) {
            selectedPosition = pos
            validMoves = calculateValidMoves(pos)
        } else {
            selectedPosition = null
            validMoves = emptyList()
        }
    }

    private fun movePiece(from: Position, to: Position) {
        val piece = board[from.row][from.col] ?: return
        val captured = board[to.row][to.col]
        
        if (captured != null) {
            capturedPieces = capturedPieces + captured
        }
        
        board[to.row][to.col] = piece
        board[from.row][from.col] = null
        boardVersion++ // Force la recomposition
    }

    private fun calculateValidMoves(pos: Position): List<Position> {
        val piece = getPiece(pos.row, pos.col) ?: return emptyList()
        val moves = mutableListOf<Position>()

        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == ChessColor.WHITE) -1 else 1
                val startRow = if (piece.color == ChessColor.WHITE) 6 else 1
                
                val nextRow = pos.row + direction
                if (nextRow in 0..7 && getPiece(nextRow, pos.col) == null) {
                    moves.add(Position(nextRow, pos.col))
                }

                if (pos.row == startRow) {
                    val twoRow = pos.row + 2 * direction
                    if (getPiece(pos.row + direction, pos.col) == null && getPiece(twoRow, pos.col) == null) {
                        moves.add(Position(twoRow, pos.col))
                    }
                }

                for (col in listOf(pos.col - 1, pos.col + 1)) {
                    if (col in 0..7) {
                        val captureRow = pos.row + direction
                        getPiece(captureRow, col)?.let {
                            if (it.color != piece.color) moves.add(Position(captureRow, col))
                        }
                    }
                }
            }

            PieceType.ROOK -> {
                // Vers le haut
                for (r in (pos.row - 1) downTo 0) {
                    val target = getPiece(r, pos.col)
                    if (target == null) {
                        moves.add(Position(r, pos.col))
                    } else {
                        if (target.color != piece.color) moves.add(Position(r, pos.col))
                        break
                    }
                }
                // Vers le bas
                for (r in (pos.row + 1)..7) {
                    val target = getPiece(r, pos.col)
                    if (target == null) {
                        moves.add(Position(r, pos.col))
                    } else {
                        if (target.color != piece.color) moves.add(Position(r, pos.col))
                        break
                    }
                }
                // Vers la gauche
                for (c in (pos.col - 1) downTo 0) {
                    val target = getPiece(pos.row, c)
                    if (target == null) {
                        moves.add(Position(pos.row, c))
                    } else {
                        if (target.color != piece.color) moves.add(Position(pos.row, c))
                        break
                    }
                }
                // Vers la droite
                for (c in (pos.col + 1)..7) {
                    val target = getPiece(pos.row, c)
                    if (target == null) {
                        moves.add(Position(pos.row, c))
                    } else {
                        if (target.color != piece.color) moves.add(Position(pos.row, c))
                        break
                    }
                }
            }

            PieceType.KNIGHT -> {
                val knightMoves = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
                for ((dr, dc) in knightMoves) {
                    val newRow = pos.row + dr
                    val newCol = pos.col + dc
                    if (newRow in 0..7 && newCol in 0..7) {
                        getPiece(newRow, newCol)?.let {
                            if (it.color != piece.color) moves.add(Position(newRow, newCol))
                        } ?: moves.add(Position(newRow, newCol))
                    }
                }
            }

            PieceType.BISHOP -> {
                for (dir in listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)) {
                    var r = pos.row + dir.first
                    var c = pos.col + dir.second
                    while (r in 0..7 && c in 0..7) {
                        if (getPiece(r, c) == null) moves.add(Position(r, c))
                        else {
                            if (getPiece(r, c)?.color != piece.color) moves.add(Position(r, c))
                            break
                        }
                        r += dir.first
                        c += dir.second
                    }
                }
            }

            PieceType.QUEEN -> {
                // Mouvements comme la tour (lignes droites)
                // Vers le haut
                for (r in (pos.row - 1) downTo 0) {
                    val target = getPiece(r, pos.col)
                    if (target == null) {
                        moves.add(Position(r, pos.col))
                    } else {
                        if (target.color != piece.color) moves.add(Position(r, pos.col))
                        break
                    }
                }
                // Vers le bas
                for (r in (pos.row + 1)..7) {
                    val target = getPiece(r, pos.col)
                    if (target == null) {
                        moves.add(Position(r, pos.col))
                    } else {
                        if (target.color != piece.color) moves.add(Position(r, pos.col))
                        break
                    }
                }
                // Vers la gauche
                for (c in (pos.col - 1) downTo 0) {
                    val target = getPiece(pos.row, c)
                    if (target == null) {
                        moves.add(Position(pos.row, c))
                    } else {
                        if (target.color != piece.color) moves.add(Position(pos.row, c))
                        break
                    }
                }
                // Vers la droite
                for (c in (pos.col + 1)..7) {
                    val target = getPiece(pos.row, c)
                    if (target == null) {
                        moves.add(Position(pos.row, c))
                    } else {
                        if (target.color != piece.color) moves.add(Position(pos.row, c))
                        break
                    }
                }
                // Mouvements comme le fou (diagonales)
                for (dir in listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)) {
                    var r = pos.row + dir.first
                    var c = pos.col + dir.second
                    while (r in 0..7 && c in 0..7) {
                        val target = getPiece(r, c)
                        if (target == null) {
                            moves.add(Position(r, c))
                        } else {
                            if (target.color != piece.color) moves.add(Position(r, c))
                            break
                        }
                        r += dir.first
                        c += dir.second
                    }
                }
            }

            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val newRow = pos.row + dr
                        val newCol = pos.col + dc
                        if (newRow in 0..7 && newCol in 0..7) {
                            getPiece(newRow, newCol)?.let {
                                if (it.color != piece.color) moves.add(Position(newRow, newCol))
                            } ?: moves.add(Position(newRow, newCol))
                        }
                    }
                }
            }
        }

        return moves
    }

    fun resetGame() {
        board.forEach { it.fill(null) }
        initializeBoard()
        selectedPosition = null
        validMoves = emptyList()
        whiteToMove = true
        gameOver = false
        winner = null
        capturedPieces = emptyList()
        boardVersion++
    }
}
