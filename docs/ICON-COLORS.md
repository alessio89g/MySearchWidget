# Icon color modes — 1.7.0

Added 2026-09-13.

In Appearance → Logo, or Appearance → Button N → Icon, the **Monochrome icon**
switch controls that element only. It is on for new configurations and defaults
to on when loading configurations/backups made before 1.7.0.

When switched off, imported bitmap colors and transparency are preserved. The
built-in Google/Chrome symbols use their native color palettes; Material vector
symbols keep their source fill colors (normally black). The switch does not
change the selected image, shape, button background or assigned action.

Original colors bypass both Material You tint and icon gradients. Manual color
and gradient controls are disabled, with a localized explanation, but all saved
values remain intact. Switching back restores the previous behavior.

The per-icon `monochrome` boolean is serialized with the widget and included in
backups/templates. The bitmap cache retains the untinted source so toggling one
slot cannot recolor another slot using the same imported image.

Older app versions do not recognize the new field in backups exported by 1.7.0;
update the receiving app before importing. Old backups remain importable.
