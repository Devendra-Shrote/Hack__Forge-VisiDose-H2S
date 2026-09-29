package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ColorimetricEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VisiDose-H2S", appName)
  }

  @Test
  fun `colorimetric deltaE increases with dosage`() {
    val color0 = ColorimetricEngine.getInterpolatedStripColor(0f)
    val color50 = ColorimetricEngine.getInterpolatedStripColor(50f)
    val color200 = ColorimetricEngine.getInterpolatedStripColor(200f)

    val deltaE50 = ColorimetricEngine.calculateDeltaE(color50, color0)
    val deltaE200 = ColorimetricEngine.calculateDeltaE(color200, color0)

    assertTrue("Delta E for 200 ppm-h should exceed Delta E for 50 ppm-h", deltaE200 > deltaE50)
  }

  @Test
  fun `environmental factor compensation calculation`() {
    val kFactorStandard = ColorimetricEngine.computeEnvironmentalFactor(25f, 50f)
    assertEquals(1.0, kFactorStandard, 0.001)

    val kFactorHighTemp = ColorimetricEngine.computeEnvironmentalFactor(35f, 50f)
    assertTrue("High temp should yield higher diffusion factor", kFactorHighTemp > 1.0)
  }
}
