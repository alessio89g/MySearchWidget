# Widget library and system theme

The library lists saved configurations whose IDs are still bound to this provider. Reading a missing configuration does not save a default instance. Launcher preview or abandoned slots without a saved configuration are therefore excluded. An explicit configuration request can open a fresh ID, and a successful pin callback records a default widget without overwriting settings already saved for it.

Android exposes bound widget IDs, not the launcher's visible Home layout. A launcher can retain an old bound ID; the provider cannot independently prove whether it is still placed on a Home page. Such entries can be hidden manually.

Long-press a widget or imported template to enter selection mode, then tap additional entries. A single trash action at the top right asks for confirmation. Templates are deleted together in one storage transaction. Widget entries are hidden while their configurations remain available to the Home widgets. Removing a Home widget itself is the launcher's responsibility. Explicitly opening and saving its configuration makes a hidden entry visible again. Back or Cancel exits selection without deleting anything.

For System theme, RemoteViews receives both light and dark bitmap icons through the Android 12+ `setIcon(viewId, methodName, notNight, night)` API. The host resolves the appropriate image using its own configuration. Fixed Light and Dark modes retain one image. No configuration-change receiver, service, timer, worker or polling is needed for this switch. Existing widget lifecycle and package/wallpaper event handling remains event-driven, with `updatePeriodMillis="0"`.

Reference: https://developer.android.com/reference/android/widget/RemoteViews#setIcon(int,%20java.lang.String,%20android.graphics.drawable.Icon,%20android.graphics.drawable.Icon)
