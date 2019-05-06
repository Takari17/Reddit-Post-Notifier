package com.example.androiddevhelper.data.remote

import android.util.Log
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Single

class FireStoreDb {

    private val db = FirebaseFirestore.getInstance()
    private val postCollection = db.collection("New Reddit Post ")

    // Holds previous reddit post list
    private val recentPostDocument = db.collection("New Reddit Post ").document("Previous Reddit Post")

    // We subscribe in our repository
    val getPreviousPostListFromDb = PublishRelay.create<List<NewRedditPost>>()


    /*
    Saves individual post in its own document
     */
    fun saveNewPostToDb(newRedditPost: NewRedditPost) {

        postCollection.add(newRedditPost)
            .addOnSuccessListener { Log.d("zwi", "Saved to db") }
            .addOnFailureListener { Log.d("zwi", "Failed saving to db") }
    }

    /*
    Saves/Updates previous reddit post to it's own document
     */
    fun saveListToDb(previousRedditPost: List<NewRedditPost>) {

        val previousRedditPostObject = PreviousRedditPost(previousRedditPost)

        recentPostDocument.set(previousRedditPostObject, SetOptions.merge())
            .addOnSuccessListener { Log.d("zwi", "Saved list to db") }
            .addOnFailureListener { Log.d("zwi", "Failed saving list to db") }
    }


    /*
    Fire store takes a few seconds to get the data, so we have to subscribe to a relay in our repository for data sending, we cant just return the method in the on success listener
    because the method will complete before firestore gets the data
     */
    fun getListFromDb() {

        recentPostDocument.get()
            .addOnSuccessListener { documentData ->

                // Only execute block if not null
                if (documentData.exists()) {
                    val previousPostObject = documentData.toObject(PreviousRedditPost::class.java)!!
                    val previousPostList = previousPostObject.previousRedditPost

                    getPreviousPostListFromDb.accept(previousPostList)
                }

            }.addOnFailureListener { Log.d("zwi", "Failed getting post") }
    }

}

/*
Used so fireStore can return an object (Not sure if there's a better way)
 */
class PreviousRedditPost(
    val previousRedditPost: List<NewRedditPost>
) {
    constructor() : this(emptyList())
}