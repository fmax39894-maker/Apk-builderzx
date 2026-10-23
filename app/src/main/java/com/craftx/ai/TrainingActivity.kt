
private fun refresh() {
    val arr = try {
        org.json.JSONArray(prefs.getString("training_examples", "[]"))
    } catch (_: Exception) {
        org.json.JSONArray()
    }

    val lines = mutableListOf<String>()

    for (i in 0 until arr.length()) {
        val item = arr.optJSONObject(i) ?: continue
        val label = item.optString("label", "unknown")
        val note = item.optString("note", "")
        lines.add("${i + 1}. $label: $note")
    }

    val recentLines = lines.takeLast(12)
    examples.text = "Saved examples: ${arr.length()}\n" +
        if (recentLines.isEmpty()) {
            "No examples yet."
        } else {
            recentLines.joinToString("\n")
        }
}
