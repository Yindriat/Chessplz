package com.example.test.chess

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChessGameScreen(modifier: Modifier = Modifier) {
    val gameState = remember { ChessGameState() }
    var gameStarted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFECECEC))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "♔ Chessplz ♚",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive,
                color = Color(0xFF5D4037)
            )
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (gameStarted) {
            if (gameState.isCheckmate) {
                Text(
                    "👑 ÉCHEC ET MAT ! 👑",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                )
                Text(
                    "Les ${if (gameState.winner == ChessColor.WHITE) "Blancs" else "Noirs"} gagnent !",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                )
            } else if (gameState.isStalemate) {
                Text(
                    "🤝 PAT - Match nul ! 🤝",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                )
            } else {
                Text(
                    "Joueur actuel: ${if (gameState.whiteToMove) "Blanc" else "Noir"}",
                    style = TextStyle(fontSize = 14.sp)
                )
                
                if (gameState.isInCheck) {
                    Text(
                        "⚠️ ÉCHEC ! ⚠️",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Échiquier
            ChessBoardUI(gameState = gameState)

            Spacer(modifier = Modifier.height(8.dp))

            // Pièces capturées par les Blancs (pièces noires mangées)
            val blackCaptured = gameState.capturedPieces.filter { it.color == ChessColor.BLACK }
            Row(
                modifier = Modifier
                    .background(Color(0xFFE0E0E0))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("♔ Blancs: ", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                blackCaptured.forEach { piece ->
                    Text(
                        getPieceUnicode(piece),
                        style = TextStyle(fontSize = 20.sp, color = Color.Black),
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Pièces capturées par les Noirs (pièces blanches mangées)
            val whiteCaptured = gameState.capturedPieces.filter { it.color == ChessColor.WHITE }
            Row(
                modifier = Modifier
                    .background(Color(0xFF424242))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("♚ Noirs: ", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
                whiteCaptured.forEach { piece ->
                    Text(
                        getPieceUnicode(piece),
                        style = TextStyle(fontSize = 20.sp, color = Color.White),
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cliquez sur 'Nouvelle Partie' pour commencer", style = TextStyle(fontSize = 16.sp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = { 
            gameStarted = true
            gameState.resetGame()
        }) {
            Text("Nouvelle partie")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/fr/learn"))
            context.startActivity(intent)
        }) {
            Text("Apprendre les règles")
        }
    }
}

@Composable
fun ChessBoardUI(gameState: ChessGameState) {
    // Voir ChessBoardDragDrop.kt pour l'implémentation
    ChessBoardWithDragDrop(gameState = gameState)
}

