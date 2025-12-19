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
    var isInCheck by mutableStateOf(false)
        private set
    var isCheckmate by mutableStateOf(false)
        private set
    var isStalemate by mutableStateOf(false)
        private set
    
    // Suivi des mouvements pour le roque
    private var whiteKingMoved = false
    private var blackKingMoved = false
    private var whiteRookKingsideMoved = false  // Tour h1
    private var whiteRookQueensideMoved = false // Tour a1
    private var blackRookKingsideMoved = false  // Tour h8
    private var blackRookQueensideMoved = false // Tour a8
    
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
            
            // Vérifier si le nouveau joueur est en échec
            val currentColor = if (whiteToMove) ChessColor.WHITE else ChessColor.BLACK
            isInCheck = isKingInCheck(currentColor)
            
            // Vérifier échec et mat ou pat
            if (!hasAnyLegalMoves(currentColor)) {
                gameOver = true
                if (isInCheck) {
                    isCheckmate = true
                    winner = if (whiteToMove) ChessColor.BLACK else ChessColor.WHITE
                } else {
                    isStalemate = true
                }
            }
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
        
        // Suivi des mouvements pour le roque
        if (piece.type == PieceType.KING) {
            if (piece.color == ChessColor.WHITE) {
                // Vérifier si c'est un roque
                if (from.col == 4 && to.col == 6) {
                    // Petit roque blanc
                    board[7][5] = board[7][7]
                    board[7][7] = null
                } else if (from.col == 4 && to.col == 2) {
                    // Grand roque blanc
                    board[7][3] = board[7][0]
                    board[7][0] = null
                }
                whiteKingMoved = true
            } else {
                // Vérifier si c'est un roque
                if (from.col == 4 && to.col == 6) {
                    // Petit roque noir
                    board[0][5] = board[0][7]
                    board[0][7] = null
                } else if (from.col == 4 && to.col == 2) {
                    // Grand roque noir
                    board[0][3] = board[0][0]
                    board[0][0] = null
                }
                blackKingMoved = true
            }
        }
        
        if (piece.type == PieceType.ROOK) {
            if (piece.color == ChessColor.WHITE) {
                if (from.row == 7 && from.col == 7) whiteRookKingsideMoved = true
                if (from.row == 7 && from.col == 0) whiteRookQueensideMoved = true
            } else {
                if (from.row == 0 && from.col == 7) blackRookKingsideMoved = true
                if (from.row == 0 && from.col == 0) blackRookQueensideMoved = true
            }
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
                
                // Roque
                if (piece.color == ChessColor.WHITE && !whiteKingMoved && !isKingInCheck(ChessColor.WHITE)) {
                    // Petit roque blanc (côté roi)
                    if (!whiteRookKingsideMoved && 
                        board[7][5] == null && board[7][6] == null &&
                        !isSquareAttacked(Position(7, 5), ChessColor.BLACK) &&
                        !isSquareAttacked(Position(7, 6), ChessColor.BLACK)) {
                        moves.add(Position(7, 6))
                    }
                    // Grand roque blanc (côté dame)
                    if (!whiteRookQueensideMoved && 
                        board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                        !isSquareAttacked(Position(7, 2), ChessColor.BLACK) &&
                        !isSquareAttacked(Position(7, 3), ChessColor.BLACK)) {
                        moves.add(Position(7, 2))
                    }
                } else if (piece.color == ChessColor.BLACK && !blackKingMoved && !isKingInCheck(ChessColor.BLACK)) {
                    // Petit roque noir (côté roi)
                    if (!blackRookKingsideMoved && 
                        board[0][5] == null && board[0][6] == null &&
                        !isSquareAttacked(Position(0, 5), ChessColor.WHITE) &&
                        !isSquareAttacked(Position(0, 6), ChessColor.WHITE)) {
                        moves.add(Position(0, 6))
                    }
                    // Grand roque noir (côté dame)
                    if (!blackRookQueensideMoved && 
                        board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                        !isSquareAttacked(Position(0, 2), ChessColor.WHITE) &&
                        !isSquareAttacked(Position(0, 3), ChessColor.WHITE)) {
                        moves.add(Position(0, 2))
                    }
                }
            }
        }

        // Filtrer les mouvements qui laisseraient le roi en échec
        return moves.filter { move ->
            !wouldLeaveKingInCheck(pos, move, piece.color)
        }
    }

    // Trouver la position du roi d'une couleur donnée
    private fun findKingPosition(color: ChessColor): Position? {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece?.type == PieceType.KING && piece.color == color) {
                    return Position(row, col)
                }
            }
        }
        return null
    }

    // Vérifier si le roi d'une couleur donnée est en échec
    private fun isKingInCheck(color: ChessColor): Boolean {
        val kingPos = findKingPosition(color) ?: return false
        return isSquareAttacked(kingPos, if (color == ChessColor.WHITE) ChessColor.BLACK else ChessColor.WHITE)
    }

    // Vérifier si une case est attaquée par une couleur donnée
    private fun isSquareAttacked(pos: Position, byColor: ChessColor): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == byColor) {
                    if (canPieceAttack(Position(row, col), pos, piece)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Vérifier si une pièce peut attaquer une position (sans vérifier l'échec)
    private fun canPieceAttack(from: Position, to: Position, piece: ChessPiece): Boolean {
        val dr = to.row - from.row
        val dc = to.col - from.col

        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == ChessColor.WHITE) -1 else 1
                // Le pion attaque en diagonale
                return dr == direction && (dc == 1 || dc == -1)
            }

            PieceType.KNIGHT -> {
                return (kotlin.math.abs(dr) == 2 && kotlin.math.abs(dc) == 1) ||
                       (kotlin.math.abs(dr) == 1 && kotlin.math.abs(dc) == 2)
            }

            PieceType.BISHOP -> {
                if (kotlin.math.abs(dr) != kotlin.math.abs(dc) || dr == 0) return false
                return isPathClear(from, to)
            }

            PieceType.ROOK -> {
                if (dr != 0 && dc != 0) return false
                return isPathClear(from, to)
            }

            PieceType.QUEEN -> {
                if (dr != 0 && dc != 0 && kotlin.math.abs(dr) != kotlin.math.abs(dc)) return false
                return isPathClear(from, to)
            }

            PieceType.KING -> {
                return kotlin.math.abs(dr) <= 1 && kotlin.math.abs(dc) <= 1 && (dr != 0 || dc != 0)
            }
        }
    }

    // Vérifier si le chemin entre deux positions est libre
    private fun isPathClear(from: Position, to: Position): Boolean {
        val dr = when {
            to.row > from.row -> 1
            to.row < from.row -> -1
            else -> 0
        }
        val dc = when {
            to.col > from.col -> 1
            to.col < from.col -> -1
            else -> 0
        }

        var r = from.row + dr
        var c = from.col + dc
        while (r != to.row || c != to.col) {
            if (board[r][c] != null) return false
            r += dr
            c += dc
        }
        return true
    }

    // Vérifier si un mouvement laisserait le roi en échec
    private fun wouldLeaveKingInCheck(from: Position, to: Position, color: ChessColor): Boolean {
        // Sauvegarder l'état actuel
        val movingPiece = board[from.row][from.col]
        val capturedPiece = board[to.row][to.col]

        // Effectuer le mouvement temporairement
        board[to.row][to.col] = movingPiece
        board[from.row][from.col] = null

        // Vérifier si le roi est en échec
        val inCheck = isKingInCheck(color)

        // Restaurer l'état
        board[from.row][from.col] = movingPiece
        board[to.row][to.col] = capturedPiece

        return inCheck
    }

    // Vérifier si un joueur a au moins un mouvement légal
    private fun hasAnyLegalMoves(color: ChessColor): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val moves = calculateValidMoves(Position(row, col))
                    if (moves.isNotEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
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
        isInCheck = false
        isCheckmate = false
        isStalemate = false
        whiteKingMoved = false
        blackKingMoved = false
        whiteRookKingsideMoved = false
        whiteRookQueensideMoved = false
        blackRookKingsideMoved = false
        blackRookQueensideMoved = false
        boardVersion++
    }
}
