package com.ramstudio.kaskita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.ui.theme.KasKitaTheme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


val supabase = createSupabaseClient(
    supabaseUrl = "https://jipxvjzjtoysavdoskoi.supabase.co",
    supabaseKey = "sb_publishable_doZwpA-lIlXxB8-Yg71eHg_ItZV38M7"
) {
    install(Auth)
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KasKitaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            InstrumentsList()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstrumentsList() {
    var instruments by remember { mutableStateOf<List<Instrument>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            instruments = supabase.from("instruments").select().decodeList<Instrument>()
        }
    }
    LazyColumn {
        items(
            instruments,
            key = { instrument -> instrument.id }
        ) { instrument ->
            Text(
                instrument.name,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Serializable
data class Instrument(
    val id: Int,
    val name: String
)