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

package androidx.compose.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.test.TestActivity
import androidx.compose.ui.unit.IntSize
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SmallTest
@RunWith(JUnit4::class)
class OnSizeChangedTest {

    @Suppress("DEPRECATION")
    @get:Rule
    val rule = androidx.test.rule.ActivityTestRule<TestActivity>(TestActivity::class.java)
    private lateinit var activity: TestActivity

    @Before
    fun setup() {
        activity = rule.activity
        activity.hasFocusLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun normalSizeChange() {
        var latch = CountDownLatch(1)
        var changedSize = IntSize.Zero
        var sizePx by mutableStateOf(10)

        rule.runOnUiThread {
            activity.setContent {
                with (DensityAmbient.current) {
                    Box(Modifier.padding(10.toDp()).onSizeChanged {
                        changedSize = it
                        latch.countDown()
                    }) {
                        Box(Modifier.size(sizePx.toDp()))
                    }
                }
            }
        }

        // Initial setting will call onSizeChanged
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(10, changedSize.height)
        assertEquals(10, changedSize.width)

        latch = CountDownLatch(1)
        sizePx = 20

        // We've changed the size of the contents, so we should receive a onSizeChanged call
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(20, changedSize.height)
        assertEquals(20, changedSize.width)
    }

    @Test
    fun onlyInnerSizeChange() {
        var latch = CountDownLatch(1)
        var changedSize = IntSize.Zero
        var sizePx by mutableStateOf(10)

        rule.runOnUiThread {
            activity.setContent {
                with (DensityAmbient.current) {
                    Box(Modifier.padding(sizePx.toDp()).onSizeChanged {
                        changedSize = it
                        latch.countDown()
                    }) {
                        Box(Modifier.size(10.toDp()))
                    }
                }
            }
        }

        // Initial setting will call onSizeChanged
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(10, changedSize.height)
        assertEquals(10, changedSize.width)

        latch = CountDownLatch(1)
        sizePx = 5

        // We've changed the padding, but the size of the contents didn't change
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS))
    }

    @Test
    fun layoutButNoSizeChange() {
        var latch = CountDownLatch(1)
        var changedSize = IntSize.Zero
        var sizePx by mutableStateOf(10)

        rule.runOnUiThread {
            activity.setContent {
                with (DensityAmbient.current) {
                    Box(Modifier.padding(10.toDp()).onSizeChanged {
                        changedSize = it
                        latch.countDown()
                    }) {
                        Box(Modifier.size(sizePx.toDp()))
                    }
                }
            }
        }

        // Initial setting will call onSizeChanged
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(10, changedSize.height)
        assertEquals(10, changedSize.width)

        latch = CountDownLatch(1)
        rule.runOnUiThread {
            sizePx = 20
            sizePx = 10
        }

        // We've triggered a layout, but the size didn't change.
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS))
    }

    @Test
    fun addedModifier() {
        var latch1 = CountDownLatch(1)
        var latch2 = CountDownLatch(1)
        var changedSize1 = IntSize.Zero
        var changedSize2 = IntSize.Zero
        var addModifier by mutableStateOf(false)

        rule.runOnUiThread {
            activity.setContent {
                with (DensityAmbient.current) {
                    val mod = if (addModifier) Modifier.onSizeChanged {
                        changedSize2 = it
                        latch2.countDown()
                    } else Modifier
                    Box(Modifier.padding(10.toDp()).onSizeChanged {
                        changedSize1 = it
                        latch1.countDown()
                    }.then(mod)) {
                        Box(Modifier.size(10.toDp()))
                    }
                }
            }
        }

        // Initial setting will call onSizeChanged
        assertTrue(latch1.await(1, TimeUnit.SECONDS))
        assertEquals(10, changedSize1.height)
        assertEquals(10, changedSize1.width)

        latch1 = CountDownLatch(1)
        addModifier = true

        // We've added an onSizeChanged modifier, so it must trigger another size change
        // notification, but only for the new one.
        assertTrue(latch2.await(1, TimeUnit.SECONDS))
        assertEquals(10, changedSize2.height)
        assertEquals(10, changedSize2.width)
        assertFalse(latch1.await(200, TimeUnit.MILLISECONDS))
    }
}