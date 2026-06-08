package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val dao: AppDao) {

    // Users
    suspend fun getUser(username: String): UserEntity? {
        return dao.getUser(username)
    }

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return dao.isUsernameTaken(username) > 0
    }

    // Items
    fun getAllItems(): Flow<List<ItemEntity>> = dao.getAllItemsFlow()

    fun getItemsByCategory(category: String): Flow<List<ItemEntity>> = dao.getItemsByCategoryFlow(category)

    suspend fun getItemById(id: Int): ItemEntity? = dao.getItemById(id)

    suspend fun insertItem(item: ItemEntity): Long = dao.insertItem(item)

    suspend fun updateItem(item: ItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = dao.deleteItem(item)

    // Favorites
    fun getFavoritesOfUser(username: String): Flow<List<FavoriteEntity>> = dao.getFavoritesOfUserFlow(username)

    suspend fun addFavorite(username: String, itemId: Int) {
        dao.insertFavorite(FavoriteEntity(username = username, itemId = itemId))
    }

    suspend fun removeFavorite(username: String, itemId: Int) {
        dao.removeFavorite(username, itemId)
    }

    suspend fun isFavorite(username: String, itemId: Int): Boolean {
        return dao.isFavorite(username, itemId) > 0
    }

    // Comments
    fun getCommentsForItem(itemId: Int): Flow<List<CommentEntity>> = dao.getCommentsForItemFlow(itemId)

    suspend fun insertComment(comment: CommentEntity) {
        dao.insertComment(comment)
    }

    suspend fun deleteComment(commentId: Int) {
        dao.deleteComment(commentId)
    }

    // Category Chat
    fun getChatMessages(category: String): Flow<List<ChatMessageEntity>> = dao.getChatMessagesByCategoryFlow(category)

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        dao.insertChatMessage(message)
    }

    // Modern Moderation & User Lists
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsersFlow()

    suspend fun getAllUsersDirect(): List<UserEntity> = dao.getAllUsersDirect()

    // Full Chapters Engine
    fun getChaptersForItem(itemId: Int): Flow<List<ChapterEntity>> = dao.getChaptersForItemFlow(itemId)

    suspend fun insertChapter(chapter: ChapterEntity) {
        dao.insertChapter(chapter)
    }

    suspend fun deleteChapter(chapterId: Int) {
        dao.deleteChapter(chapterId)
    }

    // Audit Logging Console
    fun getAllLogs(): Flow<List<LogEntity>> = dao.getAllLogsFlow()

    suspend fun insertLog(log: LogEntity) {
        dao.insertLog(log)
    }

    // Database seeding
    suspend fun prepopulateDatabaseIfNeeded() {
        // Ensure default developer "TV store" exists
        val devUser = dao.getUser("TV store")
        if (devUser == null) {
            dao.insertUser(
                UserEntity(
                    username = "TV store",
                    name = "مطور ستوديو TV",
                    password = "tvstoreadmin",
                    avatarUrl = "dev",
                    isDeveloper = true
                )
            )
        }

        // Seed sample items if database is empty
        val currentItems = dao.getAllItemsFlow().firstOrNull() ?: emptyList()
        if (currentItems.isEmpty()) {
            val sampleItems = listOf(
                ItemEntity(
                    category = "GAMES",
                    title = "لعبة مغامرة البطل العربي",
                    description = "لعبة ثنائية الأبعاد مليئة بالألغاز والحركة، تحكي قصة بطل ينقذ الكوكب الرقمي. تحتوي على رسومات بكسل خلابة ومستويات مشوقة.",
                    imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600",
                    downloadUrl = "https://example.com/games/hero-adventure.apk",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = true,
                    likesCount = 28
                ),
                ItemEntity(
                    category = "GAMES",
                    title = "سباق التحدي المشوق 3D",
                    description = "لعبة سباقات في شوارع ليلية تتألق بأنوار النيون واللون الأخضر المشع! تحكم بالسيارات السريعة وتحدى أفضل الأزمان.",
                    imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=600",
                    downloadUrl = "https://example.com/games/neon-racer.apk",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = false,
                    likesCount = 19
                ),
                ItemEntity(
                    category = "NOVELS",
                    title = "سيد الأقدار وعالم المطورين",
                    description = "رواية سينكرو-فانتاسي تأخذك في رحلة حول شاب يكتشف أنه يعيش داخل لعبة أكواد برمجية يتحكم بها المطور الغامض TV. رواية فريدة ومستمرة مع فصول مميزة.",
                    imageUrl = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?q=80&w=600",
                    chaptersJson = "[\"الفصل الأول: البداية الغامضة والسلسلة المشفرة\", \"الفصل الثاني: أول خطأ في الأكواد والنظام البرمجي\", \"الفصل الثالث: استدعاء مطور TV store الفاخر \", \"الفصل الرابع: النور الأخضر المنير والمجهول\"]",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = true,
                    likesCount = 37
                ),
                ItemEntity(
                    category = "NOVELS",
                    title = "أسرار الفجر المتوهج",
                    description = "رواية كلاسيكية تدور أحداثها في العصور المبرمجة القديمة. رحلة البحث عن المخطوطات التي تصف تحويل الطاقة إلى بلورات خضراء متوهجة.",
                    imageUrl = "https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600",
                    chaptersJson = "[\"الفصل الأول: رحلة المستكشف الصغير\", \"الفصل الثاني: سر الوادي المضيء\"]",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = false,
                    likesCount = 14
                ),
                ItemEntity(
                    category = "VIDEOS",
                    title = "كواليس تطوير واجهة TV Studio الخلابة",
                    description = "شرح تقني مميز من داخل الاستوديو نعرض فيه كيف صممنا هذه الواجهة المتألقة باللونين الأخضر والأصفر وكيف تم دمج قواعد بيانات Room لتحديث فوري وخفيف لا يصدق.",
                    imageUrl = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?q=80&w=600",
                    videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = true,
                    likesCount = 45
                ),
                ItemEntity(
                    category = "IMAGES",
                    title = "لوحة النيون الخضراء الساحرة",
                    description = "رسمة رقمية مصممة خصيصاً للتطبيق لتبرز التناغم بين الأخضر اللامع المضيء واللمسات الصفراء المشرقة التي يحبها أعضاء ستوديو TV.",
                    imageUrl = "https://images.unsplash.com/photo-1507608869274-d3177c8bb4c7?q=80&w=600",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    likesCount = 52
                ),
                ItemEntity(
                    category = "NEWS",
                    title = "انطلاق متجر TV Studio المتألق الجديد!",
                    description = "أعلن اليوم مجلس مطوري TV store رسمياً عن إطلاق التطبيق المتكامل لعرض الروائع الرقمية. يمكن لجميع المستخدمين الآن المشاركة، التعليق، التفاعل مباشرة وإضافة رواياتهم وتصاميمهم الخاصة مجاناً وبأعلى جودة وسرعة.",
                    imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?q=80&w=600",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    likesCount = 61
                ),
                ItemEntity(
                    category = "MODELS",
                    title = "مجسم سفينة الفضاء TV-Cosmos 3D",
                    description = "مجسم رائع بالكامل لمركبة فضاء مجهزة بأجنحة نيون متوهجة. مناسب لمطوري الألعاب ومصممي الثري دي للتحميل المباشر والاستخدام الحر.",
                    imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=600",
                    downloadUrl = "https://example.com/models/spacecraft.gltf",
                    publisher = "TV store",
                    publisherName = "مطور ستوديو TV",
                    publisherAvatar = "dev",
                    isExclusive = true,
                    likesCount = 29
                )
            )

            for (item in sampleItems) {
                dao.insertItem(item)
            }
        }
    }
}
