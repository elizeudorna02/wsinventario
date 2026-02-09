package com.example.wsinventario.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.ui.theme.WsinventarioTheme
import com.example.wsinventario.viewmodel.InventarioViewModel
import com.example.wsinventario.viewmodel.InventarioViewModelFactory

class ApiSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WsinventarioTheme {
                val settingsViewModel: ApiSettingsViewModel = viewModel(factory = ApiSettingsViewModelFactory(application))
                val inventarioViewModel: InventarioViewModel = viewModel(factory = InventarioViewModelFactory(application))
                
                ApiSettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onImportClick = {
                        inventarioViewModel.importarProdutosDaApi()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(settingsViewModel: ApiSettingsViewModel, onImportClick: () -> Unit) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuração da API") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = settingsViewModel.apiUrl,
                onValueChange = settingsViewModel::onUrlChange,
                label = { Text("URL da API") },
                placeholder = { Text("http://127.0.0.1:5000/api/produtos") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    settingsViewModel.saveSettings()
                    Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR CONFIGURAÇÕES")
            }
            
            Button(
                onClick = {
                    settingsViewModel.saveSettings() // Salva antes de importar
                    onImportClick()
                    activity?.finish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR E IMPORTAR")
            }
        }
    }
}
