package org.db.jetword

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel: ViewModel() {

    private var wordList: List<String> = emptyList()


    private fun readFileFromAssets(context: Context): String {
        return context.assets.open("words.txt").bufferedReader().use { it.readText() }
    }

    fun searchWords(guess: String, feedback: String, blackLetters: Set<Char>): List<String> {
        val suggestions = mutableListOf<String>()
        for (word in wordList) {
            var match = true
            for (i in feedback.indices) {
                when (feedback[i]) {
                    'G' -> if (word[i] != guess[i]) match = false
                    'Y' -> if (word[i] == guess[i] || !word.contains(guess[i])) match = false
                    'B' -> if (word.contains(guess[i]) || blackLetters.contains(word[i])) match = false
                }
            }
            if (match) suggestions.add(word)
        }
        return suggestions
    }

    fun loadWords(context: Context) {
        viewModelScope.launch {
            wordList = withContext(Dispatchers.IO)     {
                readFileFromAssets(context).split("\\s+".toRegex())
            }
        }
    }

}