package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Dashboard : Screen()
    data class CategoryDetail(val categoryKey: String) : Screen()
    data class ItemDetail(val itemId: Int) : Screen()
    object Favorites : Screen()
    object DeveloperPanel : Screen()
}

class TVViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Auth States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    // Navigation States
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _navigationHistory = MutableStateFlow<List<Screen>>(emptyList())

    // All posts
    val allItems: StateFlow<List<ItemEntity>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter items based on search query
    val filteredItems: StateFlow<List<ItemEntity>> = combine(allItems, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites of logged-in user
    val userFavorites: StateFlow<List<ItemEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else {
            combine(repository.getFavoritesOfUser(user.username), allItems) { favList, itemList ->
                val favIds = favList.map { it.itemId }.toSet()
                itemList.filter { it.id in favIds }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category detail comments & chats
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categoryMessages: StateFlow<List<ChatMessageEntity>> = _selectedCategory.flatMapLatest { cat ->
        if (cat == null) flowOf(emptyList())
        else repository.getChatMessages(cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active item detail comments
    private val _selectedItemId = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = _selectedItemId.asStateFlow()

    val activeItem: StateFlow<ItemEntity?> = _selectedItemId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else {
            allItems.map { list -> list.find { it.id == id } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeItemComments: StateFlow<List<CommentEntity>> = _selectedItemId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getCommentsForItem(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered users & terminal auditing logs
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<LogEntity>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chapters of the active novel
    val activeItemChapters: StateFlow<List<ChapterEntity>> = _selectedItemId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getChaptersForItem(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Run database seeding and check persistent session
        viewModelScope.launch {
            repository.prepopulateDatabaseIfNeeded()
            
            val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
            val savedUsername = prefs.getString("saved_username", null)
            if (savedUsername != null) {
                val user = repository.getUser(savedUsername)
                if (user != null) {
                    if (user.isBanned) {
                        prefs.edit().remove("saved_username").apply()
                    } else {
                        _currentUser.value = user
                        _currentScreen.value = Screen.Dashboard
                        logSystemAction("التهيئة الآلية", "تم استيراد جلسة عمل المعرّف @$savedUsername من وحدة التخزين الآمنة بنجاح")
                    }
                }
            }
        }

        // Real-time listener to logout banned active users instantly or sync their states
        viewModelScope.launch {
            _currentUser.collect { activeUser ->
                if (activeUser != null) {
                    repository.getAllUsers().collect { usersList ->
                        val updated = usersList.find { it.username == activeUser.username }
                        if (updated != null) {
                            if (updated.isBanned) {
                                _currentUser.value = null
                                _currentScreen.value = Screen.Login
                                val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit().remove("saved_username").apply()
                                _loginError.value = "تم رصد حظر حسابك بالكامل من قبل بروتوكولات المطورين!"
                            } else if (updated != _currentUser.value) {
                                _currentUser.value = updated
                            }
                        }
                    }
                }
            }
        }
    }

    // Navigation helper methods
    fun navigateTo(screen: Screen) {
        val current = _currentScreen.value
        _navigationHistory.value = _navigationHistory.value + current
        _currentScreen.value = screen
        
        when (screen) {
            is Screen.CategoryDetail -> _selectedCategory.value = screen.categoryKey
            is Screen.ItemDetail -> _selectedItemId.value = screen.itemId
            else -> {}
        }
    }

    fun navigateBack() {
        val history = _navigationHistory.value
        if (history.isNotEmpty()) {
            val last = history.last()
            _navigationHistory.value = history.dropLast(1)
            _currentScreen.value = last
            
            when (last) {
                is Screen.CategoryDetail -> _selectedCategory.value = last.categoryKey
                is Screen.ItemDetail -> _selectedItemId.value = last.itemId
                else -> {}
            }
        } else {
            // Default fallback
            if (_currentUser.value != null) {
                _currentScreen.value = Screen.Dashboard
            } else {
                _currentScreen.value = Screen.Login
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Login logic
    fun login(usernameInput: String, passwordInput: String, isDevLogin: Boolean) {
        viewModelScope.launch {
            try {
                _loginError.value = null
                if (usernameInput.isBlank() || passwordInput.isBlank()) {
                    _loginError.value = "الرجاء كتابة اسم المستخدم_ID ورمز المرور الخاص بالنظام"
                    return@launch
                }

                val user = repository.getUser(usernameInput)
                if (user == null) {
                    if (isDevLogin && usernameInput == "TV store" && passwordInput == "tvstoreadmin") {
                        // Force create developer if not seeded
                        val newDev = UserEntity(
                            username = "TV store",
                            name = "مطور ستوديو TV",
                            password = "tvstoreadmin",
                            avatarUrl = "dev",
                            isDeveloper = true
                        )
                        repository.insertUser(newDev)
                        _currentUser.value = newDev
                        _currentScreen.value = Screen.Dashboard
                        
                        val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putString("saved_username", "TV store").apply()
                        logSystemAction("دخول المطور", "قام المطور الرئيسي بتسجيل دخوله بأمان")
                    } else {
                        _loginError.value = "اسم المستخدم غير متاح أو غير مسجل بقاعدة البيانات!"
                    }
                } else {
                    if (user.isBanned) {
                        _loginError.value = "تم رصد حظر حسابك بالكامل من قبل بروتوكولات المطورين!"
                        return@launch
                    }
                    if (user.password != passwordInput) {
                        _loginError.value = "بروتوكول المرور المرفوض: رمز الدخول غير صحيح!"
                    } else {
                        if (isDevLogin && !user.isDeveloper) {
                            _loginError.value = "الحساب لا يمتلك صلاحيات النظام كمطور رسمي!"
                        } else {
                            _currentUser.value = user
                            _currentScreen.value = Screen.Dashboard
                            
                            val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putString("saved_username", user.username).apply()
                            
                            logSystemAction(if (user.isDeveloper) "دخول مطور" else "تسجيل دخول", "تم تسجيل حزمة دخول للمستخدم @${user.username} بنجاح")
                        }
                    }
                }
            } catch (e: Exception) {
                _loginError.value = "فشل في اختبار الدخول للقاعدة: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    // Google Sign-In Integration Logic
    fun loginWithGoogle(email: String, displayName: String, avatarUrl: String) {
        viewModelScope.launch {
            try {
                _loginError.value = null
                _registerError.value = null

                // Extract active system ID from email prefix and clean up
                val cleanUsername = email.substringBefore("@").replace(".", "").replace("-", "").lowercase()
                
                val existingUser = repository.getUser(cleanUsername)
                if (existingUser != null) {
                    if (existingUser.isBanned) {
                        _loginError.value = "تنبيه: تم رصد حظر حسابك بالكامل من قبل بروتوكولات المطورين!"
                        return@launch
                    }
                    _currentUser.value = existingUser
                    
                    val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putString("saved_username", existingUser.username).apply()
                    
                    logSystemAction("دخول GOOGLE", "سجل @${existingUser.username} دخوله عبر قناة Google")
                } else {
                    val newUser = UserEntity(
                        username = cleanUsername,
                        name = displayName,
                        password = "google_v2_auth_socket_secured", // Safe placeholder for Google Auth sockets
                        avatarUrl = avatarUrl,
                        isDeveloper = false
                    )
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    
                    val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putString("saved_username", cleanUsername).apply()
                    
                    logSystemAction("تسجيل GOOGLE", "تم تركيب وتهيئة حساب مستخدم جديد @$cleanUsername عبر Google")
                }
                _currentScreen.value = Screen.Dashboard
            } catch (e: Exception) {
                _loginError.value = "فشل بروتوكول GOOGLE_AUTH: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    // Update User Profile (display name and avatar picker)
    fun updateUserProfile(name: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val updated = current.copy(name = name, avatarUrl = avatarUrl)
                repository.insertUser(updated)
                _currentUser.value = updated
                logSystemAction("تحديث الملف", "تم تعديل الاسم أو صورة الأفتار للمستخدم @${current.username}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Link account with Google credential
    fun linkWithGoogle(email: String, name: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val updated = current.copy(
                    googleEmail = email,
                    // If current name is just username or starting with 'مستخدم', customize with Google Name on request
                    name = if (current.name.isBlank() || current.name == current.username || current.name.startsWith("مستخدم")) name else current.name,
                    avatarUrl = if (current.avatarUrl.isBlank() || current.avatarUrl == "avatar1" || current.avatarUrl == "dev") avatarUrl else current.avatarUrl
                )
                repository.insertUser(updated)
                _currentUser.value = updated
                logSystemAction("ربط قوقل", "تم ربط الحساب @${current.username} بـ Google: $email بنجاح")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Register logic
    fun register(nameInput: String, usernameInput: String, avatarUrlInput: String, passwordInput: String) {
        viewModelScope.launch {
            try {
                _registerError.value = null
                _registerSuccess.value = false

                if (nameInput.isBlank() || usernameInput.isBlank() || passwordInput.isBlank()) {
                    _registerError.value = "الرجاء ملء جميع مدخلات التهيئة الأساسية!"
                    return@launch
                }

                if (usernameInput.contains(" ")) {
                    _registerError.value = "تنبيه: يجب ألا يحتوي اسم النظام على مسافات فارغة!"
                    return@launch
                }

                val taken = repository.isUsernameTaken(usernameInput)
                if (taken || usernameInput.equals("TV store", ignoreCase = true)) {
                    _registerError.value = "الجزء المحجوز: المعرّف مستخدم مسبقاً، اختر رمزاً آخراً"
                    return@launch
                }

                val newUser = UserEntity(
                    username = usernameInput,
                    name = nameInput,
                    password = passwordInput,
                    avatarUrl = avatarUrlInput.ifBlank { "avatar1" }, // default avatar reference
                    isDeveloper = false
                )
                repository.insertUser(newUser)
                _registerSuccess.value = true
                _currentUser.value = newUser
                _currentScreen.value = Screen.Dashboard
                
                val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("saved_username", usernameInput).apply()
                
                logSystemAction("إنشاء حساب", "قام المخطط البرمجي بإنشاء حساب للمستخدم الجديد @$usernameInput")
            } catch (e: Exception) {
                _registerError.value = "فشل في تجميع وبناء الحساب الجديد: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    // Logout
    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            logSystemAction("خروج", "قام المستخدم @${user.username} بتسجيل خروجه الآمن من المحاكي")
        }
        _currentUser.value = null
        _loginError.value = null
        _currentScreen.value = Screen.Login
        _navigationHistory.value = emptyList()
        
        val prefs = getApplication<Application>().getSharedPreferences("tv_store_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("saved_username").apply()
    }

    // Favorites managers
    fun toggleFavorite(itemId: Int) {
        val username = _currentUser.value?.username ?: return
        viewModelScope.launch {
            val isFav = repository.isFavorite(username, itemId)
            if (isFav) {
                repository.removeFavorite(username, itemId)
                logSystemAction("إلغاء المفضلة", "أزال @$username المنشور $itemId من قائمته المفضلة")
            } else {
                repository.addFavorite(username, itemId)
                logSystemAction("إضافة مفضلة", "أضاف @$username المنشور $itemId إلى قائمته المفضلة")
            }
        }
    }

    fun isItemFavorite(itemId: Int): Flow<Boolean> {
        val username = _currentUser.value?.username ?: return flowOf(false)
        return flow {
            emit(repository.isFavorite(username, itemId))
        }
    }

    // Item Ratings / Likes increment
    fun likeItem(item: ItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(likesCount = item.likesCount + 1)
            repository.updateItem(updated)
            logSystemAction("إعجاب محتوى", "أبدى @_currentUser إعجابه بالمنشور [${item.title}]")
        }
    }

    // Comments Posting
    fun postComment(text: String) {
        val user = _currentUser.value ?: return
        if (user.isBannedFromWriting) {
            logSystemAction("محاولة فاشلة", "حاول @${user.username} المحظور من النشر إضافة تعليق")
            return
        }
        val itemId = _selectedItemId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val comment = CommentEntity(
                itemId = itemId,
                username = user.username,
                userDisplay = user.name,
                userAvatar = user.avatarUrl,
                commentText = text
            )
            repository.insertComment(comment)
            logSystemAction("إضافة تعليق", "أضاف @${user.username} تعليقاً على المنشور $itemId: ${text.take(30)}...")
        }
    }

    fun deleteComment(commentId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteComment(commentId)
            logSystemAction("حذف تعليق", "قام الحساب @${user.username} بحذف تعليق رقم $commentId")
        }
    }

    // Category Chat Messaging
    fun sendChatMessage(text: String) {
        val user = _currentUser.value ?: return
        if (user.isBannedFromChat) {
            logSystemAction("محاولة فاشلة", "حاول @${user.username} المحظور من الدردشة كتابة رسالة بقسم ${_selectedCategory.value}")
            return
        }
        val category = _selectedCategory.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val chatMessage = ChatMessageEntity(
                category = category,
                username = user.username,
                userDisplay = user.name,
                userAvatar = user.avatarUrl,
                messageText = text
            )
            repository.insertChatMessage(chatMessage)
            logSystemAction("دردشة القسم", "أرسل @${user.username} رسالة في شات قسم $category")
        }
    }

    // CRUD for Developers & General User posting
    // For games, novels, drawings, news, 3D Models
    fun addItem(
        category: String,
        title: String,
        description: String,
        imageUrl: String,
        downloadUrl: String = "",
        videoUrl: String = "",
        externalLink: String = "",
        chapters: List<String> = emptyList()
    ) {
        val user = _currentUser.value ?: return
        if (user.isBannedFromWriting) {
            logSystemAction("محاولة فاشلة", "حاول @${user.username} المحظور من النشر إضافة منشور بقسم $category")
            return
        }
        viewModelScope.launch {
            // Serialize chapters list to JSON manually to avoid Gson/Moshi dependency overhead in code
            val chaptersStr = chapters.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            val item = ItemEntity(
                category = category,
                title = title,
                description = description,
                imageUrl = imageUrl,
                downloadUrl = downloadUrl,
                videoUrl = videoUrl,
                externalLink = externalLink,
                chaptersJson = if (category == "NOVELS") chaptersStr else "[]",
                publisher = user.username,
                publisherName = user.name,
                publisherAvatar = user.avatarUrl,
                isExclusive = user.isDeveloper
            )
            val insertedId = repository.insertItem(item)
            logSystemAction("إضافة محتوى", "أضاف @${user.username} منشوراً جديداً بعنوان [$title] برقم معرف $insertedId")
            
            // If parsed chapters are initial, pre-insert them as real entities
            if (category == "NOVELS" && chapters.isNotEmpty()) {
                chapters.forEachIndexed { idx, chTitle ->
                    repository.insertChapter(
                        ChapterEntity(
                            itemId = insertedId.toInt(),
                            chapterNumber = idx + 1,
                            title = chTitle,
                            content = "اكتب محتوى هذا الفصل هنا لتكتمل أحداث رواية $title..."
                        )
                    )
                }
            }
        }
    }

    fun deleteItem(item: ItemEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteItem(item)
            logSystemAction("حذف محتوى", "قام @${user.username} بحذف المنشور بعنوان [${item.title}]")
        }
    }

    fun updateItemDetails(item: ItemEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateItem(item)
            logSystemAction("تحديث محتوى", "تعديل تفاصيل المنشور بعنوان [${item.title}] بواسطة @${user.username}")
        }
    }

    // Novel Chapter Managers (Legacy JSON compatibility)
    fun addChapterToNovel(item: ItemEntity, chapterName: String) {
        if (chapterName.isBlank()) return
        viewModelScope.launch {
            val cleaned = item.chaptersJson.trim()
            val list = if (cleaned == "[]" || cleaned.isEmpty()) {
                mutableListOf()
            } else {
                cleaned.substring(1, cleaned.length - 1)
                    .split(",")
                    .map { it.trim().trim('"') }
                    .filter { it.isNotEmpty() }
                    .toMutableList()
            }
            list.add(chapterName)
            val json = list.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            repository.updateItem(item.copy(chaptersJson = json))
            
            // Also insert into database table chapters for fully operational reading!
            val nextNum = list.size
            repository.insertChapter(
                ChapterEntity(
                    itemId = item.id,
                    chapterNumber = nextNum,
                    title = chapterName,
                    content = "هذا هو محتوى الفصل الجديد [$chapterName]. الرجاء من المطور أو الكاتب تحديثه بالبيانات من شاشة القراءة البرمجية."
                )
            )
            logSystemAction("إضافة فصل", "تمت إضافة الفصل رقم $nextNum [ $chapterName ] لرواية [${item.title}]")
        }
    }

    fun deleteChapterFromNovel(item: ItemEntity, chapterIndex: Int) {
        viewModelScope.launch {
            val cleaned = item.chaptersJson.trim()
            if (cleaned == "[]" || cleaned.isEmpty()) return@launch
            val list = cleaned.substring(1, cleaned.length - 1)
                .split(",")
                .map { it.trim().trim('"') }
                .filter { it.isNotEmpty() }
                .toMutableList()
            if (chapterIndex in list.indices) {
                val removedTitle = list[chapterIndex]
                list.removeAt(chapterIndex)
                val json = list.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                repository.updateItem(item.copy(chaptersJson = json))
                logSystemAction("حذف فصل", "تم حذف الفصل [$removedTitle] من رواية [${item.title}]")
            }
        }
    }

    // New Database Real Chapters System Engine
    fun addChapterWithContent(itemId: Int, title: String, content: String) {
        val user = _currentUser.value ?: return
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            val currentChapters = activeItemChapters.value
            val nextNum = currentChapters.size + 1
            val chapter = ChapterEntity(
                itemId = itemId,
                chapterNumber = nextNum,
                title = title,
                content = content
            )
            repository.insertChapter(chapter)
            logSystemAction("فصل ذكي", "أضاف @${user.username} فصلاً ثرياً برقم $nextNum بعناون [$title]")
        }
    }

    fun updateChapterContent(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.insertChapter(chapter)
            logSystemAction("تحديث فصل", "تم حفظ وتعديل محتوى الفصل برقم ${chapter.chapterNumber}")
        }
    }

    fun deleteChapterEntity(chapter: ChapterEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteChapter(chapter.id)
            logSystemAction("حذف فصل ذكي", "حذف @${user.username} فصلاً برقم ${chapter.chapterNumber} كلياً")
        }
    }

    // Moderation System Utilities
    fun warnUser(username: String, message: String) {
        val admin = _currentUser.value ?: return
        if (!admin.isDeveloper) return
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch
            val updated = user.copy(warningMessage = message)
            repository.insertUser(updated)
            logSystemAction("تنبيه مشرف", "قام المطور بتوجيه تنبيه للمستخدم @$username بقيمة [$message]")
        }
    }

    fun clearUserWarning(username: String) {
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch
            val updated = user.copy(warningMessage = "")
            repository.insertUser(updated)
            logSystemAction("تصفير تنبيهات", "تصفير قنوات الإنذار للتطبيق للمعرف @$username")
        }
    }

    fun banUserStatus(username: String, banned: Boolean) {
        val admin = _currentUser.value ?: return
        if (!admin.isDeveloper) return
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch
            val updated = user.copy(isBanned = banned)
            repository.insertUser(updated)
            logSystemAction("حظر كلي", "قرر المطور تغيير الحالة الأمنية لـ @$username إلى حظر=$banned")
        }
    }

    fun toggleUserChatBan(username: String, banned: Boolean) {
        val admin = _currentUser.value ?: return
        if (!admin.isDeveloper) return
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch
            val updated = user.copy(isBannedFromChat = banned)
            repository.insertUser(updated)
            logSystemAction("حظر شات", "تقييد صلاحيات الدردشة للمعرف @$username وحظره=$banned")
        }
    }

    fun toggleUserWritingBan(username: String, banned: Boolean) {
        val admin = _currentUser.value ?: return
        if (!admin.isDeveloper) return
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch
            val updated = user.copy(isBannedFromWriting = banned)
            repository.insertUser(updated)
            logSystemAction("حظر نشر", "تقييد صلاحيات كتابة ومشاركة المحتوى للمصنف @$username وحظره=$banned")
        }
    }

    // Automated internal logs writer
    fun logSystemAction(action: String, message: String) {
        val activeUsername = _currentUser.value?.username ?: "نظام غير معروف"
        viewModelScope.launch {
            repository.insertLog(
                LogEntity(
                    action = action,
                    user = activeUsername,
                    message = message
                )
            )
        }
    }
}

class TVViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TVViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TVViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
