<h1 align="center">Reddit Post Notifier</h1>

<p align="center">
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
</p>

<p align="center">
Reddit Post Notifier keeps you up to date with post from your favorite communities. It allows you to simultaneously observe new post from 12 subreddits at a time.

<p align="center">
<img src= "preview/post_notifier_feature_graphic.png"/>
</p>


## Download
You can download it from the playstore [right here.](https://play.google.com/store/apps/details?id=com.takari.redditpostnotifier)

<img src="/preview/preview_gif.gif" align="right" width="32%"/>

## Tech stack & Open-source libraries
- Minimum SDK level 21
- Kotlin based
- Architecture
  - MVVM Architecture (View - ViewModel - Model)
  - Repository pattern
- [Dagger2](https://dagger.dev/) - handles dependency injection/management.
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit) - abstracts complexity of REST API communication.
  - [GSON Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/gson) - handles serialization and deserialization to and from JSON.
  - ~[Rx Call Adapter Factory](https://github.com/square/retrofit/tree/master/retrofit-adapters/rxjava2) - allows Retrofit to return RxJava Observables.~
- ~[RxJava](https://github.com/ReactiveX/RxJava) - used for composing asynchronous and event based operations. Also handles multi-threading.~
  - ~[RxKotlin](https://github.com/ReactiveX/RxKotlin) - adds new Kotlin extension functions.~
  - ~[RxAndroid](https://github.com/ReactiveX/RxAndroid) - gives Schedulers access to the MainThread.~
  - ~[RxBindings](https://github.com/JakeWharton/RxBinding) - Binds UI widgets to Observables.~
  - ~[RxRelay](https://github.com/JakeWharton/RxRelay) - adds Relays which are essentially Subjects without termination events.~
- [Coroutines]() - used for reducing the complexity of asynchronous code and concurrency.
- [Kotlin Flow]() - used for simplistic cold observables
- [Toasty](https://github.com/GrenderG/Toasty) - creates beautiful custom made Toast with background colors, icons, etc.
- [Room](https://developer.android.com/training/data-storage/room) - abstracts the complexity of managing SQL databases
- [Android KTX](https://developer.android.com/kotlin/ktx) - a set of Kotlin extensions.
- [Android-SpinKit](https://github.com/ybq/Android-SpinKit) - gives access to an array of loading views.
- [Lottie](https://airbnb.io/lottie/#/android) - gives access to many user created animations.
- [Fancy Buttons](https://github.com/medyo/fancybuttons) - gives access to beautiful custom made buttons.
- [Coil](https://github.com/coil-kt/coil) - a Kotlin focused image loading library.

## Architecture
Reddit Post Notifier is based on MVVM architecture and the Repository pattern. I omitted using Live Data since it's essentially just a Behavior Subject/Relay from RxJava.

![architecture](https://cdn-images-1.medium.com/max/1200/1*KnYBBZIDDeg4zVDDEcLw2A.png)

## Open API

<img src="https://b.thumbs.redditmedia.com/7GVLmrH9CdZeqXceSEWkmL8_DSUKRGUfwMxnUNh8D8A.png" align="right" width="15%"/>

Reddit Post Notifier uses [Reddit's Open API](https://www.reddit.com/dev/api/) for observing new post.

It's API enabled free access to highly detailed data related objects pertaining to subreddits, post, users, and comments.
