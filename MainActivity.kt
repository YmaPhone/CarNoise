package com.ymaphone.carnoise

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val NavyTop = Color(0xFF0B1B3F)
private val NavyBottom = Color(0xFF050B1A)
private val CardBg = Color(0xFF111A2E)
private val Blue = Color(0xFF1F4BFF)
private val Green = Color(0xFF3DDC84)
private val Dim = Color(0xFF9AA6C2)

class MainActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Blue, surface = CardBg)) { App(tick) }
        }
    }
    override fun onResume() { super.onResume(); tick++ }
}

@Composable
fun App(tick: Int) {
    val ctx = LocalContext.current
    val p = remember { Prefs(ctx) }
    var tab by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NavyTop, NavyBottom)))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = CardBg) {
                    listOf(
                        Triple("בית", Icons.Default.Home, 0),
                        Triple("רכב", Icons.Default.DirectionsCar, 1),
                        Triple("פרופיל", Icons.Default.Tune, 2),
                    ).forEach { (label, icon, i) ->
                        NavigationBarItem(
                            selected = tab == i, onClick = { tab = i },
                            icon = { Icon(icon, null) }, label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White, indicatorColor = Blue,
                                unselectedIconColor = Dim, selectedTextColor = Color.White, unselectedTextColor = Dim
                            )
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).statusBarsPadding()) {
                key(tick) {
                    when (tab) { 0 -> HomeScreen(p); 1 -> CarScreen(p); else -> ProfileScreen(p) }
                }
            }
        }
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardBg).padding(16.dp),
        content = content
    )
}

@Composable
fun HomeScreen(p: Prefs) {
    val ctx = LocalContext.current
    var active by remember { mutableStateOf(p.inCar) }
    val access = CarMode.hasAccess(ctx)
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CarNoise", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(p.displayName, color = Dim, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.size(210.dp).clip(CircleShape)
                .background(if (active) Blue.copy(alpha = .25f) else Color(0xFF1B2236))
                .clickable {
                    if (!access) {
                        ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    } else {
                        if (active) CarMode.exit(ctx) else CarMode.enter(ctx)
                        active = p.inCar
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(160.dp).clip(CircleShape).background(Color(0xFF222B42)), Alignment.Center) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = if (active) Green else Color.White, modifier = Modifier.size(72.dp))
            }
        }
        Text(
            if (active) "מצב רכב פעיל" else "לחץ להפעלה ידנית",
            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold
        )
        if (!access) {
            Card {
                Text("נדרשת הרשאת 'נא לא להפריע'", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("בלי ההרשאה האפליקציה לא יכולה לשנות את הפרופיל.", color = Dim)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }) { Text("מתן הרשאה") }
            }
        }
        Card {
            Text("מה מותר כשמחוברים לרכב", color = Dim, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            FlowRowChips(
                listOf(
                    "מוזיקה" to p.allowMedia, "שיחות" to p.allowCalls,
                    "הודעות" to p.allowMessages, "התראות שעון" to p.allowAlarms
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowChips(items: List<Pair<String, Boolean>>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (t, on) ->
            AssistChip(
                onClick = {}, label = { Text(t) },
                leadingIcon = { Icon(if (on) Icons.Default.Check else Icons.Default.Close, null, Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (on) Green else Dim, leadingIconContentColor = if (on) Green else Dim
                ),
                border = BorderStroke(1.dp, if (on) Green else Dim)
            )
        }
    }
}

private fun hasBt(ctx: Context) =
    Build.VERSION.SDK_INT < 31 ||
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun bonded(ctx: Context) = try {
    ctx.getSystemService(BluetoothManager::class.java)?.adapter?.bondedDevices?.toList() ?: emptyList()
} catch (e: SecurityException) { emptyList() }

@SuppressLint("MissingPermission")
@Composable
fun CarScreen(p: Prefs) {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(hasBt(ctx)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var selected by remember { mutableStateOf(p.carAddress) }
    var nick by remember { mutableStateOf(p.nickname) }
    var model by remember { mutableStateOf(p.model) }
    var year by remember { mutableStateOf(p.year) }
    var plate by remember { mutableStateOf(p.plate) }
    val devices = if (granted) bonded(ctx) else emptyList()

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("הרכב שלי", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, null, tint = Blue, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(p.displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(listOf(model, year).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "הוסף פרטי רכב" }, color = Dim)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Field("כינוי", nick) { nick = it; p.nickname = it }
                Field("דגם", model) { model = it; p.model = it }
                Field("שנה", year) { year = it; p.year = it }
                Field("מספר רכב", plate) { plate = it; p.plate = it }
            }
        }
        item { Text("בחירת בלוטוס של הרכב", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
        if (!granted) {
            item {
                Card {
                    Text("נדרשת הרשאה לראות מכשירי בלוטוס מצומדים", color = Dim)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.BLUETOOTH_CONNECT) }) { Text("מתן הרשאה") }
                }
            }
        } else if (devices.isEmpty()) {
            item { Text("לא נמצאו מכשירים מצומדים. צמד את הרכב בהגדרות הבלוטוס.", color = Dim) }
        }
        items(devices) { d ->
            val isSel = selected == d.address
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(if (isSel) Blue.copy(alpha = .2f) else CardBg).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bluetooth, null, tint = if (isSel) Green else Dim)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(d.name ?: "ללא שם", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(d.address, color = Dim, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        if (isSel) { p.carAddress = null; p.btName = ""; selected = null }
                        else { p.carAddress = d.address; p.btName = d.name ?: ""; selected = d.address }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) Color(0xFFC62840) else Color(0xFF222B42))
                ) { Text(if (isSel) "הסר" else "בחר") }
            }
        }
        item { Text("Android Auto וניהול", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
        item {
            Card {
                Text("אנדרואיד לא מאפשרת לאפליקציה אחרת לשנות הגדרות של Android Auto, אז הנה קיצורים ישירים:", color = Dim, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                LinkButton("פתח את Android Auto") {
                    val i = ctx.packageManager.getLaunchIntentForPackage("com.google.android.projection.gearhead")
                    ctx.startActivity(i ?: Intent(Settings.ACTION_SETTINGS))
                }
                LinkButton("הגדרות בלוטוס") { ctx.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
                LinkButton("הגדרות מכשירים מחוברים") { ctx.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LinkButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun SwitchRow(icon: ImageVector, title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Blue)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(sub, color = Dim, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun ProfileScreen(p: Prefs) {
    var auto by remember { mutableStateOf(p.autoEnable) }
    var media by remember { mutableStateOf(p.allowMedia) }
    var calls by remember { mutableStateOf(p.allowCalls) }
    var msgs by remember { mutableStateOf(p.allowMessages) }
    var alarms by remember { mutableStateOf(p.allowAlarms) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("פרופיל רכב", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("כשהטלפון מתחבר לבלוטוס של הרכב שבחרת, הוא עובר ל'נא לא להפריע' עם החריגים האלה. בניתוק, המצב הקודם חוזר.", color = Dim, fontSize = 13.sp)
        Card {
            SwitchRow(Icons.Default.Bluetooth, "הפעלה אוטומטית", "בהתחברות לרכב בלבד", auto) { auto = it; p.autoEnable = it }
            SwitchRow(Icons.Default.MusicNote, "מוזיקה ומדיה", "ימשיכו להישמע", media) { media = it; p.allowMedia = it }
            SwitchRow(Icons.Default.Call, "שיחות", "יצלצלו (כבוי = שקט)", calls) { calls = it; p.allowCalls = it }
            SwitchRow(Icons.Default.Sms, "הודעות", "יצלצלו", msgs) { msgs = it; p.allowMessages = it }
            SwitchRow(Icons.Default.Alarm, "התראות שעון", "שעון מעורר וטיימרים", alarms) { alarms = it; p.allowAlarms = it }
        }
    }
}
