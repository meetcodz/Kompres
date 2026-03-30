package prog.ui;

import java.awt.*;

/**
 * Centralized design tokens — dark Fluent/Apple-inspired theme.
 * Single source of truth for every color, font, and radius in the app.
 */
public final class Theme {
    private Theme() {}

    // ── Backgrounds ───────────────────────────────────────────────────────────
    public static final Color BG_APP      = new Color(28,  28,  30);   // #1c1c1e — root bg
    public static final Color BG_SIDEBAR  = new Color(30,  30,  34);   // #1e1e22
    public static final Color BG_SURFACE  = new Color(36,  36,  40);   // #242428 — cards, panels
    public static final Color BG_CARD     = new Color(42,  42,  46);   // #2a2a2e
    public static final Color BG_HOVER    = new Color(255, 255, 255,  14); // #ffffff0e
    public static final Color BG_ACTIVE   = new Color(255, 255, 255,  22); // #ffffff16
    public static final Color BG_TITLEBAR = new Color(26,  26,  30);   // #1a1a1e

    // ── Borders ───────────────────────────────────────────────────────────────
    public static final Color BORDER      = new Color(255, 255, 255,  18); // #ffffff12
    public static final Color BORDER2     = new Color(255, 255, 255,  30); // #ffffff1e

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(242, 242, 247); // #f2f2f7
    public static final Color TEXT_SECONDARY = new Color(172, 172, 180); // #acacb4
    public static final Color TEXT_TERTIARY  = new Color(110, 110, 118); // #6e6e76

    // ── Green — Huffman ───────────────────────────────────────────────────────
    public static final Color GREEN          = new Color( 52, 199,  89); // #34c759
    public static final Color GREEN_DIM      = new Color( 26,  58,  34); // #1a3a22
    public static final Color GREEN_GLOW     = new Color( 52, 199,  89, 48);
    public static final Color GREEN_DARK_TXT = new Color( 13,  31,  18); // text on green btn

    // ── Amber — LZW ───────────────────────────────────────────────────────────
    public static final Color AMBER          = new Color(255, 159,  10); // #ff9f0a
    public static final Color AMBER_DIM      = new Color( 58,  36,   0); // #3a2400
    public static final Color AMBER_DARK_TXT = new Color( 31,  18,   0); // text on amber btn

    // ── Semantic ──────────────────────────────────────────────────────────────
    public static final Color BLUE     = new Color( 10, 132, 255); // #0a84ff
    public static final Color BLUE_DIM = new Color( 10,  42,  74); // #0a2a4a
    public static final Color RED      = new Color(255,  69,  58); // #ff453a

    // ── Typography (system stack — Segoe UI on Windows, SF Pro on Mac) ────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,   14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,   11);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN,  12);
    public static final Font FONT_TINY    = new Font("Segoe UI", Font.PLAIN,  10);
    public static final Font FONT_CAPS    = new Font("Segoe UI", Font.BOLD,   10);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.BOLD,   20);
    public static final Font FONT_BRAND   = new Font("Segoe UI", Font.BOLD,   15);

    // ── Geometry ──────────────────────────────────────────────────────────────
    public static final int R_SM  =  8;
    public static final int R_MD  = 12;
    public static final int R_LG  = 16;
    public static final int R_XL  = 20;
}
