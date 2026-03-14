package com.badgr.orbreader.sync

import android.util.Log
import com.badgr.orbreader.billing.ProGate
import com.badgr.orbreader.data.local.BookDao
import com.badgr.orbreader.data.local.BookEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object CloudSyncManager {

    private const val TAG = "CloudSyncManager"

    // Firestore collection names
    private const val COLLECTION_USERS    = "users"
    private const val COLLECTION_BOOKS    = "books"
    private const val COLLECTION_PROGRESS = "progress"

    private val auth: FirebaseAuth      by lazy { FirebaseAuth.getInstance() }
    private val db:   FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn:  Boolean       get() = currentUser != null

    suspend fun signUp(email: String, password: String): FirebaseUser {
        Log.d(TAG, "Attempting signUp for $email")
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user   = result.user ?: error("Sign-up succeeded but user was null.")
        user.sendEmailVerification().await()
        Log.d(TAG, "Verification email sent to $email")
        return user
    }

    suspend fun signIn(email: String, password: String): FirebaseUser {
        Log.d(TAG, "Attempting signIn for $email")
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        return result.user ?: error("Sign-in failed.")
    }

    fun signOut() {
        Log.d(TAG, "Signing out")
        auth.signOut()
        ProGate.revokeEntitlement()
    }

    suspend fun syncBooks(bookDao: BookDao) {
        if (!ProGate.cloudSync) return
        val uid   = requireUser().uid
        val books = bookDao.getAllBooks_suspend()
        books.forEach { entity ->
            bookDocRef(uid, entity.id)
                .set(entity.toFirestoreMap(), SetOptions.merge())
                .await()
        }
        Log.d(TAG, "Synced ${books.size} books for uid=$uid")
    }

    suspend fun pushBook(uid: String, entity: BookEntity) {
        if (!ProGate.cloudSync) return
        bookDocRef(uid, entity.id)
            .set(entity.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun fetchRemoteBooks(uid: String): List<BookEntity> {
        if (!ProGate.cloudSync) return emptyList()
        val snapshot = db.collection(COLLECTION_USERS).document(uid)
            .collection(COLLECTION_BOOKS).get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                BookEntity(
                    id               = doc.id,
                    title            = doc.getString("title")    ?: return@mapNotNull null,
                    fileType         = doc.getString("fileType") ?: return@mapNotNull null,
                    wordCount        = (doc.getLong("wordCount") ?: 0L).toInt(),
                    createdAt        = doc.getLong("createdAt")  ?: 0L,
                    currentWordIndex = (doc.getLong("currentWordIndex") ?: 0L).toInt(),
                    coverPath        = null
                )
            } catch (e: Exception) { null }
        }
    }

    suspend fun pushProgress(bookId: String, currentWordIndex: Int) {
        if (!ProGate.cloudSync) return
        val uid = requireUser().uid
        db.collection(COLLECTION_USERS).document(uid)
            .collection(COLLECTION_PROGRESS).document(bookId)
            .set(
                mapOf(
                    "currentWordIndex" to currentWordIndex,
                    "updatedAt"        to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
    }

    suspend fun fetchProgress(bookId: String): Int {
        if (!ProGate.cloudSync) return 0
        val uid = requireUser().uid
        val doc = db.collection(COLLECTION_USERS).document(uid)
            .collection(COLLECTION_PROGRESS).document(bookId)
            .get().await()
        return (doc.getLong("currentWordIndex") ?: 0L).toInt()
    }

    private fun requireUser(): FirebaseUser =
        currentUser ?: error("CloudSyncManager: no signed-in user.")

    private fun bookDocRef(uid: String, bookId: String) =
        db.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_BOOKS).document(bookId)

    private fun BookEntity.toFirestoreMap(): Map<String, Any> = mapOf(
        "title"            to title,
        "fileType"         to fileType,
        "wordCount"        to wordCount,
        "createdAt"        to createdAt,
        "currentWordIndex" to currentWordIndex
    )
}
