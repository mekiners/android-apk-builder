package com.example.sampleapp.ui.theme

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.sampleapp.R
import kotlin.math.roundToInt

/** Pixel-art texture tiles and decorations (PNG files in res/drawable-nodpi), loaded once. */
class Textures(
    val parchment: ImageBitmap, val parchmentDark: ImageBitmap, val beige: ImageBitmap,
    val wood: ImageBitmap, val woodDark: ImageBitmap, val cork: ImageBitmap,
    val grass: ImageBitmap, val soil: ImageBitmap, val stone: ImageBitmap, val slate: ImageBitmap,
    val lava: ImageBitmap, val rock: ImageBitmap, val honey: ImageBitmap, val honeyFrame: ImageBitmap,
    val leafFrame: ImageBitmap, val wall: ImageBitmap,
    val bolt: ImageBitmap, val drips: ImageBitmap, val mossTl: ImageBitmap, val mossBr: ImageBitmap,
    val vineA: ImageBitmap, val vineB: ImageBitmap, val vineSideL: ImageBitmap, val vineSideR: ImageBitmap
)

@Composable
fun rememberTextures(): Textures {
    val context = LocalContext.current
    return remember {
        val res = context.resources
        fun load(@DrawableRes id: Int): ImageBitmap {
            val opts = BitmapFactory.Options().apply { inScaled = false }
            return BitmapFactory.decodeResource(res, id, opts).asImageBitmap()
        }
        Textures(
            parchment = load(R.drawable.tex_parchment),
            parchmentDark = load(R.drawable.tex_parchment_dark),
            beige = load(R.drawable.tex_beige),
            wood = load(R.drawable.tex_wood),
            woodDark = load(R.drawable.tex_wood_dark),
            cork = load(R.drawable.tex_cork),
            grass = load(R.drawable.tex_grass),
            soil = load(R.drawable.tex_soil),
            stone = load(R.drawable.tex_stone),
            slate = load(R.drawable.tex_slate),
            lava = load(R.drawable.tex_lava),
            rock = load(R.drawable.tex_rock),
            honey = load(R.drawable.tex_honey),
            honeyFrame = load(R.drawable.tex_honey_frame),
            leafFrame = load(R.drawable.tex_leaf_frame),
            wall = load(R.drawable.tex_wall),
            bolt = load(R.drawable.deco_bolt),
            drips = load(R.drawable.deco_drips),
            mossTl = load(R.drawable.deco_moss_tl),
            mossBr = load(R.drawable.deco_moss_br),
            vineA = load(R.drawable.deco_vine_a),
            vineB = load(R.drawable.deco_vine_b),
            vineSideL = load(R.drawable.deco_vine_side_l),
            vineSideR = load(R.drawable.deco_vine_side_r)
        )
    }
}

val LocalTextures = staticCompositionLocalOf<Textures> { error("Textures not provided") }

/** How a framed panel looks: frame texture around an interior texture. */
class PanelLook(
    val frame: ImageBitmap,
    val interior: ImageBitmap,
    val ring: Dp = 4.dp,
    val studs: Boolean = true
) {
    fun withInterior(i: ImageBitmap) = PanelLook(frame, i, ring, studs)
}

val LocalPanelLook = compositionLocalOf<PanelLook> { error("PanelLook not provided") }

fun Textures.woodLook() = PanelLook(wood, parchment, 4.dp, true)
fun Textures.stoneLook() = PanelLook(slate, parchment, 5.dp, false)

enum class ScreenKind { HOME, QUESTS, SHOP, MARKET, SHIFTS, GYM }

// ------------------------------------------------------------------ drawing helpers

fun DrawScope.pxScale(dp: Dp): Int = dp.toPx().roundToInt().coerceAtLeast(1)

/** Repeats a tile (anchored at the top-left of this drawing area) across the given rectangle. */
fun DrawScope.drawTiled(img: ImageBitmap, scale: Int, left: Float, top: Float, right: Float, bottom: Float) {
    if (right <= left || bottom <= top) return
    val tw = img.width * scale
    val th = img.height * scale
    clipRect(left, top, right, bottom) {
        var y = (top / th).toInt() * th
        while (y < bottom) {
            var x = (left / tw).toInt() * tw
            while (x < right) {
                drawImage(
                    image = img,
                    dstOffset = IntOffset(x, y),
                    dstSize = IntSize(tw, th),
                    filterQuality = FilterQuality.None
                )
                x += tw
            }
            y += th
        }
    }
}

fun DrawScope.drawPixels(img: ImageBitmap, x: Float, y: Float, scale: Int) {
    drawImage(
        image = img,
        dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
        dstSize = IntSize(img.width * scale, img.height * scale),
        filterQuality = FilterQuality.None
    )
}

fun Modifier.tiledBackground(img: ImageBitmap, pixel: Dp = 1.dp): Modifier = drawBehind {
    drawTiled(img, pxScale(pixel), 0f, 0f, size.width, size.height)
}

/** Light top/left edge and dark bottom/right edge, drawn just inside a border. */
fun Modifier.bevel(inset: Dp = 3.dp, thickness: Dp = 1.5.dp): Modifier = drawBehind {
    val i = inset.toPx()
    val t = thickness.toPx()
    val w = (size.width - 2 * i).coerceAtLeast(0f)
    val h = (size.height - 2 * i).coerceAtLeast(0f)
    drawRect(Color(0x40FFFFFF), Offset(i, i), Size(w, t))
    drawRect(Color(0x40FFFFFF), Offset(i, i), Size(t, h))
    drawRect(Color(0x45000000), Offset(i, size.height - i - t), Size(w, t))
    drawRect(Color(0x45000000), Offset(size.width - i - t, i), Size(t, h))
}

/** Build the clipped, stepped silhouette used by the hand-cut pixel-art panels. */
private fun cutCornerPath(left: Float, top: Float, right: Float, bottom: Float, cut: Float): Path {
    val c = cut.coerceAtMost(((right - left).coerceAtLeast(0f) / 3f)).coerceAtMost(((bottom - top).coerceAtLeast(0f) / 3f))
    return Path().apply {
        moveTo(left + c, top)
        lineTo(right - c, top)
        lineTo(right, top + c)
        lineTo(right, bottom - c)
        lineTo(right - c, bottom)
        lineTo(left + c, bottom)
        lineTo(left, bottom - c)
        lineTo(left, top + c)
        close()
    }
}

/**
 * Handcrafted frame: dark stepped outline, material-textured rim, narrow inner shadow,
 * and a softly textured center. The optional overlay adds honey or moss details.
 */
fun Modifier.panelChrome(
    look: PanelLook,
    tex: Textures,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null
): Modifier = drawBehind {
    val pixel = pxScale(1.dp)
    val w = size.width
    val h = size.height
    val outline = 2.dp.toPx()
    val ring = look.ring.toPx().coerceAtLeast(3.dp.toPx())
    val corner = 5.dp.toPx()

    val outer = cutCornerPath(0f, 0f, w, h, corner)
    drawPath(outer, StardewWoodDark)

    val framePath = cutCornerPath(outline, outline, w - outline, h - outline, corner - outline / 2f)
    clipPath(framePath) {
        drawTiled(look.frame, pixel, outline, outline, w - outline, h - outline)
        // Lit upper edge and shaded lower edge give the material a restrained 3D read.
        drawRect(Color(0x42FFFFFF), Offset(outline, outline), Size(w - 2 * outline, 1.2.dp.toPx()))
        drawRect(Color(0x32FFFFFF), Offset(outline, outline), Size(1.2.dp.toPx(), h - 2 * outline))
        drawRect(Color(0x40000000), Offset(outline, h - outline - 1.5.dp.toPx()), Size(w - 2 * outline, 1.5.dp.toPx()))
        drawRect(Color(0x35000000), Offset(w - outline - 1.5.dp.toPx(), outline), Size(1.5.dp.toPx(), h - 2 * outline))
    }

    val innerInset = outline + ring
    val innerLine = 1.2.dp.toPx()
    val centerPath = cutCornerPath(innerInset, innerInset, w - innerInset, h - innerInset, (corner - outline - ring / 2f).coerceAtLeast(1.dp.toPx()))
    drawPath(centerPath, StardewWoodDark)
    val contentInset = innerInset + innerLine
    val contentPath = cutCornerPath(contentInset, contentInset, w - contentInset, h - contentInset, (corner - outline - ring / 2f - innerLine).coerceAtLeast(1.dp.toPx()))
    clipPath(contentPath) {
        drawTiled(look.interior, pixel, contentInset, contentInset, w - contentInset, h - contentInset)
        drawRect(Color(0x18FFFFFF), Offset(contentInset, contentInset), Size(w - 2 * contentInset, 1.dp.toPx()))
    }

    if (look.studs) {
        val studScale = pxScale(0.65.dp)
        val studW = tex.bolt.width * studScale
        val studH = tex.bolt.height * studScale
        val c = outline + ring / 2f
        drawPixels(tex.bolt, c - studW / 2f, c - studH / 2f, studScale)
        drawPixels(tex.bolt, w - c - studW / 2f, c - studH / 2f, studScale)
        drawPixels(tex.bolt, c - studW / 2f, h - c - studH / 2f, studScale)
        drawPixels(tex.bolt, w - c - studW / 2f, h - c - studH / 2f, studScale)
    }
    overlay?.invoke(this, w, h, pixel)
}

/** Each screen gets a background that fits it: grass, tilled soil, shop wallpaper, wood, cobblestone. */
fun Modifier.screenBackground(kind: ScreenKind, tex: Textures): Modifier = drawBehind {
    val img = when (kind) {
        ScreenKind.HOME -> tex.grass
        ScreenKind.QUESTS -> tex.soil
        ScreenKind.SHOP -> tex.wall
        ScreenKind.MARKET -> tex.wood
        ScreenKind.SHIFTS -> tex.woodDark
        ScreenKind.GYM -> tex.stone
    }
    drawTiled(img, pxScale(1.dp), 0f, 0f, size.width, size.height)
    drawRect(
        Brush.verticalGradient(
            0f to Color(0x18000000),
            0.12f to Color(0x00000000),
            0.88f to Color(0x00000000),
            1f to Color(0x22000000),
            startY = 0f,
            endY = size.height
        )
    )
}

/** Hanging vines at the screen edges (drawn over the content, so they overlap the signboard a little). */
@Composable
fun VineOverlay(kind: ScreenKind) {
    val tex = LocalTextures.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val vs = pxScale(1.dp)
        when (kind) {
            ScreenKind.HOME -> {
                drawPixels(tex.vineA, (-10).dp.toPx(), 0f, vs)
                drawPixels(tex.vineB, size.width - tex.vineB.width * vs + 8.dp.toPx(), 0f, vs)
            }
            ScreenKind.GYM -> {
                val wl = tex.vineSideL.width * vs
                drawPixels(tex.vineSideL, (-8).dp.toPx(), size.height * 0.16f, vs)
                drawPixels(tex.vineSideL, (-8).dp.toPx(), size.height * 0.56f, vs)
                drawPixels(tex.vineSideR, size.width - wl + 8.dp.toPx(), size.height * 0.04f, vs)
                drawPixels(tex.vineSideR, size.width - wl + 8.dp.toPx(), size.height * 0.40f, vs)
            }
            else -> Unit
        }
    }
}
