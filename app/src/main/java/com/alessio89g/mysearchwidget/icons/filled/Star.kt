/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alessio89g.mysearchwidget.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Star: ImageVector
    get() {
        if (_star != null) {
            return _star!!
        }
        _star = materialIcon(name = "Filled.Star") {
            materialPath {
                moveTo(12.0f, 17.27f)
                lineTo(18.18f, 21.0f)
                lineToRelative(-1.64f, -7.03f)
                lineTo(22.0f, 9.24f)
                lineToRelative(-7.19f, -0.61f)
                lineTo(12.0f, 2.0f)
                lineTo(9.19f, 8.63f)
                lineTo(2.0f, 9.24f)
                lineToRelative(5.46f, 4.73f)
                lineTo(5.82f, 21.0f)
                close()
            }
        }
        return _star!!
    }

private var _star: ImageVector? = null
