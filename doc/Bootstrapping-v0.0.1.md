Bootstrapping
=============

This is a (very rough) document outlining my current understanding of how Pathway will bootstrap itself.

Steps
-----

#### Unpack natives

We need to unpack the LWJGL native libraries from our jarfile and link them onto the classpath.

This is currently implemented as the `unpackNatives` method on `Unpacker`.

It currently has the following signature:

```scala
def unpackNatives(destLocation: String, srcURL: URL): Try[Boolean]
```

This method currently works correctly. However, I have some thoughts on things we may want to change:

 + Change return type to `Try[Unit]`.

   Right now, we return a `Boolean` determining  whether or not the natives were unpacked. We return `true` if they were unpacked successfully, or `false` if they were not. However, we also return a `Try` with any errors that occurred.  Currently, we only return `Success(false)` if we could not write to the target directory. I don't see why we can't just return `Failure()` with an exception in that case, instead. If we changed the method signature to `Try[Unit]`, it would make it easier for other methods to succeed/fail on the result of this method, rather than having to check inside and see if we got a true or a false.

 + Change return type from `Try` to `Future`.

   Unpacking natives can take a little while, and it would be nice if we could run other tasks that don't require graphics natives in another thread. My understanding is that `java.nio` is already designed to be concurrency-friendly, so it wouldn't take much work to change the return type of this to `Future`.

If we institute both of these changes, the method signature would be changed to
```scala
def unpackNatives(destLocation: String, srcURL: URL): Future[Unit]
```

#### Create graphics API

This function would create whatever the graphics API object provided to JavaScript is. It obviously requires that the natives be unpacked, so we could `flatMap` it over the result of `unpackNatives()`.

To create the graphics API object, whatever it ends up looking like, we'll need to instantiate all the LWJGL graphics stuff. I'm not sure if we'd want to do it in this method, or separately; if we do it separately this action would also depend on that.

The signature will probably look something like:
```scala
def createGraphicsAPI: Future[GraphicsAPI]
// where `GraphicsAPI` is the type of whatever the graphics API object is.
```

We can make this depend on natives simply by saying:
```scala
val graphicsAPI: Future[GraphicsAPI]
  = Unpacker.unpackNatives() flatMap { _ => createGraphicsAPI() }
  // discarding the Unit return value from unpackNatives()
```

#### Create `ResourceManager`

This would just be instantiating a new `ResourceManager` for the game's FS. We might want to include a function for creating a `ResourceManager` that returns a `Future` so all of the FS traversal could be done asynchronously.

#### Create FS API

Once we have a `ResourceManager`, we'd need to create whatever wrapper/API object we provide to JavaScript.

The signature for this might be something like
```scala
def createFilesystemAPI(manager: ResourceManager): Future[FilesystemAPI]
// where `FilesystemAPI` is whatever type the FS API object is
```

#### Run  game's init script

Now that we have all the API objects and access to the FS,  we can find and run the game's init script and provide it with the appropriate APIs. This obviously depends on all of the JS-facing API objects.

This might look something like the following:
```scala
def init(graphics: GrapicsAPI, fs: FilesystemAPI): Future[ScriptMonad]
```

If there are other JS-facing API objects besides graphics and filesystem ones I mentioend above, such as some kind of eventual network/multiplayer API, those would also probably need to be provided to the init script.

Alternatively, we don't need to provide the init script with _all_ the Pathway JS API objects that the full game loop gets. if that's the case, we could be creating some of these objects in another thread while the init script is running. Whatever method starts the game loop could then depend on both the completion of the init scripts _and_ whatever other API methods the game loop receives. We could take the monadic state from executing the init script and, inject other APIs, and then pass that new monadic state to the game's main loop.

As Max and I have discussed on chat, this is a fairly complex process and will require rerunning the init script, to compensate for auto-updating. Here's what we have to do once we've created the base APIs:

 1. Use a predefined procedure (cooperating with resmangler?) to load the first stage of the game, maybe some sort of master init script that we allow to be fairly free-form. Here, we begin ceding control to the game author.
 2.  Re-init resmangler in order to build layers 2 and 3. This is important, because the game author could have installed a load order policy or a resmangler extension during master init.
 3.  Re-run master init (or maybe a different init script entirely?) on layer 2. We pick layer 2, so that updates that the game has just discovered after properly bringing up Resmangler can be observed, but a malicious script can't overwrite the init in the write dir on layer 3.

As far as applying updates, we determined:

> Ideally, updates to the init script would be able to make use of the magic that is resmangler layer 2, and be included in a zip update just like any other. But because load policy is configurable, we're going to have trouble resolving the correct way to *find* the update....

> What if resmangler init was special somehow? What if it were its own special script file, with a well defined procedure from us on how to update that file specifically? Like, the init script loads resmangler, and then tries to find itself, and if there's a new one, you reinit resmangler.

#### Load and apply mods

We will also want to seek out all non-core modules, such as mods, expansion packs, and Pathway libraries. This will probably look _sort of_ like the current `ModLoader` code, and depends on the `ResourceManager`. However, I'm not sure where exactly in the process this would have to run; I don't know if we want to do this before or after running the game's init script. I'm also not sure how much control the init script will have over the process, and how much will be controlled by Pathway. We need to work out more about how this will work.
