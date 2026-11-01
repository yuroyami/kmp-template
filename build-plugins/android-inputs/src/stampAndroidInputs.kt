package io.github.yuroyami.buildplugins.androidinputs

import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.io.path.writeText

/** The resource that carries the stamp. It lands in the app's jar. */
private const val STAMP = "META-INF/android-inputs.sha256"

/**
 * Writes one SHA-256 over every file in [resDir], [manifest] and [proguardRules] to [STAMP] under [outputDir].
 *
 * The stamp is a generated resource of the app, so a change in any of those files changes the app's jar.
 * The next APK or AAB build then packages the new files.
 */
@OptIn(ExperimentalPathApi::class)
@TaskAction
fun stampAndroidInputs(
    @Input resDir: Path,
    @Input manifest: Path,
    @Input proguardRules: Path,
    @Output outputDir: Path,
) {
    val digest = MessageDigest.getInstance("SHA-256")
    for (root in listOf(resDir, manifest, proguardRules).filter { it.exists() }) {
        val files = if (root.isRegularFile()) sequenceOf(root) else root.walk().filter { it.isRegularFile() }
        for (file in files.sortedBy { it.invariantSeparatorsPathString }) {
            // The path takes part too, so a renamed or moved file also changes the stamp.
            digest.update(file.relativeTo(root.parent).invariantSeparatorsPathString.toByteArray())
            digest.update(file.readBytes())
        }
    }
    val hex = digest.digest().joinToString("") { "%02x".format(it) }
    outputDir.deleteRecursively()
    outputDir.resolve(STAMP).apply { parent.createDirectories() }.writeText("$hex\n")
    println("Android inputs stamp: $hex")
}
