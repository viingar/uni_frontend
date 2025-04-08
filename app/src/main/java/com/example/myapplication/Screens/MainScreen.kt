package com.example.myapplication.Screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.myapplication.ViewModels.RegisterViewModel
import com.example.myapplication.api.Receipt
import com.example.myapplication.api.ReceiptData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current)
    )
) {
    val showDialog by viewModel.showDialog.collectAsState()
    val receipts by remember { derivedStateOf { viewModel.receipts } }
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf<FilterType?>(null) }
    var selectedStore by remember { mutableStateOf<String?>(null) }
    var showStoreSubmenu by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(Period.WEEK) }

    // Для навигации
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Карта", "Главная", "Профиль")

    // Получаем уникальные названия магазинов
    val storeNames by remember(receipts) {
        derivedStateOf {
            receipts.map { it.storeName ?: "Неизвестный магазин" }.distinct().sorted()
        }
    }

    // Фильтрованные чеки
    val filteredReceipts by remember(receipts, filterType, selectedStore) {
        derivedStateOf {
            when (filterType) {
                FilterType.DATE -> {
                    receipts.sortedByDescending { receipt ->
                        try {
                            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
                                .parse(receipt.dateTime ?: "", LocalDate::from)
                        } catch (e: Exception) {
                            LocalDate.MIN
                        }
                    }
                }
                FilterType.STORE -> {
                    if (selectedStore != null) {
                        receipts.filter { it.storeName == selectedStore }
                    } else {
                        receipts.sortedBy { it.storeName }
                    }
                }
                FilterType.AMOUNT -> receipts.sortedByDescending {
                    it.totalSum?.toDoubleOrNull() ?: 0.0
                }
                null -> receipts
            }
        }
    }

    // Группируем чеки по датам
    val groupedReceipts by remember(filteredReceipts) {
        derivedStateOf {
            filteredReceipts.groupBy { receipt ->
                receipt.dateTime ?: "Без даты"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои чеки") },
                actions = {
                    // Кнопка аналитики
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

                    // Подменю выбора магазина
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
            FloatingActionButton(onClick = { viewModel.showAddManualDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add manually")
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(
                                    painter = painterResource(id = R.drawable.map),
                                    contentDescription = item,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(end = 8.dp),
                                )
                                1 -> Icon(
                                    Icons.Default.Home,
                                    contentDescription = item,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(end = 8.dp),
                                )
                                2 -> Icon(
                                    painter = painterResource(id = R.drawable.profile),
                                    contentDescription = item,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(end = 8.dp),
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
                Text("No receipts found")
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

        // Диалог аналитики расходов
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
                // Выбор периода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Period.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onPeriodSelected(period) },
                            label = { Text(period.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // График
                BarChart(spendingData = spendingData)

                // Итоговая сумма
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

    return filteredReceipts.groupBy { it.storeName ?: "Другое" }
        .mapValues { (_, receipts) ->
            receipts.fold(0f) { acc, receipt ->
                acc + (receipt.totalSum?.parseLocalizedNumber() ?: 0f)
            }
        }
}

// Helper function to parse numbers with comma decimal separator
fun String?.parseLocalizedNumber(): Float {
    if (this == null) return 0f
    return try {
        // Replace comma with dot and parse as float
        replace(",", ".").toFloat()
    } catch (e: NumberFormatException) {
        0f
    }
}

fun parseReceiptDate(dateString: String?): LocalDate? {
    return try {
        dateString?.let {
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
                .parse(it, LocalDate::from)
        }
    } catch (e: Exception) {
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
                    text = receipt.storeName ?: "Неизвестный магазин",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = receipt.dateTime ?: "",
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
                        text = receipt.storeName ?: "Неизвестный магазин",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text("Дата: ${receipt.dateTime ?: "не указана"}")
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

// Функция для получения ID ресурса по названию магазина
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