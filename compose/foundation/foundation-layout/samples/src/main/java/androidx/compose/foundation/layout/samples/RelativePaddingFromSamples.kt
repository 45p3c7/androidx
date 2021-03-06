/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.foundation.layout.samples

import androidx.annotation.Sampled
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.relativePaddingFrom
import androidx.compose.foundation.text.FirstBaseline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.sp

@Sampled
@Composable
fun RelativePaddingFromSample() {
    // We want to have 30.sp distance from the top of the layout box to the baseline of the
    // first line of text.
    val distanceToBaseline = 30.sp
    // We convert the 30.sp value to dps, which is required for the relativePaddingFrom API.
    val distanceToBaselineDp = with(DensityAmbient.current) { distanceToBaseline.toDp() }
    // The result will be a layout with 30.sp distance from the top of the layout box to the
    // baseline of the first line of text.
    Text(
        text = "This is an example.",
        modifier = Modifier.relativePaddingFrom(FirstBaseline, before = distanceToBaselineDp)
    )
}