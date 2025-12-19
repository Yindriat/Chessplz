package com.example.test.chess

import androidx.compose.ui.geometry.Offset

// État du drag and drop
data class DragDropState(
    val isDragging: Boolean = false,
    val draggedPiece: ChessPiece? = null,
    val dragStartPosition: Position? = null,
    val currentDragOffset: Offset = Offset.Zero,
    val boardTopLeft: Offset = Offset.Zero,
    val cellSize: Float = 40f
) {
    // Calcule la cellule sur laquelle le doigt se trouve actuellement
    fun getCurrentHoveredCell(): Position? {
        if (!isDragging) return null
        
        val absoluteX = boardTopLeft.x + currentDragOffset.x
        val absoluteY = boardTopLeft.y + currentDragOffset.y
        
        val col = ((absoluteX - boardTopLeft.x) / cellSize).toInt()
        val row = ((absoluteY - boardTopLeft.y) / cellSize).toInt()
        
        return if (row in 0..7 && col in 0..7) {
            Position(row, col)
        } else {
            null
        }
    }

    // Position de la pièce en cours de déplacement (pour l'affichage)
    fun getDraggedPieceOffset(): Offset {
        if (!isDragging) return Offset.Zero
        
        val centerOffsetX = currentDragOffset.x - cellSize / 2
        val centerOffsetY = currentDragOffset.y - cellSize / 2
        
        return Offset(centerOffsetX, centerOffsetY)
    }
}
