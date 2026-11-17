package com.example.isnscale

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.isnscale.ar.ArMeasureView
import com.example.isnscale.ar.ArStatus
import com.example.isnscale.ar.ArSupport
import com.example.isnscale.ui.theme.ISNscaleTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISNscaleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeasureScreen()
                }
            }
        }
    }
}

/**
 * Root screen: resolves permission + AR support, then shows the live AR measurement view.
 */
@Composable
fun MeasureScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(ArSupport.hasCameraPermission(context)) }
    var arStatus by remember { mutableStateOf(ArStatus.CHECKING) }
    var useInches by remember { mutableStateOf(false) }
    var supportCheckKey by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Re-check permission and AR support whenever the app comes back to the foreground, for
    // example after the user installs Google Play Services for AR.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ArSupport.hasCameraPermission(context)
                supportCheckKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!ArSupport.hasCameraPermission(context)) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(supportCheckKey) {
        arStatus = ArSupport.resolve(context)
    }

    when {
        !hasCameraPermission -> MessageScreen(
            title = "Camera access needed",
            body = "ISNscale measures real objects through the camera. Grant camera access to continue.",
            actionLabel = "Grant camera access",
            onAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        )

        arStatus == ArStatus.CHECKING -> LoadingScreen()

        arStatus == ArStatus.NEEDS_INSTALL -> MessageScreen(
            title = "One-time AR setup",
            body = "This phone supports AR measurement, but Google Play Services for AR is not installed. " +
                "Install it once and ISNscale can measure anything without a reference object.",
            actionLabel = "Install AR services",
            onAction = { ArSupport.requestInstall(context) },
        )

        arStatus == ArStatus.UNSUPPORTED -> MessageScreen(
            title = "AR is not supported",
            body = "This device cannot run ARCore, which ISNscale needs to know how far away objects are. " +
                "AR works on most recent Android phones with Google Play Services for AR available.",
            actionLabel = "Check again",
            onAction = { supportCheckKey++ },
        )

        else -> ArMeasurementScreen(
            useInches = useInches,
            onToggleUnits = { useInches = !useInches },
        )
    }
}

/** The live AR camera view with the measurement readout on top of it. */
@Composable
private fun ArMeasurementScreen(useInches: Boolean, onToggleUnits: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val arView = remember { ArMeasureView(context) }
    val uiState by arView.state.collectAsStateWithLifecycle()
    val tapPosition by arView.tapPosition.collectAsStateWithLifecycle(floatArrayOf(0.5f, 0.5f))
    val tapHit by arView.tapHit.collectAsStateWithLifecycle(false)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    arView.onResume()
                    // Request session creation - will happen on GL thread during onDrawFrame
                    arView.requestSessionCreation()
                }
                Lifecycle.Event.ON_PAUSE -> arView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // The observer only fires on future events, so start the session immediately if the
        // lifecycle is already resumed.
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            arView.onResume()
            arView.requestSessionCreation()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            arView.onPause()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full-screen camera preview (background)
        AndroidView(
            factory = { arView },
            modifier = Modifier.fillMaxSize()
        )

        // Center crosshair - always visible so users know where to tap
        Crosshair()

        // Tap feedback dot - shows where user last tapped
        TapDot(
            x = tapPosition[0],
            y = tapPosition[1],
            hit = tapHit,
        )

        // UI overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            StatusPill(
                text = when {
                    uiState.error != null -> "AR error"
                    uiState.tracking -> "Tracking"
                    else -> "Look for a surface"
                },
                healthy = uiState.tracking && uiState.error == null,
            )

            Spacer(modifier = Modifier.weight(1f))

            MeasurementCard(
                value = uiState.distanceCm?.let { Measurement.format(it, imperial = useInches) } ?: "--",
                hint = uiState.error ?: uiState.hint,
                points = uiState.points,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { arView.clear() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onToggleUnits,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (useInches) "Show cm" else "Show inches")
                }
            }
        }
    }
}

/** Center crosshair - iPhone Measure app style. */
@Composable
private fun Crosshair() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(2.dp)
                    .background(Color.White)
                    .height(24.dp),
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .width(2.dp)
                    .background(Color.White),
            )
        }
    }
}

/** Tap feedback dot - green for hit, red for miss. */
@Composable
private fun TapDot(x: Float, y: Float, hit: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = (x - 0.5f) * size.width
                // Compose y increases downward, matching Android touch coords
                translationY = (y - 0.5f) * size.height
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    color = if (hit) Color.Green else Color.Red,
                    shape = CircleShape,
                )
                .alpha(0.85f)
        )
    }
}

@Composable
private fun StatusPill(text: String, healthy: Boolean) {
    Row(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.45f),
                shape = CircleShape,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (healthy) Color(0xFF2ECC71) else Color(0xFFFFB300),
                    shape = CircleShape,
                )
                .width(10.dp)
                .height(10.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun MeasurementCard(value: String, hint: String, points: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (points == 0) "Tap two points to measure" else "Point $points of 2",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Starting AR…")
        }
    }
}

@Composable
private fun MessageScreen(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
