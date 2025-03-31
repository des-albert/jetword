package org.db.jetword

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.db.jetword.ui.theme.JetWordTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import org.db.jetword.ui.theme.provider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetWordTheme {
                Scaffold(
                    topBar = {
                        WordTopBar()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    WordFind(innerPadding)
                }
            }
        }
    }
}

val fontName = GoogleFont("Orbitron")
val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTopBar() {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fontFamily = fontFamily, text = "JetWord"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
fun WordFind(
    innerPadding: PaddingValues,
    wordFindModel: WordViewModel = viewModel()
) {
    var guess by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var guessCount by remember { mutableIntStateOf(0) }
    var resultWords by remember { mutableStateOf(listOf<String>()) }
    var blackLetters by remember { mutableStateOf(setOf<Char>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        wordFindModel.loadWords(context)
    }

    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = guess,
                        onValueChange = {
                            guess = it
                        },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        placeholder = {
                            Text(
                                text = "Enter Guess",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                            focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                            focusedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = {
                            feedback = it
                        },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Enter feedback",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                            focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                            focusedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )

                }

            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ElevatedButton(
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .padding(8.dp),
                    onClick = {
                        if (guessCount < 5) {
                            resultWords = wordFindModel.searchWords(guess, feedback, blackLetters)
                            guessCount++
                            blackLetters = updateBlackLetters(guess, feedback, blackLetters)
                        }

                    })
                {
                    Text(
                        text = "Solve",
                        fontSize = 16.sp
                    )
                }

            }
            Box(
                modifier = Modifier
                    .width(350.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(resultWords.size) { index ->
                        Text(
                            text = resultWords[index],
                            fontSize = 16.sp,

                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

fun updateBlackLetters(guess: String, feedback: String, blackLetters: Set<Char>): Set<Char> {
    val newBlackLetters = blackLetters.toMutableSet()
    for (i in feedback.indices) {
        if (feedback[i] == 'B') {
            newBlackLetters.add(guess[i])
        }
    }
    return newBlackLetters
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun WordFindPreview() {
    JetWordTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            WordFind(innerPadding)
        }
    }
}