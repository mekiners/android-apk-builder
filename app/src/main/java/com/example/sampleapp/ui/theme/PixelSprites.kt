package com.example.sampleapp.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** Hand-drawn pixel art, stored as text grids and painted with the palette below. (Generated file) */
class Sprite(val rows: List<String>) {
    val width: Int = rows[0].length
    val height: Int = rows.size
}

val SpritePalette: Map<Char, Color> = mapOf(
    'k' to Color(0xFF382D24),
    'w' to Color(0xFF765036),
    'h' to Color(0xFFA77A50),
    'c' to Color(0xFFF2E5C9),
    't' to Color(0xFFE5D3AE),
    'W' to Color(0xFFFFF6E2),
    'g' to Color(0xFFE3AD43),
    'G' to Color(0xFFB77A2E),
    'y' to Color(0xFFF4D67E),
    'l' to Color(0xFF6F9258),
    'L' to Color(0xFF9DBB73),
    'D' to Color(0xFF3E6541),
    'r' to Color(0xFFC87555),
    'R' to Color(0xFF8F4938),
    'o' to Color(0xFFE18C45),
    'm' to Color(0xFFEA632F),
    'a' to Color(0xFFD89D31),
    'b' to Color(0xFF7098B1),
    'B' to Color(0xFF456D8A),
    's' to Color(0xFFA9D1D6),
    'p' to Color(0xFFE8A6A0),
    'e' to Color(0xFFB8B6A9),
    'E' to Color(0xFF68736A),
    'n' to Color(0xFFC79A5B),
    'u' to Color(0xFF8A7191),
)

@Composable
fun PixelIcon(sprite: Sprite, iconSize: Dp, modifier: Modifier = Modifier) {
    val iconHeight = iconSize * sprite.height / sprite.width
    // Rasterize the tiny authored grid once, then scale with nearest-neighbour sampling.
    // This keeps every original sprite crisp at different screen densities and icon sizes.
    val bitmap = remember(sprite) {
        Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.ARGB_8888).apply {
            for (r in 0 until sprite.height) {
                val row = sprite.rows[r]
                for (c in 0 until sprite.width) {
                    val color = SpritePalette[row[c]]
                    setPixel(c, r, color?.toArgb() ?: android.graphics.Color.TRANSPARENT)
                }
            }
        }.asImageBitmap()
    }
    Canvas(modifier = modifier.size(width = iconSize, height = iconHeight)) {
        drawImage(
            image = bitmap,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.None
        )
    }
}

object Sprites {
    val coin = Sprite(listOf(
        ".....kk.....",
        "...kkyykk...",
        "..kyyygggk..",
        ".kyyyGGgggk.",
        ".kyygGGgggk.",
        "kyyggGGgggGk",
        "kygggGGggGGk",
        ".kgggGGgGGk.",
        ".kgggGGGGGk.",
        "..kgggGGGk..",
        "...kkGGkk...",
        ".....kk....."
    ))
    val coins = Sprite(listOf(
        "....kkkk....",
        "...kyyggk...",
        "..kyggggGk..",
        "..kyggGgGk..",
        "..kgggGgGk..",
        ".kkgggggGkk.",
        "kygkGGGGkgGk",
        "kyggkkkkGgGk",
        "kgggkgggGgGk",
        "kgggkgggggGk",
        ".kGGGkGGGGk.",
        "..kkkkkkkk.."
    ))
    val bag = Sprite(listOf(
        "....kkkk....",
        ".....kk.....",
        "....kGGk....",
        "...kyyyyk...",
        "..kyyyggyk..",
        ".kyyygGggGk.",
        ".kyygGggGgk.",
        ".kygggGGggk.",
        ".kyggGgggGk.",
        ".kgggGGgggk.",
        "..kGgggggk..",
        "...kkkkkk..."
    ))
    val mic = Sprite(listOf(
        "....kkkk....",
        "...kWWWWk...",
        "...kWeeWk...",
        "...kWWWWk...",
        "...kWeeWk...",
        "...kWWWWk...",
        ".k.kWeeWk.k.",
        ".kkkWWWWkkk.",
        "..kkkkkkkk..",
        ".....kk.....",
        ".....kk.....",
        "...kkkkkk..."
    ))
    val plus = Sprite(listOf(
        "............",
        "............",
        ".....WW.....",
        ".....WW.....",
        ".....WW.....",
        "..WWWWWWWW..",
        "..WWWWWWWW..",
        ".....WW.....",
        ".....WW.....",
        ".....WW.....",
        "............",
        "............"
    ))
    val plusDark = Sprite(listOf(
        "............",
        "............",
        ".....kk.....",
        ".....kk.....",
        ".....kk.....",
        "..kkkkkkkk..",
        "..kkkkkkkk..",
        ".....kk.....",
        ".....kk.....",
        ".....kk.....",
        "............",
        "............"
    ))
    val down = Sprite(listOf(
        "............",
        "............",
        "............",
        ".WW......WW.",
        "..WW....WW..",
        "...WW..WW...",
        "....WWWW....",
        ".....WW.....",
        "............",
        "............",
        "............",
        "............"
    ))
    val up = Sprite(listOf(
        "............",
        "............",
        "............",
        "............",
        ".....WW.....",
        "....WWWW....",
        "...WW..WW...",
        "..WW....WW..",
        ".WW......WW.",
        "............",
        "............",
        "............"
    ))
    val downDark = Sprite(listOf(
        "............",
        "............",
        "............",
        ".kk......kk.",
        "..kk....kk..",
        "...kk..kk...",
        "....kkkk....",
        ".....kk.....",
        "............",
        "............",
        "............",
        "............"
    ))
    val upDark = Sprite(listOf(
        "............",
        "............",
        "............",
        "............",
        ".....kk.....",
        "....kkkk....",
        "...kk..kk...",
        "..kk....kk..",
        ".kk......kk.",
        "............",
        "............",
        "............"
    ))
    val check = Sprite(listOf(
        "............",
        "............",
        "............",
        ".........W..",
        "........WW..",
        ".......WW...",
        "..W...WW....",
        "..WW.WW.....",
        "...WWW......",
        "....W.......",
        "............",
        "............"
    ))
    val pencil = Sprite(listOf(
        "..........k.",
        ".........kp.",
        "........kpR.",
        ".......kyRk.",
        "......kyak..",
        ".....kyak...",
        "....kyak....",
        "...kyak.....",
        "..kyak......",
        ".khak.......",
        ".kwk........",
        "kkk........."
    ))
    val trash = Sprite(listOf(
        "....kkkk....",
        ".kkkkkkkkkk.",
        ".khhhhhhhhk.",
        ".kkkkkkkkkk.",
        "..kwhkwhwk..",
        "..kwhkwhwk..",
        "..kwhkwhwk..",
        "..kwhkwhwk..",
        "..kwhkwhwk..",
        "..kwhkwhwk..",
        "...kkkkkk...",
        "............"
    ))
    val tree = Sprite(listOf(
        "....kkkk....",
        "..kkLLLLkk..",
        ".kLLllLLLLk.",
        "kLLlLLllLLLk",
        "kLlLLLLLllLk",
        "kLLLllLLLLlk",
        ".klLLLLllLk.",
        "..kklLLlkk..",
        "....khhk....",
        "....khhk....",
        "....khhk....",
        "...kkkkkk..."
    ))
    val honey = Sprite(listOf(
        "...kkkkkk...",
        "..khhhhhhk..",
        "..kkkkkkkk..",
        ".kaaaaaaaak.",
        "kayyyaaaaaak",
        "kayyaaaaaaak",
        "kayaaaccaaak",
        "kaaaaccWcaak",
        "kaaaaaccaGak",
        "kaaaaaaaaGak",
        ".kGGGGGGGGk.",
        "..kkkkkkkk.."
    ))
    val flame = Sprite(listOf(
        ".....kk.....",
        ".....kmk....",
        "....kmmk....",
        "...kmmmmk.k.",
        "..kmmooomkmk",
        "..kmooyoomk.",
        ".kmooyyyomk.",
        ".kmooyyyomk.",
        ".kmmooyommk.",
        "..kmmooommk.",
        "...kkmmmkk..",
        ".....kkk...."
    ))
    val loop = Sprite(listOf(
        "........kkk.",
        "...wwwww.kk.",
        "..wwwww...k.",
        ".www........",
        ".ww.........",
        ".ww......ww.",
        ".ww......ww.",
        ".........ww.",
        "........www.",
        ".k...wwwww..",
        ".kk.wwwww...",
        ".kkk........"
    ))
    val clock = Sprite(listOf(
        ".....kk.....",
        "...kkWWkk...",
        "..kWWWkWWk..",
        ".kWWWWkWWWk.",
        ".kWWWWkWWWk.",
        "kWWWWWkWWWWk",
        "kWWWWWkkkWWk",
        ".kWWWWWWWWk.",
        ".kWWWWWWWWk.",
        "..kWWWWWWk..",
        "...kkWWkk...",
        ".....kk....."
    ))
    val house = Sprite(listOf(
        ".....kk.....",
        "....kRRk....",
        "...kRRRRk...",
        "..kRRRrRRk..",
        ".kRRRRRRRRk.",
        "kRRRRRRRRRRk",
        ".kkkkkkkkkk.",
        "..kWWWWWWk..",
        "..kWWkkWWk..",
        "..kWWkhWWk..",
        "..kWWkkWWk..",
        "..kkkkkkkk.."
    ))
    val scroll = Sprite(listOf(
        "............",
        ".kkkkkkkkkk.",
        ".kwwwwwwwwk.",
        ".kkkkkkkkkk.",
        "..kccccccck.",
        "..kckkkkkck.",
        "..kccccccck.",
        "..kckkkkkck.",
        "..kccccccck.",
        ".kkkkkkkkkk.",
        ".kwwwwwwwwk.",
        ".kkkkkkkkkk."
    ))
    val basket = Sprite(listOf(
        ".....kk.....",
        "....k..k....",
        "...k....k...",
        "..kkkkkkkk..",
        ".kaayaayaak.",
        ".kyaayaayak.",
        ".kaayaayaak.",
        "..kyaayaak..",
        "..kaayaayk..",
        "...kkkkkk...",
        "............",
        "............"
    ))
    val calendar = Sprite(listOf(
        "............",
        ".kkkkkkkkkk.",
        ".kRRRRRRRRk.",
        ".kRkRRRRkRk.",
        ".kkkkkkkkkk.",
        ".kWWWWWWWWk.",
        ".kWkWkWkWWk.",
        ".kWWWWWWWWk.",
        ".kWkWkWkWWk.",
        ".kWWWWWWWWk.",
        ".kkkkkkkkkk.",
        "............"
    ))
    val dumbbell = Sprite(listOf(
        "............",
        "............",
        ".kk......kk.",
        "kEEk....kEEk",
        "kEEkkkkkkEEk",
        "kEEkhhhhkEEk",
        "kEEkkkkkkEEk",
        "kEEk....kEEk",
        ".kk......kk.",
        "............",
        "............",
        "............"
    ))
    val apple = Sprite(listOf(
        "......k.....",
        ".....kDLk...",
        "..kkkkkDLk..",
        ".krrrkrrrrk.",
        "krrWrrrrrrrk",
        "krWrrrrrrrRk",
        "krrrrrrrrrRk",
        "krrrrrrrrRRk",
        ".krrrrrrrRk.",
        ".krrrrrrRRk.",
        "..kkrrRRkk..",
        "....kkkk...."
    ))
    val meat = Sprite(listOf(
        "..kkkkk.....",
        ".koooooRk...",
        "koooWooooRk.",
        "kooooooooRk.",
        "kooooooooRk.",
        ".kooooooRRk.",
        "..kkoRRRkk..",
        "....kkRkWk..",
        ".....kkWWWk.",
        "......kWWWWk",
        ".......kWWk.",
        "........kk.."
    ))
    val milk = Sprite(listOf(
        "....kkkk....",
        "....kbbk....",
        "...kkWWkk...",
        "..kWWWWWWk..",
        "..kWbbbbWk..",
        "..kWbWWbWk..",
        "..kWbbbbWk..",
        "..kWWWWWWk..",
        "..kWWWWWWk..",
        "..kWWWWWWk..",
        "..kssssssk..",
        "...kkkkkk..."
    ))
    val bread = Sprite(listOf(
        "............",
        "...kkkkkk...",
        "..kayayaak..",
        ".kayayayaak.",
        "kayyayayayak",
        "kaaaaaaaaaak",
        "kaaaaaaaaaak",
        "kayyyyyyyyak",
        "kayyyyyyyyak",
        ".kaaaaaaaak.",
        "..kkkkkkkk..",
        "............"
    ))
    val jar = Sprite(listOf(
        "...kkkkkk...",
        "...khhhhk...",
        "..kkkkkkkk..",
        "..kssssssk..",
        ".kssWssssRk.",
        ".ksrrrrrrRk.",
        ".kcrrWrrrRk.",
        ".kcrrrrrrRk.",
        ".ksrrrrrrRk.",
        ".kssssssssk.",
        "..kkkkkkkk..",
        "............"
    ))
    val sponge = Sprite(listOf(
        "............",
        "............",
        "..kkkkkkkk..",
        ".kLLLLLLLLk.",
        ".kllllllllk.",
        ".kkkkkkkkkk.",
        ".kyyyyyyyyk.",
        ".kyyaayyayk.",
        ".kyyyyyyyyk.",
        ".kyayyyyayk.",
        ".kkkkkkkkkk.",
        "............"
    ))
    val phone = Sprite(listOf(
        "...kkkkkk...",
        "..kEEEEEEk..",
        "..kEssssEk..",
        "..kEsBBsEk..",
        "..kEssssEk..",
        "..kEssssEk..",
        "..kEssssEk..",
        "..kEssssEk..",
        "..kEEEEEEk..",
        "..kEEggEEk..",
        "..kEEEEEEk..",
        "...kkkkkk..."
    ))
    val crate = Sprite(listOf(
        "............",
        ".kkkkkkkkkk.",
        ".khhhhhhhhk.",
        ".kwkhhhhkwk.",
        ".khwkhhkwhk.",
        ".khhwkkwhhk.",
        ".khhhwwhhhk.",
        ".khhwkkwhhk.",
        ".khwkhhkwhk.",
        ".kwkhhhhkwk.",
        ".khhhhhhhhk.",
        ".kkkkkkkkkk."
    ))
    val sun = Sprite(listOf(
        ".....gg.....",
        ".....gg.....",
        "..g......g..",
        "....kkkk....",
        "...kyyyyk...",
        "gg.kyyyyk.gg",
        "gg.kyyyyk.gg",
        "...kyyyyk...",
        "....kkkk....",
        "..g......g..",
        ".....gg.....",
        ".....gg....."
    ))
    val sunset = Sprite(listOf(
        ".....gg.....",
        "............",
        "............",
        ".g........g.",
        "............",
        "....kkkk....",
        "g..kooook..g",
        "..kooooook..",
        "..kooooook..",
        "kkkkkkkkkkkk",
        "aaaaaaaaaaaa",
        "............"
    ))
    val moon = Sprite(listOf(
        "............",
        "...kk.......",
        "..ky......W.",
        ".kyy........",
        ".kyy........",
        ".kyy........",
        ".kyy........",
        ".kyyy.......",
        ".kyyyy...W..",
        "..kyyyyyyk..",
        "...kkkkkk...",
        "............"
    ))
    val pin = Sprite(listOf(
        "..kkkk..",
        ".kRrrrk.",
        ".kRrWrk.",
        ".kRrrrk.",
        "..kkkk..",
        "...ee...",
        "...ke...",
        "....k..."
    ))
    val cat = Sprite(listOf(
        "................",
        "..k.k...........",
        ".kokok..kkkkkk..",
        ".koooookkooooook",
        ".kowoowoooaaoook",
        ".kooooooooooaook",
        ".kopokoooooooook",
        "..kkkoooaaooook.",
        "....kooooooooook",
        "....kkkkkkkkkkk."
    ))
    val sprout = Sprite(listOf(
        "..L....L..",
        ".LlL..LlL.",
        ".LllLLllL.",
        "..LlllLl..",
        "....ll....",
        "....lk....",
        "...hhhh...",
        "..hhhhhh.."
    ))
    val cloud = Sprite(listOf(
        "......WWWW......",
        "....WWWWWWWW....",
        "..WWWWWWWWWWWW..",
        ".WWWWWWWWWWWWWW.",
        "WWWWWWWWWWWWWWWW",
        ".tttttttttttttt."
    ))
    val left = Sprite(listOf(
        "............",
        "............",
        "......WW....",
        ".....WW.....",
        "....WW......",
        "...WW.......",
        "...WW.......",
        "....WW......",
        ".....WW.....",
        "......WW....",
        "............",
        "............"
    ))
    val right = Sprite(listOf(
        "............",
        "............",
        "....WW......",
        ".....WW.....",
        "......WW....",
        ".......WW...",
        ".......WW...",
        "......WW....",
        ".....WW.....",
        "....WW......",
        "............",
        "............"
    ))
    val leftDark = Sprite(listOf(
        "............",
        "............",
        "......kk....",
        ".....kk.....",
        "....kk......",
        "...kk.......",
        "...kk.......",
        "....kk......",
        ".....kk.....",
        "......kk....",
        "............",
        "............"
    ))
    val rightDark = Sprite(listOf(
        "............",
        "............",
        "....kk......",
        ".....kk.....",
        "......kk....",
        ".......kk...",
        ".......kk...",
        "......kk....",
        ".....kk.....",
        "....kk......",
        "............",
        "............"
    ))
}
