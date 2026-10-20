# MySearchWidget

> [!NOTE]
> **The entire project was created using vibe coding. There is no human-written code in this repository.**

**English** · [Italiano](README.it.md) · [Download APK](https://github.com/alessio89g/MySearchWidget/releases)

An Android search widget inspired by the appearance of the **Google Circle to Search** bar. Customize search, actions and appearance through a **Material 3 Expressive** configurator with a live preview. Each widget has independent settings.

![Default MySearchWidget in dark mode, with voice search, Chrome and incognito buttons](assets/screenshots/widget-default-dark-en-capture.png)

## Getting started

Requires **Android 12 or later** and a launcher that supports widgets.

1. Install the APK from [Releases](https://github.com/alessio89g/MySearchWidget/releases).
2. Open the app and tap **Add widget**, or use your launcher's widget picker.
3. Confirm placement, customize the widget and tap **Save**.

To edit it, select it on the app's home screen or open its configuration from the launcher. The **home icon** next to Save returns to the app's home screen. Changes remain in the preview until saved; discarding them requires confirmation.

The interface starts in English. The **EN / IT** button switches languages immediately and remembers your choice.

## What you can customize

| Section | Controls |
| --- | --- |
| **Layout** | Height and width, spacing, individual element positions using arrows or numeric values, aspect-ratio locks, movement links and layer order. **0 to 3 action buttons**. |
| **Appearance** | Light, dark or system theme; Material You; colors, opacity and two-color gradients with any direction; button shapes; built-in icons, app icons, icon packs or imported images, monochrome or in their original colors. |
| **Actions** | Built-in actions, app launches and shortcuts for the logo and buttons. Separate haptic feedback for the search field, logo and each button, enabled by default. |
| **Search** | Built-in or custom search engines, Google app search, placeholder text and typed-query styling. |
| **Links** | Shared or separate light/dark settings, per element and feature. |
| **Backup** | Export configurations and import them as reusable templates, including fonts and images. |

**Appearance.** Button shapes include circle/square, squircle, flower, clover, leaf, pebble, scallop and teardrop. The color picker combines a graphical area, sliders, HEX/RGB codes and presets.

For the logo and buttons, choose built-in icons, images, original app icons or icon packs. Selecting a compatible pack opens its own picker; alternatively, browse its available catalogue in the app. Private launcher customizations are not read automatically: choose the same pack and icon to reproduce them.

Outline, monochrome tint and gradients also apply to imported icons. Monochrome mode preserves tonal detail; **Keep outer outline**, off by default, lets you include the background border in the outline. When excluded, an identifiable uniform background is removed; already transparent symbols retain their outline. Manual tints require Material You to be off and monochrome mode to be on.

SVG icons can be imported from a file. Their original artwork and transparency are preserved on import. Enable Outline to trace filled shapes, or leave it off for icons that already have outline strokes; monochrome mode applies the selected colour or gradient. The SVG file is included in backups.

**Actions.** Shortcuts are grouped by app. For dynamic shortcuts, use temporary access as the Home app: select and save the shortcut, then tap **Restore your launcher**. Pinned shortcuts remain usable; reinstalling or changing devices may require selecting them again. Availability depends on installed apps.

When choosing an app or shortcut, **Icon for this action** lets you keep the current icon, use the app icon or choose one from a pack. The icon follows its own light/dark link, independently of the action’s link.

**Search.** Text supports imported fonts, size, weight, bold, italic, underline, strikethrough, colors and gradients. Format individual parts of the placeholder using the visual editor or **BBCode**; query styling applies to all typed text.

The preview can use a solid background or your wallpaper and switch between light and dark. **Undo/Redo** steps through edits. **Reset** commands restore individual options to the installed version's defaults without automatically changing existing saved widgets.

## How the widget behaves

- **Initial configuration:** 68 dp height, system theme, a full-color Google logo and three buttons for voice search, Chrome and incognito browsing. Material You and opening Google search from the field are off.
- **Automatic text:** leave the placeholder empty to display “Search with…” followed by the selected engine, including custom engines. Custom placeholder text stays unchanged; switching engines preserves formatting.
- **Search:** queries open in your browser using the chosen engine. Alternatively, tapping the field opens search in the Google app; search-engine controls are then disabled, but their settings are retained.
- **Colors:** Material You disables manual color and gradient controls. Icons using original colors ignore custom tint. Your manual choices remain stored.
- **Light/dark:** settings are shared by default, except colors and gradients. Separate them in **Links**; the configurator indicates which mode you are editing.
- **Resizing:** narrowing the widget adapts the search field to the available width while retaining height and margins, subject to custom settings. Elements can overlap or extend outside the visible area. If the launcher provides insufficient vertical space, the entire composition scales down.

## Backup

A backup is a single JSON file containing settings, imported fonts and the images in use. No separate files or installed icon pack are needed to restore them. Wallpaper is not included; apps assigned to actions must be installed to launch them.

Imported backups become **templates**: applying one to a widget is a separate step. Long-press a widget or template to enable multiple selection, then use the **trash button in the top-right corner** and confirm. This deletes selected templates, but only hides widget entries without removing their Home screen widgets or settings. Use your launcher to remove a widget from the Home screen.

## Updates and permissions

The configurator checks GitHub releases when opened and offers direct APK download and installation, checking the package, version and signature. It does not run periodic checks or keep persistent background services. Configuration remains available offline.

Wallpaper preview is optional and may require image or all-files access, depending on the system. Without access, the solid background remains; wallpaper is not included in backups. Android may also request permission to install updates. Searches and voice actions are handled by external apps; MySearchWidget does not request microphone permission.

Sizing, shortcuts and Google search behavior also depend on your launcher and installed apps. **This project is not affiliated with Google and does not implement Circle to Search.**

## Source and license

See the [build instructions](docs/BUILD.md), [validation notes](docs/TESTING.md) and [changelog](CHANGELOG.md).

The [MySearchWidget Non-Commercial Source-Available License 1.1](LICENSE) permits **non-commercial** use, modification, forks and redistribution:

- Every redistribution must retain the license and notices and credit the [original repository](https://github.com/alessio89g/MySearchWidget).
- For **unmodified versions**, the link in the credits is sufficient: source delivery is not required.
- For **modified versions**, distribute the complete corresponding source and build instructions, retain the same license and document your changes.

The full license text governs. Third-party fonts, icons and other assets retain their own licenses: see [Credits](CREDITS.md).
