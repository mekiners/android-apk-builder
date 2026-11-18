package com.example.sampleapp.util

object GroceryLogic {
    val sections = listOf("Produce", "Pantry", "Dairy", "Bakery", "Household", "Misc")

    fun emoji(section: String): String = when (section) {
        "Produce" -> "\uD83E\uDD6C"   // leafy green
        "Pantry" -> "\uD83E\uDD6B"    // canned food
        "Dairy" -> "\uD83E\uDD5B"     // milk glass
        "Bakery" -> "\uD83E\uDD56"    // baguette
        "Household" -> "\uD83E\uDDFC" // soap
        else -> "\uD83D\uDCE6"        // package
    }

    private val keywords: Map<String, List<String>> = mapOf(
        "Produce" to listOf(
            "apple", "banana", "tomato", "onion", "garlic", "lettuce", "carrot", "potato", "fruit",
            "veg", "spinach", "lemon", "lime", "berry", "berries", "mango", "cucumber", "broccoli",
            "cabbage", "pepper", "ginger", "orange", "grape", "avocado", "mushroom", "corn",
            "kangkong", "pechay", "talong", "sibuyas", "bawang", "kamatis", "calamansi", "papaya"
        ),
        "Dairy" to listOf("milk", "cheese", "yogurt", "yoghurt", "butter", "egg", "cream", "creamer"),
        "Bakery" to listOf("bread", "bun", "pandesal", "cake", "pastry", "croissant", "bagel", "muffin", "donut"),
        "Household" to listOf(
            "soap", "detergent", "tissue", "toilet", "shampoo", "dish", "sponge", "bleach", "trash",
            "garbage", "toothpaste", "conditioner", "napkin", "foil", "cleaner", "laundry"
        ),
        "Pantry" to listOf(
            "rice", "pasta", "noodle", "oil", "sauce", "salt", "sugar", "flour", "coffee", "tea",
            "beans", "cereal", "vinegar", "canned", "can ", "oat", "spice", "honey", "peanut",
            "chips", "biscuit", "cracker", "instant", "sardines", "tuna", "soy"
        )
    )

    fun detect(name: String): String {
        val n = " " + name.lowercase() + " "
        for ((section, words) in keywords) {
            if (words.any { n.contains(it) }) return section
        }
        return "Misc"
    }
}
