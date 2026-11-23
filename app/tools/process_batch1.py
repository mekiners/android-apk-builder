"""Turns the user's generated art (magenta backgrounds, big files) into the art_* WebP assets used by the app.

Input folder: batch1/ extracted from the user's archive. Output: app/src/main/res/drawable-nodpi/art_*.webp
- chroma-keys the magenta background (soft key + despill), crops to the visible art, downsizes
- bg_gym / bg_market / bg_shop are made tileable by cross-fading with a half-shifted copy (bg_home is already seamless)
- bg_quests is a scene (not a tile): the magenta frame is cropped away
The app finds these files at runtime by name (see loadArt in ui/theme/Textures.kt); a missing file just means the built-in art is used.
Nine-slice insets (source pixels) live in loadArt(); change them there if you replace an image.
"""
from PIL import Image
import numpy as np, os
SRC = '/tmp/b1/batch1/'
OUT = 'app/src/main/res/drawable-nodpi/'

def key(im):
    a = np.asarray(im.convert('RGB')).astype(np.float32) / 255
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    mag = np.minimum(r, b) - g
    alpha = 1 - np.clip((mag - 0.28) / 0.30, 0, 1)
    spill = np.clip(mag, 0, 1) * (alpha < 0.999)
    r2 = np.where(spill > 0, np.minimum(r, g + 0.15), r); b2 = np.where(spill > 0, np.minimum(b, g + 0.15), b)
    return Image.fromarray((np.clip(np.dstack([r2, g, b2, alpha]), 0, 1) * 255).astype(np.uint8), 'RGBA')

def crop_alpha(im, t=40):
    a = np.asarray(im)[..., 3]; ys, xs = np.where(a > t)
    return im.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))

def resize_w(im, w): return im.resize((w, round(im.height * w / im.width)), Image.LANCZOS)
def save(im, name, lossless=True): im.save(OUT + name + '.webp', 'WEBP', lossless=lossless, quality=92, method=6)

def seamless(im):
    a = np.asarray(im.convert('RGB')).astype(np.float32); h, w, _ = a.shape
    b = np.roll(np.roll(a, h // 2, axis=0), w // 2, axis=1)
    m = (np.sin(np.pi * np.linspace(0, 1, w)) ** 2)[None, :] * (np.sin(np.pi * np.linspace(0, 1, h)) ** 2)[:, None]
    m = m[..., None]
    return Image.fromarray(np.clip(a * m + b * (1 - m), 0, 255).astype(np.uint8))

for src, name, w in [('card_easy.png', 'art_card_easy', 1080), ('card_medium.png', 'art_card_medium', 1080),
                     ('card_hard.png', 'art_card_hard', 1080), ('panel_parchment.jpg', 'art_panel_parchment', 900),
                     ('panel_stone.png', 'art_panel_stone', 900), ('banner_plaque.png', 'art_banner', 900),
                     ('honey_drips.png', 'art_honey', 1080), ('cat_sleeping.png', 'art_cat', 360)]:
    save(resize_w(crop_alpha(key(Image.open(SRC + src))), w), name)
save(resize_w(Image.open(SRC + 'dock_bar.png').convert('RGB'), 1080), 'art_dock', lossless=False)
for src, name in [('bg_home.png', 'art_bg_home'), ('bg_gym.png', 'art_bg_gym'), ('bg_market.png', 'art_bg_market'), ('bg_shop.png', 'art_bg_shop')]:
    im = Image.open(SRC + src).convert('RGB').resize((512, 512), Image.LANCZOS)
    save(im if name == 'art_bg_home' else seamless(im), name, lossless=False)
save(Image.open(SRC + 'bg_quests.jpg').convert('RGB').crop((86, 86, 937, 937)).resize((768, 768), Image.LANCZOS), 'art_bg_quests', lossless=False)
