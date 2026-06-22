package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.db.ActivityEntity
import com.example.db.CartItemEntity
import com.example.db.UserEntity
import com.example.db.WalletTransactionEntity
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChaloAppMain(viewModel: ChaloViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val activities by viewModel.allActivities.collectAsStateWithLifecycle()
    val transactions by viewModel.walletTransactions.collectAsStateWithLifecycle()

    var showAddressDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, "tab_home"),
                    Triple("Activity", Icons.Default.List, "tab_activity"),
                    Triple("AI Chat", Icons.Default.Face, "tab_ai"),
                    Triple("Account", Icons.Default.AccountCircle, "tab_account")
                )
                tabs.forEachIndexed { index, (label, icon, tag) ->
                    NavigationBarItem(
                        selected = viewModel.activeTab == index,
                        onClick = { viewModel.setTab(index) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronOrange,
                            selectedTextColor = SaffronOrange,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = SaffronOrange.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (cartItems.isNotEmpty() && !viewModel.isCartOpen) {
                FloatingActionButton(
                    onClick = { viewModel.isCartOpen = true },
                    containerColor = SaffronOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("floating_cart_fab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-8).dp)
                                .background(Color.Red, CircleShape)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = cartItems.sumOf { it.quantity }.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            // Background ambient gradients for high-fidelity luxury vibe
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SaffronOrange.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.2f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SavingGreen.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.7f),
                        radius = size.width * 0.8f
                    )
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Address Location Header (Blinkit style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = SaffronOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showAddressDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Current Location",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = viewModel.currentAddress,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Simulated live GPS toggle button
                    IconButton(
                        onClick = {
                            viewModel.toggleLocationPermission()
                            // Try to fetch location physically if granted
                            if (viewModel.isLocationPermissionGranted) {
                                viewModel.fetchCurrentLocation(context)
                            }
                        },
                        modifier = Modifier.testTag("location_permission_toggle")
                    ) {
                        Icon(
                            imageVector = if (viewModel.isLocationPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "GPS Toggle",
                            tint = if (viewModel.isLocationPermissionGranted) SavingGreen else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tiny Cart indicator icon
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.isCartOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart Open",
                                tint = SaffronOrange
                            )
                        }
                    }
                }

                // Floating Active simulated Ride status panel
                viewModel.rideProgressStatus?.let { status ->
                    if (status != "Completed") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.95f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Live Ride: $status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = viewModel.rideProgressDetails ?: "",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Screen container
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = viewModel.activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "tab_transitions"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> HomeScreen(viewModel, userProfile)
                            1 -> ActivityScreen(viewModel, activities)
                            2 -> AIAssistantScreen(viewModel)
                            3 -> AccountScreen(viewModel, userProfile, transactions)
                        }
                    }
                }
            }

            // Cart Bottom Sheet Overlay
            if (viewModel.isCartOpen) {
                UnifiedCartOverlay(viewModel = viewModel, cartItems = cartItems, userEntity = userProfile)
            }

            // User Onboarding Guide Tour modal
            if (!viewModel.isOnboardingCompleted) {
                OnboardingOverlay(viewModel = viewModel)
            }
        }
    }

    // Address list selector popup dialog
    if (showAddressDialog) {
        Dialog(onDismissRequest = { showAddressDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Delivery Address",
                        fontWeight = FontWeight.Bold,
                        color = SaffronOrange,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val addresses = listAddresses(userProfile?.savedAddressesJson)
                    addresses.forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeAddressAndDismiss(value)
                                    showAddressDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(value, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

// Helpers
fun listAddresses(json: String?): List<Pair<String, String>> {
    if (json.isNullOrBlank()) {
        return listOf(
            "Home" to "H-25, Connaught Place, New Delhi",
            "Work" to "DLF Cyber City, Gurugram"
        )
    }
    return try {
        json.split(";").map {
            val parts = it.split(":")
            if (parts.size >= 2) {
                parts[0].trim() to parts.subList(1, parts.size).joinToString(":").trim()
            } else {
                "Saved Spot" to it.trim()
            }
        }
    } catch (e: Exception) {
        listOf("Saved spot" to json)
    }
}

fun ChaloViewModel.changeAddressAndDismiss(value: String) {
    changeActiveAddress(value)
}

// --- SCREEN 1: HOME PAGE ---

enum class HomeModule { Rides, Intercity, Food, Mart, Stays, Planner }

@Composable
fun HomeScreen(viewModel: ChaloViewModel, userEntity: UserEntity?) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Store the expanded/collapsed state for all 6 core categories
    val expandedStates = remember {
        mutableStateMapOf<HomeModule, Boolean>().apply {
            put(HomeModule.Rides, false)
            put(HomeModule.Food, false)
            put(HomeModule.Mart, false)
            put(HomeModule.Intercity, false)
            put(HomeModule.Stays, false)
            put(HomeModule.Planner, false)
        }
    }

    val activeMod = HomeModule.values().firstOrNull { expandedStates[it] == true }

    if (activeMod != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { expandedStates[activeMod] = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                val title = when (activeMod) {
                    HomeModule.Rides -> "🚗 Cab & Rides"
                    HomeModule.Food -> "😋 Food Delivery"
                    HomeModule.Mart -> "🥛 Quick Mart"
                    HomeModule.Intercity -> "🛺 Outstation"
                    HomeModule.Stays -> "🏨 Stays"
                    HomeModule.Planner -> "✨ AI Travel Planner"
                }
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = GlassBorder)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when (activeMod) {
                    HomeModule.Rides -> RidesModuleView(viewModel)
                    HomeModule.Food -> FoodModuleView(viewModel)
                    HomeModule.Mart -> MartModuleView(viewModel)
                    HomeModule.Intercity -> IntercityModuleView(viewModel)
                    HomeModule.Stays -> StaysModuleView(viewModel)
                    HomeModule.Planner -> TravelPlannerView(viewModel)
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Image
            item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Chalo Super App Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Black mask
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkBg.copy(alpha = 0.95f))
                            )
                        )
                )

                // High Contrast Promo Text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🇮🇳 CHALO SUPER APP",
                        color = SaffronOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Compare Rides, Foods, Grocery & Lodging",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // --- INLINE SEARCH BAR ON THE PAGE ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.globalQuery,
                    onValueChange = { viewModel.globalQuery = it },
                    placeholder = { Text("Jaipur Trip under 10k, Biryani Swiggy...", fontSize = 13.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("search_field_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronOrange,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = SaffronOrange)
                    },
                    trailingIcon = {
                        if (viewModel.globalQuery.isNotEmpty() || viewModel.aiSearchResponse != null) {
                            IconButton(onClick = { 
                                viewModel.globalQuery = "" 
                                viewModel.aiSearchResponse = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.executeGlobalSearch()
                        keyboardController?.hide()
                    })
                )

                // Suggestion Trends directly on the page
                if (viewModel.globalQuery.isEmpty() && viewModel.aiSearchResponse == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                    ) {
                        val promptSamples = listOf(
                            "Cheapest grocery from Blinkit vs Zepto",
                            "Delhi to Jaipur cab 4 passengers",
                            "Cheapest flight to Goa",
                            "Best Biryani under ₹300 Swiggy"
                        )
                        promptSamples.forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(GlassWhite, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.globalQuery = prompt
                                        viewModel.executeGlobalSearch()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(prompt, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Inline Search Progress / Results Card
                if (viewModel.aiSearchLoading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = SaffronOrange, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing rates, distances, and ETAs instantly...", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                } else if (viewModel.aiSearchResponse != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("global_search_results_card"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🤖 Chalo AI Comparative Results", fontWeight = FontWeight.Black, color = SaffronOrange, fontSize = 13.sp)
                                IconButton(
                                    onClick = { 
                                        viewModel.globalQuery = "" 
                                        viewModel.aiSearchResponse = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(viewModel.aiSearchResponse ?: "", fontSize = 12.sp, color = Color.White, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }

        // --- CATEGORY ICONS QUICK SELECTOR (GRID) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val modules = HomeModule.values().toList()
                modules.chunked(3).forEach { rowMods ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowMods.forEach { mod ->
                            val emoji = when (mod) {
                                HomeModule.Rides -> "🚗"
                                HomeModule.Food -> "😋"
                                HomeModule.Mart -> "🥛"
                                HomeModule.Intercity -> "🛺"
                                HomeModule.Stays -> "🏨"
                                HomeModule.Planner -> "✨"
                            }
                            val label = when (mod) {
                                HomeModule.Rides -> "Rides"
                                HomeModule.Food -> "Food"
                                HomeModule.Mart -> "Mart"
                                HomeModule.Intercity -> "Outstation"
                                HomeModule.Stays -> "Stays"
                                HomeModule.Planner -> "AI Plan"
                            }
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clickable {
                                        HomeModule.values().forEach { m -> expandedStates[m] = (m == mod) }
                                    }
                                    .testTag("icon_category_${mod.name.lowercase()}"),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Text(emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Add empty boxes if row is not full to maintain spacing
                        if (rowMods.size < 3) {
                            repeat(3 - rowMods.size) {
                                Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Smart AI Recommendations Section (proactive insights, always show below categories list)
        item {
            ChaloRecommendationsView(
                viewModel = viewModel,
                onModuleSelected = { mod -> expandedStates[mod] = true }
            )
        }

        // Referral Box & Quick Shortcut
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_refer_gift),
                        contentDescription = "Gifts",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Earn ₹100 inside Wallet! 💸",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Share Chalo with friends. They sign up, you both get 2000 Points!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                viewModel.simulateReferralLinkShare()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simulate Invite", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Preference optimization mode display
        item {
            userEntity?.let { profile ->
                val optMode = profile.preferencesJson.substringAfter("\"Mode\":\"").substringBefore("\"")
                Card(
                    colors = CardDefaults.cardColors(containerColor = SavingGreen.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SavingGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "",
                            tint = SavingGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Optimizer Mode is set to ",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = optMode,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = SavingGreen
                        )
                    }
                }
            }
        }
    }
    } // this closes the else block
}

// --- RIDES COMPONENT ---
@Composable
fun RidesModuleView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Compare Cabs", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.ridesPickup,
                onValueChange = { viewModel.ridesPickup = it },
                label = { Text("Pickup Location") },
                modifier = Modifier.fillMaxWidth().testTag("rides_pickup_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronOrange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = SaffronOrange,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.ridesDestination,
                onValueChange = { viewModel.ridesDestination = it },
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth().testTag("rides_dest_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronOrange,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = SaffronOrange,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Select Vehicle Class", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                val vehicles = listOf("Bike", "Auto", "Sedan", "SUV", "Premium")
                vehicles.forEach { type ->
                    val isSel = viewModel.selectedRideVehicle == type
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) SaffronOrange.copy(alpha = 0.2f) else GlassWhite)
                            .border(1.dp, if (isSel) SaffronOrange else GlassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectedRideVehicle = type
                                viewModel.simulatedRidePrice = when (type) {
                                    "Bike" -> 90.0
                                    "Auto" -> 140.0
                                    "Sedan" -> 280.0
                                    "SUV" -> 390.0
                                    "Premium" -> 550.0
                                    else -> 300.0
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(type, color = if (isSel) SaffronOrange else Color.LightGray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Compare Quotes Side-by-Side", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val prices = listOf(
                Triple("Uber", viewModel.simulatedRidePrice, "⭐ 4.7 • 8 min away"),
                Triple("Ola", viewModel.simulatedRidePrice + 15.0, "⭐ 4.5 • 11 min away (No surge)"),
                Triple("Rapido", viewModel.simulatedRidePrice - 20.0, "⭐ 4.2 • 14 min away"),
                Triple("Namma Yatri", viewModel.simulatedRidePrice - 5.0, "⭐ 4.7 • Zero Commission")
            )

            prices.forEach { (platform, price, meta) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val logoEmoji = when (platform) {
                                "Uber" -> "⬛"
                                "Ola" -> "🟩"
                                "Rapido" -> "🟨"
                                else -> "🛺"
                            }
                            Text("$logoEmoji $platform", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                        }
                        Text(meta, color = Color.Gray, fontSize = 11.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${price.toInt()}", fontWeight = FontWeight.Black, color = SavingGreen, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                viewModel.bookSimulatedRide(platform, viewModel.selectedRideVehicle, price)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp).testTag("book_button_${platform}")
                        ) {
                            Text("Book", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- INTERCITY MODULE COMPONENT ---
@Composable
fun IntercityModuleView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Intercity Cab Routing & Recommendation", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.intercityFrom,
                    onValueChange = { viewModel.intercityFrom = it },
                    label = { Text("From") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
                OutlinedTextField(
                    value = viewModel.intercityTo,
                    onValueChange = { viewModel.intercityTo = it },
                    label = { Text("To") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.intercityPassengers,
                    onValueChange = { viewModel.intercityPassengers = it },
                    label = { Text("Passengers") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
                OutlinedTextField(
                    value = viewModel.intercityLuggage,
                    onValueChange = { viewModel.intercityLuggage = it },
                    label = { Text("Luggage Bags") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            val pCount = viewModel.intercityPassengers.toIntOrNull() ?: 1
            val recommendation = when {
                pCount <= 2 -> Pair("Sedan (Maruti Dzire)", "Small compact and highly cost efficient for small group.")
                pCount in 3..5 -> Pair("SUV (Ertiga / Triber)", "Spacious legroom & boot capacity for short family weekend trip.")
                pCount in 6..8 -> Pair("Innova Crysta", "Premium luxury 7-seater choice, optimal for Indian expressways.")
                pCount in 9..15 -> Pair("Tempo Traveller", "Group mini bus suitable for pilgrimage or large weddings.")
                else -> Pair("Mini Bus", "Full charter group vehicle booking required.")
            }

            Card(colors = CardDefaults.cardColors(containerColor = SaffronOrange.copy(alpha = 0.08f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 Chalo AI Recommendation",
                        fontWeight = FontWeight.Bold,
                        color = SaffronOrange,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vehicle recommended: ${recommendation.first}",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = recommendation.second,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Comfort Score:", color = Color.Gray, fontSize = 12.sp)
                Text("9.5 / 10", color = SavingGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Toll Taxes:", color = Color.Gray, fontSize = 12.sp)
                Text("₹380", color = Color.White, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cheapest Compare (Ola):", color = Color.Gray, fontSize = 12.sp)
                Text("₹3,200", color = SavingGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.bookSimulatedRide("Ola Intercity", recommendation.first, 3200.0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Intercity Cab", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- FOOD COMPONENT ---
@Composable
fun FoodModuleView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Compare Foods Across Platforms", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.foodQuery,
                onValueChange = { viewModel.foodQuery = it },
                label = { Text("Search Food item (e.g. Biryani)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val query = viewModel.foodQuery.ifBlank { "Biryani" }
            Text("Comparing Quotes for '$query' nearby:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val list = listOf(
                Triple("Zomato", 290.0, "⚡ 25 min • Delivers Free with Gold • ⭐ 4.4"),
                Triple("Swiggy", 275.0, "⚡ 30 min • Delivery fee ₹30 • ⭐ 4.3"),
                Triple("EatSure", 310.0, "⚡ 40 min • Free delivery • ⭐ 4.6 (No surcharge)")
            )

            list.forEach { (platform, price, meta) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(GlassWhite, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(platform, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(meta, color = Color.LightGray, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${price.toInt()}", fontWeight = FontWeight.Bold, color = SavingGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                viewModel.addToCart(query, platform, "Food", price, 30.0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Add Cart", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- MART COMPONENT ---
@Composable
fun MartModuleView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Grocery Quick Cart & Comparisons", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val items = listOf(
                Triple("Nandini Premium Milk 1L", 56.0, "🥛 Essential Daily Dairy"),
                Triple("Farm Fresh Eggs 6pcs", 42.0, "🥚 High protein eggs"),
                Triple("Harvest Gold Atta 5kg", 240.0, "🌾 Premium whole wheat"),
                Triple("Himalayan Apples 1kg", 180.0, "🍎 Fresh fruit baskets")
            )

            items.forEach { (name, price, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(GlassWhite, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(desc, color = Color.Gray, fontSize = 10.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${price.toInt()}", fontWeight = FontWeight.Bold, color = SavingGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Button(
                                onClick = {
                                    viewModel.addToCart(name, "Blinkit", "Mart", price, 15.0)
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(22.dp).padding(end = 4.dp)
                            ) {
                                Text("Blinkit", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.addToCart(name, "Zepto", "Mart", price - 2.0, 15.0)
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SavingGreen),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(22.dp)
                            ) {
                                Text("Zepto", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STAYS COMPONENT ---
@Composable
fun StaysModuleView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Compare Hotels & Lodging", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            val hotels = listOf(
                Triple("Goa Beachside Villa", 3200.0, "Agoda • Free Wifi • Cancellation allowed"),
                Triple("Hotel Jaipur Heritage", 1900.0, "Booking.com • ⭐4.5 • Breakfast free"),
                Triple("Udaipur Grand Palace", 4500.0, "MakeMyTrip • ⭐4.8 • Pool access")
            )

            hotels.forEach { (name, price, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(GlassWhite, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(desc, color = Color.LightGray, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${price.toInt()}/N", fontWeight = FontWeight.Bold, color = SavingGreen, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                viewModel.addToCart(name, desc.substringBefore(" •"), "Stays", price, 0.0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Book Stay", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TRAVEL PLANNER COMPONENT ---
@Composable
fun TravelPlannerView(viewModel: ChaloViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🤖 AI Smart Travel Itinerary Planner", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 15.sp)
            Text("Plan a dynamic trip using Gemini API", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.plannerDestination,
                onValueChange = { viewModel.plannerDestination = it },
                label = { Text("Destination (e.g. Udaipur, Manali)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.plannerDurationDays,
                    onValueChange = { viewModel.plannerDurationDays = it },
                    label = { Text("Days") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
                OutlinedTextField(
                    value = viewModel.plannerBudget,
                    onValueChange = { viewModel.plannerBudget = it },
                    label = { Text("Budget (INR)") },
                    modifier = Modifier.weight(1.5f).padding(start = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaffronOrange)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.plannerLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = SaffronOrange)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Drafting daily itineraries with weather & rates forecasts...", color = Color.LightGray, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.generateTravelItinerary() },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Complete AI Itinerary (Earn 50 Pts)", fontWeight = FontWeight.Bold)
                }
            }

            viewModel.plannerResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Customized Travel Map", fontWeight = FontWeight.Bold, color = SaffronLight, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBg)
                        .padding(10.dp)
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = result,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// --- SCREEN 2: GLOBAL SEARCH ---
@Composable
fun SearchScreen(viewModel: ChaloViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.globalQuery,
            onValueChange = { viewModel.globalQuery = it },
            placeholder = { Text("Jaipur Trip under 10k, Biryani Swiggy...") },
            modifier = Modifier.fillMaxWidth().testTag("search_field_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronOrange,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            trailingIcon = {
                IconButton(onClick = { viewModel.executeGlobalSearch() }) {
                    Icon(Icons.Default.Search, contentDescription = "", tint = SaffronOrange)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.executeGlobalSearch()
                keyboardController?.hide()
            })
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Try India's Trending Phrases", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        val promptSamples = listOf(
            "Cheapest grocery from Blinkit vs Zepto",
            "Delhi to Jaipur cab 4 passengers",
            "Cheapest flight to Goa",
            "Best Biryani under ₹300 Swiggy"
        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            promptSamples.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .background(GlassWhite, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.globalQuery = prompt
                            viewModel.executeGlobalSearch()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(prompt, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.aiSearchLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = SaffronOrange)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Analyzing rates, distances, and ETAs instantly...", color = Color.LightGray, fontSize = 12.sp)
            }
        } else {
            viewModel.aiSearchResponse?.let { ans ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 Chalo AI comparative intelligence", fontWeight = FontWeight.Black, color = SaffronOrange, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ans, fontSize = 13.sp, color = Color.White, lineHeight = 18.sp)
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Search anything! Our AI automatically compares foods, cabs, and stays across India's largest delivery & cab platforms.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

// --- SCREEN 3: ACTIVITY TIMELINE ---
@Composable
fun ActivityScreen(viewModel: ChaloViewModel, activities: List<ActivityEntity>) {
    var selectedCategory by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Activity Center", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

            Text(
                "Unified Ledger",
                color = SaffronOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier
                    .background(SaffronOrange.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            val categories = listOf("All", "Ongoing", "Completed", "Rides", "Food", "Mart", "Stays")
            categories.forEach { cat ->
                val active = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .background(if (active) SaffronOrange else GlassWhite, RoundedCornerShape(12.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (active) Color.White else Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filtered = activities.filter {
            when (selectedCategory) {
                "All" -> true
                "Ongoing" -> it.status == "Ongoing"
                "Completed" -> it.status == "Completed"
                else -> it.category == selectedCategory
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No recent bookings found. Place an order or book a cab to track progress on this chronological timeline!", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered) { act ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val emblem = when (act.platform) {
                                        "Swiggy" -> "🍊"
                                        "Zomato" -> "🔴"
                                        "Uber" -> "🔲"
                                        "Ola" -> "✳️"
                                        "Blinkit" -> "🟡"
                                        "Zepto" -> "🟣"
                                        else -> "🏢"
                                    }
                                    Text("$emblem ${act.platform}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = act.category,
                                        fontSize = 10.sp,
                                        color = SaffronLight,
                                        modifier = Modifier
                                            .background(SaffronOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text("₹${act.amount.toInt()}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(act.details, fontSize = 12.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${act.date} • ${act.time}", color = Color.Gray, fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (act.status == "Ongoing") SaffronOrange else SavingGreen,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        act.status,
                                        fontWeight = FontWeight.Bold,
                                        color = if (act.status == "Ongoing") SaffronOrange else SavingGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: AI CHAT ASSISTANT ---
@Composable
fun AIAssistantScreen(viewModel: ChaloViewModel) {
    var rawText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                "🤖 Chalo India Assistant Mode (Low Latency)",
                fontWeight = FontWeight.Bold,
                color = SaffronOrange,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestion Chips
        val sampleCues = listOf(
            "How do I save money on Swiggy Gold?",
            "Cheapest way Delhi to CP?",
            "Milk price Blinkit vs Zepto"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            sampleCues.forEach { cue ->
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .background(GlassWhite, RoundedCornerShape(10.dp))
                        .clickable { viewModel.submitChatMessage(cue) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(cue, color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Bubble Scrollable
        val state = rememberLazyListState()
        LaunchedEffect(viewModel.aiChatMessages.size) {
            if (viewModel.aiChatMessages.isNotEmpty()) {
                state.animateScrollToItem(viewModel.aiChatMessages.size - 1)
            }
        }

        LazyColumn(
            state = state,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(viewModel.aiChatMessages) { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .background(
                                if (msg.isUser) SaffronOrange else SurfaceDark,
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                    bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                )
                            )
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (viewModel.aiChatLoading) {
                item {
                    Row(modifier = Modifier.padding(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SaffronOrange, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assistant is formulating comparison metrics...", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Input Field Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                placeholder = { Text("Ask about price hacks...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronOrange,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (rawText.isNotBlank()) {
                        viewModel.submitChatMessage(rawText)
                        rawText = ""
                        keyboardController?.hide()
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (rawText.isNotBlank()) {
                        viewModel.submitChatMessage(rawText)
                        rawText = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .background(SaffronOrange, CircleShape)
                    .size(48.dp)
                    .testTag("ai_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "", tint = Color.White)
            }
        }
    }
}

enum class AccountSubScreen {
    LinkedPlatforms,
    PaymentMethods,
    AddressManagement
}

@Composable
fun AccountScreen(
    viewModel: ChaloViewModel,
    profile: UserEntity?,
    transactions: List<WalletTransactionEntity>
) {
    var activeSubScreen by remember { mutableStateOf<AccountSubScreen?>(null) }
    var selectedBrandingSuggestion by remember { mutableStateOf("Golden Chakra") }
    var brandingSuccessMessage by remember { mutableStateOf("") }
    
    AnimatedContent(
        targetState = activeSubScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
        },
        label = "account_page_transitions"
    ) { subScreen ->
        when (subScreen) {
            AccountSubScreen.LinkedPlatforms -> {
                LinkedPlatformsSubScreen(
                    viewModel = viewModel,
                    profile = profile,
                    onBack = { activeSubScreen = null }
                )
            }
            AccountSubScreen.PaymentMethods -> {
                PaymentMethodsSubScreen(
                    viewModel = viewModel,
                    onBack = { activeSubScreen = null }
                )
            }
            AccountSubScreen.AddressManagement -> {
                AddressManagementSubScreen(
                    viewModel = viewModel,
                    profile = profile,
                    onBack = { activeSubScreen = null }
                )
            }
            null -> {
                // Main Account Hub Menu
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp).testTag("account_main_hub_column"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Profile Block
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(SaffronOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile?.name?.firstOrNull()?.toString() ?: "C",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 24.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(profile?.name ?: "Kunal Pareek", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    Text(profile?.email ?: "kunal@gmail.com", color = Color.Gray, fontSize = 12.sp)
                                    Text(profile?.phone ?: "+91 98765 43210", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Chalo Smart Wallet Block
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, SavingGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Chalo Smart Wallet", fontWeight = FontWeight.Bold, color = SavingGreen, fontSize = 14.sp)
                                    Text("20 Pts = ₹1 Saved", color = Color.Gray, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("${profile?.walletPoints ?: 0}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Points", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))

                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "≈ ₹${(profile?.walletPoints ?: 0) / 20}",
                                        fontWeight = FontWeight.Bold,
                                        color = SavingGreen,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Recent Coin Ledger", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                transactions.take(3).forEach { tx ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(tx.description, color = Color.LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            text = "${if (tx.isCredit) "+" else "-"}${tx.points} Pts",
                                            color = if (tx.isCredit) SavingGreen else SaffronOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // DEVIATION COMPLIANCE - High Fidelity Isolated Detail Submenu Navigation Rows
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth().testTag("subpages_navigation_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Profile Preferences Hub", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)
                                Text("Configure external APIs, payment accounts, and destination centers", color = Color.Gray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Button 1: Manage Address Centers
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeSubScreen = AccountSubScreen.AddressManagement }
                                        .padding(vertical = 12.dp)
                                        .testTag("nav_to_address_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Addresses", tint = SaffronOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Manage Delivery Addresses", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Adjust primary Home, Office & Spot coordinators", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                            .padding(6.dp)
                                    ) {
                                        Text("📍", fontSize = 10.sp)
                                    }
                                }

                                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                                // Button 2: Manage Payment Accounts
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeSubScreen = AccountSubScreen.PaymentMethods }
                                        .padding(vertical = 12.dp)
                                        .testTag("nav_to_payments_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Payments", tint = SaffronOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Unified Checkout & Cards", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Set up standard credit cards, UPI handles & wallets", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                            .padding(6.dp)
                                    ) {
                                        Text("💳", fontSize = 10.sp)
                                    }
                                }

                                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.4f))

                                // Button 3: Manage Connected Platforms
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeSubScreen = AccountSubScreen.LinkedPlatforms }
                                        .padding(vertical = 12.dp)
                                        .testTag("nav_to_platforms_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Linked Platforms", tint = SaffronOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Linked Accounts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Manage active linked accounts like Uber, Ola, Swiggy, etc.", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                            .padding(6.dp)
                                    ) {
                                        Text("🔗", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // App Sorting Optimizations Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Super App Optimizer Mode", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                val modes = listOf("Cheapest First", "Fastest First", "Best Rated", "AI Recommended")
                                val currentMode = profile?.preferencesJson?.substringAfter("\"Mode\":\"")?.substringBefore("\"") ?: "AI Recommended"
                                modes.forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.changePreferenceMode(mode) }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(mode, color = Color.White, fontSize = 13.sp)
                                        RadioButton(
                                            selected = currentMode == mode,
                                            onClick = { viewModel.changePreferenceMode(mode) },
                                            colors = RadioButtonDefaults.colors(selectedColor = SaffronOrange)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Onboarding Tutorial Options Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_replay_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("App Onboarding & Help", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Re-experience the feature introductions anytime", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { viewModel.startOnboarding() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                    modifier = Modifier.fillMaxWidth().testTag("replay_onboarding_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Replay Application Intro Tour", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-Screen A: Linked Platforms Hub detail page (relocated here)
@Composable
fun LinkedPlatformsSubScreen(
    viewModel: ChaloViewModel,
    profile: UserEntity?,
    onBack: () -> Unit
) {
    var portalQuery by remember { mutableStateOf("") }
    var lastSuccessMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("linked_platforms_subscreen_col")
    ) {
        // Upper back navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .testTag("platforms_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaffronOrange, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Linked Accounts", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                Text("Manage links for Uber, Ola, Swiggy, and other platforms", color = Color.Gray, fontSize = 10.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Search Integrated Providers", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Filters active ONDC direct delivery segments", color = Color.Gray, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        WebSearchFilterField(
                            query = portalQuery,
                            onQueryChange = { portalQuery = it }
                        )

                        // Unified local re-auth success animation
                        AnimatedVisibility(
                            visible = lastSuccessMessage.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A20)),
                                border = BorderStroke(1.dp, SavingGreen.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Success", tint = SavingGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = lastSuccessMessage,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { lastSuccessMessage = "" },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        val categoriesWithApps = listOf(
                            "🚕 Rides & Cab Portals" to listOf("Uber", "Ola", "Rapido", "BluSmart"),
                            "🍔 Food & Restaurants" to listOf("Swiggy", "Zomato", "EatSure", "ONDC Food"),
                            "🥛 Grocery & Quick Commerce" to listOf("Blinkit", "Zepto", "Instamart", "BigBasket"),
                            "🏨 Hotels & Lodging" to listOf("Booking", "Agoda", "Oyo Rooms", "MakeMyTrip")
                        )

                        val filteredCategories = categoriesWithApps.map { (catName, apps) ->
                            catName to apps.filter { app ->
                                app.lowercase().contains(portalQuery.lowercase()) ||
                                        catName.lowercase().contains(portalQuery.lowercase())
                            }
                        }.filter { (_, apps) -> apps.isNotEmpty() }

                        if (filteredCategories.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No integrations match", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            filteredCategories.forEach { (catName, appsInCat) ->
                                Text(
                                    text = catName,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronOrange,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )

                                appsInCat.forEach { app ->
                                    val isConnected = profile?.connectedAccountsJson?.contains("\"$app\":true") == true
                                    val isReauthorizing = viewModel.reauthorizingApps[app] == true

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                                        border = BorderStroke(0.5.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(app, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (isConnected) "Session Active • Encrypted Link" else "Token Expired • Inactive",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                if (isReauthorizing) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        CircularProgressIndicator(
                                                            color = SaffronOrange,
                                                            strokeWidth = 1.5.dp,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Linking...", color = SaffronOrange, fontSize = 12.sp)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            if (isConnected) {
                                                                viewModel.toggleConnectedAccount(app)
                                                                lastSuccessMessage = "Unlinked $app successfully."
                                                            } else {
                                                                viewModel.reauthorizeAccount(app) // Will wait mock delay and then toggle
                                                                // viewModel.toggleConnectedAccount(app) will happen after delay
                                                                lastSuccessMessage = "Successfully connected $app! Shared wallet sync active."
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isConnected) Color.DarkGray else SaffronOrange,
                                                            contentColor = Color.White
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(if (isConnected) "Unlink" else "Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "OAuth 2.0 (Token Auth)",
                                                fontSize = 8.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebSearchFilterField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Filter integrated portals...", fontSize = 11.sp, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = SaffronOrange, modifier = Modifier.size(14.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(14.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(12.dp))
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SaffronOrange,
            focusedContainerColor = Color.Black.copy(alpha = 0.15f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.15f),
            unfocusedBorderColor = Color.DarkGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

// Sub-Screen B: Payment Methods Page (manage multiple methods & set EXACTLY 1 preference)
@Composable
fun PaymentMethodsSubScreen(
    viewModel: ChaloViewModel,
    onBack: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputDetails by remember { mutableStateOf("") }
    var inputEmoji by remember { mutableStateOf("💳") }
    var successToast by remember { mutableStateOf("") }

    val methods = viewModel.getPaymentMethods()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("payment_methods_subscreen_col")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .testTag("payments_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaffronOrange, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Payment Methods Hub", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                Text("Select 1 Preferred source & attach direct accounts", color = Color.Gray, fontSize = 10.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Toast banner
            item {
                AnimatedVisibility(
                    visible = successToast.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132B18)),
                        border = BorderStroke(0.5.dp, SavingGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "", tint = SavingGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successToast, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { successToast = "" }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // Methods List
            items(methods.size) { idx ->
                val pm = methods[idx]
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("payment_method_card_$idx"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pm.isPreferred) SaffronOrange.copy(alpha = 0.07f) else SurfaceDark
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (pm.isPreferred) SaffronOrange else GlassBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Badge
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pm.iconEmoji, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(pm.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(pm.details, color = Color.Gray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Selection Action Button
                        if (pm.isPreferred) {
                            Box(
                                modifier = Modifier
                                    .background(SavingGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, SavingGreen, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("preferred_badge_$idx")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "", tint = SavingGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PREFERRED", color = SavingGreen, fontWeight = FontWeight.Black, fontSize = 8.sp)
                                }
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    viewModel.setPreferredPaymentMethod(idx)
                                    successToast = "Preference set to: ${pm.name}."
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("select_preferred_payment_$idx")
                            ) {
                                Text("Set Pref", color = SaffronOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Delete button
                        IconButton(
                            onClick = {
                                val deletedName = pm.name
                                viewModel.deletePaymentMethod(idx)
                                successToast = "Account: $deletedName deleted successfully."
                            },
                            modifier = Modifier.size(24.dp).testTag("delete_payment_$idx")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Collapsible Link Button & Form Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("add_payment_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showForm = !showForm },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Link New Source", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = SaffronOrange.copy(alpha = 0.2f)) {
                                    Text("SECURE", color = SaffronOrange, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                            Icon(
                                imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Toggle add form",
                                tint = SaffronOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showForm) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("Account Label (e.g. SBI Credit, GPay)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("add_payment_name_field"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    focusedLabelColor = SaffronOrange,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inputDetails,
                                onValueChange = { inputDetails = it },
                                label = { Text("ID or Card Details (e.g. •••• 9811, user@oksbi)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("add_payment_details_field"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    focusedLabelColor = SaffronOrange,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Emoji icon picker
                            Text("Symbol Indicator", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val emojis = listOf("💳", "🪙", "⚡", "📱", "🏦")
                                emojis.forEach { emoji ->
                                    val isSel = inputEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (isSel) SaffronOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                CircleShape
                                            )
                                            .border(
                                                width = if (isSel) 1.dp else 0.dp,
                                                color = if (isSel) SaffronOrange else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { inputEmoji = emoji }
                                            .testTag("emoji_picker_$emoji"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (inputName.isNotBlank() && inputDetails.isNotBlank()) {
                                        viewModel.addPaymentMethod(inputName, inputDetails, inputEmoji)
                                        successToast = "Linked account $inputName securely to Chalo Wallet core."
                                        inputName = ""
                                        inputDetails = ""
                                        showForm = false
                                    } else {
                                        successToast = "Error: Input fields cannot be empty."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                modifier = Modifier.fillMaxWidth().testTag("submit_payment_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Securely Link Source", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-Screen C: Address Management Page (manage multiple nodes & toggle 1 active preference)
@Composable
fun AddressManagementSubScreen(
    viewModel: ChaloViewModel,
    profile: UserEntity?,
    onBack: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var inputLabel by remember { mutableStateOf("") }
    var inputStreet by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf("") }

    val addresses = listAddresses(profile?.savedAddressesJson)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("address_subscreen_col")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .testTag("addresses_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaffronOrange, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Saved Delivery Spots", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                Text("Select active coordinates & manage navigation points", color = Color.Gray, fontSize = 10.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Toast banner
            item {
                AnimatedVisibility(
                    visible = actionMessage.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132B18)),
                        border = BorderStroke(0.5.dp, SavingGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "", tint = SavingGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(actionMessage, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { actionMessage = "" }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // Addresses list
            items(addresses.size) { idx ->
                val (label, streetValue) = addresses[idx]
                val isPref = streetValue.trim() == viewModel.currentAddress.trim()

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("address_row_card_$idx"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPref) SaffronOrange.copy(alpha = 0.07f) else SurfaceDark
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPref) SaffronOrange else GlassBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location pin indicator
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isPref) SaffronOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "",
                                tint = if (isPref) SaffronOrange else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                val emoji = when (label.lowercase()) {
                                    "home" -> "🏠"
                                    "work", "office" -> "💼"
                                    "hotel" -> "🏨"
                                    else -> "📍"
                                }
                                Text(emoji, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(streetValue, color = Color.Gray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Selection Action Button
                        if (isPref) {
                            Box(
                                modifier = Modifier
                                    .background(SavingGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, SavingGreen, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("active_address_badge_$idx")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = "", tint = SavingGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ACTIVE", color = SavingGreen, fontWeight = FontWeight.Black, fontSize = 8.sp)
                                }
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    viewModel.setPreferredAddressFromList(idx)
                                    actionMessage = "Primary checkout point set to $label."
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("select_preferred_address_$idx")
                            ) {
                                Text("Map Active", color = SaffronOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Delete button
                        IconButton(
                            onClick = {
                                viewModel.deleteSavedAddress(idx)
                                actionMessage = "Location $label removed safely."
                            },
                            modifier = Modifier.size(24.dp).testTag("delete_address_$idx")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Link Location Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("add_address_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showForm = !showForm },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Map New Delivery Hub", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 13.sp)
                            Icon(
                                imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Toggle add address form",
                                tint = SaffronOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showForm) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = inputLabel,
                                onValueChange = { inputLabel = it },
                                label = { Text("Spot Label (e.g. Gym, Friend's Place)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("add_address_label_field"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    focusedLabelColor = SaffronOrange,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inputStreet,
                                onValueChange = { inputStreet = it },
                                label = { Text("Exact Physical Address", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("add_address_street_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SaffronOrange,
                                    focusedLabelColor = SaffronOrange,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (inputLabel.isNotBlank() && inputStreet.isNotBlank()) {
                                        viewModel.addSavedAddress(inputLabel, inputStreet)
                                        actionMessage = "Added spot $inputLabel successfully to database."
                                        inputLabel = ""
                                        inputStreet = ""
                                        showForm = false
                                    } else {
                                        actionMessage = "Error: Fields cannot be blank."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                modifier = Modifier.fillMaxWidth().testTag("submit_address_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Locate and Save Hub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- UNIFIED CART DRAWER SHEET OVERLAY ---
@Composable
fun UnifiedCartOverlay(
    viewModel: ChaloViewModel,
    cartItems: List<CartItemEntity>,
    userEntity: UserEntity?
) {
    var checkPoints by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { viewModel.isCartOpen = false }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(enabled = false) {}, // consume clicks
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Unified Cart 🛒", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.isCartOpen = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.platform,
                                        fontSize = 10.sp,
                                        color = SaffronOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.decrementCartItem(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("-", color = SaffronOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${item.quantity}", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp), fontSize = 13.sp)
                                IconButton(
                                    onClick = { viewModel.incrementCartItem(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("+", color = SaffronOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Text("₹${(item.price * item.quantity).toInt()}", fontWeight = FontWeight.Bold, color = SavingGreen, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.DarkGray)

                // Loyalty Coins checkbox switcher
                userEntity?.let { prf ->
                    if (prf.walletPoints > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkPoints = !checkPoints }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checkPoints,
                                onCheckedChange = { checkPoints = it },
                                colors = CheckboxDefaults.colors(checkedColor = SaffronOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Redeem Chalo Wallet Coins", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val savings = prf.walletPoints / 20
                                Text("Use up to ${prf.walletPoints} Points (Saves ₹$savings)", color = SavingGreen, fontSize = 11.sp)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }

                // Summary calculations
                val itemCost = cartItems.sumOf { it.price * it.quantity }
                val delFee = cartItems.distinctBy { it.platform }.sumOf { it.deliveryFee }
                val totalSum = itemCost + delFee
                val savedRupees = if (checkPoints && userEntity != null) {
                    val ptsWorth = userEntity.walletPoints / 20.0
                    if (ptsWorth >= totalSum) totalSum else ptsWorth
                } else 0.0

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Color.Gray, fontSize = 12.sp)
                    Text("₹${itemCost.toInt()}", color = Color.White, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery & Gate fee comparison", color = Color.Gray, fontSize = 12.sp)
                    Text("₹${delFee.toInt()}", color = Color.White, fontSize = 12.sp)
                }
                if (savedRupees > 0) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Wallet Coins Discount", color = SavingGreen, fontSize = 12.sp)
                        Text("-₹${savedRupees.toInt()}", color = SavingGreen, fontSize = 12.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Price", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("₹${(totalSum - savedRupees).toInt()}", fontWeight = FontWeight.Black, color = SavingGreen, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.checkoutCart(checkPoints) },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    modifier = Modifier.fillMaxWidth().testTag("cart_checkout_button")
                ) {
                    Text("Checkout via Instant API Wallet", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// --- ONBOARDING & RECOMMENDATIONS VIEWS ---
// ==========================================

@Composable
fun OnboardingOverlay(viewModel: ChaloViewModel) {
    Dialog(
        onDismissRequest = { /* Prevent accidental dismiss */ },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Carousel Content
                when (viewModel.onboardingStep) {
                    0 -> {
                        Text(
                            text = "🇮🇳 Welcome to Chalo",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = SaffronOrange,
                            modifier = Modifier.testTag("onboarding_welcome_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "India's Everyday Smart Super App",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(SaffronOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 50.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Why waste time switching apps? Compare Rides, Food delivery, Mart/Groceries, and Hotel prices side-by-side in real-time, optimizing for speed and price.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                    1 -> {
                        Text(
                            text = "🔍 Smart Unified Search",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = SaffronOrange,
                            modifier = Modifier.testTag("onboarding_search_title")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(AccentTeal.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚀", fontSize = 50.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Type Biryani, Cabs, Milk, or Stays in the Search bar. Our engine directly aggregates live comparatives of Uber, Ola, Rapido, Swiggy, Zomato, Blinkit, and Agoda in one click!",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                    2 -> {
                        Text(
                            text = "✨ Conversational AI Assistant",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = SaffronOrange,
                            modifier = Modifier.testTag("onboarding_ai_title")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(SavingGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 50.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Stuck planning a trip, budgeting, or finding cheap local canteens? Talk to Chalo's AI core to draft travel itineraries, analyze spend habits, and suggest savings hacks.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                    3 -> {
                        Text(
                            text = "💰 Chalo Coin Rewards",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = SaffronOrange,
                            modifier = Modifier.testTag("onboarding_rewards_title")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Yellow.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪙", fontSize = 50.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Earn up to 10% cash-refund points automatically on every booking. Use your points directly inside Chalo's unified shopping cart to reduce payment totals! (20 points = ₹1).",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (viewModel.onboardingStep == idx) SaffronOrange else Color.DarkGray
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.onboardingStep > 0) {
                        TextButton(
                            onClick = { viewModel.onboardingStep-- },
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Text("BACK", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    Button(
                        onClick = {
                            if (viewModel.onboardingStep < 3) {
                                viewModel.onboardingStep++
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (viewModel.onboardingStep == 3) "START SAVING" else "NEXT",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(
                        onClick = { viewModel.completeOnboarding() },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text("SKIP", color = SaffronOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChaloRecommendationsView(
    viewModel: ChaloViewModel,
    onModuleSelected: (HomeModule) -> Unit
) {
    val recommendations = viewModel.aiRecommendations
    val isSearching = viewModel.isRecommendationsLoading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("ai_recommendations_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💡 Chalo Smart Insights",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SaffronOrange.copy(alpha = 0.2f))
                        .border(1.dp, SaffronOrange, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE AI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronOrange
                    )
                }
            }
            IconButton(
                onClick = { viewModel.generateSmartRecommendations() },
                modifier = Modifier.size(24.dp).testTag("refresh_recommendations_btn")
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = SaffronOrange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Text(
            text = viewModel.recommendationExplanation ?: "Sifting live comparisons tailored to your preferences.",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaffronOrange)
            }
        } else {
            if (recommendations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Setting up personalized advice based on your search queries and ride routes...",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                recommendations.forEach { recommendation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("recommendation_card_${recommendation.category.lowercase()}"),
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = when (recommendation.category) {
                                                    "Rides" -> SaffronOrange.copy(alpha = 0.15f)
                                                    "Food" -> Color.Red.copy(alpha = 0.15f)
                                                    "Mart" -> SavingGreen.copy(alpha = 0.15f)
                                                    else -> AccentTeal.copy(alpha = 0.15f)
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(recommendation.iconEmoji, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = recommendation.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = recommendation.platform,
                                                color = SaffronOrange,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Category: ${recommendation.category}",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SavingGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = recommendation.dealTag,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SavingGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recommendation.description,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (recommendation.price > 0 && recommendation.category != "Stays") {
                                    Text(
                                        text = "Est. Cost: ₹${recommendation.price.toInt()}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else if (recommendation.price > 0) {
                                    Text(
                                        text = "From: ₹${recommendation.price.toInt()}/night",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Button(
                                    onClick = {
                                        when (recommendation.category) {
                                            "Mart" -> {
                                                viewModel.addToCart(
                                                    name = if (recommendation.title.contains("Milk")) "Nandini Milk 1L Bundle" else "Smart Grocery Bundle",
                                                    platform = recommendation.platform,
                                                    category = "Mart",
                                                    price = recommendation.price,
                                                    deliveryFee = 15.0
                                                )
                                                viewModel.isCartOpen = true
                                            }
                                            "Rides" -> {
                                                viewModel.ridesPickup = "H-25, Connaught Place, New Delhi"
                                                viewModel.ridesDestination = "DLF Cyber City, Gurugram"
                                                viewModel.selectedRideVehicle = if (recommendation.platform == "Rapido") "Bike" else "Sedan"
                                                viewModel.rideComparisonType = recommendation.platform
                                                onModuleSelected(HomeModule.Rides)
                                            }
                                            "Food" -> {
                                                viewModel.foodQuery = if (recommendation.title.contains("Biryani")) "Chicken Biryani" else "Paneer Veg Match"
                                                viewModel.foodResultPlatform = recommendation.platform
                                                onModuleSelected(HomeModule.Food)
                                            }
                                            "Stays" -> {
                                                onModuleSelected(HomeModule.Stays)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp).testTag("recommendation_action_${recommendation.category.lowercase()}")
                                ) {
                                    Text(recommendation.actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

