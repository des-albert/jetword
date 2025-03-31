ndroidx.compose.ui.unit.sp

enum class ClueState {
    DEFAULT, // Not guessed or incorrect position (Grey/Black)
    PRESENT, // Correct letter, wrong position (Yellow)
    CORRECT  // Correct letter, correct position (Green)
}

// Helper function to get the background color for a state (Unchanged)
fun getBackgroundColor(state: ClueState): Color {
    return when (state) {
        ClueState.DEFAULT -> Color.DarkGray
        ClueState.PRESENT -> Color(0xFFCEB02C) // Standard Wordle Yellow
        ClueState.CORRECT -> Color(0xFF6AAA64) // Standard Wordle Green
    }
}

// Helper function to get the next state in the cycle (Unchanged)
fun getNextState(currentState: ClueState): ClueState {
    return when (currentState) {
        ClueState.DEFAULT -> ClueState.PRESENT // Grey -> Yellow
        ClueState.PRESENT -> ClueState.CORRECT // Yellow -> Green
        ClueState.CORRECT -> ClueState.DEFAULT // Green -> Grey
    }
}

/**
 * Composable function for the row of 5 Wordle input tiles
 * where users can type letters and tap to set clue colors.
 */
@Composable
fun WordleInputAndClueRow(
    modifier: Modifier = Modifier,
    // Optional: Callback to get the final word and states
    onWordComplete: (word: String, states: List<ClueState>) -> Unit = { _, _ -> }
) {
    // State for the letters in each box (initially empty)
    val letters = remember { mutableStateListOf("", "", "", "", "") }
    // State for the clue color of each box
    val states = remember { mutableStateListOf(*Array(5) { ClueState.DEFAULT }) }
    // Focus Requesters to manage focus between TextFields
    val focusRequesters = remember { List(5) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Request focus on the first field when the composable enters composition
    LaunchedEffect(Unit) {
        delay(100) // Add a small delay to ensure readiness
        focusRequesters[0].requestFocus()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp) // Spacing between tiles
    ) {
        letters.forEachIndexed { index, letter ->
            val currentState = states[index]
            val backgroundColor = getBackgroundColor(currentState)
            // Text color is usually white for these backgrounds
            val textColor = Color.White
            var hasFocus by remember { mutableStateOf(false) }

            // --- The Tile Box ---
            Box(
                modifier = Modifier
                    .size(60.dp) // Tile size
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (hasFocus) 2.dp else 1.dp, // Highlight focused border
                        color = if (hasFocus) Color.LightGray else Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { // --- TAP TO CHANGE COLOR ---
                        states[index] = getNextState(currentState)
                        // Optional: Clear focus when tapping background to prevent
                        // keyboard staying open unnecessarily after color selection.
                        // focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                // --- The Input Field ---
                BasicTextField(
                    value = letter,
                    onValueChange = { newValue ->
                        // Allow only zero or one uppercase letter
                        val newLetter = newValue.filter { it.isLetter() }.take(1).uppercase()

                        if (letters[index] != newLetter) {
                             letters[index] = newLetter

                            // If a letter was entered, move focus to the next box
                            if (newLetter.isNotEmpty() && index < 4) {
                                focusRequesters[index + 1].requestFocus()
                            }
                             // Check if word is complete
                             if (letters.all { it.isNotEmpty() }) {
                                 val word = letters.joinToString("")
                                 onWordComplete(word, states.toList())
                                 // Optionally clear focus
                                 // focusManager.clearFocus()
                             }
                        }
                    },
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { focusState ->
                            hasFocus = focusState.isFocused
                        }
                        .onKeyEvent { keyEvent -> // --- BACKSPACE HANDLING ---
                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                if (letters[index].isEmpty() && index > 0) {
                                    // If current field is empty and backspace is pressed,
                                    // clear the previous field's letter and move focus back.
                                    letters[index - 1] = "" // Clear previous letter
                                    focusRequesters[index - 1].requestFocus()
                                    return@onKeyEvent true // Consume the event
                                } else if (letters[index].isNotEmpty()) {
                                    // If current field is not empty, clear it but stay focused
                                    letters[index] = ""
                                    return@onKeyEvent true // Consume the event (prevents double action)
                                }
                            }
                            false // Don't consume other key events
                        }
                        .fillMaxSize(), // Fill the Box
                    textStyle = TextStyle( // Center text visually
                        color = textColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        // Adjust line height if text isn't perfectly centered vertically
                        lineHeight = 30.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Ascii, // Restrict to ASCII letters
                        imeAction = if (index == 4) ImeAction.Done else ImeAction.Next // Next or Done action
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (index < 4) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        },
                        onDone = {
                            focusManager.clearFocus() // Hide keyboard
                            // Trigger completion check again if needed
                            if (letters.all { it.isNotEmpty() }) {
                                onWordComplete(letters.joinToString(""), states.toList())
                            }
                        }
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Transparent) // Hide the cursor
                )
            } // End Box
        } // End forEachIndexed
    } // End Row
}
