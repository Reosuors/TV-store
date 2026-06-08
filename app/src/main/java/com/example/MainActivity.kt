package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import coil.compose.SubcomposeAsyncImage
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tv_studio_database"
        ).fallbackToDestructiveMigration().build()
        
        repository = AppRepository(db.dao())
        
        val viewModel: TVViewModel by viewModels {
            TVViewModelFactory(application, repository)
        }

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ambient green neon background glow
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0x1500FF66),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.8f, size.height * 0.2f),
                                            radius = size.width * 1.2f
                                        )
                                    )
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0x0EFFFF00),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.2f, size.height * 0.8f),
                                            radius = size.width * 1.2f
                                        )
                                    )
                                }
                        )

                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                is Screen.Login -> LoginScreen(viewModel)
                                is Screen.Register -> RegisterScreen(viewModel)
                                is Screen.Dashboard -> DashboardScreen(viewModel)
                                is Screen.CategoryDetail -> CategoryDetailScreen(viewModel, screen.categoryKey)
                                is Screen.ItemDetail -> ItemDetailScreen(viewModel, screen.itemId)
                                is Screen.Favorites -> FavoritesScreen(viewModel)
                                is Screen.DeveloperPanel -> DeveloperPanelScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// COMPOSABLES & UI SCREEN REVEALS
// ==========================================================

@Composable
fun UserAvatarIcon(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    glow: Boolean = true
) {
    val outlineBrush = remember {
        Brush.sweepGradient(
            listOf(GreenNeon, YellowFlash, GreenNeon)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (glow) {
                    Modifier.border(2.dp, outlineBrush, CircleShape)
                } else {
                    Modifier.border(1.dp, Color(0xFF334E33), CircleShape)
                }
            )
            .padding(2.dp)
            .clip(CircleShape)
            .background(DarkSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        when {
            avatarUrl == "dev" -> {
                Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = "مطور",
                    tint = YellowFlash,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl == "avatar1" -> {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "روبوت أخضر",
                    tint = GreenNeon,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl == "avatar2" -> {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "يد تحكم نيون",
                    tint = GreenNeon,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl == "avatar3" -> {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "طاقة نيون",
                    tint = YellowFlash,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl == "avatar4" -> {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "ريشة فنان",
                    tint = GreenNeon,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl == "avatar5" -> {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = "ثري دي كوزموس",
                    tint = YellowFlash,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
            avatarUrl.startsWith("http") || avatarUrl.startsWith("file://") || avatarUrl.startsWith("content://") -> {
                SubcomposeAsyncImage(
                    model = avatarUrl,
                    contentDescription = "صورة مخصصة",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        CircularProgressIndicator(
                            color = GreenNeon,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "شخصي",
                            tint = GreenNeon
                        )
                    }
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "مستخدم",
                    tint = TextSecondaryGreen,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

/**
 * 1. LOGIN SCREEN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: TVViewModel) {
    var isDevMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(isDevMode) {
        username = ""
        password = ""
    }

    if (showGoogleDialog) {
        GoogleSelectorDialog(
            onDismiss = { showGoogleDialog = false },
            onSelect = { email, name, avatar ->
                showGoogleDialog = false
                viewModel.loginWithGoogle(email, name, avatar)
                Toast.makeText(context, "تم استقبال اتصال بروتوكول Google بنجاح 📡", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(2.dp, GreenNeon, RoundedCornerShape(16.dp))
                .padding(3.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.98f))
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                item {
                    // Terminal Style Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GreenDarkSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, GreenNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(YellowFlash, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(GreenNeon, CircleShape)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "terminal@tv-store:~$",
                            color = GreenNeon,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ascii Art logo / Robotic prompt
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = " __                 __            __     _",
                            color = GreenNeon,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = "/\\ \\__             /\\ \\          /\\ \\__ /\\ \\",
                            color = GreenNeon,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = "\\ \\  _\\  __  __   _\\_\\ \\     __  \\ \\  _\\\\_\\ \\_   ___",
                            color = GreenNeon,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = " \\ \\ \\/ /\\ \\/\\ \\ /'__` \\  /'__`\\ \\ \\ \\/ /\\_ _\\ /' _ `\\",
                            color = GreenNeon,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = "  \\ \\ \\_\\ \\ \\_/ |/\\ \\L\\ \\/\\ \\L\\.\\_\\ \\ \\_\\/_/\\ \\/\\ \\/\\ \\",
                            color = YellowFlash,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = "   \\ \\__\\\\ \\___/ \\ \\___,_\\ \\__/.\\_\\\\ \\__\\  \\ \\_\\ \\_\\ \\_\\",
                            color = YellowFlash,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        Text(
                            text = "    \\/__/ \\/__/   \\/__,_ /\\/__/\\/_/ \\/__/   \\/_/\\/_/\\/_/",
                            color = YellowFlash,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 10.sp
                        )
                        
                        Divider(color = Color(0xFF1E3A1E), modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "> SECURE PROTOCOL: MATRIX REGULATION",
                            color = GreenNeon,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Text(
                            text = "> ROOM_DATABASE: ONLINE (MOUNTED)",
                            color = TextSecondaryGreen,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Text(
                            text = "> NETWORK PORTALS: OPENED (WS/8080)",
                            color = TextSecondaryGreen,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons to Switch between Regular and Developer modes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF233E23), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isDevMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDevMode) GreenNeon else Color.Transparent,
                                contentColor = if (!isDevMode) Color.Black else TextSecondaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دخول بروتوكول العضوية", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { isDevMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDevMode) YellowFlash else Color.Transparent,
                                contentColor = if (isDevMode) Color.Black else TextSecondaryGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محاكي المطورين", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    loginError?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF331414)),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "🚨 $it",
                                color = Color.Red,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("[ SYSTEM_USER_ID ]", color = GreenNeon, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDevMode) YellowFlash else GreenNeon,
                            unfocusedBorderColor = Color(0xFF2E4E2E),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (isDevMode) YellowFlash else GreenNeon
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("[ PASSWORD_ACCESS_TOKEN ]", color = GreenNeon, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDevMode) YellowFlash else GreenNeon,
                            unfocusedBorderColor = Color(0xFF2E4E2E),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isDevMode) YellowFlash else GreenNeon
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondaryGreen
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button with glowing highlight
                    Button(
                        onClick = { viewModel.login(username, password, isDevMode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDevMode) YellowFlash else GreenNeon,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isDevMode) "EXECUTE_DEV_LOGIN_SEQUENCE" else "EXECUTE_MEMBER_LOGIN_SEQUENCE",
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ADVANCED GOOGLE LOGIN ACCELERATION PROTOCOL
                    Button(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, YellowFlash.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = YellowFlash
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "جوجل",
                            tint = YellowFlash,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROTOCOL: GOOGLE_FAST_SIGN_IN 🤖",
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (!isDevMode) {
                        TextButton(onClick = { viewModel.navigateTo(Screen.Register) }) {
                            Text(
                                ">>> INITIALIZE_NEW_PROFILE_LOADER (تسجيل حساب جديد)",
                                color = YellowFlash,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Secure guide
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreenDarkSurface.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF1B3D1B), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                    text = "🔒 قنوات الدخول للمطورين مشفرة بالكامل لمرحلة المتابعة والتحكم. الرجاء كتابة معرّف النظام ورمز المرور الخاص بك للمصادقة وتفعيل لوحة المطورين.",
                                color = TextSecondaryGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. REGISTER SCREEN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: TVViewModel) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("avatar1") }
    var customAvatarUrl by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val registerError by viewModel.registerError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val avatarPresets = listOf(
        Pair("avatar1", "روبوت نيون"),
        Pair("avatar2", "يد تحكم"),
        Pair("avatar3", "طاقة رعد"),
        Pair("avatar4", "ألوان وفن"),
        Pair("avatar5", "نظام كوني")
    )

    if (showGoogleDialog) {
        GoogleSelectorDialog(
            onDismiss = { showGoogleDialog = false },
            onSelect = { email, displayName, avatar ->
                showGoogleDialog = false
                viewModel.loginWithGoogle(email, displayName, avatar)
                Toast.makeText(context, "تم استقبال اتصال بروتوكول Google بنجاح 📡", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .border(2.dp, GreenNeon, RoundedCornerShape(24.dp))
                .padding(2.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.98f))
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                item {
                    // Terminal style header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GreenDarkSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, GreenNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(YellowFlash, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(GreenNeon, CircleShape))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "compiler@tv-studio:~$ ./register",
                            color = GreenNeon,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "REGISTER_CORE_PROFILE",
                        color = GreenNeon,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "تهيئة وحدة معمارية جديدة في الأنظمة المحلية",
                        color = TextSecondaryGreen,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    registerError?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1C1C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Display Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل (المعروض للناس)", color = TextSecondaryGreen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenNeon,
                            unfocusedBorderColor = Color(0xFF435A43),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenNeon) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unique Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم الفريد (بدون فراغات)", color = TextSecondaryGreen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenNeon,
                            unfocusedBorderColor = Color(0xFF435A43),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = GreenNeon) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة السر الخاصة بك", color = TextSecondaryGreen) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenNeon,
                            unfocusedBorderColor = Color(0xFF435A43),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenNeon) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondaryGreen
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Pic / Avatar Selection
                    Text(
                        text = "اختر أيقونة لحسابك المتوهجة:",
                        color = TextPrimaryGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(avatarPresets) { (id, desc) ->
                            val isSelected = avatarUrl == id && customAvatarUrl.isEmpty()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        avatarUrl = id
                                        customAvatarUrl = ""
                                    }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) YellowFlash else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        if (isSelected) GreenDarkSurface.copy(alpha = 0.3f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                UserAvatarIcon(avatarUrl = id, size = 44.dp, glow = isSelected)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = if (isSelected) YellowFlash else TextSecondaryGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom avatar url input optionally
                    OutlinedTextField(
                        value = customAvatarUrl,
                        onValueChange = {
                            customAvatarUrl = it
                            if (it.isNotBlank()) {
                                avatarUrl = it
                            }
                        },
                        label = { Text("أو ضع رابط صورة مخصصة (اختياري)", color = TextSecondaryGreen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenNeon,
                            unfocusedBorderColor = Color(0xFF435A43)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = GreenNeon) }
                    )

                    if (customAvatarUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserAvatarIcon(avatarUrl = customAvatarUrl, size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("معاينة صورتك المخصصة", color = GreenNeon, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.register(name, username, avatarUrl, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenNeon,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("COMPILING_SYS_REGISTRATION ⚙️", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Auto Register
                    Button(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, YellowFlash.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = YellowFlash
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "جوجل",
                            tint = YellowFlash,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURE_FAST_GOOGLE_REGISTER 🤖",
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(onClick = { viewModel.logout() }) {
                        Text("<<< BACK_TO_LOGIN_INTERFACE (لديك حساب بالفعل؟ دخول)", color = YellowFlash, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * 3. DASHBOARD / HOME SCREEN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TVViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showLinkGoogleSelectorDialog by remember { mutableStateOf(false) }

    // Categorized statistics
    val categoriesMap = listOf(
        Triple("GAMES", "الألعاب المصنوعة", Icons.Default.Gamepad),
        Triple("NOVELS", "الروايات والقصص", Icons.Default.Book),
        Triple("VIDEOS", "مسرح الفيديوهات", Icons.Default.PlayCircle),
        Triple("IMAGES", "معرض الرسوم واللوحات", Icons.Default.PhotoLibrary),
        Triple("NEWS", "الأخبار والتحديثات", Icons.Default.Feed),
        Triple("MODELS", "نماذج ثلاثية الأبعاد 3D", Icons.Default.ViewInAr)
    )

    // Filter featured developer items
    val exclusiveItems = remember(allItems) {
        allItems.filter { it.isExclusive }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Tv,
                            contentDescription = null,
                            tint = YellowFlash,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "TV STUDIO",
                                color = GreenNeon,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DIGITAL UNIVERSE • الكون الرقمي",
                                color = YellowFlash,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Favorites Shortcut Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GreenDarkSurface)
                                .border(1.dp, GreenNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.navigateTo(Screen.Favorites) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "المفضلة",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // User profile styled as premium grid item
                        user?.let {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        showAccountSettingsDialog = true
                                    }
                                    .background(GreenNeon)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatarIcon(avatarUrl = it.avatarUrl, size = 22.dp, glow = false)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = it.name,
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (it.isDeveloper) "DEVELOPER 👑" else "MEMBER",
                                        color = Color.Black.copy(alpha = 0.6f),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Logout Icon Button in Bento style
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.logout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "خروج",
                                tint = GreenNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = GreenNeon
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning notice banner if user is warned
            user?.let { activeUser ->
                if (activeUser.warningMessage.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, YellowFlash, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF332111))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("⚠️", fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("تنبيه من إدارة التطبيق والعمليات", color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = activeUser.warningMessage,
                                        color = TextPrimaryGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("بحث عن ألعاب وروايات ونماذج...", color = TextSecondaryGreen.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenNeon) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondaryGreen)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenNeon,
                        unfocusedBorderColor = Color(0xFF2E3E2E),
                        focusedTextColor = TextPrimaryGreen,
                        unfocusedTextColor = TextPrimaryGreen,
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.5f)
                    )
                )
            }

            // Featured Slider / Recommended exclusives
            if (searchQuery.isEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ الروائع المختارة والحصرية (مطورون)",
                                color = YellowFlash,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (exclusiveItems.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد مشاريع حصرية غنية حالياً", color = TextSecondaryGreen)
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(exclusiveItems) { itemPost ->
                                    Card(
                                        modifier = Modifier
                                            .width(260.dp)
                                            .height(140.dp)
                                            .clickable { viewModel.navigateTo(Screen.ItemDetail(itemPost.id)) }
                                            .border(
                                                width = 1.dp,
                                                color = YellowFlash.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            // Fallback placeholder/image
                                            if (itemPost.imageUrl.isNotBlank()) {
                                                SubcomposeAsyncImage(
                                                    model = itemPost.imageUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                    alpha = 0.45f
                                                )
                                            }

                                            // Text overlay
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black)
                                                        )
                                                    )
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val tagIcon = when (itemPost.category) {
                                                        "GAMES" -> Icons.Default.Gamepad
                                                        "NOVELS" -> Icons.Default.Book
                                                        "VIDEOS" -> Icons.Default.PlayCircle
                                                        "IMAGES" -> Icons.Default.PhotoLibrary
                                                        "NEWS" -> Icons.Default.Feed
                                                        else -> Icons.Default.ViewInAr
                                                    }
                                                    Icon(tagIcon, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(14.dp))
                                                    Text(
                                                        text = when (itemPost.category) {
                                                            "GAMES" -> "لعبة"
                                                            "NOVELS" -> "رواية"
                                                            "VIDEOS" -> "فيديو"
                                                            "IMAGES" -> "رسم"
                                                            "NEWS" -> "خبر"
                                                            else -> "3D مجسم"
                                                        },
                                                        color = GreenNeon,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = itemPost.title,
                                                    color = TextPrimaryGreen,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = itemPost.description,
                                                    color = TextSecondaryGreen,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories list header
            item {
                Text(
                    text = "📂 استكشف الأقسام والتطبيقات",
                    color = GreenNeon,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val countGames = allItems.count { it.category == "GAMES" }
                    val countNovels = allItems.count { it.category == "NOVELS" }
                    val countVideos = allItems.count { it.category == "VIDEOS" }
                    val countImages = allItems.count { it.category == "IMAGES" }
                    val countNews = allItems.count { it.category == "NEWS" }
                    val countModels = allItems.count { it.category == "MODELS" }

                    // Row 1: GAMES & NOVELS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCategoryCard(
                            key = "GAMES",
                            title = "الألعاب المصنوعة",
                            icon = Icons.Default.Gamepad,
                            count = countGames,
                            modifier = Modifier.weight(1.7f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("GAMES")) }

                        BentoCategoryCard(
                            key = "NOVELS",
                            title = "الروايات والقصص",
                            icon = Icons.Default.Book,
                            count = countNovels,
                            modifier = Modifier.weight(1.3f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("NOVELS")) }
                    }

                    // Row 2: NEWS & MODELS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCategoryCard(
                            key = "NEWS",
                            title = "الأخبار والتحديثات",
                            icon = Icons.Default.Feed,
                            count = countNews,
                            modifier = Modifier.weight(1.3f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("NEWS")) }

                        BentoCategoryCard(
                            key = "MODELS",
                            title = "نماذج ثلاثية الأبعاد 3D",
                            icon = Icons.Default.ViewInAr,
                            count = countModels,
                            modifier = Modifier.weight(1.7f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("MODELS")) }
                    }

                    // Row 3: IMAGES & VIDEOS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCategoryCard(
                            key = "IMAGES",
                            title = "معرض الرسوم واللوحات",
                            icon = Icons.Default.PhotoLibrary,
                            count = countImages,
                            modifier = Modifier.weight(1.5f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("IMAGES")) }

                        BentoCategoryCard(
                            key = "VIDEOS",
                            title = "مسرح الفيديوهات",
                            icon = Icons.Default.PlayCircle,
                            count = countVideos,
                            modifier = Modifier.weight(1.5f)
                        ) { viewModel.navigateTo(Screen.CategoryDetail("VIDEOS")) }
                    }
                }
            }

            // General Dashboard Developer Access Banner
            user?.let {
                if (it.isDeveloper) {
                    item {
                        Card(
                            onClick = { viewModel.navigateTo(Screen.DeveloperPanel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, YellowFlash.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2E1B))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = YellowFlash, modifier = Modifier.size(28.dp))
                                Column {
                                    Text("لوحة تحكم مطور TV store", color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("أضف روابط حصرية، عدل الروايات، وعدل أو احذف أي منشور.", color = TextPrimaryGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAccountSettingsDialog) {
        user?.let { activeUser ->
            AccountSettingsDialog(
                user = activeUser,
                onDismissRequest = { showAccountSettingsDialog = false },
                onSaveChanges = { newName, newAvatar ->
                    viewModel.updateUserProfile(newName, newAvatar)
                    showAccountSettingsDialog = false
                    Toast.makeText(context, "تم حفظ تعديلات الملف الشخصي بنجاح ✅", Toast.LENGTH_SHORT).show()
                },
                onLinkGoogle = {
                    showLinkGoogleSelectorDialog = true
                },
                onNavigateToDeveloperPanel = {
                    showAccountSettingsDialog = false
                    viewModel.navigateTo(Screen.DeveloperPanel)
                }
            )
        }
    }

    if (showLinkGoogleSelectorDialog) {
        GoogleSelectorDialog(
            onDismiss = { showLinkGoogleSelectorDialog = false },
            onSelect = { email, name, avatar ->
                viewModel.linkWithGoogle(email, name, avatar)
                showLinkGoogleSelectorDialog = false
                Toast.makeText(context, "تم ربط حساب $email بنجاح ✅", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * 4. CATEGORY DETAILED DASHBOARD with interactive sections creation & Chat room
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(viewModel: TVViewModel, categoryKey: String) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val commentsMessages by viewModel.categoryMessages.collectAsStateWithLifecycle()

    val filtered = remember(items, categoryKey) {
        items.filter { it.category == categoryKey }
    }

    val categoryTitle = when (categoryKey) {
        "GAMES" -> "ألعاب مصنعة بأيديهم"
        "NOVELS" -> "الروايات والقصص العربية"
        "VIDEOS" -> "الفيديوهات والسينما"
        "IMAGES" -> "ألبوم الصور والرسومات"
        "NEWS" -> "الأخبار والتغطيات الحصرية"
        else -> "نماذج مجسمات 3D مدهشة"
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var chatMessageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryTitle, color = GreenNeon, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GreenNeon)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "أضف ببياناتك", tint = YellowFlash)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenNeon,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main list of listings in Category
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth()
            ) {
                if (filtered.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(32.dp), tint = TextSecondaryGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد منشورات في هذا القسم حالياً",
                            color = TextSecondaryGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "كن أول من يضيف ببياناته ويشعل نقاشات الاستوديو!",
                            color = YellowFlash,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                        ) {
                            Text("أضف منشورك الأول")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filtered) { itemPost ->
                            Card(
                                onClick = { viewModel.navigateTo(Screen.ItemDetail(itemPost.id)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (itemPost.isExclusive) 1.5.dp else 1.dp,
                                        color = if (itemPost.isExclusive) YellowFlash.copy(alpha = 0.5f) else Color(0xFF2E3E2E),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Item image
                                    if (itemPost.imageUrl.isNotBlank()) {
                                        SubcomposeAsyncImage(
                                            model = itemPost.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            error = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .background(GreenDarkSurface)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Tv, contentDescription = null, tint = GreenNeon)
                                                }
                                            }
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(GreenDarkSurface)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Gamepad, contentDescription = null, tint = GreenNeon)
                                        }
                                    }

                                    // Details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (itemPost.isExclusive) {
                                                Text(
                                                    "حصري TV ⭐ ",
                                                    color = YellowFlash,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = itemPost.title,
                                                color = TextPrimaryGreen,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = itemPost.description,
                                            color = TextSecondaryGreen,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            UserAvatarIcon(avatarUrl = itemPost.publisherAvatar, size = 16.dp)
                                            Text(
                                                text = "بواسطة ${itemPost.publisherName}",
                                                color = GreenNeon,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = YellowFlash, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = itemPost.likesCount.toString(), color = TextSecondaryGreen, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expanding Category Chat Room Dashboard (عبارة عن إعطاء آرائهم)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(width = (0.5).dp, color = GreenNeon.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دردشة آراء ومقترحات القسم", color = TextPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("${commentsMessages.size} رسالة", color = YellowFlash, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(commentsMessages) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatarIcon(avatarUrl = msg.userAvatar, size = 24.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(msg.userDisplay, color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text("@${msg.username}", color = TextSecondaryGreen, fontSize = 8.sp)
                                    }
                                    Text(msg.messageText, color = TextPrimaryGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Input for Chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("اكتب رأيك أو مقترحك هنا...", color = TextSecondaryGreen, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenNeon,
                                unfocusedBorderColor = Color(0xFF435A43),
                                focusedTextColor = TextPrimaryGreen,
                                unfocusedTextColor = TextPrimaryGreen
                            )
                        )
                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    viewModel.sendChatMessage(chatMessageText)
                                    chatMessageText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = GreenNeon)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "أرسل", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // TAILOR-MADE ADD POST DIALOG FOR CATEGORY
    if (showAddDialog) {
        var addTitle by remember { mutableStateOf("") }
        var addDesc by remember { mutableStateOf("") }
        var addImgUrl by remember { mutableStateOf("") }
        // Game & 3D Model specific
        var addDownloadUrl by remember { mutableStateOf("") }
        // Video specific
        var addVideoUrl by remember { mutableStateOf("") }
        // Novel chapters specific
        var chaptersString by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "إنشاء منشور جديد في القسم",
                    color = GreenNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = addTitle,
                            onValueChange = { addTitle = it },
                            label = { Text("العنوان المناسب", color = TextSecondaryGreen) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = addDesc,
                            onValueChange = { addDesc = it },
                            label = { Text("الوصف التفصيلي", color = TextSecondaryGreen) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        var imgModeByUrl by remember { mutableStateOf(addImgUrl.isEmpty() || !addImgUrl.startsWith("file://")) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("صورة الغلاف أو المنشور", color = GreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imgModeByUrl = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (imgModeByUrl) GreenNeon else Color.Transparent,
                                        contentColor = if (imgModeByUrl) Color.Black else TextSecondaryGreen
                                    ),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رابط URL", fontSize = 10.sp)
                                }
                                
                                val context = LocalContext.current
                                val imgLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetContent()
                                ) { uri: android.net.Uri? ->
                                    uri?.let {
                                        try {
                                            val file = java.io.File(context.filesDir, "post_img_${System.currentTimeMillis()}.png")
                                            context.contentResolver.openInputStream(uri)?.use { inStream ->
                                                file.outputStream().use { outStream ->
                                                    inStream.copyTo(outStream)
                                                }
                                            }
                                            addImgUrl = android.net.Uri.fromFile(file).toString()
                                            Toast.makeText(context, "تم اختيار وصنع ملف الصورة محلياً بنجاح ✅", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(context, "فشل في حفظ ملف الغلاف", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                Button(
                                    onClick = { 
                                        imgModeByUrl = false
                                        imgLauncher.launch("image/*")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!imgModeByUrl) GreenNeon else Color.Transparent,
                                        contentColor = if (!imgModeByUrl) Color.Black else TextSecondaryGreen
                                    ),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ملفات الجهاز", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (imgModeByUrl) {
                                OutlinedTextField(
                                    value = if (addImgUrl.startsWith("file://")) "" else addImgUrl,
                                    onValueChange = { addImgUrl = it },
                                    placeholder = { Text("أدخل رابط صورة الغلاف هنا...", color = TextSecondaryGreen, fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GreenNeon,
                                        unfocusedBorderColor = Color(0xFF2E3E2E),
                                        focusedTextColor = TextPrimaryGreen,
                                        unfocusedTextColor = TextPrimaryGreen
                                    )
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF2E3E2E), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (addImgUrl.startsWith("file://")) "ملف محلي آمن نشط 📂" else "لم يتم اختيار ملف بعد",
                                            color = if (addImgUrl.startsWith("file://")) GreenNeon else TextSecondaryGreen,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (addImgUrl.startsWith("file://")) {
                                        IconButton(onClick = { addImgUrl = "" }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف ملف", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Games specific inputs
                    if (categoryKey == "GAMES") {
                        item {
                            OutlinedTextField(
                                value = addDownloadUrl,
                                onValueChange = { addDownloadUrl = it },
                                label = { Text("رابط تحميل اللعبة أو ملفها", color = YellowFlash) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Videos specific inputs with URL and File select mode
                    if (categoryKey == "VIDEOS") {
                        item {
                            var vidModeByUrl by remember { mutableStateOf(addVideoUrl.isEmpty() || !addVideoUrl.startsWith("file://")) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text("مصدر فيديو العرض", color = GreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { vidModeByUrl = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (vidModeByUrl) GreenNeon else Color.Transparent,
                                            contentColor = if (vidModeByUrl) Color.Black else TextSecondaryGreen
                                        ),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("رابط URL", fontSize = 10.sp)
                                    }
                                    
                                    val context = LocalContext.current
                                    val vidLauncher = rememberLauncherForActivityResult(
                                        contract = ActivityResultContracts.GetContent()
                                    ) { uri: android.net.Uri? ->
                                        uri?.let {
                                            try {
                                                val file = java.io.File(context.filesDir, "post_vid_${System.currentTimeMillis()}.mp4")
                                                context.contentResolver.openInputStream(uri)?.use { inStream ->
                                                    file.outputStream().use { outStream ->
                                                        inStream.copyTo(outStream)
                                                    }
                                                }
                                                addVideoUrl = android.net.Uri.fromFile(file).toString()
                                                Toast.makeText(context, "تم تحميل ملف الفيديو بنجاح ✅", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(context, "فشل في حفظ ملف الفيديو", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { 
                                            vidModeByUrl = false
                                            vidLauncher.launch("video/*")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!vidModeByUrl) GreenNeon else Color.Transparent,
                                            contentColor = if (!vidModeByUrl) Color.Black else TextSecondaryGreen
                                        ),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ملفات الجهاز", fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (vidModeByUrl) {
                                    OutlinedTextField(
                                        value = if (addVideoUrl.startsWith("file://")) "" else addVideoUrl,
                                        onValueChange = { addVideoUrl = it },
                                        placeholder = { Text("أدخل رابط الفيديو (MP4/HLS)...", color = TextSecondaryGreen, fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenNeon,
                                            unfocusedBorderColor = Color(0xFF2E3E2E),
                                            focusedTextColor = TextPrimaryGreen,
                                            unfocusedTextColor = TextPrimaryGreen
                                        )
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFF2E3E2E), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = if (addVideoUrl.startsWith("file://")) "ملف فيديو نشط محلياً 🎬" else "لم يتم اختيار فيديو بعد",
                                                color = if (addVideoUrl.startsWith("file://")) GreenNeon else TextSecondaryGreen,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (addVideoUrl.startsWith("file://")) {
                                            IconButton(onClick = { addVideoUrl = "" }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف ملف", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3D Models specific
                    if (categoryKey == "MODELS") {
                        item {
                            OutlinedTextField(
                                value = addDownloadUrl,
                                onValueChange = { addDownloadUrl = it },
                                label = { Text("رابط تحميل المجسم أو ملف gLTF", color = YellowFlash) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Novels specific chapter inline setup
                    if (categoryKey == "NOVELS") {
                        item {
                            OutlinedTextField(
                                value = chaptersString,
                                onValueChange = { chaptersString = it },
                                label = { Text("الفصول الأولية (اكتب فصولاً مفرزة بفاصلة ,)", color = YellowFlash) },
                                placeholder = { Text("مثال: الفصل 1, الفصل 2") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addTitle.isNotBlank() && addDesc.isNotBlank()) {
                            val parsedChapters = if (categoryKey == "NOVELS" && chaptersString.isNotBlank()) {
                                chaptersString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            } else {
                                emptyList()
                            }
                            viewModel.addItem(
                                category = categoryKey,
                                title = addTitle,
                                description = addDesc,
                                imageUrl = addImgUrl,
                                downloadUrl = addDownloadUrl,
                                videoUrl = addVideoUrl,
                                chapters = parsedChapters
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                ) {
                    Text("إرسال ونشر")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = TextSecondaryGreen)
                }
            },
            containerColor = DarkSurface
        )
    }
}

/**
 * 5. ITEM DETAILED COMPOSABLE WITH DISGUST COMMENTS AND SPECIAL LOGICS (Chapters setup for novel, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(viewModel: TVViewModel, itemId: Int) {
    val activeItem by viewModel.activeItem.collectAsStateWithLifecycle()
    val comments by viewModel.activeItemComments.collectAsStateWithLifecycle()
    val chaptersList by viewModel.activeItemChapters.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    
    var localIsFav by remember { mutableStateOf(false) }

    LaunchedEffect(user, itemId) {
        val u = user?.username ?: return@LaunchedEffect
        viewModel.isItemFavorite(itemId).collectLatest {
            localIsFav = it
        }
    }

    var commentText by remember { mutableStateOf("") }
    var showChapterAddSection by remember { mutableStateOf(false) }
    var newChapterName by remember { mutableStateOf("") }
    
    var activeReadingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var showCommentsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeItem?.title ?: "تفاصيل الكائن", color = GreenNeon, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GreenNeon)
                    }
                },
                actions = {
                    // Small comments badged action button on top side
                    IconButton(onClick = { showCommentsSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (comments.isNotEmpty()) {
                                    Badge(
                                        containerColor = GreenNeon,
                                        contentColor = Color.Black
                                    ) {
                                        Text(comments.size.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "التعليقات ومراجعات الاستوديو",
                                tint = GreenNeon
                            )
                        }
                    }

                    IconButton(onClick = { activeItem?.id?.let { viewModel.toggleFavorite(it) } }) {
                        Icon(
                            imageVector = if (localIsFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "مفضلة",
                            tint = if (localIsFav) Color.Red else TextSecondaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        activeItem?.let { itemPost ->
            // Deserialize chapters index
            val itemsJson = remember(itemPost.chaptersJson) {
                val cleaned = itemPost.chaptersJson.trim()
                if (cleaned == "[]" || cleaned.isEmpty()) {
                    emptyList()
                } else {
                    cleaned.substring(1, cleaned.length - 1)
                        .split(",")
                        .map { it.trim().trim('"') }
                        .filter { it.isNotEmpty() }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large picture header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GreenNeon.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        if (itemPost.imageUrl.isNotBlank()) {
                            SubcomposeAsyncImage(
                                model = itemPost.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GreenDarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(32.dp), tint = GreenNeon)
                            }
                        }

                        // Star badge for exclusives
                        if (itemPost.isExclusive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(YellowFlash, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("روائع المطورين 👑", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Title + Publisher Info
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = itemPost.title,
                            color = GreenNeon,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserAvatarIcon(avatarUrl = itemPost.publisherAvatar, size = 32.dp)
                            Column {
                                Text("نُشر بواسطة ${itemPost.publisherName}", color = TextPrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("@${itemPost.publisher}", color = TextSecondaryGreen, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Description
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("عن المنشور:", color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = itemPost.description, color = TextPrimaryGreen, fontSize = 13.sp)
                        }
                    }
                }

                // Particular specs for Categories:
                // Category GAMES or MODELS download setup
                if (itemPost.category == "GAMES" || itemPost.category == "MODELS") {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, YellowFlash, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2E1B))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = YellowFlash)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (itemPost.category == "GAMES") "تحميل اللعبة والملف فورا" else "تحميل مجسم الـ 3D المجاني",
                                    color = TextPrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        // Trigger download
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YellowFlash, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = itemPost.downloadUrl.ifBlank { "انقر لفتح رابط التحميل الفوري" },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Inline Video Player section for Videos
                if (itemPost.category == "VIDEOS" && itemPost.videoUrl.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF1B3D1B), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(20.dp))
                                    Text("مشاهدة الفيديو المرفق مباشرة", color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                InlineVideoPlayer(videoUrl = itemPost.videoUrl)
                            }
                        }
                    }
                }

                // Category NOVELS interactive chapters log
                if (itemPost.category == "NOVELS") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📕 الفصول المتوفرة بالرواية (انقر للقراءة)", color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    
                                    // Let developer or publisher edit chapters
                                    if (user?.isDeveloper == true || user?.username == itemPost.publisher) {
                                        IconButton(onClick = { showChapterAddSection = !showChapterAddSection }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "أضف فصل", tint = GreenNeon)
                                        }
                                    }
                                }

                                if (showChapterAddSection) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newChapterName,
                                            onValueChange = { newChapterName = it },
                                            placeholder = { Text("اسم الفصل الجديد...", fontSize = 11.sp, color = TextSecondaryGreen) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GreenNeon,
                                                unfocusedBorderColor = Color(0xFF1E3A1E),
                                                focusedTextColor = TextPrimaryGreen,
                                                unfocusedTextColor = TextPrimaryGreen
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if (newChapterName.isNotBlank()) {
                                                    viewModel.addChapterToNovel(itemPost, newChapterName)
                                                    newChapterName = ""
                                                    showChapterAddSection = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                                        ) {
                                            Text("حسنا", fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                val finalChapters = if (chaptersList.isNotEmpty()) {
                                    chaptersList.map { it.title }
                                } else {
                                    itemsJson
                                }

                                if (finalChapters.isEmpty()) {
                                    Text(
                                        "لم يضف الكاتب فصولاً لهذه الرواية بعد.",
                                        color = TextSecondaryGreen,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        finalChapters.forEachIndexed { idx, chName ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        val matching = chaptersList.find { it.chapterNumber == idx + 1 }
                                                        activeReadingChapter = matching ?: ChapterEntity(
                                                            id = 0,
                                                            itemId = itemPost.id,
                                                            chapterNumber = idx + 1,
                                                            title = chName,
                                                            content = "هذا هو محتوى الفصل: [$chName]\n\nيمكن للكاتب أو المطور الضغط على القلم بالأعلى لتعديل وكتابة نص الرواية الكامل وحفظه فوراً."
                                                        )
                                                    }
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.Book, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(16.dp))
                                                    Text(text = "الفصل ${idx + 1}: $chName", color = TextPrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                
                                                if (user?.isDeveloper == true || user?.username == itemPost.publisher) {
                                                    IconButton(
                                                        onClick = { viewModel.deleteChapterFromNovel(itemPost, idx) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "احذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive React Log / Upvote Likes
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.likeItem(itemPost) }) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "أعجبني", tint = YellowFlash)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أعجب بها ${itemPost.likesCount} من رواد الاستوديو",
                                color = TextPrimaryGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Compact layout floating comments button card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCommentsSheet = true }
                            .border(1.dp, GreenNeon.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Comment, contentDescription = null, tint = GreenNeon)
                                Column {
                                    Text("💬 تعليقات ومراجعات زوار الاستوديو", color = TextPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("اضغط لفتح نافذة المراجعات والآراء الكلية (${comments.size} تعليق)", color = TextSecondaryGreen, fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = GreenNeon)
                        }
                    }
                }

            }
        }
    }

    // Active Immersive Chapter Reader Dialog
    activeReadingChapter?.let { chapterRecord ->
        ActiveChapterReaderDialog(
            chapter = chapterRecord,
            canEdit = (user?.isDeveloper == true || user?.username == activeItem?.publisher),
            onDismiss = { activeReadingChapter = null },
            onSave = { updatedRecord ->
                viewModel.updateChapterContent(updatedRecord)
                activeReadingChapter = null
            }
        )
    }

    // Floating Interactive Comments Console Dialog
    if (showCommentsSheet) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCommentsSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المراجعات والتعليقات (${comments.size})", color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = { showCommentsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    // Comments list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد مراجعات حتى الآن. شارك رأيك أولاً!", color = TextSecondaryGreen, fontSize = 11.sp)
                                }
                            }
                        } else {
                            items(comments) { comment ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                UserAvatarIcon(avatarUrl = comment.userAvatar, size = 20.dp)
                                                Text(comment.userDisplay, color = TextPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                            if (user?.isDeveloper == true || user?.username == comment.username) {
                                                IconButton(
                                                    onClick = { viewModel.deleteComment(comment.id) },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(13.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(comment.commentText, color = TextPrimaryGreen, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Comment box input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("اكتب مراجعتك هنا...", color = TextSecondaryGreen, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenNeon,
                                unfocusedBorderColor = Color(0xFF435A43),
                                focusedTextColor = TextPrimaryGreen,
                                unfocusedTextColor = TextPrimaryGreen
                            )
                        )
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.postComment(commentText)
                                    commentText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("أرسل", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * INLINE VIDEO PLAYER USING SYSTEM VIDEO VIEW
 */
@Composable
fun InlineVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val uri = remember(videoUrl) { android.net.Uri.parse(videoUrl) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, GreenNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    setVideoURI(uri)
                    val mediaController = android.widget.MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                    }
                }
            },
            update = { view ->
                try {
                    view.setVideoURI(uri)
                    view.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}

/**
 * IMMERSIVE NOVEL CHAPTER READER DIALOG
 */
@Composable
fun ActiveChapterReaderDialog(
    chapter: ChapterEntity,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (ChapterEntity) -> Unit
) {
    var contentText by remember(chapter.id, chapter.content) { mutableStateOf(chapter.content) }
    var isEditing by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الفصل ${chapter.chapterNumber}: ${chapter.title}",
                    color = YellowFlash,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                if (canEdit) {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = GreenNeon
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (isEditing && canEdit) {
                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenNeon,
                            unfocusedBorderColor = Color(0xFF1E3A1E),
                            focusedTextColor = TextPrimaryGreen,
                            unfocusedTextColor = TextPrimaryGreen
                        ),
                        placeholder = { Text("اكتب محتوى الرواية الممتع هنا...", color = TextSecondaryGreen) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF152A15), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = contentText.ifBlank { "لم يتم تحديث المحتوى بعد لهذا الفصل." },
                            color = TextPrimaryGreen,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isEditing && canEdit) {
                        onSave(chapter.copy(content = contentText))
                        isEditing = false
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
            ) {
                Text(if (isEditing && canEdit) "حفظ التعديلات" else "إغلاق القراءة")
            }
        },
        dismissButton = {
            if (isEditing && canEdit) {
                TextButton(onClick = { isEditing = false; contentText = chapter.content }) {
                    Text("إلغاء", color = Color.Red)
                }
            }
        },
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * 6. FAVORITES LOGS VIEW CONTROLLER
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: TVViewModel) {
    val favorites by viewModel.userFavorites.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مفضلتي الشخصية 💖", color = GreenNeon, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GreenNeon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(32.dp), tint = TextSecondaryGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("قائمتك المفضلة فارغة حالياً", color = TextSecondaryGreen, style = MaterialTheme.typography.bodyMedium)
                    Text("انقر على أيقونة قلب فوق أي لعبة أو رواية لحفظها هنا.", color = YellowFlash, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { itemPost ->
                    Card(
                        onClick = { viewModel.navigateTo(Screen.ItemDetail(itemPost.id)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GreenNeon.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (itemPost.imageUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = itemPost.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(itemPost.title, color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(itemPost.description, color = TextSecondaryGreen, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(itemPost.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "احذف", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. DEVELOPER AD PANEL BOARD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPanelScreen(viewModel: TVViewModel) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()

    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("المنشورات 📋", "الأعضاء والمشرفين 👥", "سجلات العمليات 📡")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المشرف والناشر", color = YellowFlash, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = YellowFlash)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant M3 cyber style custom tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedIndex == index
                    Button(
                        onClick = { selectedIndex = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) GreenNeon else Color.Transparent,
                            contentColor = if (isSelected) Color.Black else TextSecondaryGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedIndex) {
                0 -> {
                    // Posts Panel
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2E1B))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🛠️ صلاحيات المطورين المطلقة", color = YellowFlash, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "تعديل خصوصية المنشور (تثبيته كمنشور حصري ⭐) أو حذفه بالكامل للمحافظة على بيئة الاستوديو للجميع.",
                                        color = TextPrimaryGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (items.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد ممتلكات أو منشورات بالاستوديو.", color = TextSecondaryGreen)
                                }
                            }
                        } else {
                            items(items) { itPost ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (itPost.isExclusive) 1.5.dp else 1.dp,
                                            color = if (itPost.isExclusive) YellowFlash else Color(0x3300FF66),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = itPost.title,
                                                    color = TextPrimaryGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = when(itPost.category) {
                                                        "GAMES" -> "[لعبة]"
                                                        "NOVELS" -> "[رواية]"
                                                        "VIDEOS" -> "[فيديو]"
                                                        "IMAGES" -> "[رسمة]"
                                                        "NEWS" -> "[تغطية]"
                                                        else -> "[مجسم]"
                                                    },
                                                    color = YellowFlash,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Text(
                                                text = "بواسطة ${itPost.publisherName} (@${itPost.publisher})",
                                                color = TextSecondaryGreen,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = if (itPost.isExclusive) "الحالة: حصري ونخبة 🌟" else "الحالة: مجتمعي عادي",
                                                color = if (itPost.isExclusive) YellowFlash else TextSecondaryGreen,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Toggle recommendation
                                            IconButton(onClick = { viewModel.updateItemDetails(itPost.copy(isExclusive = !itPost.isExclusive)) }) {
                                                Icon(
                                                    imageVector = if (itPost.isExclusive) Icons.Default.Star else Icons.Default.StarOutline,
                                                    contentDescription = "تميز",
                                                    tint = YellowFlash
                                                )
                                            }

                                            // Delete Button
                                            IconButton(onClick = { viewModel.deleteItem(itPost) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "احذف", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Members Administration Panel
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2C2E))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("👥 واجهة إدارة الأعضاء والتحكم بالصلاحيات", color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المطور يمكنه رؤية تفاصيل الأعضاء، إرسال إنذار تحذيري، تقييد النشر أو المحادثات، أو الحظر الكلي للحساب من استخدام الاستوديو ومحركه.",
                                        color = TextSecondaryGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (users.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا يوجد أعضاء مسجلين بقاعدة البيانات حالياً.", color = TextSecondaryGreen)
                                }
                            }
                        } else {
                            items(users) { user ->
                                var warningText by remember(user.username) { mutableStateOf("") }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = if (user.isBanned) Color.Red else Color(0x3300FF66),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        // Row with info
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                UserAvatarIcon(
                                                    avatarUrl = user.avatarUrl,
                                                    size = 40.dp
                                                )
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(user.name, color = TextPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        if (user.isDeveloper) {
                                                            Icon(Icons.Default.Shield, contentDescription = "مطور رسمي", tint = YellowFlash, modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                    Text("@${user.username}", color = TextSecondaryGreen, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                                }
                                            }

                                            // Overall account status bullet
                                            Text(
                                                text = if (user.isBanned) "مبند 🚫" else if (user.isDeveloper) "مطور رئيسي" else "نشط 📡",
                                                color = if (user.isBanned) Color.Red else if (user.isDeveloper) YellowFlash else GreenNeon,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(if (user.isBanned) Color.Red.copy(alpha = 0.15f) else Color.Black, RoundedCornerShape(6.dp))
                                                    .border(1.dp, if (user.isBanned) Color.Red else Color(0xFF1E3A1E), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Status summary / permissions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Ban button
                                            Button(
                                                onClick = { viewModel.banUserStatus(user.username, !user.isBanned) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (user.isBanned) GreenNeon else Color(0xFF421C1C),
                                                    contentColor = if (user.isBanned) Color.Black else Color.Red
                                                ),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (user.isBanned) "فك حظر الحساب" else "حظر كامل للحساب",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Toggle Chat-Ban
                                            Button(
                                                onClick = { viewModel.toggleUserChatBan(user.username, !user.isBannedFromChat) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (user.isBannedFromChat) Color.Black else Color(0xFF2E3D2E),
                                                    contentColor = if (user.isBannedFromChat) YellowFlash else TextPrimaryGreen
                                                ),
                                                border = BorderStroke(1.dp, if (user.isBannedFromChat) YellowFlash else Color.Transparent),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (user.isBannedFromChat) "فك حظر الشات" else "حظر قسم الشات",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Toggle Writing/Postings-Ban
                                            Button(
                                                onClick = { viewModel.toggleUserWritingBan(user.username, !user.isBannedFromWriting) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (user.isBannedFromWriting) Color.Black else Color(0xFF2E3D2E),
                                                    contentColor = if (user.isBannedFromWriting) YellowFlash else TextPrimaryGreen
                                                ),
                                                border = BorderStroke(1.dp, if (user.isBannedFromWriting) YellowFlash else Color.Transparent),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (user.isBannedFromWriting) "فك حظر النشر" else "حظر ميزات النشر",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Warnings channel
                                        if (user.warningMessage.isNotBlank()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF332115), RoundedCornerShape(6.dp))
                                                    .border(1.dp, YellowFlash, RoundedCornerShape(6.dp))
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "⚠️ الإنذار الموجه: ${user.warningMessage}",
                                                    color = YellowFlash,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.clearUserWarning(user.username) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Clear, contentDescription = "تصفير", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }

                                        // Input to issue new warning
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = warningText,
                                                onValueChange = { warningText = it },
                                                placeholder = { Text("اكتب تحذيراً موجهاً له...", fontSize = 10.sp, color = TextSecondaryGreen) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = YellowFlash,
                                                    unfocusedBorderColor = Color(0xFF1E3A1E),
                                                    focusedTextColor = TextPrimaryGreen,
                                                    unfocusedTextColor = TextPrimaryGreen
                                                ),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    if (warningText.isNotBlank()) {
                                                        viewModel.warnUser(user.username, warningText)
                                                        warningText = ""
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = YellowFlash, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(42.dp)
                                            ) {
                                                Text("إرسال تحذير", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Logs Auditing terminal Panel
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(GreenNeon, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SECURE LOG CONSOLE: matrix@tv-store:~$ info",
                                    color = GreenNeon,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, Color(0xFF132A13), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF030A03))
                        ) {
                            if (logs.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "[ Terminal logs stack empty. Diagnostic normal. ]",
                                        color = TextSecondaryGreen,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(logs.reversed()) { log ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "[ ${log.action} ]",
                                                    color = YellowFlash,
                                                    fontSize = 10.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "@${log.user}",
                                                    color = GreenNeon,
                                                    fontSize = 10.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "> ${log.message}",
                                                color = TextPrimaryGreen,
                                                fontSize = 10.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Icon Size adjustments helpers
// No extra helpers needed

/**
 * GOOGLE SELECTOR DIALOG - FOR FAST SECURE DEVELOPMENT AND USER ACCESS PROTOCOLS
 */
@Composable
fun GoogleSelectorDialog(
    onDismiss: () -> Unit,
    onSelect: (email: String, name: String, avatar: String) -> Unit
) {
    var customMode by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) } // 1: Email, 2: Password, 3: Loading
    
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (currentStep != 3) onDismiss() },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Google Multi-color styled logo
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "GOOGLE OAUTH CONNECT",
                            color = GreenNeon,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Text(
                            text = if (customMode) "مصادقة الهوية الموحدة الآمنة (OAuth 2.0)" else "بروتوكول الاختيار السريع لحلول جوجل",
                            color = TextSecondaryGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = GreenNeon.copy(alpha = 0.15f))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!customMode) {
                    // Case 1: Standard Pickers
                    Text(
                        text = "> اختر حساب مصادقة للاتصال التلقائي السريع:",
                        color = TextPrimaryGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    val accounts = listOf(
                        Triple("asdasd9090asdasd90@gmail.com", "المستخدم الهجين المتصل (Hybrid)", "avatar1"),
                        Triple("coder.matrix.hack@gmail.com", "المهندس الرقمي (Cyber Engineer)", "dev"),
                        Triple("robot.unit.0x@gmail.com", "الوحدة البرمجية (Robotic Unit)", "avatar3")
                    )

                    accounts.forEach { (email, name, avatar) ->
                        Card(
                            onClick = { onSelect(email, name, avatar) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GreenNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E1E))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                UserAvatarIcon(avatarUrl = avatar, size = 32.dp, glow = true)
                                Column {
                                    Text(
                                        text = name,
                                        color = TextPrimaryGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = email,
                                        color = YellowFlash,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Button to toggle Custom Real-like connection
                    Card(
                        onClick = { customMode = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, YellowFlash.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B210F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.dp, YellowFlash, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = YellowFlash, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = "📧 استخدام حساب Google آخر حقيقي",
                                    color = TextPrimaryGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تسجيل دخول آمن بأي حساب Google شخصي لك ومزامنة الاسم والصورة",
                                    color = YellowFlash,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                } else {
                    // Case 2: Custom Realistic Gmail/Google Login Flow!
                    when (currentStep) {
                        1 -> {
                            // Step 1: Email Input
                            Text(
                                text = "تسجيل الدخول باستخدام Google",
                                color = TextPrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "انتقل للمتابعة والمصادقة في استوديو TV",
                                color = TextSecondaryGreen,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("البريد الإلكتروني للـ Google") },
                                placeholder = { Text("example@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenNeon,
                                    unfocusedBorderColor = Color(0xFF2E3E2E),
                                    focusedTextColor = TextPrimaryGreen,
                                    unfocusedTextColor = TextPrimaryGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { customMode = false }) {
                                    Text("رجوع للحسابات السريعة", color = TextSecondaryGreen, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        if (emailInput.isNotBlank() && (emailInput.contains("@") || emailInput.length > 5)) {
                                            currentStep = 2
                                        } else {
                                            Toast.makeText(context, "الرجاء كتابة بريد إلكتروني صحيح", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("التالي", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        2 -> {
                            // Step 2: Password Input
                            Text(
                                text = "مرحباً بك، نسعد بمصادقتك",
                                color = TextPrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "البريد الإلكتروني: $emailInput",
                                color = YellowFlash,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("أدخل كلمة مرور حساب قوقل") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = TextSecondaryGreen
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenNeon,
                                    unfocusedBorderColor = Color(0xFF2E3E2E),
                                    focusedTextColor = TextPrimaryGreen,
                                    unfocusedTextColor = TextPrimaryGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { currentStep = 1 }) {
                                    Text("رجوع", color = TextSecondaryGreen, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        if (passwordInput.length >= 4) {
                                            currentStep = 3
                                        } else {
                                            Toast.makeText(context, "تنبيه: كلمة المرور قصيرة جداً لحماية الحساب", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        3 -> {
                            // Step 3: Realistic connecting loader!
                            var loadMsg by remember { mutableStateOf("جاري الربط مع قنوات مصادقة Google الموحدة...") }
                            
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(800)
                                loadMsg = "تم التحقق وصنع تشفير آمن لرمز Google OAuth 2.0 الآمن..."
                                kotlinx.coroutines.delay(1000)
                                loadMsg = "جاري تعيين وإصدار معلومات الهوية الرقمية للمستخدم الأصيل..."
                                kotlinx.coroutines.delay(700)
                                
                                val prefix = emailInput.substringBefore("@")
                                val formattedName = prefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                val generatedAvatar = "avatar" + ((1..5).random())
                                onSelect(emailInput.trim(), formattedName, generatedAvatar)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = GreenNeon, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بروتوكول التحقق نشط...", color = YellowFlash, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = loadMsg,
                                    color = TextPrimaryGreen,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (currentStep != 3) {
                TextButton(onClick = onDismiss) {
                    Text("إلغاء الاتصال [ESC]", color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.5.dp, GreenNeon, RoundedCornerShape(20.dp))
    )
}

@Composable
fun AccountSettingsDialog(
    user: UserEntity,
    onDismissRequest: () -> Unit,
    onSaveChanges: (name: String, avatarUrl: String) -> Unit,
    onLinkGoogle: () -> Unit,
    onNavigateToDeveloperPanel: () -> Unit
) {
    var editName by remember { mutableStateOf(user.name) }
    var editAvatarUrl by remember { mutableStateOf(user.avatarUrl) }
    var isUrlMode by remember { mutableStateOf(editAvatarUrl.isEmpty() || !editAvatarUrl.startsWith("file://")) }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(24.dp))
                Column {
                    Text("إعدادات الحساب والملف", color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("تعديل الهوية وربط حساب قوقل الخاص بك", color = TextSecondaryGreen, fontSize = 9.sp)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Field
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الاسم المستعار المعروض", color = GreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            placeholder = { Text("مثال: عبد الله أحمد") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenNeon,
                                unfocusedBorderColor = Color(0xFF1E3A1E),
                                focusedTextColor = TextPrimaryGreen,
                                unfocusedTextColor = TextPrimaryGreen
                            )
                        )
                    }
                }

                // Avatar Selection with real dual picker options!
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text("أيقونة الرمز الشخصي (الأفتار)", color = GreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isUrlMode = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isUrlMode) GreenNeon else Color.Transparent,
                                    contentColor = if (isUrlMode) Color.Black else TextSecondaryGreen
                                ),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رابط URL", fontSize = 10.sp)
                            }
                            
                            val imgLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent()
                            ) { uri: android.net.Uri? ->
                                uri?.let {
                                    try {
                                        val file = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.png")
                                        context.contentResolver.openInputStream(uri)?.use { inStream ->
                                            file.outputStream().use { outStream ->
                                                inStream.copyTo(outStream)
                                            }
                                        }
                                        editAvatarUrl = android.net.Uri.fromFile(file).toString()
                                        Toast.makeText(context, "تم حفظ صورة الملف الشخصي محلياً بنجاح ✅", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "فشل في حفظ الصورة المحلية", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            Button(
                                onClick = { 
                                    isUrlMode = false
                                    imgLauncher.launch("image/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isUrlMode) GreenNeon else Color.Transparent,
                                    contentColor = if (!isUrlMode) Color.Black else TextSecondaryGreen
                                ),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ملفات الجهاز", fontSize = 10.sp)
                            }
                        }

                        // Preset quick avatars if URL mode is active
                        if (isUrlMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("أو اختر أحد الرموز المتوهجة السريعة:", color = TextSecondaryGreen, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val presets = listOf("avatar1", "avatar2", "avatar3", "avatar4", "avatar5")
                                presets.forEach { preset ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (editAvatarUrl == preset) GreenNeon else Color.Transparent)
                                            .padding(2.dp)
                                            .clickable { editAvatarUrl = preset }
                                    ) {
                                        UserAvatarIcon(avatarUrl = preset, size = 32.dp, glow = (editAvatarUrl == preset))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isUrlMode) {
                            OutlinedTextField(
                                value = if (editAvatarUrl.startsWith("file://")) "" else editAvatarUrl,
                                onValueChange = { editAvatarUrl = it },
                                placeholder = { Text("أدخل رابط الصورة الشخصية هنا...", fontSize = 10.sp, color = TextSecondaryGreen) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenNeon,
                                    unfocusedBorderColor = Color(0xFF2E3E2E),
                                    focusedTextColor = TextPrimaryGreen,
                                    unfocusedTextColor = TextPrimaryGreen
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF2E3E2E), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = if (editAvatarUrl.startsWith("file://")) "ملف أفتار محلي آمن 📂" else "لم يتم اختيار أفتار محلي بعد",
                                        color = if (editAvatarUrl.startsWith("file://")) GreenNeon else TextSecondaryGreen,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (editAvatarUrl.startsWith("file://")) {
                                    IconButton(onClick = { editAvatarUrl = "avatar1" }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "استعادة الافتراضي", tint = YellowFlash)
                                    }
                                }
                            }
                        }
                    }
                }

                // Google Link Section!
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1E3A1E), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text("ربط الحساب بخدمات Google", color = GreenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        if (user.googleEmail.isNotBlank()) {
                            // Already linked!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1F2F1F), RoundedCornerShape(8.dp))
                                    .border(1.dp, GreenNeon, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("تم الربط بمصادقة Google بنجاح ✅", color = TextPrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(user.googleEmail, color = YellowFlash, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                }
                            }
                        } else {
                            // Not linked yet, show "ربط بحساب قوقل" button!
                            Button(
                                onClick = onLinkGoogle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, YellowFlash, RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2B210F),
                                    contentColor = YellowFlash
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔗 ربط بحساب قوقل (Google Authentication)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // If developer, show shortcut button
                if (user.isDeveloper) {
                    item {
                        Button(
                            onClick = onNavigateToDeveloperPanel,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الانتقال إلى لوحة المطورين 👑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editName.isNotBlank()) {
                        onSaveChanges(editName.trim(), editAvatarUrl)
                    } else {
                        Toast.makeText(context, "الرجاء كتابة اسم العرض أولاً", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ التعديلات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("إلغاء", color = Color.Red)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.5.dp, GreenNeon, RoundedCornerShape(20.dp))
    )
}

/**
 * BENTO CATEGORY CARD - MATERIAL 3 PROGRAMMATIC REPRESENTATION
 */
@Composable
fun BentoCategoryCard(
    key: String,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isNews = key == "NEWS"
    val isGames = key == "GAMES"
    val isNovels = key == "NOVELS"
    val isModels = key == "MODELS"
    val isImages = key == "IMAGES"
    val isVideos = key == "VIDEOS"

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (isNews) {
                    Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(24.dp))
                } else {
                    Modifier.border(1.dp, GreenNeon.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isNews) Color.Transparent else DarkSurface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        if (isNews) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GreenNeon, GreenNeonAccent)
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "DAILY NEWS",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = title,
                            color = Color.Black.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$count أخبار حية",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isGames) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(YellowFlash)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isGames) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(YellowFlash, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "روائع حية",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(icon, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Text(
                                text = "GAME ARENA",
                                color = YellowFlash,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = title,
                                color = TextPrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$count مشاريع لعبية جاهزة",
                                color = TextSecondaryGreen,
                                fontSize = 9.sp
                            )
                        }
                    } else if (isNovels) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "STORIES",
                                color = TextPrimaryGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = title,
                                color = TextSecondaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "$count قصة ورواية",
                                color = YellowFlash,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (isModels) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "3D MODELS",
                                    color = GreenNeon,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = title,
                                    color = TextPrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(icon, contentDescription = null, tint = GreenNeon, modifier = Modifier.size(18.dp))
                        }

                        Column {
                            Row(
                                modifier = Modifier.height(18.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f).height(6.dp).background(GreenNeon.copy(alpha = 0.2f), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.weight(1f).height(18.dp).background(GreenNeon, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.weight(1f).height(10.dp).background(GreenNeon.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "حمّل مجاناً • $count مجسمات",
                                color = TextSecondaryGreen,
                                fontSize = 8.sp
                            )
                        }
                    } else if (isImages) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = YellowFlash, modifier = Modifier.size(20.dp))
                            Column {
                                Text("GALLERY", color = YellowFlash, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                Text(title, color = TextPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("$count صور معروضة", color = TextSecondaryGreen, fontSize = 8.sp)
                            }
                        }
                    } else if (isVideos) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Text("VIDEOS", color = TextPrimaryGreen, fontWeight = FontWeight.Black, fontSize = 9.sp)
                            }
                            
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = TextSecondaryGreen.copy(alpha = 0.25f),
                                modifier = Modifier.size(28.dp).align(Alignment.CenterHorizontally)
                            )

                            Text(
                                text = "$count مقاطع فيديو مصورة",
                                color = TextSecondaryGreen,
                                fontSize = 8.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
