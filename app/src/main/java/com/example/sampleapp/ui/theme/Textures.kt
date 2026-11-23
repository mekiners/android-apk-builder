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

/** Pixel-art texture tiles and decorations (PNG files in res/drawable-nodpi), loaded once. */
class Textures(
    val parchment: ImageBitmap, val parchmentDark: ImageBitmap, val beige: ImageBitmap,
    val wood: ImageBitmap, val woodDark: ImageBitmap, val cork: ImageBitmap,
    val grass: ImageBitmap, val soil: ImageBitmap, val stone: ImageBitmap, val slate: ImageBitmap,
    val lava: ImageBitmap, val rock: ImageBitmap, val honey: ImageBitmap, val honeyFrame: ImageBitmap,
    val leafFrame: ImageBitmap, val wall: ImageBitmap,
    val bolt: ImageBitmap, val drips: ImageBitmap, val mossTl: ImageBitmap, val mossBr: ImageBitmap,
    val vineA: ImageBitmap, val vineB: ImageBitmap, val vineSideL: ImageBitmap, val vineSideR: ImageBitmap,
    val art: ArtAssets
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
            vineSideR = load(R.drawable.deco_vine_side_r),
            art = loadArt(context)
        )
    }
}

val LocalTextures = staticCompositionLocalOf<Textures> { error("Textures not provided") }

// ------------------------------------------------------------------ user-supplied art (optional)

/** An image with fixed corner insets (source pixels) so it can be stretched to any size. */
class NineSlice(val image: ImageBitmap, val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Hand-made art that the user generated. Each piece is looked up by file name in res/drawable-nodpi
 * (art_*.webp or .png); anything missing is null and the app falls back to the built-in drawn art.
 */
class ArtAssets(
    val panelParchment: NineSlice?, val panelStone: NineSlice?,
    val cardEasy: NineSlice?, val cardMedium: NineSlice?, val cardHard: NineSlice?,
    val banner: NineSlice?,
    val honey: ImageBitmap?, val dock: ImageBitmap?, val cat: ImageBitmap?,
    val bgHome: ImageBitmap?, val bgGym: ImageBitmap?, val bgMarket: ImageBitmap?,
    val bgShop: ImageBitmap?, val bgQuests: ImageBitmap?
)

private fun loadOptional(context: android.content.Context, name: String): ImageBitmap? {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    if (id == 0) return null
    return try {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        BitmapFactory.decodeResource(context.resources, id, opts)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

private fun loadArt(context: android.content.Context): ArtAssets {
    fun slice(name: String, l: Int, t: Int, r: Int, b: Int): NineSlice? =
        loadOptional(context, name)?.let { NineSlice(it, l, t, r, b) }
    return ArtAssets(
        panelParchment = slice("art_panel_parchment", 70, 70, 70, 70),
        panelStone = slice("art_panel_stone", 85, 85, 85, 85),
        cardEasy = slice("art_card_easy", 100, 0, 100, 0),
        cardMedium = slice("art_card_medium", 70, 0, 70, 0),
        cardHard = slice("art_card_hard", 170, 0, 170, 0),
        banner = slice("art_banner", 70, 0, 70, 0),
        honey = loadOptional(context, "art_honey"),
        dock = loadOptional(context, "art_dock"),
        cat = loadOptional(context, "art_cat"),
        bgHome = loadOptional(context, "art_bg_home"),
        bgGym = loadOptional(context, "art_bg_gym"),
        bgMarket = loadOptional(context, "art_bg_market"),
        bgShop = loadOptional(context, "art_bg_shop"),
        bgQuests = loadOptional(context, "art_bg_quests")
    )
}

/** How a framed panel looks: frame texture around an interior texture, or a stretchable piece of art. */
class PanelLook(
    val frame: ImageBitmap,
    val interior: ImageBitmap,
    val ring: Dp = 4.dp,
    val studs: Boolean = true,
    val art: NineSlice? = null,
    val artByHeight: Boolean = false,   // true: scale the whole image to the height (wide bars); false: fixed-size corners
    val artCapDp: Dp = 24.dp
) {
    fun withInterior(i: ImageBitmap) = PanelLook(frame, i, ring, studs, art, artByHeight, artCapDp)
}

val LocalPanelLook = compositionLocalOf<PanelLook> { error("PanelLook not provided") }

fun Textures.woodLook() =
    PanelLook(wood, parchment, if (art.panelParchment != null) 8.dp else 4.dp, true, art.panelParchment, false, 28.dp)

fun Textures.stoneLook() =
    PanelLook(slate, parchment, if (art.panelStone != null) 9.dp else 5.dp, false, art.panelStone, false, 28.dp)

enum class ScreenKind { HOME, QUESTS, SHOP, MARKET, SHIFTS, GYM, STATS }

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

/** Draws a nine-slice image: corners keep their size (times capScale), edges and centre stretch. */
fun DrawScope.drawNineSlice(n: NineSlice, x: Float, y: Float, w: Float, h: Float, capScale: Float) {
    val img = n.image
    var cl = n.left * capScale
    var cr = n.right * capScale
    var ct = n.top * capScale
    var cb = n.bottom * capScale
    if (cl + cr > w && cl + cr > 0f) { val k = w / (cl + cr); cl *= k; cr *= k }
    if (ct + cb > h && ct + cb > 0f) { val k = h / (ct + cb); ct *= k; cb *= k }
    val dx = intArrayOf(
        x.roundToInt(), (x + cl).roundToInt(), (x + w - cr).roundToInt(), (x + w).roundToInt()
    )
    val dy = intArrayOf(
        y.roundToInt(), (y + ct).roundToInt(), (y + h - cb).roundToInt(), (y + h).roundToInt()
    )
    val sx = intArrayOf(0, n.left, img.width - n.right, img.width)
    val sy = intArrayOf(0, n.top, img.height - n.bottom, img.height)
    for (r in 0..2) {
        for (c in 0..2) {
            val sw = sx[c + 1] - sx[c]
            val sh = sy[r + 1] - sy[r]
            val dw = dx[c + 1] - dx[c]
            val dh = dy[r + 1] - dy[r]
            if (sw <= 0 || sh <= 0 || dw <= 0 || dh <= 0) continue
            drawImage(
                image = img,
                srcOffset = IntOffset(sx[c], sy[r]),
                srcSize = IntSize(sw, sh),
                dstOffset = IntOffset(dx[c], dy[r]),
                dstSize = IntSize(dw, dh),
                filterQuality = FilterQuality.Low
            )
        }
    }
}

/** Repeats a square tile of a fixed on-screen size. */
fun DrawScope.drawTiledSized(img: ImageBitmap, tilePx: Int, width: Float, height: Float) {
    if (tilePx <= 0) return
    clipRect(0f, 0f, width, height) {
        var y = 0
        while (y < height) {
            var x = 0
            while (x < width) {
                drawImage(
                    image = img,
                    dstOffset = IntOffset(x, y),
                    dstSize = IntSize(tilePx, tilePx),
                    filterQuality = FilterQuality.Low
                )
                x += tilePx
            }
            y += tilePx
        }
    }
}

/** Fills the area with the image, scaled up and centre-cropped. */
fun DrawScope.drawCover(img: ImageBitmap, width: Float, height: Float) {
    val scale = maxOf(width / img.width, height / img.height)
    val srcW = (width / scale).roundToInt().coerceIn(1, img.width)
    val srcH = (height / scale).roundToInt().coerceIn(1, img.height)
    drawImage(
        image = img,
        srcOffset = IntOffset((img.width - srcW) / 2, (img.height - srcH) / 2),
        srcSize = IntSize(srcW, srcH),
        dstOffset = IntOffset(0, 0),
        dstSize = IntSize(width.roundToInt(), height.roundToInt()),
        filterQuality = FilterQuality.Low
    )
}

/** Repeats a square art tile at a fixed on-screen size (in dp). */
fun Modifier.artTile(img: ImageBitmap, tile: Dp): Modifier = drawBehind {
    drawTiledSized(img, tile.toPx().roundToInt(), size.width, size.height)
}

fun Modifier.stretchedImage(img: ImageBitmap): Modifier = drawBehind {
    drawImage(
        image = img,
        dstOffset = IntOffset(0, 0),
        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        filterQuality = FilterQuality.Low
    )
}

fun DrawScope.drawPixels(img: ImageBitmap, x: Float, y: Float, scale: Int) {
    drawImage(
        image = img,
        dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
        dstSize = IntSize(img.width * scale, img.height * scale),
        filterQuality = FilterQuality.None
    )
}

fun Modifier.tiledBackground(img: ImageBitmap, pixel: Dp = 3.dp): Modifier = drawBehind {
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

/**
 * Framed panel: dark outline, textured frame ring, dark inner line, textured interior, corner rivets.
 * The optional overlay draws extra decoration (moss, honey drips...) on top of the chrome.
 */
fun Modifier.panelChrome(
    look: PanelLook,
    tex: Textures,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null
): Modifier = drawBehind {
    val s = pxScale(3.dp)
    val outline = 3.dp.toPx()
    val ring = look.ring.toPx()
    val w = size.width
    val h = size.height
    val bevel = 1.5.dp.toPx()
    val line = 2.dp.toPx()
    val inner = outline + ring

    val art = look.art
    if (art != null) {
        val capScale = if (look.artByHeight) h / art.image.height else look.artCapDp.toPx() / maxOf(art.left, art.top, 1)
        drawNineSlice(art, 0f, 0f, w, h, capScale)
        overlay?.invoke(this, w, h, s)
        return@drawBehind
    }

    fun fill(color: Color, x: Float, y: Float, ww: Float, hh: Float) {
        drawRect(color, Offset(x, y), Size(ww.coerceAtLeast(0f), hh.coerceAtLeast(0f)))
    }

    fill(StardewWoodDark, 0f, 0f, w, h)
    drawTiled(look.frame, s, outline, outline, w - outline, h - outline)
    fill(Color(0x33FFFFFF), outline, outline, w - 2 * outline, bevel)
    fill(Color(0x33FFFFFF), outline, outline, bevel, h - 2 * outline)
    fill(Color(0x40000000), outline, h - outline - bevel, w - 2 * outline, bevel)
    fill(Color(0x40000000), w - outline - bevel, outline, bevel, h - 2 * outline)

    fill(StardewWoodDark, inner - line, inner - line, w - 2 * (inner - line), h - 2 * (inner - line))
    drawTiled(look.interior, s, inner, inner, w - inner, h - inner)
    fill(Color(0x26000000), inner, inner, w - 2 * inner, 2.dp.toPx())
    fill(Color(0x1A000000), inner, inner, 2.dp.toPx(), h - 2 * inner)

    if (look.studs) {
        val bs = pxScale(2.dp)
        val half = tex.bolt.width * bs / 2f
        val c = outline + ring / 2f
        drawPixels(tex.bolt, c - half, c - half, bs)
        drawPixels(tex.bolt, w - c - half, c - half, bs)
        drawPixels(tex.bolt, c - half, h - c - half, bs)
        drawPixels(tex.bolt, w - c - half, h - c - half, bs)
    }
    overlay?.invoke(this, w, h, s)
}

/** Each screen gets a background that fits it: grass, tilled soil, shop wallpaper, wood, cobblestone. */
fun Modifier.screenBackground(kind: ScreenKind, tex: Textures): Modifier = drawBehind {
    val art = tex.art
    val artTile = when (kind) {
        ScreenKind.HOME -> art.bgHome
        ScreenKind.GYM -> art.bgGym
        ScreenKind.SHOP, ScreenKind.STATS -> art.bgShop
        else -> null
    }
    if (kind == ScreenKind.QUESTS && art.bgQuests != null) {
        drawCover(art.bgQuests, size.width, size.height)
    } else if (artTile != null) {
        drawTiledSized(artTile, (if (kind == ScreenKind.SHOP || kind == ScreenKind.STATS) 200.dp else 260.dp).toPx().roundToInt(), size.width, size.height)
    } else {
        val img = when (kind) {
            ScreenKind.HOME -> tex.grass
            ScreenKind.QUESTS -> tex.soil
            ScreenKind.SHOP -> tex.wall
            ScreenKind.MARKET -> tex.wood
            ScreenKind.SHIFTS -> tex.woodDark
            ScreenKind.GYM -> tex.stone
            ScreenKind.STATS -> tex.wall
        }
        drawTiled(img, pxScale(3.dp), 0f, 0f, size.width, size.height)
    }
    drawRect(
        Brush.verticalGradient(
            0f to Color(0x55000000),
            0.12f to Color(0x00000000),
            0.88f to Color(0x00000000),
            1f to Color(0x4D000000),
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
        val vs = pxScale(2.dp)
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
