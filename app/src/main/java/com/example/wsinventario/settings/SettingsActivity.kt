@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wsinventario.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    
    var showClearProdutosDialog by remember { mutableStateOf(false) }
    var showClearContagemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurações") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp), // Padding at the bottom to avoid overlapping with the button
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

                FieldCountDropdown(label = "Campos na Importação", selection = viewModel.importFieldCount, onSelectionChanged = viewModel::onFieldCountChanged)

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

                FieldCountDropdown(label = "Campos na Exportação", selection = viewModel.exportFieldCount, onSelectionChanged = viewModel::onExportFieldCountChanged)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // --- Ações Perigosas ---
                Text(
                    text = "Manutenção de Dados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                OutlinedButton(
                    onClick = { showClearProdutosDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("LIMPAR CATÁLOGO DE PRODUTOS")
                }
                
                OutlinedButton(
                    onClick = { showClearContagemDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("LIMPAR CONTAGEM ATUAL")
                }

            }

            // Botão Salvar
            Button(
                onClick = {
                    viewModel.saveSettings()
                    Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("SALVAR")
            }
        }
        
        // Dialogs de Confirmação
        if (showClearProdutosDialog) {
            ConfirmationDialog(
                onDismissRequest = { showClearProdutosDialog = false },
                onConfirmation = { 
                    viewModel.clearProdutosTable()
                    showClearProdutosDialog = false 
                    Toast.makeText(context, "Catálogo de produtos limpo!", Toast.LENGTH_SHORT).show()
                },
                dialogTitle = "Limpar Catálogo?",
                dialogText = "Todos os produtos do seu catálogo local serão apagados. Esta ação não pode ser desfeita.",
                icon = Icons.Outlined.Delete
            )
        }

        if (showClearContagemDialog) {
            ConfirmationDialog(
                onDismissRequest = { showClearContagemDialog = false },
                onConfirmation = { 
                    viewModel.clearContagemTable()
                    showClearContagemDialog = false 
                    Toast.makeText(context, "Contagem limpa!", Toast.LENGTH_SHORT).show()
                 },
                dialogTitle = "Limpar Contagem?",
                dialogText = "Todos os itens da sua contagem atual serão apagados. Esta ação não pode ser desfeita.",
                icon = Icons.Outlined.Delete
            )
        }
    }
}

@Composable
fun FieldCountDropdown(label: String, selection: String, onSelectionChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("2", "3", "4")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selection,
            onValueChange = {}, // Não é alterado diretamente pelo usuário
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor() // Conecta o campo ao menu
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onSelectionChanged(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = null)
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
