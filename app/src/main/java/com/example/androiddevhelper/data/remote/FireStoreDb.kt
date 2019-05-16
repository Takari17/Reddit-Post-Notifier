package com.example.androiddevhelper.data.remote

import android.util.Log
import com.example.androiddevhelper.data.remote.reddit.NewRedditPost
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject


class FireStoreDb @Inject constructor(
    private val postDocument: DocumentReference
) {

    //Saves/Updates previous reddit post list to fireStore
    fun saveListToDb(previousRedditPost: List<NewRedditPost>) {
        val previousRedditPostObject = PreviousRedditPost(previousRedditPost)

        postDocument.set(previousRedditPostObject, SetOptions.merge())
            .addOnSuccessListener { Log.d("zwi", "Saved list to db") }
            .addOnFailureListener { Log.d("zwi", "Failed saving list to db") }
    }

    fun getListFromDb(): Task<DocumentSnapshot> = postDocument.get()
}


//Used so fireStore can return an object
class PreviousRedditPost(
    val previousRedditPost: List<NewRedditPost>
) {
    constructor() : this(emptyList())
}