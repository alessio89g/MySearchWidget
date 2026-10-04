# Icon color modes

In **Appearance → Logo** or **Appearance → Button N → Icon**, the **Monochrome icon** switch controls that element only. It is enabled by default, including when a backup does not specify a color mode.

With monochrome mode disabled, imported bitmap colors and transparency are preserved. Google and Chrome symbols use their native palettes; Material vector symbols use their source fills, normally black. The switch does not change the image, shape, button background or action.

Original colors bypass Material You tint and icon gradients. Manual color and gradient controls are disabled with an explanation, while stored values remain available when monochrome mode is enabled again.

The per-icon `monochrome` value is saved with widget configurations and backups. The bitmap cache retains the untinted source so changing one slot cannot recolor another slot using the same image.

Icon sizes, positions, movement links and layer order are configured in **Layout**. Import backups using a version that supports their settings.
