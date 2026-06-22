import 'dart:ui';
import 'package:flutter/material.dart';

/// [ThemeConfig] manages the Material 3 theme definitions, typography system,
/// and premium glassmorphic/frosted-glass design configurations.
class ThemeConfig {
  // Brand color palette (inspired by Chalo's elegant palette)
  static const Color saffronOrange = Color(0xFFFF9E0A);
  static const Color accentTeal = Color(0xFF00E5FF);
  static const Color savingGreen = Color(0xFF10B981);
  static const Color charcoalDark = Color(0xFF121212);
  static const Color surfaceDark = Color(0xFF1E1E1E);
  static const Color surfaceLight = Color(0xFFF9F9FB);

  /// Standard dark color scheme matching Material 3 specifications.
  static final ColorScheme darkColorScheme = ColorScheme.fromSeed(
    seedColor: saffronOrange,
    brightness: Brightness.dark,
    primary: saffronOrange,
    secondary: accentTeal,
    tertiary: savingGreen,
    surface: charcoalDark,
    onSurface: Colors.white,
    onPrimary: Colors.black,
  );

  /// Standard light color scheme matching Material 3 specifications.
  static final ColorScheme lightColorScheme = ColorScheme.fromSeed(
    seedColor: saffronOrange,
    brightness: Brightness.light,
    primary: saffronOrange,
    secondary: accentTeal,
    tertiary: savingGreen,
    surface: surfaceLight,
    onSurface: charcoalDark,
    onPrimary: Colors.white,
  );

  /// M3 Typography styling using elegant font sizing & letter-spacing setup.
  static TextTheme _buildTextTheme(Color textColor) {
    return TextTheme(
      displayLarge: TextStyle(
        fontSize: 32,
        fontWeight: FontWeight.black,
        letterSpacing: -0.5,
        color: textColor,
      ),
      headlineMedium: TextStyle(
        fontSize: 22,
        fontWeight: FontWeight.bold,
        letterSpacing: 0.25,
        color: textColor,
      ),
      titleLarge: TextStyle(
        fontSize: 18,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.15,
        color: textColor,
      ),
      bodyLarge: TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.normal,
        letterSpacing: 0.5,
        color: textColor.withOpacity(0.87),
      ),
      bodyMedium: TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.normal,
        letterSpacing: 0.25,
        color: textColor.withOpacity(0.60),
      ),
      labelLarge: TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.bold,
        letterSpacing: 1.25,
        color: textColor,
      ),
    );
  }

  /// ThemeData for Dark Theme configuration
  static ThemeData get darkTheme => ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        colorScheme: darkColorScheme,
        scaffoldBackgroundColor: const Color(0xFF0D0D0D),
        textTheme: _buildTextTheme(Colors.white),
        cardTheme: CardTheme(
          color: surfaceDark,
          elevation: 0,
          shape: RoundedCornerShape.all12,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          centerTitle: true,
        ),
      );

  /// ThemeData for Light Theme configuration
  static ThemeData get lightTheme => ThemeData(
        useMaterial3: true,
        brightness: Brightness.light,
        colorScheme: lightColorScheme,
        scaffoldBackgroundColor: const Color(0xFFF4F4F6),
        textTheme: _buildTextTheme(charcoalDark),
        cardTheme: CardTheme(
          color: Colors.white,
          elevation: 2,
          shape: RoundedCornerShape.all12,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          centerTitle: true,
        ),
      );

  // ==========================================
  // --- GLASSMORPHISM (FROSTED GLASS) SETTINGS ---
  // ==========================================

  /// Radial/Ambient blurred background gradient with high-transparency support
  static BoxDecoration glassBoxDecorationDark = BoxDecoration(
    color: Colors.white.withOpacity(0.06),
    borderRadius: BorderRadius.all(Radius.circular(16)),
    border: Border.all(
      color: Colors.white.withOpacity(0.15),
      width: 1.2,
    ),
    gradient: LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: [
        Colors.white.withOpacity(0.10),
        Colors.white.withOpacity(0.02),
      ],
    ),
  );

  static BoxDecoration glassBoxDecorationLight = BoxDecoration(
    color: Colors.black.withOpacity(0.04),
    borderRadius: BorderRadius.all(Radius.circular(16)),
    border: Border.all(
      color: Colors.black.withOpacity(0.08),
      width: 1.2,
    ),
    gradient: LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: [
        Colors.black.withOpacity(0.05),
        Colors.black.withOpacity(0.01),
      ],
    ),
  );
}

/// Helper extension defining custom border radius constants
extension RoundedCornerShape on CardTheme {
  static const RoundedRectangleBorder all12 = RoundedRectangleBorder(
    borderRadius: BorderRadius.all(Radius.circular(12)),
  );
}

/// Reusable [GlassmorphicBox] container to apply premium frosted glassmorphism safely.
/// Leverages [ClipRRect], [BackdropFilter] for dynamic UI blurring, and elegant thin borders.
class GlassmorphicBox extends StatelessWidget {
  final Widget child;
  final double blurAmount;
  final double borderRadius;
  final BoxDecoration? customDecoration;
  final EdgeInsetsGeometry padding;

  const GlassmorphicBox({
    super.key,
    required this.child,
    this.blurAmount = 15.0,
    this.borderRadius = 16.0,
    this.customDecoration,
    this.padding = const EdgeInsets.all(16.0),
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final decoration = customDecoration ??
        (isDark ? ThemeConfig.glassBoxDecorationDark : ThemeConfig.glassBoxDecorationLight);

    return ClipRRect(
      borderRadius: BorderRadius.all(Radius.circular(borderRadius)),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blurAmount, sigmaY: blurAmount),
        child: Container(
          decoration: decoration.copyWith(
            borderRadius: BorderRadius.all(Radius.circular(borderRadius)),
          ),
          padding: padding,
          child: child,
        ),
      ),
    );
  }
}
