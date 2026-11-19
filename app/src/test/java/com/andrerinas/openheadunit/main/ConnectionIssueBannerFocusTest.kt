package com.andrerinas.openheadunit.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Xml
import android.view.ContextThemeWrapper
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.ColorUtils
import com.andrerinas.openheadunit.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [28])
class ConnectionIssueBannerFocusTest {
    @Test
    @Config(qualifiers = "w428dp-h240dp-land-notnight-mdpi")
    fun `close button focus has a high contrast edge in day mode`() = assertVisibleFocus()

    @Test
    @Config(qualifiers = "w428dp-h240dp-land-night-mdpi")
    fun `close button focus has a high contrast edge in night mode`() = assertVisibleFocus()

    private fun assertVisibleFocus() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.AppTheme)
        // Use the real layout, but do not start the main activity's connection stack.
        val (bannerBackground, background) = context.resources.getLayout(R.layout.activity_main).use { parser ->
            var bannerBackground: Drawable? = null
            var buttonBackground: Drawable? = null
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    val attrs = Xml.asAttributeSet(parser)
                    when (attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "id", 0)) {
                        R.id.connection_issue_banner -> bannerBackground = LinearLayout(context, attrs).background
                        R.id.connection_issue_dismiss -> {
                            buttonBackground = AppCompatImageButton(context, attrs).background
                            break
                        }
                    }
                }
                parser.next()
            }
            requireNotNull(bannerBackground) to requireNotNull(buttonBackground)
        }
        val surface = draw(bannerBackground, 396, 120, 0).getPixel(198, 60)
        background.state = intArrayOf(android.R.attr.state_enabled)
        background.jumpToCurrentState()
        val normalEdge = draw(background, 32, 32, surface).getPixel(1, 16)
        background.state = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
        background.jumpToCurrentState()
        val focusedEdge = draw(background, 32, 32, surface).getPixel(1, 16)

        // Measure a persistent focus edge, without relying on a pressed-state flash or the X icon.
        assertTrue("Focus must contrast with the banner", ColorUtils.calculateContrast(focusedEdge, surface) >= 3.0)
        assertTrue("Focus must differ from the idle button", ColorUtils.calculateContrast(focusedEdge, normalEdge) >= 3.0)
    }

    private fun draw(drawable: Drawable, width: Int, height: Int, surface: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(surface)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}
