import 'dart:ui';
import 'package:flutter/material.dart';
import 'theme_config.dart';

/// A premium, interactive [CategoryCard] widget styled with Frosted Glassmorphism.
/// It uses [BackdropFilter] for dynamic background blurring, custom border stroke gradients
/// to emulate glass edges, and supports elegant hover and press micro-animations.
class CategoryCard extends StatefulWidget {
  final String title;
  final String description;
  final IconData icon;
  final VoidCallback onTap;
  
  /// The color accent representing this specific service category (e.g., saffonOrange, accentTeal).
  final Color accentColor;
  
  /// The blur intensity of the frosted background.
  final double blurAmount;

  const CategoryCard({
    super.key,
    required this.title,
    required this.description,
    required this.icon,
    required this.onTap,
    this.accentColor = ThemeConfig.saffronOrange,
    this.blurAmount = 15.0,
  });

  @override
  State<CategoryCard> createState() => _CategoryCardState();
}

class _CategoryCardState extends State<CategoryCard> with SingleTickerProviderStateMixin {
  bool _isHovered = false;
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    // Fluid responsive design helper variables based on states
    final scale = _isPressed ? 0.95 : (_isHovered ? 1.04 : 1.0);
    final glowColor = widget.accentColor.withOpacity(_isHovered ? 0.25 : 0.08);
    final borderColor = isDark
        ? Colors.white.withOpacity(_isHovered ? 0.25 : 0.12)
        : Colors.black.withOpacity(_isHovered ? 0.18 : 0.08);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() {
        _isHovered = false;
        _isPressed = false;
      }),
      child: GestureDetector(
        onTapDown: (_) => setState(() => _isPressed = true),
        onTapUp: (_) {
          setState(() => _isPressed = false);
          widget.onTap();
        },
        onTapCancel: () => setState(() => _isPressed = false),
        child: AnimatedScale(
          scale: scale,
          duration: const Duration(milliseconds: 150),
          curve: Curves.easeOutCubic,
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: glowColor,
                  blurRadius: _isHovered ? 20.0 : 8.0,
                  spreadRadius: _isHovered ? 2.0 : -1.0,
                  offset: const Offset(0, 4),
                )
              ],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: widget.blurAmount, sigmaY: widget.blurAmount),
                child: Container(
                  padding: const EdgeInsets.all(18.0),
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.white.withOpacity(_isHovered ? 0.09 : 0.05)
                        : Colors.black.withOpacity(_isHovered ? 0.06 : 0.03),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: borderColor,
                      width: 1.2,
                    ),
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: isDark
                          ? [
                              Colors.white.withOpacity(0.12),
                              Colors.white.withOpacity(0.02),
                            ]
                          : [
                              Colors.black.withOpacity(0.06),
                              Colors.black.withOpacity(0.01),
                            ],
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      // Header Accent Row (Icon + Corner Indicator)
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          AnimatedContainer(
                            duration: const Duration(milliseconds: 200),
                            padding: const EdgeInsets.all(10.0),
                            decoration: BoxDecoration(
                              color: widget.accentColor.withOpacity(_isHovered ? 0.25 : 0.12),
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: widget.accentColor.withOpacity(0.3),
                                width: 1.0,
                              ),
                            ),
                            child: Icon(
                              widget.icon,
                              color: widget.accentColor,
                              size: 24,
                            ),
                          ),
                          // Premium Interactive Indicator
                          AnimatedOpacity(
                            opacity: _isHovered ? 1.0 : 0.4,
                            duration: const Duration(milliseconds: 150),
                            child: Icon(
                              Icons.arrow_forward_rounded,
                              color: widget.accentColor,
                              size: 16,
                            ),
                          ),
                        ],
                      ),
                      const Spacer(),
                      // Service Description Content
                      Text(
                        widget.title,
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.black,
                              fontSize: 16,
                              color: isDark ? Colors.white : ThemeConfig.charcoalDark,
                            ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 6),
                      Text(
                        widget.description,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              fontSize: 11,
                              color: isDark
                                  ? Colors.white.withOpacity(0.60)
                                  : ThemeConfig.charcoalDark.withOpacity(0.60),
                              height: 1.4,
                            ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
