package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.*
import javafx.scene.media.*
import javafx.util.Duration
import java.io.File

@Composable
actual fun SignVideoPlayer(
    resourcePath: String,
    modifier: Modifier
) {
    var isReady by remember(resourcePath) { mutableStateOf(false) }
    var loadFailed by remember(resourcePath) { mutableStateOf(false) }

    val jfxPanel = remember { JFXPanel() }
    val mediaPlayerHolder = remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(resourcePath) {
        Platform.runLater {
            try {
                isReady = false
                loadFailed = false

                // Dispose previous player first
                mediaPlayerHolder.value?.let { oldPlayer ->
                    runCatching {
                        oldPlayer.stop()
                        oldPlayer.dispose()
                    }
                }
                mediaPlayerHolder.value = null

                val mediaUri =
                    when {
                        resourcePath.startsWith("http://") ||
                                resourcePath.startsWith("https://") ||
                                resourcePath.startsWith("file:/") -> resourcePath
                        else -> File(resourcePath).toURI().toString()
                    }

                val media = Media(mediaUri)
                val mediaPlayer = MediaPlayer(media).apply {
                    cycleCount = 1
                }
                mediaPlayerHolder.value = mediaPlayer

                val mediaView = MediaView(mediaPlayer).apply {
                    isPreserveRatio = true
                    fitWidth = 800.0
                    fitHeight = 450.0
                }

                val playButton = Button("Play")
                val pauseButton = Button("Pause")

                fun currentPlayer(): MediaPlayer? = mediaPlayerHolder.value

                fun updateButtons() {
                    val player = currentPlayer()
                    val playing = player?.status == MediaPlayer.Status.PLAYING
                    playButton.isDisable = player == null || playing || !isReady || loadFailed
                    pauseButton.isDisable = player == null || !playing || !isReady || loadFailed
                }

                playButton.setOnAction {
                    val player = currentPlayer() ?: return@setOnAction
                    runCatching { player.play() }
                    updateButtons()
                }

                pauseButton.setOnAction {
                    val player = currentPlayer() ?: return@setOnAction
                    runCatching { player.pause() }
                    updateButtons()
                }

                mediaPlayer.setOnReady {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnReady
                    isReady = true
                    loadFailed = false
                    updateButtons()
                }

                mediaPlayer.setOnPlaying {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnPlaying
                    updateButtons()
                }

                mediaPlayer.setOnPaused {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnPaused
                    updateButtons()
                }

                mediaPlayer.setOnStopped {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnStopped
                    updateButtons()
                }

                mediaPlayer.setOnEndOfMedia {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnEndOfMedia
                    runCatching {
                        mediaPlayer.pause()
                        mediaPlayer.seek(Duration.ZERO)
                    }
                    updateButtons()
                }

                mediaPlayer.setOnError {
                    if (mediaPlayerHolder.value !== mediaPlayer) return@setOnError
                    loadFailed = true
                    println("MediaPlayer error: ${mediaPlayer.error?.message}")
                    updateButtons()
                }

                updateButtons()

                val controls = HBox(12.0, playButton, pauseButton).apply {
                    alignment = Pos.CENTER
                    style = "-fx-padding: 8 0 0 0;"
                }

                val videoContainer = StackPane(mediaView).apply {
                    alignment = Pos.CENTER
                    style = "-fx-background-color: black;"
                    minHeight = 260.0
                    prefHeight = 450.0
                    maxHeight = 450.0
                }

                val root = VBox(12.0, videoContainer, controls).apply {
                    alignment = Pos.CENTER
                    style = "-fx-background-color: transparent;"
                    VBox.setVgrow(videoContainer, Priority.ALWAYS)
                }

                jfxPanel.scene = Scene(root)
            } catch (e: MediaException) {
                loadFailed = true
                println("MediaException: ${e.message}")
            } catch (e: Exception) {
                loadFailed = true
                println("Exception: ${e.message}")
            }
        }

        onDispose {
            Platform.runLater {
                mediaPlayerHolder.value?.let { player ->
                    runCatching {
                        player.stop()
                        player.dispose()
                    }
                }
                mediaPlayerHolder.value = null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!loadFailed) {
                SwingPanel(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { jfxPanel }
                )
            }

            when {
                loadFailed -> {
                    Text(
                        text = "Video unavailable",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                !isReady -> {
                    Text(
                        text = "Loading video…",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}