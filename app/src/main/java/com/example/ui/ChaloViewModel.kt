package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.GeminiContent
import com.example.api.GeminiPart
import com.example.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.example.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChaloRecommendation(
    val title: String,
    val description: String,
    val dealTag: String, // e.g. "CHEAPEST RIDE", "TOP ORDERED", "COUPON HACK"
    val iconEmoji: String,
    val actionLabel: String,
    val platform: String,
    val category: String, // "Rides", "Food", "Mart", "Stays"
    val price: Double
)

class ChaloViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ChaloDatabase.getInstance(application)
    private val repository = ChaloRepository(database)

    // Onboarding persistent state via SharedPreferences
    private val prefs = application.getSharedPreferences("chalo_prefs", android.content.Context.MODE_PRIVATE)
    var isOnboardingCompleted by mutableStateOf(prefs.getBoolean("onboarding_completed", false))
    var onboardingStep by mutableStateOf(0)

    // Proactive AI recommendations state
    var aiRecommendations by mutableStateOf<List<ChaloRecommendation>>(emptyList())
    var isRecommendationsLoading by mutableStateOf(false)
    var recommendationExplanation by mutableStateOf<String?>(null)

    // --- Flows from persistent DB ---
    val userProfile: StateFlow<UserEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivities: StateFlow<List<ActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = repository.walletTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive State variables ---
    var activeTab by mutableStateOf(0) // 0: Home, 1: Activity, 2: AI Chat, 3: Account
    
    // Address management
    var currentAddress by mutableStateOf("H-25, Connaught Place, New Delhi")
    var isAddressSelectorExpanded by mutableStateOf(false)
    var isLocationPermissionGranted by mutableStateOf(true)

    // Search query & responses
    var globalQuery by mutableStateOf("")
    var aiSearchResponse by mutableStateOf<String?>(null)
    var aiSearchLoading by mutableStateOf(false)

    // Chat assistant
    var aiChatMessages by mutableStateOf(
        listOf(
            ChatMessage("Namaste, I am Chalo's AI Super Assistant! 🇮🇳✨\nI can compare prices, track canteens, suggest travel plans, and help you save money. Choose a prompt or message me anything!", false)
        )
    )
    var aiChatLoading by mutableStateOf(false)

    // Smart Travel Planner state
    var plannerDestination by mutableStateOf("Goa")
    var plannerDurationDays by mutableStateOf("3")
    var plannerBudget by mutableStateOf("25000")
    var plannerPeopleCount by mutableStateOf("2")
    var plannerTransportPref by mutableStateOf("Flight")
    var plannerResult by mutableStateOf<String?>(null)
    var plannerLoading by mutableStateOf(false)

    // Module-specific form inputs
    // Rides
    var ridesPickup by mutableStateOf("Connaught Place, Delhi")
    var ridesDestination by mutableStateOf("DLF Cyber City, Gurugram")
    var selectedRideVehicle by mutableStateOf("Sedan") // Bike, Auto, Sedan, SUV, Premium
    var rideComparisonType by mutableStateOf("Uber") // Uber, Ola, Rapido, Namma Yatri, BluSmart
    var simulatedRidePrice by mutableStateOf(320.0)
    var simulatedRideETA by mutableStateOf(12) // mins

    // Intercity
    var intercityFrom by mutableStateOf("Delhi")
    var intercityTo by mutableStateOf("Jaipur")
    var intercityPassengers by mutableStateOf("4")
    var intercityLuggage by mutableStateOf("2")

    // Food
    var foodQuery by mutableStateOf("Chicken Biryani")
    var foodResultPlatform by mutableStateOf("Swiggy")

    // Mart
    var martQuery by mutableStateOf("Fresh Milk 1L")

    // UI Overlay control
    var isCartOpen by mutableStateOf(false)

    // Booking simulator live fields
    var rideProgressStatus by mutableStateOf<String?>(null)
    var rideProgressDetails by mutableStateOf<String?>(null)

    init {
        // Fetch currentAddress from DB when available
        viewModelScope.launch {
            userProfile.collect { profile ->
                profile?.let {
                    currentAddress = it.currentAddress
                    generateSmartRecommendations()
                }
            }
        }
    }

    // --- Onboarding Controls ---
    fun completeOnboarding() {
        isOnboardingCompleted = true
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun startOnboarding() {
        isOnboardingCompleted = false
        onboardingStep = 0
    }

    // --- Proactive AI Recommendation Engine ---
    fun generateSmartRecommendations() {
        isRecommendationsLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val modePreference = userProfile.value?.preferencesJson?.substringAfter("\"Mode\":\"")?.substringBefore("\"") ?: "AI Recommended"
            val connectedApps = userProfile.value?.connectedAccountsJson ?: ""
            val pastActivities = repository.getAllActivitiesSync()

            // Construct Gemini request context
            val ordersSummary = pastActivities.take(5).joinToString(", ") { "${it.platform} ${it.category}: ${it.details} (₹${it.amount.toInt()})" }
            
            val systemPrompt = """
                You are the AI Recommendation Core Engine for Chalo Super App.
                The user has selected the optimizer preference mode: "$modePreference".
                Their connected accounts json is: "$connectedApps".
                Their recent orders history is: "$ordersSummary".

                Generate exactly 3 extremely realistic, highly targeted everyday consumer recommendations for India (using Ola, Uber, Swiggy, Zomato, Blinkit, Zepto, or Agoda).
                Each recommendation must have:
                1. Title: short, snappy (e.g. "Cheapest Dzire to Cyber City", "Paneer Lababdar Combo Match")
                2. Description: explain why we recommend this based on active preference "$modePreference" and their history/pricing (e.g. "Swap Agoda for Booking.com and use coupon CHALOSTAY to save ₹350 on your next weekend.")
                3. DealTag: uppercase, short (e.g. "CHEAPEST RIDE", "WEEKEND SAVINGS", "AI OPTIMIZER", "HEALTHY DELIGHT")
                4. IconEmoji: accurate emoji (e.g. "🚗", "🍛", "🏨", "🥛")
                5. ActionLabel: short phrase (e.g. "Book Ride", "Add to Cart", "Examine Deal")
                6. Platform: One of: "Uber", "Ola", "Rapido", "Swiggy", "Zomato", "Blinkit", "Zepto", "Booking.com", "Agoda"
                7. Category: One of: "Rides", "Food", "Mart", "Stays"
                8. Price: numeric average cost equivalent (e.g. 150.0)

                Output the exact list in raw, parseable JSON block format. Do not add markdown backticks or formatting other than the pure JSON matching this exact Kotlin class format:
                [
                  {
                    "title": "...",
                    "description": "...",
                    "dealTag": "...",
                    "iconEmoji": "...",
                    "actionLabel": "...",
                    "platform": "...",
                    "category": "...",
                    "price": 120.0
                  },
                  ...
                ]
            """.trimIndent()

            var fetchedList = emptyList<ChaloRecommendation>()

            // Call Gemini
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
                    val apiResponse = GeminiClient.generateContent(
                        prompt = "Produce 3 parseable recommendation JSON objects for $modePreference mode.",
                        systemInstructionText = systemPrompt
                    ).trim()

                    // Parse json safely
                    val cleanJson = apiResponse.substringAfter("[").substringBeforeLast("]")
                    if (cleanJson.isNotBlank()) {
                        val items = cleanJson.split("},")
                        fetchedList = items.mapNotNull { rawItem ->
                            try {
                                val chunk = if (rawItem.endsWith("}")) rawItem else "$rawItem}"
                                val title = chunk.substringAfter("\"title\":\"").substringBefore("\"")
                                val description = chunk.substringAfter("\"description\":\"").substringBefore("\"")
                                val dealTag = chunk.substringAfter("\"dealTag\":\"").substringBefore("\"")
                                val iconEmoji = chunk.substringAfter("\"iconEmoji\":\"").substringBefore("\"")
                                val actionLabel = chunk.substringAfter("\"actionLabel\":\"").substringBefore("\"")
                                val platform = chunk.substringAfter("\"platform\":\"").substringBefore("\"")
                                val category = chunk.substringAfter("\"category\":\"").substringBefore("\"")
                                val priceStr = chunk.substringAfter("\"price\":").substringBefore("}").trim().replace(",", "")
                                val price = priceStr.toDoubleOrNull() ?: 100.0
                                ChaloRecommendation(title, description, dealTag, iconEmoji, actionLabel, platform, category, price)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChaloRecommendations", "Error calling/parsing recommendation: ${e.message}")
            }

            // Fallback deterministic rules matching preferred superapp mode
            if (fetchedList.size < 3) {
                fetchedList = getLocalRuleRecommendations(modePreference)
            }

            withContext(Dispatchers.Main) {
                aiRecommendations = fetchedList.take(3)
                isRecommendationsLoading = false
                recommendationExplanation = "Personalized with ₹ Savings using your stated $modePreference strategy."
            }
        }
    }

    private fun getLocalRuleRecommendations(mode: String): List<ChaloRecommendation> {
        return when (mode) {
            "Cheapest First" -> listOf(
                ChaloRecommendation(
                    title = "Ride Smart: Swap Uber for Namma Yatri",
                    description = "Namma Yatri charges zero platform commission! Get the exact same auto ride to and from DLF Cyber City for ₹130 instead of ₹145.",
                    dealTag = "CHEAPEST AUTO",
                    iconEmoji = "🛺",
                    actionLabel = "Compare Rides",
                    platform = "Namma Yatri",
                    category = "Rides",
                    price = 130.0
                ),
                ChaloRecommendation(
                    title = "Groceries: Milk Bundle on Zepto",
                    description = "Apply code CHALOMART to save ₹30 on Nandini Milk 1L when paired with Farm Fresh Eggs 6pcs on Zepto today.",
                    dealTag = "GROCERY STEAL",
                    iconEmoji = "🥛",
                    actionLabel = "Add to Cart",
                    platform = "Zepto",
                    category = "Mart",
                    price = 98.0
                ),
                ChaloRecommendation(
                    title = "Double Swiggy Coupon Stack",
                    description = "Order Chicken Biryani. Use code 'WELCOME50' + Chalo Wallet points to reduce food bill from ₹275 to ₹190 with free delivery.",
                    dealTag = "50% OFF FOOD",
                    iconEmoji = "🍛",
                    actionLabel = "Compare Foods",
                    platform = "Swiggy",
                    category = "Food",
                    price = 190.0
                )
            )
            "Fastest First" -> listOf(
                ChaloRecommendation(
                    title = "Rapid Travel: Choose Rapido Bike Taxi",
                    description = "Heavy bottleneck detected on Outer Ring Road. Skip cabs and hop on a Rapido Bike to arrive in 11 mins instead of 32 mins.",
                    dealTag = "FAST BYPASS",
                    iconEmoji = "🏍️",
                    actionLabel = "Compare Rides",
                    platform = "Rapido",
                    category = "Rides",
                    price = 75.0
                ),
                ChaloRecommendation(
                    title = "Zepto Super-Fast: 10 Min Delivery",
                    description = "Get fresh groceries and breakfast essentials delivered to your door in exactly 9 mins from the local Saket dark store.",
                    dealTag = "9 MIN DELIVER",
                    iconEmoji = "⚡",
                    actionLabel = "Shop Mart",
                    platform = "Zepto",
                    category = "Mart",
                    price = 220.0
                ),
                ChaloRecommendation(
                    title = "Zomato Everyday instant canteen",
                    description = "Delicious homestyle dal chawal prepared in local Zomato Everyday cloud kitchens. Dispatched in 4 mins.",
                    dealTag = "INSTANT FOOD",
                    iconEmoji = "🍛",
                    actionLabel = "Compare Foods",
                    platform = "Zomato",
                    category = "Food",
                    price = 120.0
                )
            )
            "Best Rated" -> listOf(
                ChaloRecommendation(
                    title = "Elite Stay: Udaipur Grand Palace",
                    description = "Highly rated with a pristine 4.8/5 on MakeMyTrip. Includes private infinity pool access & dynamic buffet breakfasts.",
                    dealTag = "PREMIUM HOSPITALITY",
                    iconEmoji = "🏨",
                    actionLabel = "Book Stay",
                    platform = "MakeMyTrip",
                    category = "Stays",
                    price = 4500.0
                ),
                ChaloRecommendation(
                    title = "Safe Luxury: BluSmart All-Electric Cab",
                    description = "Arrive in style on a verified premium electric car. 5.0 rated professional drivers. Zero cancellations guaranteed.",
                    dealTag = "ZERO CANCEL",
                    iconEmoji = "🚙",
                    actionLabel = "Compare Rides",
                    platform = "Uber",
                    category = "Rides",
                    price = 380.0
                ),
                ChaloRecommendation(
                    title = "Top Biryani: Swiggy Gourmet",
                    description = "Order from 'Biryani by Kilo', rated 4.6 stars. Handcrafted clay handi biryani cooked individually on charcoal.",
                    dealTag = "LEGENDARY FOOD",
                    iconEmoji = "🍛",
                    actionLabel = "Compare Foods",
                    platform = "Swiggy",
                    category = "Food",
                    price = 390.0
                )
            )
            else -> listOf( // "AI Recommended" custom state
                ChaloRecommendation(
                    title = "Since you ordered Paneer on Swiggy",
                    description = "Try Swiggy's Handi Veg from Lucknowee today. It fits your savory Indian palate, and Swiggy Gold saves ₹45 on shipping.",
                    dealTag = "AI RECOMMEND MATCH",
                    iconEmoji = "😋",
                    actionLabel = "Compare Foods",
                    platform = "Swiggy",
                    category = "Food",
                    price = 280.0
                ),
                ChaloRecommendation(
                    title = "Weekly Grocery Restock Suggestion",
                    description = "You previously bought Milk & Bread on Blinkit. Easily restock today to earn 90 coin ledger points and maintain your daily routine.",
                    dealTag = "HEALTH ROUTINE",
                    iconEmoji = "🥛",
                    actionLabel = "Add to Cart",
                    platform = "Blinkit",
                    category = "Mart",
                    price = 98.0
                ),
                ChaloRecommendation(
                    title = "Uber Premier Weekend discount",
                    description = "Uber offers a personalized ₹100 discount coupon CHALOUBER tailored around your frequent CP to Cyber City trip routing pattern.",
                    dealTag = "ROUTING SAVINGS",
                    iconEmoji = "🚗",
                    actionLabel = "Compare Rides",
                    platform = "Uber",
                    category = "Rides",
                    price = 220.0
                )
            )
        }
    }

    // --- Action Methods ---

    fun changeActiveAddress(address: String) {
        currentAddress = address
        isAddressSelectorExpanded = false
        // Update user Entity
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(currentAddress = address))
            }
        }
    }

    fun toggleLocationPermission() {
        isLocationPermissionGranted = !isLocationPermissionGranted
    }

    // Actual Google Location Service Intergration
    @android.annotation.SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: android.content.Context) {
        if (!isLocationPermissionGranted) return
        
        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                // In a real app we'd reverse-geocode it securely, here we just show the coords
                currentAddress = "Lat: ${location.latitude.toString().take(6)}, Lng: ${location.longitude.toString().take(6)}"
            } else {
                currentAddress = "Location fetched but unavailable."
            }
        }.addOnFailureListener {
            currentAddress = "Failed to fetch GPS"
        }
    }

    fun setTab(index: Int) {
        activeTab = index
    }

    fun executeGlobalSearch() {
        if (globalQuery.isBlank()) return
        aiSearchLoading = true
        aiSearchResponse = null

        viewModelScope.launch(Dispatchers.IO) {
            val systemGuide = """
                You are the core intelligence of Chalo - India's Super App.
                Analyze the user query: "${globalQuery}" and output a clean, comparative decision block.
                Keep it very visually engaging, structured with emojis, and highly relevant to India (mentioning Indian Rupee values, typical Indian travel times, and comparing top local brands Swiggy vs Zomato vs EatSure or Uber vs Ola vs Rapido vs Namma Yatri or Blinkit vs Zepto vs Instamart).
                Show a table or comparison list of Price, Speed, and Rating, and give a clear recommendation on what platform provides the best deal today. Offer a tip to save money.
            """.trimIndent()

            val response = GeminiClient.generateContent(
                prompt = "Compare and optimize this search query: ${globalQuery}",
                systemInstructionText = systemGuide
            )

            withContext(Dispatchers.Main) {
                aiSearchResponse = response
                aiSearchLoading = false
            }
        }
    }

    fun submitChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text, true)
        aiChatMessages = aiChatMessages + userMsg
        aiChatLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            // Build chat history context for Gemini API
            val historyContent = aiChatMessages.map { 
                GeminiContent(
                    parts = listOf(GeminiPart(text = it.text)),
                    role = if (it.isUser) "user" else "model"
                )
            }.takeLast(10) // Only send last 10 messages to avoid token bloating

            val systemPrompt = """
                You are the intelligent Chalo - India's Everyday Super App Chatbot.
                Your goal is to save users time, money, and cognitive fatigue. 
                Keep answers extremely helpful, brief, conversational, and direct. 
                Use Indian Rupee (₹) and reference local contexts like Indian cities, foods (Biryani, Samosa, Chaat), and platforms:
                - Rides: Ola, Uber, Rapido, Namma Yatri, BluSmart
                - Food: Swiggy, Zomato, EatSure
                - Grocery/Mart: Blinkit, Zepto, Dunzo, BigBasket, Instamart
                - Stays: booking.com, Agoda, MakeMyTrip
                Guide them on how they can earn Chalo Wallet points (20 Points = ₹1) on every booking and manage their budget. Use bold text and bullet points.
            """.trimIndent()

            val reply = GeminiClient.generateContent(
                prompt = text,
                systemInstructionText = systemPrompt,
                history = historyContent
            )

            withContext(Dispatchers.Main) {
                aiChatMessages = aiChatMessages + ChatMessage(reply, false)
                aiChatLoading = false
            }
        }
    }

    fun generateTravelItinerary() {
        plannerLoading = true
        plannerResult = null

        viewModelScope.launch(Dispatchers.IO) {
            val prompt = """
                Plan a complete itinerary for ${plannerDestination}:
                - Duration: ${plannerDurationDays} days
                - Budget: ₹${plannerBudget}
                - Travelers: ${plannerPeopleCount} people
                - Transport Preference: ${plannerTransportPref}
            """.trimIndent()

            val systemGuide = """
                You are Chalo's Elite Smart Travel Planner (Grounding-enabled style).
                Design a breathtaking day-by-day travel plan including:
                1. Beautiful customizable daily itinerary (Morning, Afternoon, Evening)
                2. Recommended Stays (comparing Agoda, Booking.com, and MakeMyTrip pricing ranges)
                3. Best local transport hacks (Auto, Cab, Metro options)
                4. Local delicacies & recommended food joints (praising specific dishes)
                5. Complete budget splits and travel hacks to save up to ₹5,000 on this trip.
                Output in clean, markdown syntax with clear headings, bullets, and bold emphasis. Focus on authenticity!
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt = prompt, systemInstructionText = systemGuide)

            withContext(Dispatchers.Main) {
                plannerResult = response
                plannerLoading = false

                // Increment dummy wallet points as visual delight!
                userProfile.value?.let { profile ->
                    val newPoints = profile.walletPoints + 50
                    val transaction = WalletTransactionEntity(
                        description = "Smart Travel Plan bonus",
                        points = 50,
                        isCredit = true
                    )
                    repository.updateProfile(profile.copy(walletPoints = newPoints))
                    repository.insertTransaction(transaction)
                }
            }
        }
    }

    // --- Unified Cart Management ---

    fun addToCart(name: String, platform: String, category: String, price: Double, deliveryFee: Double = 0.0) {
        viewModelScope.launch {
            val existing = repository.getCartItemsSync()
            val match = existing.find { it.itemName == name && it.platform == platform }
            if (match != null) {
                repository.updateCartQuantity(match.id, match.quantity + 1)
            } else {
                repository.insertCartItem(
                    CartItemEntity(
                        itemName = name,
                        platform = platform,
                        category = category,
                        price = price,
                        deliveryFee = deliveryFee
                    )
                )
            }
        }
    }

    fun incrementCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.updateCartQuantity(item.id, item.quantity + 1)
        }
    }

    fun decrementCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            if (item.quantity <= 1) {
                repository.deleteCartItem(item.id)
            } else {
                repository.updateCartQuantity(item.id, item.quantity - 1)
            }
        }
    }

    fun checkoutCart(useWalletPoints: Boolean) {
        val items = cartItems.value
        val profile = userProfile.value
        if (items.isEmpty() || profile == null) return

        viewModelScope.launch {
            val totalItemCost = items.sumOf { it.price * it.quantity }
            val totalDelivery = items.distinctBy { it.platform }.sumOf { it.deliveryFee }
            val absoluteTotal = totalItemCost + totalDelivery

            var pointsUsed = 0
            var rupeeValueSaved = 0.0
            if (useWalletPoints && profile.walletPoints > 0) {
                // 20 points = 1 rupee
                val maxPointsEquivalent = profile.walletPoints / 20
                if (maxPointsEquivalent >= absoluteTotal) {
                    pointsUsed = (absoluteTotal * 20).toInt()
                    rupeeValueSaved = absoluteTotal
                } else {
                    pointsUsed = profile.walletPoints
                    rupeeValueSaved = profile.walletPoints / 20.0
                }
            }

            val finalPayable = absoluteTotal - rupeeValueSaved
            val pointsEarned = (totalItemCost * 0.1).toInt() // 10% cash-back in points on spend!

            val newPointsBalance = profile.walletPoints - pointsUsed + pointsEarned

            // Update user profile wallet point balance
            repository.updateProfile(profile.copy(walletPoints = newPointsBalance))

            // Add wallet debit transaction if points used
            if (pointsUsed > 0) {
                repository.insertTransaction(
                    WalletTransactionEntity(
                        description = "Defrayed cart costs with points",
                        points = pointsUsed,
                        isCredit = false
                    )
                )
            }
            // Add wallet credit cash-back
            if (pointsEarned > 0) {
                repository.insertTransaction(
                    WalletTransactionEntity(
                        description = "Super App shopping cash-back",
                        points = pointsEarned,
                        isCredit = true
                    )
                )
            }

            // Group items by platform and create Activities
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateStr = "Today"
            val timeStr = timeSdf.format(Date())

            items.groupBy { it.platform }.forEach { (platform, platformItems) ->
                val platformCost = platformItems.sumOf { it.price * it.quantity }
                val detailsStr = platformItems.joinToString(", ") { "${it.itemName} x${it.quantity}" }
                val randomId = (100000..999999).random().toString()

                val testActivity = ActivityEntity(
                    orderId = "CH-$randomId",
                    platform = platform,
                    category = platformItems.first().category,
                    date = dateStr,
                    time = timeStr,
                    amount = platformCost,
                    status = "Ongoing",
                    details = detailsStr,
                    paymentMethod = if (useWalletPoints && pointsUsed > 0) "Chalo Wallet + Card" else "UPI / Net Banking"
                )
                repository.insertActivity(testActivity)
                
                // Track simulated delivery status for Ongoing checkout activities
                simulateOngoingStatusTransition(platform, platformItems.first().category, "CH-$randomId")
            }

            // Clear the cart & close overlay
            repository.clearCart()
            isCartOpen = false
            activeTab = 1 // Redirect to activities timeline so the user sees their live status!
        }
    }

    // --- Dynamic Booking Simulators ---

    fun bookSimulatedRide(platform: String, vehicle: String, price: Double) {
        val randId = (100000..999999).random().toString()
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = timeSdf.format(Date())

        viewModelScope.launch {
            val rideAct = ActivityEntity(
                orderId = "CH-$randId",
                platform = platform,
                category = "Rides",
                date = "Today",
                time = timeStr,
                amount = price,
                status = "Ongoing",
                details = "$vehicle Booking - Cabs comparing $platform",
                paymentMethod = "UPI"
            )
            repository.insertActivity(rideAct)

            // Dynamic tracking notification toast triggers:
            rideProgressStatus = "Searching..."
            rideProgressDetails = "Broadcasting request to nearby $platform drivers..."

            // Switch to Activity tab to witness the live progress
            activeTab = 1

            // Simulate Driver Status Updates:
            delay(3000)
            rideProgressStatus = "Arriving Soon"
            rideProgressDetails = "Driver Ramesh (White Dzire - DL1R-7489) accepted! ETA 3m. ⭐4.8"
            
            // Log point credit
            val profile = repository.getUserProfileSync()
            profile?.let {
                val newPoints = it.walletPoints + 40
                repository.updateProfile(it.copy(walletPoints = newPoints))
                repository.insertTransaction(
                    WalletTransactionEntity(
                        description = "Ride cashback points",
                        points = 40,
                        isCredit = true
                    )
                )
            }

            delay(5000)
            rideProgressStatus = "On Trip"
            rideProgressDetails = "You are currently on-trip from CP to Cyber City. Share live tracking with family."

            delay(6000)
            rideProgressStatus = "Completed"
            rideProgressDetails = "Arrived safely! Ride fee loaded. Saved ₹28 comparing with Namma Yatri."
            repository.insertActivity(
                rideAct.copy(status = "Completed", details = "$vehicle Cab ($platform) - CP to DLF Cyber City - DL1R-7489")
            )
        }
    }

    private fun simulateOngoingStatusTransition(platform: String, category: String, orderId: String) {
        viewModelScope.launch {
            delay(5000)
            // Retrieve current database activities to find this order
            val currentList = repository.getAllActivitiesSync()
            val match = currentList.find { it.orderId == orderId }
            match?.let {
                val updatedDetails = if (category == "Food") "Out for Delivery - Rider Kabir is 1.5km away" else "Order Processed - Preparing Package"
                repository.insertActivity(it.copy(details = "$updatedDetails (${it.details})"))
            }

            delay(8000)
            val updatedList = repository.getAllActivitiesSync()
            val secondMatch = updatedList.find { it.orderId == orderId }
            secondMatch?.let {
                repository.insertActivity(it.copy(status = "Completed", details = "Delivered! Saved ₹15 with Chalo Smart Routing (${it.details.substringAfter("- ")})"))
            }
        }
    }

    // Connect / Disconnect third-party apps
    fun toggleConnectedAccount(app: String) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val json = profile.connectedAccountsJson
                val updatedJson = when {
                    json.contains("\"$app\":true") -> {
                        json.replace("\"$app\":true", "\"$app\":false")
                    }
                    json.contains("\"$app\":false") -> {
                        json.replace("\"$app\":false", "\"$app\":true")
                    }
                    else -> {
                        if (json.endsWith("}")) {
                            val inner = json.substring(1, json.length - 1)
                            val comma = if (inner.trim().isEmpty()) "" else ","
                            "{$inner$comma\"$app\":true}"
                        } else {
                            "{\"$app\":true}"
                        }
                    }
                }
                repository.updateProfile(profile.copy(connectedAccountsJson = updatedJson))
            }
        }
    }

    val reauthorizingApps = mutableStateMapOf<String, Boolean>()

    fun reauthorizeAccount(app: String) {
        viewModelScope.launch {
            reauthorizingApps[app] = true
            delay(1200) // Simulate local OAuth API network trip
            userProfile.value?.let { profile ->
                val json = profile.connectedAccountsJson
                val updatedJson = when {
                    json.contains("\"$app\":true") -> json
                    json.contains("\"$app\":false") -> json.replace("\"$app\":false", "\"$app\":true")
                    else -> {
                        if (json.endsWith("}")) {
                            val inner = json.substring(1, json.length - 1)
                            val comma = if (inner.trim().isEmpty()) "" else ","
                            "{$inner$comma\"$app\":true}"
                        } else {
                            "{\"$app\":true}"
                        }
                    }
                }
                repository.updateProfile(profile.copy(connectedAccountsJson = updatedJson))
            }
            reauthorizingApps[app] = false
        }
    }

    // Change Preference Mode
    fun changePreferenceMode(mode: String) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val json = profile.preferencesJson
                // Basic regex-free mode replace
                val modeParts = json.split("\"Mode\":\"")
                if (modeParts.size == 2) {
                    val updatedPreferences = modeParts[0] + "\"Mode\":\"" + mode + "\"}"
                    repository.updateProfile(profile.copy(preferencesJson = updatedPreferences))
                }
            }
        }
    }

    // Perform Referral signup simulator
    fun simulateReferralLinkShare() {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val updatedPoints = profile.walletPoints + 2000
                val updatedReferralCount = profile.referralPointsEarned + 2000
                repository.updateProfile(
                    profile.copy(
                        walletPoints = updatedPoints,
                        referralPointsEarned = updatedReferralCount
                    )
                )
                repository.insertTransaction(
                    WalletTransactionEntity(
                        description = "Simulated Referral sign-up bonus",
                        points = 2000,
                        isCredit = true
                    )
                )
                // Add an completed activity log for visual delight
                repository.insertActivity(
                    ActivityEntity(
                        orderId = "CH-REF-" + (1000..9999).random(),
                        platform = "Chalo",
                        category = "Wallet",
                        date = "Today",
                        time = "Referral Reward",
                        amount = 100.0,
                        status = "Completed",
                        details = "Referral bonus Rs.100 credited in platform ledger"
                    )
                )
            }
        }
    }

    // --- Dynamic Address & Payment Management Systems ---

    fun getPaymentMethods(): List<PaymentMethod> {
        val profile = userProfile.value
        val json = profile?.preferencesJson ?: ""
        val rawString = if (json.contains("\"payments\":\"")) {
            json.substringAfter("\"payments\":\"").substringBefore("\"")
        } else {
            "Chalo Pay Wallet:kunal@chalo:true:🪙;HDFC CC:•••• 4321:false:💳;Google Pay:kunal@okhdfc:false:⚡;PayTM Wallet:9876543212:false:📱"
        }
        if (rawString.isBlank()) return emptyList()
        return try {
            rawString.split(";").filter { it.isNotBlank() }.map {
                val parts = it.split(":")
                PaymentMethod(
                    name = parts.getOrNull(0) ?: "Card",
                    details = parts.getOrNull(1) ?: "",
                    isPreferred = parts.getOrNull(2) == "true",
                    iconEmoji = parts.getOrNull(3) ?: "💳"
                )
            }
        } catch (e: Exception) {
            listOf(
                PaymentMethod("Chalo Pay Wallet", "kunal@chalo", true, "🪙"),
                PaymentMethod("HDFC CC", "•••• 4321", false, "💳"),
                PaymentMethod("Google Pay", "kunal@okhdfc", false, "⚡"),
                PaymentMethod("PayTM Wallet", "9876543212", false, "📱")
            )
        }
    }

    fun savePaymentMethods(methods: List<PaymentMethod>) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val json = profile.preferencesJson
                val serialized = methods.joinToString(";") { "${it.name}:${it.details}:${it.isPreferred}:${it.iconEmoji}" }
                val updatedJson = if (json.contains("\"payments\":\"")) {
                    val beforePayments = json.substringBefore("\"payments\":\"")
                    val afterPaymentsVal = json.substringAfter("\"payments\":\"")
                    val afterPaymentsQuote = afterPaymentsVal.substringAfter("\"")
                    beforePayments + "\"payments\":\"" + serialized + "\"" + afterPaymentsQuote
                } else {
                    val base = json.trim()
                    if (base.endsWith("}")) {
                        base.substring(0, base.length - 1) + ",\"payments\":\"" + serialized + "\"}"
                    } else {
                        json
                    }
                }
                repository.updateProfile(profile.copy(preferencesJson = updatedJson))
            }
        }
    }

    fun addPaymentMethod(name: String, details: String, emoji: String) {
        val current = getPaymentMethods().toMutableList()
        val isPref = current.none { it.isPreferred }
        current.add(PaymentMethod(name, details, isPref, emoji))
        savePaymentMethods(current)
    }

    fun deletePaymentMethod(index: Int) {
        val current = getPaymentMethods().toMutableList()
        if (index in current.indices) {
            val wasPreferred = current[index].isPreferred
            current.removeAt(index)
            if (wasPreferred && current.isNotEmpty()) {
                current[0] = current[0].copy(isPreferred = true)
            }
            savePaymentMethods(current)
        }
    }

    fun setPreferredPaymentMethod(index: Int) {
        val current = getPaymentMethods().mapIndexed { idx, pm ->
            pm.copy(isPreferred = idx == index)
        }
        savePaymentMethods(current)
    }

    fun addSavedAddress(label: String, value: String) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val currentStr = profile.savedAddressesJson.trim()
                val cleanValue = value.replace(";", "").replace(":", "")
                val cleanLabel = label.replace(";", "").replace(":", "")
                val updatedStr = if (currentStr.isBlank()) {
                    "$cleanLabel: $cleanValue"
                } else {
                    "$currentStr; $cleanLabel: $cleanValue"
                }
                repository.updateProfile(profile.copy(savedAddressesJson = updatedStr))
            }
        }
    }

    fun deleteSavedAddress(index: Int) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val currentAddresses = listAddresses(profile.savedAddressesJson).toMutableList()
                if (index in currentAddresses.indices) {
                    val deletedValue = currentAddresses[index].second
                    currentAddresses.removeAt(index)
                    
                    val updatedStr = currentAddresses.joinToString("; ") { "${it.first}: ${it.second}" }
                    
                    var updatedActive = profile.currentAddress
                    if (profile.currentAddress == deletedValue) {
                        if (currentAddresses.isNotEmpty()) {
                            updatedActive = currentAddresses[0].second
                            currentAddress = updatedActive
                        }
                    }
                    
                    repository.updateProfile(profile.copy(
                        savedAddressesJson = updatedStr,
                        currentAddress = updatedActive
                    ))
                }
            }
        }
    }

    fun setPreferredAddressFromList(index: Int) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val currentAddresses = listAddresses(profile.savedAddressesJson)
                if (index in currentAddresses.indices) {
                    val targetAddress = currentAddresses[index].second
                    currentAddress = targetAddress
                    repository.updateProfile(profile.copy(currentAddress = targetAddress))
                }
            }
        }
    }
}

data class PaymentMethod(
    val name: String,
    val details: String,
    val isPreferred: Boolean,
    val iconEmoji: String = "💳"
)

