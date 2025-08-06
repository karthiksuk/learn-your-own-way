package com.karthik.learnmyownway.data.models

data class AnalogyProfile(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val domainKeywords: List<String>,
    val exampleTerms: List<String>
) {
    companion object {
        val DEFAULT_PROFILES = listOf(
            AnalogyProfile(
                id = "chef",
                name = "Chef",
                description = "Cooking and culinary arts",
                iconEmoji = "👨‍🍳",
                domainKeywords = listOf("cooking", "recipe", "ingredients", "kitchen", "seasoning"),
                exampleTerms = listOf("recipe", "ingredients", "mise en place", "sauté", "reduction")
            ),
            AnalogyProfile(
                id = "mechanic",
                name = "Mechanic",
                description = "Automotive and machinery",
                iconEmoji = "🔧",
                domainKeywords = listOf("engine", "parts", "tools", "repair", "maintenance"),
                exampleTerms = listOf("components", "assembly", "troubleshoot", "tune-up", "diagnostics")
            ),
            AnalogyProfile(
                id = "musician",
                name = "Musician",
                description = "Music and sound",
                iconEmoji = "🎵",
                domainKeywords = listOf("rhythm", "harmony", "melody", "composition", "instrument"),
                exampleTerms = listOf("rhythm", "harmony", "composition", "performance", "practice")
            ),
            AnalogyProfile(
                id = "gardener",
                name = "Gardener",
                description = "Plants and gardening",
                iconEmoji = "🌱",
                domainKeywords = listOf("plants", "soil", "growth", "seeds", "cultivation"),
                exampleTerms = listOf("cultivation", "growth", "pruning", "fertilizer", "ecosystem")
            ),
            AnalogyProfile(
                id = "builder",
                name = "Builder",
                description = "Construction and building",
                iconEmoji = "🏗️",
                domainKeywords = listOf("foundation", "structure", "tools", "blueprint", "construction"),
                exampleTerms = listOf("foundation", "framework", "blueprint", "structure", "assembly")
            ),
            AnalogyProfile(
                id = "artist",
                name = "Artist",
                description = "Visual arts and creativity",
                iconEmoji = "🎨",
                domainKeywords = listOf("canvas", "colors", "composition", "texture", "creativity"),
                exampleTerms = listOf("composition", "palette", "technique", "expression", "medium")
            ),
            AnalogyProfile(
                id = "athlete",
                name = "Athlete",
                description = "Sports and fitness",
                iconEmoji = "🏃‍♀️",
                domainKeywords = listOf("training", "performance", "endurance", "technique", "competition"),
                exampleTerms = listOf("training", "performance", "stamina", "technique", "competition")
            ),
            AnalogyProfile(
                id = "teacher",
                name = "Teacher",
                description = "Education and learning",
                iconEmoji = "📚",
                domainKeywords = listOf("lesson", "knowledge", "understanding", "practice", "learning"),
                exampleTerms = listOf("curriculum", "understanding", "practice", "assessment", "growth")
            )
        )
    }
}