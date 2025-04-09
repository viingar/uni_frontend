package com.example.myapplication.Screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ViewModels.MainViewModel
import com.example.myapplication.ViewModels.MainViewModel.MainViewModelFactory
import com.example.myapplication.api.Receipt
import com.example.myapplication.api.ReceiptData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current)
    )
) {
    val showDialog by viewModel.showDialog.collectAsState()
    val receipts by remember { derivedStateOf { viewModel.receipts } }
    var showFilterMenu by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf<FilterType?>(null) }
    var selectedStore by remember { mutableStateOf<String?>(null) }
    var showStoreSubmenu by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(Period.WEEK) }
    var showScanner by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Карта", "Главная", "Профиль")


    val storeNames by remember(receipts) {
        derivedStateOf {
            receipts.map { it.storeName }.distinct().sorted()
        }
    }

    val filteredReceipts by remember(receipts, filterType, selectedStore) {
        derivedStateOf {
            when (filterType) {
                FilterType.DATE -> {
                    receipts.sortedByDescending { receipt ->
                        receipt.dateTime
                    }
                }
                FilterType.STORE -> {
                    if (selectedStore != null) {
                        receipts.filter { it.storeName == selectedStore }
                    } else {
                        receipts.sortedBy { it.storeName }
                    }
                }
                FilterType.AMOUNT -> {
                    receipts.sortedByDescending { receipt ->
                        receipt.totalSum.parseLocalizedNumber()
                    }
                }
                null -> receipts
            }
        }
    }

    val groupedReceipts by remember(filteredReceipts) {
        derivedStateOf {
            filteredReceipts.groupBy { receipt ->
                receipt.dateTime
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    if (showScanner) {
        QRCodeScanner { qrContent ->
            showScanner = false
            if (qrContent != null) {
                val receiptData = parseQRContent(qrContent)
                if (receiptData != null) {
                    viewModel.addReceipt(receiptData)
                    viewModel.loadReceipts()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои чеки") },
                actions = {
                    IconButton(onClick = { showAnalyticsDialog = true }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Аналитика расходов")
                    }

                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("По дате") },
                            onClick = {
                                filterType = FilterType.DATE
                                selectedStore = null
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По магазину") },
                            onClick = {
                                showStoreSubmenu = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По сумме") },
                            onClick = {
                                filterType = FilterType.AMOUNT
                                selectedStore = null
                                showFilterMenu = false
                            }
                        )
                        if (filterType != null || selectedStore != null) {
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Сбросить фильтр") },
                                onClick = {
                                    filterType = null
                                    selectedStore = null
                                    showFilterMenu = false
                                }
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStoreSubmenu,
                        onDismissRequest = { showStoreSubmenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Все магазины") },
                            onClick = {
                                filterType = FilterType.STORE
                                selectedStore = null
                                showStoreSubmenu = false
                                showFilterMenu = false
                            }
                        )
                        Divider()
                        storeNames.forEach { storeName ->
                            DropdownMenuItem(
                                text = { Text(storeName) },
                                onClick = {
                                    filterType = FilterType.STORE
                                    selectedStore = if (storeName == "Неизвестный магазин") null else storeName
                                    showStoreSubmenu = false
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clickable { showScanner = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Сканировать чек",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Сканировать чек",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                FloatingActionButton(onClick = { viewModel.showAddManualDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add manually")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(
                                    Icons.Default.AddLocation,
                                    contentDescription = item,
                                    modifier = Modifier.size(25.dp)
                                )
                                1 -> Icon(
                                    Icons.Default.Home,
                                    contentDescription = item,
                                    modifier = Modifier.size(25.dp)
                                )
                                2 -> Icon(
                                    painter = painterResource(id = R.drawable.profile),
                                    contentDescription = item,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (index) {
                                0 -> navController.navigate("mapScreen")
                                1 -> {}
                                2 -> navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        if (receipts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Чеки отсутствуют")
                    Button(
                        onClick = { showScanner = true },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сканировать чек")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                groupedReceipts.forEach { (date, receiptsForDate) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp, 8.dp)
                        )
                    }

                    items(receiptsForDate, key = { it.id }) { receipt ->
                        ReceiptItem(receipt)
                    }
                }
            }
        }

        if (showDialog) {
            AddReceiptDialog(
                onDismiss = { viewModel.hideDialog() },
                onAddReceipt = { data ->
                    viewModel.addReceipt(data)
                    viewModel.loadReceipts()
                }
            )
        }

        if (showAnalyticsDialog) {
            AnalyticsDialog(
                receipts = receipts,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                onDismiss = { showAnalyticsDialog = false }
            )
        }
    }
}
private fun parseQRContent(qrContent: String): ReceiptData? {
    return try {
        val params = qrContent.split("&")
        val map = params.associate {
            val parts = it.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        }

        ReceiptData(
            t = map["t"] ?: "",
            s = map["s"] ?: "",
            fn = map["fn"] ?: "",
            i = map["i"] ?: "",
            fp = map["fp"] ?: "",
            n = map["n"] ?: "1"
        )
    } catch (_: Exception) {
        null
    }
}

@Composable
@androidx.camera.core.ExperimentalGetImage
private fun QRScannerContent(onResult: (String?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val executor = ContextCompat.getMainExecutor(ctx)
            val barcodeScanner = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            val image = imageProxy.image
                            if (image != null) {
                                val inputImage = InputImage.fromMediaImage(
                                    image,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                BarcodeScanning.getClient(barcodeScanner)
                                    .process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull()?.rawValue?.let { qrContent ->
                                            Handler(Looper.getMainLooper()).post {
                                                onResult(qrContent)
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch(ex: Exception) {
                    Log.e("QRCodeScanner", "Use case binding failed", ex)
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { onResult(null) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close scanner",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.e("QRCodeScanner", "Error cleaning up camera", e)
            }
        }
    }
}

@Composable
@androidx.camera.core.ExperimentalGetImage
private fun QRCodeScanner(onResult: (String?) -> Unit) {
    RequestCameraPermission {
        QRScannerContent(onResult)
    }
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(onPermissionGranted: @Composable () -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState) {
        cameraPermissionState.launchPermissionRequest()
    }

    when {
        cameraPermissionState.status.isGranted -> {
            onPermissionGranted()
        }
        cameraPermissionState.status.shouldShowRationale -> {
            Text("Разрешение на камеру необходимо для сканирования QR-кода.")
        }
        else -> {
            Text("Разрешение отклонено. Включите его в настройках.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDialog(
    receipts: List<Receipt>,
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit,
    onDismiss: () -> Unit
) {
    val spendingData = calculateSpending(receipts, selectedPeriod)
    val total = spendingData.values.sum()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Аналитика расходов") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Period.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onPeriodSelected(period) },
                            label = { Text(period.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BarChart(spendingData = spendingData)

                Text(
                    text = "Всего потрачено: ${"%.2f".format(total)} руб",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun BarChart(spendingData: Map<String, Float>) {
    val maxValue = spendingData.values.maxOrNull() ?: 1f
    val sortedData = spendingData.toList().sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        sortedData.forEach { (store, amount) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = store,
                    modifier = Modifier.width(100.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        .fillMaxWidth(fraction = amount / maxValue)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${"%.2f".format(amount)} руб",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 

fun calculateSpending(receipts: List<Receipt>, period: Period): Map<String, Float> {
    println("Calculating spending for ${receipts.size} receipts")
    receipts.forEach { receipt ->
        println("Receipt from ${receipt.storeName}: sum=${receipt.totalSum}, parsed=${receipt.totalSum?.parseLocalizedNumber()}")
    }
    val now = LocalDate.now()
    val filteredReceipts = when (period) {
        Period.WEEK -> receipts.filter {
            parseReceiptDate(it.dateTime)?.isAfter(now.minusWeeks(1)) ?: false
        }
        Period.MONTH -> receipts.filter {
            parseReceiptDate(it.dateTime)?.isAfter(now.minusMonths(1)) ?: false
        }
        Period.YEAR -> receipts.filter {
            parseReceiptDate(it.dateTime)?.isAfter(now.minusYears(1)) ?: false
        }
        Period.ALL -> receipts
    }

    return filteredReceipts.groupBy { it.storeName }
        .mapValues { (_, receipts) ->
            receipts.fold(0f) { acc, receipt ->
                acc + (receipt.totalSum?.parseLocalizedNumber() ?: 0f)
            }
        }
}

fun String?.parseLocalizedNumber(): Float {
    if (this == null) return 0f
    return try {

        replace(" ", "")
            .replace(" ", "")
            .replace(",", ".")
            .toFloat()
    } catch (_: NumberFormatException) {
        0f
    }
}

fun parseReceiptDate(dateString: String?): LocalDate? {
    return try {
        dateString?.let {
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
                .parse(it, LocalDate::from)
        }
    } catch (_: Exception) {
        null
    }
}

enum class Period(val displayName: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год"),
    ALL("Все время")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptItem(receipt: Receipt) {
    var showDetails by remember { mutableStateOf(false) }
    val logoResId = remember(receipt.storeName) {
        getStoreLogoResource(receipt.storeName)
    }
    val logoPainter = painterResource(id = logoResId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { showDetails = true }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = logoPainter,
                contentDescription = "Store logo",
                modifier = Modifier
                    .size(70.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.storeName,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = receipt.dateTime,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "${receipt.totalSum} ₽",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = logoPainter,
                        contentDescription = "Store logo",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = receipt.storeName,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text("Дата: ${receipt.dateTime}")
                    Text("Сумма: ${receipt.totalSum} руб")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Товары:", fontWeight = FontWeight.Bold)
                    LazyColumn {
                        items(receipt.items ?: emptyList()) { item ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = item.name, fontWeight = FontWeight.Medium)
                                Text(text = "${item.price} руб x ${item.quantity}")
                                Divider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDetails = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

enum class FilterType {
    DATE, STORE, AMOUNT
}

fun getStoreLogoResource(storeName: String?): Int {
    return when (storeName?.lowercase()) {
        "магнит" -> R.drawable.magnit_logo
        "eurospar" -> R.drawable.sparlogo
        "пятерочка" -> R.drawable.pyaterochka_logo
        else -> R.drawable.default_logo
    }
}

@Composable
fun AddReceiptDialog(
    onDismiss: () -> Unit,
    onAddReceipt: (ReceiptData) -> Unit
) {
    var t by remember { mutableStateOf("") }
    var s by remember { mutableStateOf("") }
    var fn by remember { mutableStateOf("") }
    var i by remember { mutableStateOf("") }
    var fp by remember { mutableStateOf("") }
    var n by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить чек вручную") },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("Дата и время (t)") }
                )
                OutlinedTextField(
                    value = s,
                    onValueChange = { s = it },
                    label = { Text("Сумма (s)") }
                )
                OutlinedTextField(
                    value = fn,
                    onValueChange = { fn = it },
                    label = { Text("ФН (fn)") }
                )
                OutlinedTextField(
                    value = i,
                    onValueChange = { i = it },
                    label = { Text("Номер чека (i)") }
                )
                OutlinedTextField(
                    value = fp,
                    onValueChange = { fp = it },
                    label = { Text("ФП (fp)") }
                )
                OutlinedTextField(
                    value = n,
                    onValueChange = { n = it },
                    label = { Text("Система налогообложения (n)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAddReceipt(ReceiptData(t, s, fn, i, fp, n))
                onDismiss()
            }) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}