package com.example.androiddevhelper.data.remote

import android.util.Log
import com.example.androiddevhelper.data.remote.reddit.response.NewRedditPost
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/*
Wait....why the hell are we storing the individual post in firebase??

I think this was beacuase we thought we needed firebase to send a notification, and adding
a new entry would trigger that.

Now with what I know now all We need to save is the List of previous network calls, dont need anything else

TODO: i JUST RELIZED SOMETHING, using firestore is actually good for this because we can maintain the state of our post for all of our users, i think?

Wait no, when individual users start the app and we make our first network call we'll bombard the user with 20 new post ;-;, using firebase for this is actually bad

Even if we stop using firebase mention in the project decription that we used firebase

I think we're just gonna rstore the list for ALL users, it'll make sense that way because the user wont get 30 notifications
on start, he'll start getting the most recent notiications by the time he starts the app
 */
class FireStoreDb {

    private val db = FirebaseFirestore.getInstance()

    // Holds previous reddit post list
    private val recentPostDocument = db.collection("New Reddit Post ").document("Previous Reddit Post")

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
    We cant use Rx, we have to add the success listener call back, using Rx will just mak things more complex
     */
    fun getListFromDb(): Task<DocumentSnapshot> = recentPostDocument.get()
}

/*
Used so fireStore can return an object (Not sure if there's a better way)
 */
class PreviousRedditPost(
    val previousRedditPost: List<NewRedditPost>
) {
    constructor() : this(emptyList())
}