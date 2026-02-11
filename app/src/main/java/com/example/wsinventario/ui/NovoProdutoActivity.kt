package com.example.wsinventario.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.viewmodel.CadastroViewModel
import com.example.wsinventario.viewmodel.CadastroViewModelFactory
import com.example.wsinventario.ui.theme.WsinventarioTheme
import kotlinx.coroutines.flow.collectLatest

class NovoProdutoActivity : ComponentActivity() {

    companion object {
        const val RESULT_START_CONTAGEM = 1001
        const val EXTRA_EAN = "extra_ean"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WsinventarioTheme {
                val viewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(application))
                NovoProdutoScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoProdutoScreen(viewModel: CadastroViewModel) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    var showProductExistsDialog by remember { mutableStateOf<com.example.wsinventario.data.Produto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearForm()
        viewModel.uiEvents.collectLatest { event ->
            when(event) {
                is CadastroViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is CadastroViewModel.UiEvent.ShowProductExistsDialog -> {
                    showProductExistsDialog = event.product
                }
                is CadastroViewModel.UiEvent.CadastroSuccess -> {
                    Toast.makeText(context, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
                }
            }
        }
    }

    if (showProductExistsDialog != null) {
        ConfirmationDialog(
            onDismissRequest = { showProductExistsDialog = null },
            onConfirmation = {
                val intent = Intent().apply {
                    putExtra(NovoProdutoActivity.EXTRA_EAN, showProductExistsDialog?.ean)
                }
                activity?.setResult(NovoProdutoActivity.RESULT_START_CONTAGEM, intent)
                activity?.finish()
            },
            dialogTitle = "Produto já existe",
            dialogText = "Deseja iniciar uma contagem para este item?",
            icon = Icons.Outlined.HelpOutline
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Novo Produto no Catálogo") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.codigoInput,
                onValueChange = viewModel::onCodigoChanged,
                label = { Text("CÓDIGO") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.produtoOriginal == null
            )
            OutlinedTextField(
                value = viewModel.eanInput,
                onValueChange = viewModel::onEanChanged,
                label = { Text("EAN (Código de Barras)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.submitNewProduto() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.nomeInput,
                onValueChange = viewModel::onNomeChanged,
                label = { Text("NOME") },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.produtoOriginal == null
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { activity?.finish() }) {
                    Text("CANCELAR")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.submitNewProduto() }) {
                    Text("ADICIONAR")
                }
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
                Text("SIM")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("NÃO")
            }
        }
    )
}
