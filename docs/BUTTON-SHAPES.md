# Button outline implementation

`ButtonOutlines.kt` contains normalized vector paths reconstructed from the six user-supplied visual references. Only their outer silhouettes are used; clock hands, dates, glyphs and photo backgrounds are excluded.

Flower, Clover, Pebble and Scallop follow smoothed samples of the reference contours. Leaf and Teardrop use cubic and quadratic curves fitted to their visible edges. Scallop’s bottom tip is cropped in the reference, so that small missing section follows the upper contour’s symmetry. The output is an approximation of the raster references, not an extraction of their interior artwork.

The paths scale to each button’s rectangle. Cached paths are copied before transformation so widgets at different sizes cannot modify one another. Circle and Squircle retain their previous implementations.

The shape catalog is shared by the picker and backup validation. Existing `flower` values use the replacement outline; default `circle` values and per-button independence are unchanged. The gallery in `assets/screenshots/button-shapes.png` is exported by the Android renderer test, not drawn by a separate documentation implementation.
