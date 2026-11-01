package io.github.yuroyami.buildplugins.appicons

import org.jetbrains.amper.plugins.ExecutionAvoidance
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.TaskAction
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories

/** The adaptive icon canvas at xxxhdpi: 108 dp at a scale of 4. minSdk 26 needs no other density. */
private const val ANDROID_SIZE = 432

private const val IOS_SIZE = 1024

/**
 * Draws the app icons from the two PNGs in art/.
 *
 * - Android: `mipmap-xxxhdpi/ic_launcher_foreground.png` and `ic_launcher_background.png`.
 *   `mipmap-anydpi-v26/ic_launcher.xml` names both.
 * - iOS: `AppIcon-1024.png`, opaque, because the App Store rejects an icon with alpha.
 *
 * Every path is an input with no inferred dependency, and the task has no outputs. So a build
 * never runs this task, and the command rewrites the committed files.
 */
@TaskAction(ExecutionAvoidance.Disabled)
fun syncAppIcons(
    @Input(inferTaskDependency = false) logoForeground: Path,
    @Input(inferTaskDependency = false) logoBackground: Path,
    @Input(inferTaskDependency = false) androidResources: Path,
    @Input(inferTaskDependency = false) iosAppIcon: Path,
    androidLogoPercent: Int,
    iosLogoPercent: Int,
) {
    System.setProperty("java.awt.headless", "true")
    val foreground = checkNotNull(ImageIO.read(logoForeground.toFile())) { "$logoForeground is not a PNG image." }
    val background = checkNotNull(ImageIO.read(logoBackground.toFile())) { "$logoBackground is not a PNG image." }
    // A foreground with no alpha fills the whole themed icon with one color.
    check(foreground.colorModel.hasAlpha()) { "$logoForeground needs a transparent background." }

    val mipmap = androidResources.resolve("mipmap-xxxhdpi")
    val androidMark = ANDROID_SIZE * androidLogoPercent / 100
    BufferedImage(ANDROID_SIZE, ANDROID_SIZE, BufferedImage.TYPE_INT_ARGB)
        .draw { drawCentered(foreground, androidMark, ANDROID_SIZE) }
        .writeTo(mipmap.resolve("ic_launcher_foreground.png"))
    BufferedImage(ANDROID_SIZE, ANDROID_SIZE, BufferedImage.TYPE_INT_RGB)
        .draw { fill(background, ANDROID_SIZE) }
        .writeTo(mipmap.resolve("ic_launcher_background.png"))

    BufferedImage(IOS_SIZE, IOS_SIZE, BufferedImage.TYPE_INT_RGB)
        .draw {
            fill(background, IOS_SIZE)
            drawCentered(foreground, IOS_SIZE * iosLogoPercent / 100, IOS_SIZE)
        }
        .writeTo(iosAppIcon.resolve("AppIcon-1024.png"))
    println("Wrote 3 icon files.")
}

private inline fun BufferedImage.draw(block: Graphics2D.() -> Unit): BufferedImage {
    val graphics = createGraphics()
    try {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.block()
    } finally {
        graphics.dispose()
    }
    return this
}

private fun BufferedImage.writeTo(file: Path) {
    file.parent.createDirectories()
    check(ImageIO.write(this, "PNG", file.toFile())) { "This JDK has no PNG encoder." }
}

/** Covers the [size] square. Transparency in [image] lands on white. */
private fun Graphics2D.fill(image: BufferedImage, size: Int) {
    color = Color.WHITE
    fillRect(0, 0, size, size)
    drawImage(image, 0, 0, size, size, null)
}

/** Draws [image] at [side] pixels in the middle of the [canvas] square. */
private fun Graphics2D.drawCentered(image: BufferedImage, side: Int, canvas: Int) {
    val offset = (canvas - side) / 2
    drawImage(image, offset, offset, side, side, null)
}
