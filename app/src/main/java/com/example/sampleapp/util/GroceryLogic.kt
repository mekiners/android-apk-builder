package com.example.sampleapp.util

object GroceryLogic {
    val sections = listOf("Produce", "Meat", "Dairy", "Bakery", "Pantry", "Household", "Tech", "Misc")

    // Multi-word phrases are checked first so "peanut butter" is Pantry, not Dairy.
    private val phrases: List<Pair<String, String>> = listOf(
        "peanut butter" to "Pantry", "coconut milk" to "Pantry", "soy sauce" to "Pantry",
        "fish sauce" to "Pantry", "oyster sauce" to "Pantry", "rice cooker" to "Household",
        "cream cheese" to "Dairy", "ice cream" to "Dairy", "oat milk" to "Dairy",
        "power bank" to "Tech", "flash drive" to "Tech", "memory card" to "Tech", "sd card" to "Tech",
        "light bulb" to "Household", "toilet paper" to "Household", "paper towel" to "Household",
        "hot dog" to "Meat", "corned beef" to "Meat", "dish soap" to "Household",
        "usb cable" to "Tech", "phone case" to "Tech", "screen protector" to "Tech"
    )

    // Single words are matched against whole words (plural forms allowed), checked in this order.
    private val words: List<Pair<String, List<String>>> = listOf(
        "Tech" to listOf(
            "battery", "batteries", "charger", "cable", "usb", "phone", "earphone", "earphones", "earbuds",
            "headphone", "headphones", "laptop", "keyboard", "mouse", "hdmi", "router", "webcam", "speaker",
            "monitor", "adapter", "extension", "tablet", "ssd", "gadget", "powerbank", "tripod", "console"
        ),
        "Household" to listOf(
            "soap", "detergent", "tissue", "shampoo", "conditioner", "sponge", "bleach", "trash",
            "garbage", "toothpaste", "toothbrush", "napkin", "foil", "cleaner", "laundry", "bulb",
            "broom", "mop", "deodorant", "lotion", "towel", "fabcon", "perfume", "razor"
        ),
        "Meat" to listOf(
            "chicken", "beef", "pork", "fish", "bangus", "tilapia", "shrimp", "prawn", "sausage",
            "bacon", "ham", "hotdog", "steak", "lamb", "meat", "longganisa", "tocino", "salmon",
            "squid", "crab", "liempo", "ribs", "drumstick", "wings", "bacon", "burger", "patty", "turkey",
            "galunggong", "dilis"
        ),
        "Dairy" to listOf("milk", "cheese", "yogurt", "yoghurt", "butter", "egg", "eggs", "cream", "creamer", "margarine"),
        "Bakery" to listOf("bread", "bun", "buns", "pandesal", "cake", "pastry", "croissant", "bagel", "muffin", "donut", "cookie", "cookies", "tortilla"),
        "Produce" to listOf(
            "apple", "banana", "tomato", "onion", "garlic", "lettuce", "carrot", "potato", "fruit", "fruits",
            "veggie", "veggies", "vegetable", "vegetables", "spinach", "lemon", "lime", "berry", "berries",
            "mango", "cucumber", "broccoli", "cabbage", "pepper", "ginger", "orange", "grape", "grapes",
            "avocado", "mushroom", "corn", "kangkong", "pechay", "talong", "eggplant", "sibuyas", "bawang",
            "kamatis", "calamansi", "papaya", "pineapple", "watermelon", "melon", "squash", "okra", "sitaw",
            "celery", "herbs", "basil", "kalabasa", "ampalaya", "singkamas", "pear", "peach", "strawberry"
        ),
        "Pantry" to listOf(
            "rice", "pasta", "noodle", "noodles", "oil", "sauce", "salt", "sugar", "flour", "coffee", "tea",
            "beans", "cereal", "vinegar", "canned", "oat", "oats", "spice", "honey", "chips", "biscuit",
            "cracker", "crackers", "instant", "sardines", "tuna", "ketchup", "mayo", "mayonnaise", "jam",
            "cornstarch", "spaghetti", "macaroni", "pancit", "snack", "snacks", "chocolate", "candy", "nuts", "water", "juice", "soda"
        )
    )

    fun detect(name: String): String {
        val lower = name.lowercase()
        for ((phrase, section) in phrases) {
            if (lower.contains(phrase)) return section
        }
        val tokens = lower.split(Regex("[^a-z]+")).filter { it.isNotBlank() }
        for ((section, list) in words) {
            for (t in tokens) {
                if (list.any { k -> t == k || t == k + "s" || t == k + "es" }) return section
            }
        }
        return "Misc"
    }
}
