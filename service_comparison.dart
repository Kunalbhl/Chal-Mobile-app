import 'dart:async';
import 'dart:math';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'theme_config.dart';

/// Models representing a specific provider comparison item
class ServiceProviderOption {
  final String name;
  final String iconEmoji;
  final double basePrice;
  final double surgeMultiplier;
  final double discount;
  final String etaText;
  final String rating;
  final String additionalMetric; // e.g., "No surge" or "Free Delivery"

  ServiceProviderOption({
    required this.name,
    required this.iconEmoji,
    required this.basePrice,
    required this.surgeMultiplier,
    required this.discount,
    required this.etaText,
    required this.rating,
    required this.additionalMetric,
  });

  double get finalPrice => double.parse(((basePrice * surgeMultiplier) - discount).toStringAsFixed(2));
}

/// A premium [ServiceComparisonComponent] that fetches and displays live price differences
/// between various ride-sharing and food delivery providers using a Glassmorphic responsive table.
class ServiceComparisonComponent extends StatefulWidget {
  final double blurAmount;
  final double borderRadius;

  const ServiceComparisonComponent({
    super.key,
    this.blurAmount = 15.0,
    this.borderRadius = 16.0,
  });

  @override
  State<ServiceComparisonComponent> createState() => _ServiceComparisonComponentState();
}

class _ServiceComparisonComponentState extends State<ServiceComparisonComponent> {
  bool _isRidesSelected = true;
  bool _isLoading = false;
  String _selectedSubCategory = "Sedan (4 Seater)";
  final Random _random = Random();

  // Active comparative items list
  List<ServiceProviderOption> _options = [];

  final List<String> _rideCategories = [
    "Bike (Fastest)",
    "Auto (Eco)",
    "Sedan (4 Seater)",
    "SUV (6 Seater)",
    "Premium (Luxury)"
  ];

  final List<String> _foodCategories = [
    "Butter Chicken",
    "Paneer Butter Masala",
    "Hyderabadi Veg Biryani",
    "Margherita Pizza (Medium)",
    "Masala Dosa Combo"
  ];

  @override
  void initState() {
    super.initState();
    _fetchLiveRates();
  }

  void _fetchLiveRates() {
    setState(() {
      _isLoading = true;
    });

    // Simulate real-time API call latency of 1200ms
    Timer(const Duration(milliseconds: 1200), () {
      if (!mounted) return;
      
      setState(() {
        _isLoading = false;
        if (_isRidesSelected) {
          _options = _generateSimulatedRides(_selectedSubCategory);
        } else {
          _options = _generateSimulatedFood(_selectedSubCategory);
        }
      });
    });
  }

  List<ServiceProviderOption> _generateSimulatedRides(String subCategory) {
    double base;
    switch (subCategory) {
      case "Bike (Fastest)":
        base = 50.0;
        break;
      case "Auto (Eco)":
        base = 90.0;
        break;
      case "Sedan (4 Seater)":
        base = 150.0;
        break;
      case "SUV (6 Seater)":
        base = 250.0;
        break;
      case "Premium (Luxury)":
        base = 350.0;
        break;
      default:
        base = 150.0;
    }

    return [
      ServiceProviderOption(
        name: "Uber",
        iconEmoji: "🚗",
        basePrice: base + _random.nextInt(15),
        surgeMultiplier: 1.0 + (_random.nextDouble() * 0.3),
        discount: _random.nextInt(10) > 6 ? 20.0 : 0.0,
        etaText: "${3 + _random.nextInt(4)} mins",
        rating: "4.7 ★",
        additionalMetric: _random.nextBool() ? "Surge Pricing" : "Standard Rate",
      ),
      ServiceProviderOption(
        name: "Ola Cabs",
        iconEmoji: "🛺",
        basePrice: base + _random.nextInt(10) - 5,
        surgeMultiplier: 1.0 + (_random.nextDouble() * 0.2),
        discount: 10.0,
        etaText: "${5 + _random.nextInt(5)} mins",
        rating: "4.5 ★",
        additionalMetric: "Discount Active",
      ),
      ServiceProviderOption(
        name: "Rapido",
        iconEmoji: "🏍️",
        basePrice: base - 5 + _random.nextInt(12),
        surgeMultiplier: 1.0,
        discount: 0.0,
        etaText: "${2 + _random.nextInt(4)} mins",
        rating: "4.6 ★",
        additionalMetric: "Lowest ETA",
      ),
      ServiceProviderOption(
        name: "BluSmart",
        iconEmoji: "⚡",
        basePrice: base + 15 + _random.nextInt(10),
        surgeMultiplier: 1.0, // Eco EVs have no surge
        discount: 15.0,
        etaText: "${8 + _random.nextInt(8)} mins",
        rating: "4.9 ★",
        additionalMetric: "100% EV • Zero Surge",
      ),
    ];
  }

  List<ServiceProviderOption> _generateSimulatedFood(String dish) {
    double base;
    switch (dish) {
      case "Butter Chicken":
        base = 280.0;
        break;
      case "Paneer Butter Masala":
        base = 240.0;
        break;
      case "Hyderabadi Veg Biryani":
        base = 190.0;
        break;
      case "Margherita Pizza (Medium)":
        base = 320.0;
        break;
      case "Masala Dosa Combo":
        base = 120.0;
        break;
      default:
        base = 200.0;
    }

    return [
      ServiceProviderOption(
        name: "Zomato",
        iconEmoji: "❤️",
        basePrice: base + _random.nextInt(20),
        surgeMultiplier: 1.0,
        discount: 40.0,
        etaText: "${25 + _random.nextInt(15)} mins",
        rating: "4.4 ★",
        additionalMetric: "Zomato Gold Free Del.",
      ),
      ServiceProviderOption(
        name: "Swiggy",
        iconEmoji: "🧡",
        basePrice: base + _random.nextInt(15) - 5,
        surgeMultiplier: 1.0,
        discount: 50.0,
        etaText: "${20 + _random.nextInt(12)} mins",
        rating: "4.5 ★",
        additionalMetric: "Swiggy One Discount",
      ),
      ServiceProviderOption(
        name: "ONDC Direct",
        iconEmoji: "🇮🇳",
        basePrice: base - 35 + _random.nextInt(10), // ONDC is usually 20-30% cheaper base price
        surgeMultiplier: 1.0,
        discount: 0.0,
        etaText: "${30 + _random.nextInt(20)} mins",
        rating: "4.1 ★",
        additionalMetric: "No middleman markup",
      ),
      ServiceProviderOption(
        name: "Magicpin",
        iconEmoji: "✨",
        basePrice: base + _random.nextInt(10),
        surgeMultiplier: 1.0,
        discount: 60.0, // Magicpin is famous for huge voucher discounts
        etaText: "${28 + _random.nextInt(15)} mins",
        rating: "4.2 ★",
        additionalMetric: "Voucher Saved ₹60",
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    
    // Sort options to find the absolute minimum price (Best Deal)
    double minPrice = double.infinity;
    if (_options.isNotEmpty) {
      for (var opt in _options) {
        if (opt.finalPrice < minPrice) {
          minPrice = opt.finalPrice;
        }
      }
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // --- PROVIDER TYPE TOGGLER ---
        Row(
          children: [
            Expanded(
              child: _buildCategoryTabTap(
                title: "Compare Ride Taxis",
                icon: Icons.local_taxi_rounded,
                selected: _isRidesSelected,
                color: ThemeConfig.saffronOrange,
                onTap: () {
                  if (!_isRidesSelected) {
                    setState(() {
                      _isRidesSelected = true;
                      _selectedSubCategory = _rideCategories[2]; // Sedan default
                    });
                    _fetchLiveRates();
                  }
                },
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _buildCategoryTabTap(
                title: "Compare Foods",
                icon: Icons.fastfood_rounded,
                selected: !_isRidesSelected,
                color: ThemeConfig.accentTeal,
                onTap: () {
                  if (_isRidesSelected) {
                    setState(() {
                      _isRidesSelected = false;
                      _selectedSubCategory = _foodCategories[2]; // Biryani default
                    });
                    _fetchLiveRates();
                  }
                },
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),

        // --- HORIZONTAL SUB-CATEGORY CAROUSEL ---
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          physics: const BouncingScrollPhysics(),
          child: Row(
            children: (_isRidesSelected ? _rideCategories : _foodCategories).map((subCat) {
              final isCurrent = _selectedSubCategory == subCat;
              final accentColor = _isRidesSelected ? ThemeConfig.saffronOrange : ThemeConfig.accentTeal;

              return Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: ChoiceChip(
                  label: Text(subCat),
                  selected: isCurrent,
                  onSelected: (selected) {
                    if (selected && _selectedSubCategory != subCat) {
                      setState(() {
                        _selectedSubCategory = subCat;
                      });
                      _fetchLiveRates();
                    }
                  },
                  labelStyle: TextStyle(
                    color: isCurrent ? Colors.black : (isDark ? Colors.white70 : ThemeConfig.charcoalDark),
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                  selectedColor: accentColor,
                  backgroundColor: isDark ? Colors.white.withOpacity(0.05) : Colors.black.withOpacity(0.04),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: BorderSide(
                      color: isCurrent ? accentColor : (isDark ? Colors.white12 : Colors.black12),
                    ),
                  ),
                  showCheckmark: false,
                ),
              );
            }).toList(),
          ),
        ),
        const SizedBox(height: 12),

        // --- COMPARATIVE DATA TABLE CARD (GLASSMORPHIC) ---
        GlassmorphicBox(
          blurAmount: widget.blurAmount,
          borderRadius: widget.borderRadius,
          padding: const EdgeInsets.all(14.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Header Controls Row
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _isRidesSelected ? "Realtime Taxi Rates" : "Direct Menu Rates",
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontSize: 15,
                              fontWeight: FontWeight.black,
                              color: isDark ? Colors.white : ThemeConfig.charcoalDark,
                            ),
                      ),
                      const SizedBox(height: 2),
                      Row(
                        children: [
                          Icon(
                            Icons.cloud_sync_rounded,
                            size: 11,
                            color: _isRidesSelected ? ThemeConfig.saffronOrange : ThemeConfig.accentTeal,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            "LIVE rate compared dynamically",
                            style: TextStyle(
                              fontSize: 10,
                              color: isDark ? Colors.white38 : Colors.black45,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  // Refresh Rate Button
                  IconButton(
                    onPressed: _isLoading ? null : _fetchLiveRates,
                    icon: _isLoading
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: ThemeConfig.saffronOrange,
                            ),
                          )
                        : Icon(
                            Icons.refresh_rounded,
                            color: _isRidesSelected ? ThemeConfig.saffronOrange : ThemeConfig.accentTeal,
                            size: 20,
                          ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Dynamic Overlay Loading Block
              AnimatedCrossFade(
                firstChild: _buildLoadingShimmer(isDark),
                secondChild: _buildComparativeTable(context, isDark, minPrice),
                crossFadeState: _isLoading ? CrossFadeState.showFirst : CrossFadeState.showSecond,
                duration: const Duration(milliseconds: 250),
              ),

              const SizedBox(height: 12),
              // Savings Callout Helper Banner
              if (!_isLoading && _options.isNotEmpty) _buildSavingsCalloutBanner(isDark, minPrice),
            ],
          ),
        ),
      ],
    );
  }

  /// Builds individual top category selector tab
  Widget _buildCategoryTabTap({
    required String title,
    required IconData icon,
    required bool selected,
    required Color color,
    required VoidCallback onTap,
  }) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
          decoration: BoxDecoration(
            color: selected
                ? color.withOpacity(isDark ? 0.15 : 0.08)
                : (isDark ? Colors.white.withOpacity(0.03) : Colors.black.withOpacity(0.02)),
            borderRadius: BorderRadius.circular(14),
            border: Border.all(
              color: selected ? color : (isDark ? Colors.white12 : Colors.black12),
              width: selected ? 1.5 : 1.0,
            ),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                icon,
                color: selected ? color : (isDark ? Colors.white54 : Colors.black45),
                size: 16,
              ),
              const SizedBox(width: 6),
              Text(
                title,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: selected ? FontWeight.black : FontWeight.w600,
                  color: selected
                      ? (isDark ? Colors.white : ThemeConfig.charcoalDark)
                      : (isDark ? Colors.white54 : Colors.black45),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// Displays simulated shimmer/loading state inside table
  Widget _buildLoadingShimmer(bool isDark) {
    return Column(
      children: List.generate(4, (index) {
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 10.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      color: isDark ? Colors.white10 : Colors.black12,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 10),
                  Container(
                    width: 80,
                    height: 12,
                    decoration: BoxDecoration(
                      color: isDark ? Colors.white10 : Colors.black12,
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ],
              ),
              Container(
                width: 60,
                height: 12,
                decoration: BoxDecoration(
                  color: isDark ? Colors.white10 : Colors.black12,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
              Container(
                width: 50,
                height: 12,
                decoration: BoxDecoration(
                  color: isDark ? Colors.white10 : Colors.black12,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ],
          ),
        );
      }),
    );
  }

  /// Builds the elegant comparative data table comparing prices, eta, and details
  Widget _buildComparativeTable(BuildContext context, bool isDark, double minPrice) {
    if (_options.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 20.0),
        child: Center(
          child: Text(
            "No rates available. Try refreshing.",
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
        ),
      );
    }

    return Table(
      columnWidths: const {
        0: FlexColumnWidth(2.6), // Provider Name Info
        1: FlexColumnWidth(1.6), // ETA Status
        2: FlexColumnWidth(1.8), // Calced Price
        3: FlexColumnWidth(1.6), // Best Deal Action
      },
      verticalAlignment: TableCellVerticalAlignment.middle,
      children: [
        // Table Header
        TableRow(
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(
                color: isDark ? Colors.white10 : Colors.black12,
                width: 1,
              ),
            ),
          ),
          children: [
            _buildTableHeaderCell("Provider", Alignment.centerLeft),
            _buildTableHeaderCell("ETA", Alignment.centerLeft),
            _buildTableHeaderCell("Price", Alignment.centerLeft),
            _buildTableHeaderCell("Rating", Alignment.centerRight),
          ],
        ),

        // Table Rows
        ..._options.map((opt) {
          final isBestDeal = opt.finalPrice == minPrice;

          return TableRow(
            decoration: BoxDecoration(
              color: isBestDeal
                  ? ThemeConfig.savingGreen.withOpacity(isDark ? 0.10 : 0.05)
                  : Colors.transparent,
              border: Border(
                bottom: BorderSide(
                  color: isDark ? Colors.white.withOpacity(0.04) : Colors.black.withOpacity(0.04),
                  width: 0.8,
                ),
              ),
            ),
            children: [
              // Provider + Badge Details
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0),
                child: Row(
                  children: [
                    Text(
                      opt.iconEmoji,
                      style: const TextStyle(fontSize: 16),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(
                                opt.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.black,
                                  fontSize: 13,
                                ),
                              ),
                              if (isBestDeal) ...[
                                const SizedBox(width: 4),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: ThemeConfig.savingGreen.withOpacity(0.2),
                                    borderRadius: BorderRadius.circular(4),
                                    border: Border.all(color: ThemeConfig.savingGreen, width: 0.5),
                                  ),
                                  child: const Text(
                                    "LOWEST",
                                    style: TextStyle(
                                      color: ThemeConfig.savingGreen,
                                      fontSize: 7,
                                      fontWeight: FontWeight.black,
                                    ),
                                  ),
                                ),
                              ]
                            ],
                          ),
                          const SizedBox(height: 1),
                          Text(
                            opt.additionalMetric,
                            style: TextStyle(
                              fontSize: 9,
                              color: isDark ? Colors.white38 : Colors.black45,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // ETA details
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0),
                child: Text(
                  opt.etaText,
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: isDark ? Colors.white70 : ThemeConfig.charcoalDark,
                  ),
                ),
              ),

              // Comparative Price Box Column
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "₹${opt.finalPrice.toStringAsFixed(0)}",
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.black,
                        color: isBestDeal
                            ? ThemeConfig.savingGreen
                            : (isDark ? Colors.white70 : ThemeConfig.charcoalDark),
                      ),
                    ),
                    if (opt.discount > 0)
                      Text(
                        "-₹${opt.discount.toStringAsFixed(0)} coupon",
                        style: TextStyle(
                          fontSize: 9,
                          color: ThemeConfig.savingGreen.withOpacity(0.9),
                          fontWeight: FontWeight.w600,
                        ),
                      )
                    else if (opt.surgeMultiplier > 1.0)
                      Text(
                        "${opt.surgeMultiplier.toStringAsFixed(1)}x surge",
                        style: TextStyle(
                          fontSize: 9,
                          color: ThemeConfig.saffronOrange.withOpacity(0.9),
                          fontWeight: FontWeight.w600,
                        ),
                      )
                    else
                      Text(
                        "No extra charges",
                        style: TextStyle(
                          fontSize: 9,
                          color: isDark ? Colors.white24 : Colors.black38,
                        ),
                      ),
                  ],
                ),
              ),

              // Customer rating + Small Enter icon
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0),
                child: Align(
                  alignment: Alignment.centerRight,
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(isDark ? 0.05 : 0.03),
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(
                            color: isDark ? Colors.white10 : Colors.black12,
                            width: 0.8,
                          ),
                        ),
                        child: Text(
                          opt.rating,
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            color: isDark ? Colors.orange[200] : Colors.orange[800],
                          ),
                        ),
                      ),
                      const SizedBox(width: 4),
                      Icon(
                        Icons.chevron_right_rounded,
                        size: 14,
                        color: isBestDeal
                            ? ThemeConfig.savingGreen
                            : (isDark ? Colors.white24 : Colors.black26),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          );
        }).toList()
      ],
    );
  }

  Widget _buildTableHeaderCell(String value, Alignment alignment) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Align(
        alignment: alignment,
        child: Text(
          value.toUpperCase(),
          style: TextStyle(
            fontSize: 9,
            fontWeight: FontWeight.black,
            color: isDark ? Colors.white38 : Colors.black38,
            letterSpacing: 1.0,
          ),
        ),
      ),
    );
  }

  /// Renders savings banner demonstrating best deal insights
  Widget _buildSavingsCalloutBanner(bool isDark, double minPrice) {
    // Find highest final price to compute potential savings
    double maxPrice = 0.0;
    String cheapestName = "";
    String expensiveName = "";

    for (var opt in _options) {
      if (opt.finalPrice > maxPrice) {
        maxPrice = opt.finalPrice;
        expensiveName = opt.name;
      }
      if (opt.finalPrice == minPrice) {
        cheapestName = opt.name;
      }
    }

    final potentialSavings = maxPrice - minPrice;

    if (potentialSavings <= 0) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.all(10.0),
      decoration: BoxDecoration(
        color: ThemeConfig.savingGreen.withOpacity(0.08),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: ThemeConfig.savingGreen.withOpacity(0.15),
          width: 0.8,
        ),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(6),
            decoration: const BoxDecoration(
              color: ThemeConfig.savingGreen,
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.check_circle_outline_rounded,
              color: Colors.white,
              size: 14,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Smart Comparison Insight Available",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 11,
                    color: isDark ? Colors.white : ThemeConfig.charcoalDark,
                  ),
                ),
                const SizedBox(height: 2),
                RichText(
                  text: TextSpan(
                    style: TextStyle(
                      fontSize: 10,
                      color: isDark ? Colors.white70 : Colors.black54,
                    ),
                    children: [
                      const TextSpan(text: "Choosing "),
                      TextSpan(
                        text: cheapestName,
                        style: const TextStyle(fontWeight: FontWeight.bold, color: ThemeConfig.savingGreen),
                      ),
                      const TextSpan(text: " instead of "),
                      TextSpan(
                        text: expensiveName,
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      const TextSpan(text: " saves you "),
                      TextSpan(
                        text: "₹${potentialSavings.toStringAsFixed(0)}",
                        style: const TextStyle(fontWeight: FontWeight.black, color: ThemeConfig.savingGreen),
                      ),
                      const TextSpan(text: " instantly!"),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
