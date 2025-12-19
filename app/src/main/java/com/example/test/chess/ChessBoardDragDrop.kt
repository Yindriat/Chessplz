package com.example.test.chess

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun ChessBoardWithDragDrop(gameState: ChessGameState) {
    val cellSizeDp: Dp = 40.dp
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSizeDp.toPx() }

    // Lecture du boardVersion pour forcer la recomposition
    val boardVersion = gameState.boardVersion

    // États du drag
    var isDragging by remember { mutableStateOf(false) }
    var draggedPiece by remember { mutableStateOf<ChessPiece?>(null) }
    var dragStartPos by remember { mutableStateOf<Position?>(null) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val files = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val ranks = listOf("8", "7", "6", "5", "4", "3", "2", "1")

    Column(
        modifier = Modifier
            .background(Color(0xFF5D4037))
            .padding(4.dp)
    ) {
        // Ligne des lettres en haut
        Row {
            Box(modifier = Modifier.size(20.dp)) // Coin vide
            files.forEach { letter ->
                Box(
                    modifier = Modifier.size(width = cellSizeDp, height = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFFF0D9B5)
                        )
                    )
                }
            }
            Box(modifier = Modifier.size(20.dp)) // Coin vide
        }

        Row {
            // Colonne des chiffres à gauche
            Column {
                ranks.forEach { number ->
                    Box(
                        modifier = Modifier.size(width = 20.dp, height = cellSizeDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            number,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFFF0D9B5)
                            )
                        )
                    }
                }
            }

            // Le plateau principal
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                // Le plateau
                Column(
                    modifier = Modifier
                        .pointerInput(gameState.whiteToMove, gameState.boardVersion) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val col = (startOffset.x / cellSizePx).toInt().coerceIn(0, 7)
                            val row = (startOffset.y / cellSizePx).toInt().coerceIn(0, 7)
                            val pos = Position(row, col)
                            val piece = gameState.getPiece(row, col)

                            if (piece != null && piece.color.toBoolean() == gameState.whiteToMove) {
                                isDragging = true
                                draggedPiece = piece
                                dragStartPos = pos
                                offsetX = startOffset.x - cellSizePx / 2
                                offsetY = startOffset.y - cellSizePx / 2
                                gameState.selectSquare(pos)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (isDragging) {
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        },
                        onDragEnd = {
                            if (isDragging && dragStartPos != null) {
                                val endCol = ((offsetX + cellSizePx / 2) / cellSizePx).toInt().coerceIn(0, 7)
                                val endRow = ((offsetY + cellSizePx / 2) / cellSizePx).toInt().coerceIn(0, 7)
                                val endPos = Position(endRow, endCol)

                                if (endPos in gameState.validMoves) {
                                    gameState.selectSquare(endPos)
                                }
                            }
                            isDragging = false
                            draggedPiece = null
                            dragStartPos = null
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            draggedPiece = null
                            dragStartPos = null
                            offsetX = 0f
                            offsetY = 0f
                        }
                    )
                }
        ) {
            repeat(8) { row ->
                Row {
                    repeat(8) { col ->
                        val isLight = (row + col) % 2 == 0
                        val pos = Position(row, col)
                        val piece = gameState.getPiece(row, col)
                        val isSelected = gameState.selectedPosition == pos
                        val isValidMove = pos in gameState.validMoves
                        val isBeingDragged = isDragging && dragStartPos == pos

                        Box(
                            modifier = Modifier
                                .size(cellSizeDp)
                                .background(
                                    when {
                                        isSelected -> Color(0xFF82B1FF)
                                        isValidMove -> Color(0xFFFFC107)
                                        isLight -> Color(0xFFF0D9B5)
                                        else -> Color(0xFFB58863)
                                    }
                                )
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isBeingDragged && piece != null) {
                                Text(
                                    getPieceUnicode(piece),
                                    style = TextStyle(fontSize = 28.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pièce en cours de déplacement
                if (isDragging && draggedPiece != null) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .zIndex(100f)
                            .size(cellSizeDp)
                            .background(Color(0xCCFFEB3B))
                            .border(2.dp, Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            getPieceUnicode(draggedPiece!!),
                            style = TextStyle(fontSize = 32.sp)
                        )
                    }
                }
            }

            // Colonne des chiffres à droite
            Column {
                ranks.forEach { number ->
                    Box(
                        modifier = Modifier.size(width = 20.dp, height = cellSizeDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            number,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFFF0D9B5)
                            )
                        )
                    }
                }
            }
        }

        // Ligne des lettres en bas
        Row {
            Box(modifier = Modifier.size(20.dp)) // Coin vide
            files.forEach { letter ->
                Box(
                    modifier = Modifier.size(width = cellSizeDp, height = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFFF0D9B5)
                        )
                    )
                }
            }
            Box(modifier = Modifier.size(20.dp)) // Coin vide
        }
    }
}
