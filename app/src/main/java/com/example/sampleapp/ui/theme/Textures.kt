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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.sampleapp.R
import kotlin.math.roundToInt

/** Pixel-art material atlas and small decorative sprites, loaded once per app process. */
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

/** Screen order intentionally matches the six bottom-navigation destinations. */
enum class ScreenKind { HOME, QUESTS, SHOP, MARKET, SHIFTS, GYM }

/** Material frame and interior textures are separate so each surface has its own character. */
class PanelLook(
    val frame: ImageBitmap,
    val interior: ImageBitmap,
    val ring: Dp = 3.dp,
    val studs: Boolean = false
) {
    fun withInterior(i: ImageBitmap) = PanelLook(frame, i, ring, studs)
}

val LocalPanelLook = compositionLocalOf<PanelLook> { error("PanelLook not provided") }

fun Textures.forestLook() = PanelLook(leafFrame, parchment, 3.dp, false)
fun Textures.woodLook() = PanelLook(wood, parchment, 3.dp, false)
fun Textures.stoneLook() = PanelLook(slate, parchment, 3.dp, false)
fun Textures.corkLook() = PanelLook(cork, parchment, 3.dp, false)

// ------------------------------------------------------------------ drawing helpers

fun DrawScope.pxScale(dp: Dp): Int = dp.toPx().roundToInt().coerceAtLeast(1)

/** Texture tiles are deliberately larger and softly sampled; character sprites remain pixel-crisp. */
fun DrawScope.drawTiled(img: ImageBitmap, scale: Int, left: Float, top: Float, right: Float, bottom: Float) {
    if (right <= left || bottom <= top) return
    val tw = img.width * scale
    val th = img.height * scale
    if (tw <= 0 || th <= 0) return
    clipRect(left, top, right, bottom) {
        var y = (top / th).toInt() * th
        while (y < bottom) {
            var x = (left / tw).toInt() * tw
            while (x < right) {
                drawImage(
                    image = img,
                    dstOffset = IntOffset(x, y),
                    dstSize = IntSize(tw, th),
                    filterQuality = FilterQuality.Medium
                )
                x += tw
            }
            y += th
        }
    }
}

/** Sprite rendering stays nearest-neighbour so original pixel icons keep their exact silhouettes. */
fun DrawScope.drawPixels(img: ImageBitmap, x: Float, y: Float, scale: Int, alpha: Float = 1f) {
    drawImage(
        image = img,
        dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
        dstSize = IntSize(img.width * scale, img.height * scale),
        alpha = alpha.coerceIn(0f, 1f),
        filterQuality = FilterQuality.None
    )
}

fun Modifier.tiledBackground(img: ImageBitmap, pixel: Dp = 2.dp): Modifier = drawBehind {
    drawTiled(img, pxScale(pixel), 0f, 0f, size.width, size.height)
}

/** Soft material framing: thin ink edge, textured rim, warm interior and restrained light/shadow. */
fun Modifier.panelChrome(
    look: PanelLook,
    tex: Textures,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null
): Modifier = drawBehind {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@drawBehind

    val outline = 1.2.dp.toPx()
    val ring = look.ring.toPx().coerceAtLeast(2.dp.toPx())
    val inner = outline + ring
    val edge = Color(0xFF493628)

    drawRect(edge, Offset.Zero, Size(w, h))
    drawTiled(look.frame, pxScale(1.dp), outline, outline, w - outline, h - outline)
    drawRect(Color(0x1FFFFFFF), Offset(outline, outline), Size(w - outline * 2, 1.dp.toPx()))
    drawRect(Color(0x22000000), Offset(outline, h - outline - 1.dp.toPx()), Size(w - outline * 2, 1.dp.toPx()))

    // The interior has a very light paper wash so text remains readable over the texture.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF7E5), Color(0xFFF1E3C6), Color(0xFFE9D7B5)),
            startY = inner,
            endY = h - inner
        ),
        topLeft = Offset(inner, inner),
        size = Size((w - inner * 2).coerceAtLeast(0f), (h - inner * 2).coerceAtLeast(0f))
    )
    drawTiled(look.interior, pxScale(1.dp), inner, inner, w - inner, h - inner)
    drawRect(Color(0x22FFF8E7), Offset(inner, inner), Size(w - inner * 2, 1.dp.toPx()))
    drawRect(Color(0x18000000), Offset(inner, h - inner - 1.dp.toPx()), Size(w - inner * 2, 1.dp.toPx()))

    if (look.studs) {
        val radius = 1.5.dp.toPx()
        val inset = (outline + ring / 2f)
        val centers = listOf(
            Offset(inset, inset), Offset(w - inset, inset),
            Offset(inset, h - inset), Offset(w - inset, h - inset)
        )
        centers.forEach { c ->
            drawCircle(Color(0xFF5A4634), radius, c)
            drawCircle(Color(0x55FFF3D8), radius * 0.35f, Offset(c.x - radius * 0.28f, c.y - radius * 0.28f))
        }
    }
    overlay?.invoke(this, w, h, pxScale(1.dp))
}

/** Distinct screen grounds; richer tile scale reduces obvious repetition without blurring sprites. */
fun Modifier.screenBackground(kind: ScreenKind, tex: Textures): Modifier = drawBehind {
    val (img, top, bottom) = when (kind) {
        ScreenKind.HOME -> Triple(tex.grass, Color(0xFF75905D), Color(0xFF3F6848))
        ScreenKind.QUESTS -> Triple(tex.soil, Color(0xFF9B7951), Color(0xFF6D5138))
        ScreenKind.SHOP -> Triple(tex.wall, Color(0xFF57918B), Color(0xFF356E69))
        ScreenKind.MARKET -> Triple(tex.cork, Color(0xFFC59A67), Color(0xFF9D744C))
        ScreenKind.SHIFTS -> Triple(tex.wood, Color(0xFF896344), Color(0xFF60442F))
        ScreenKind.GYM -> Triple(tex.stone, Color(0xFF78816C), Color(0xFF4B5A4D))
    }
    drawRect(Brush.verticalGradient(listOf(top, bottom), startY = 0f, endY = size.height))
    drawTiled(img, pxScale(1.dp), 0f, 0f, size.width, size.height)
    drawRect(Color(0x160F1A12))
}

/** Decorative vines sit behind screen content and only frame the outer edges. */
@Composable
fun VineOverlay(kind: ScreenKind) {
    val tex = LocalTextures.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val vs = pxScale(1.dp)
        when (kind) {
            ScreenKind.HOME -> {
                drawPixels(tex.vineA, (-5).dp.toPx(), 0f, vs, alpha = 0.82f)
                drawPixels(tex.vineB, size.width - tex.vineB.width * vs + 5.dp.toPx(), 0f, vs, alpha = 0.82f)
            }
            ScreenKind.GYM -> {
                val wl = tex.vineSideL.width * vs
                drawPixels(tex.vineSideL, (-10).dp.toPx(), size.height * 0.12f, vs, alpha = 0.58f)
                drawPixels(tex.vineSideR, size.width - wl + 10.dp.toPx(), size.height * 0.06f, vs, alpha = 0.58f)
            }
            else -> Unit
        }
    }
}
