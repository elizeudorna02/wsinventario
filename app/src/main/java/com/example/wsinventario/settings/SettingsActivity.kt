@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wsinventario.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.ui.theme.WsinventarioTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WsinventarioTheme {
                val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(application))
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    var expanded by remember { mutableStateOf(false) }
    val fieldCountOptions = listOf("2", "3")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurações") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Grupo de Importação ---
            Text(
                text = "Importação",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = viewModel.importDelimiter,
                onValueChange = viewModel::onImportDelimiterChanged,
                label = { Text("Delimitador de Arquivo de Importação") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = viewModel.importFieldCount,
                    onValueChange = {}, // Não é alterado diretamente pelo usuário
                    readOnly = true,
                    label = { Text("Campos na Importação") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor() // Conecta o campo ao menu
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    fieldCountOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onFieldCountChanged(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Grupo de Exportação ---
            Text(
                text = "Exportação",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = viewModel.exportDelimiter,
                onValueChange = viewModel::onExportDelimiterChanged,
                label = { Text("Delimitador de Arquivo de Exportação") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botão Salvar
            Button(
                onClick = {
                    viewModel.saveSettings()
                    Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR")
            }
        }
    }
}
