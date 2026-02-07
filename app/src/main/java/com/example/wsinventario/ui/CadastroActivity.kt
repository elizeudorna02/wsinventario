
package com.example.wsinventario.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.viewmodel.CadastroViewModel
import com.example.wsinventario.viewmodel.CadastroViewModelFactory
import com.example.wsinventario.ui.theme.WsinventarioTheme
import kotlinx.coroutines.flow.collectLatest

class CadastroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WsinventarioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: CadastroViewModel = viewModel(
                        factory = CadastroViewModelFactory(application)
                    )
                    CadastroProdutoScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CadastroProdutoScreen(
    modifier: Modifier = Modifier,
    viewModel: CadastroViewModel
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val onSuccessAction: () -> Unit = {
        Toast.makeText(context, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show()
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }

    val onFailureAction: (String) -> Unit = {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest {
            when (it) {
                is CadastroViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is CadastroViewModel.UiEvent.ShowConfirmationDialog -> {
                    showConfirmationDialog = true
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Produto não cadastrado") },
            text = { Text("Deseja cadastrar este novo produto?") },
            confirmButton = {
                TextButton(onClick = { 
                    showConfirmationDialog = false
                    viewModel.confirmAndCreateProduct(onSuccess = onSuccessAction, onFailure = onFailureAction)
                }) {
                    Text("SIM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("NÃO")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = viewModel.codigoDeBarras,
                onValueChange = viewModel::onCodigoDeBarrasChanged,
                label = { Text("Código de Barras") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = viewModel.nome,
                onValueChange = viewModel::onNomeChanged,
                label = { Text("Nome do Produto") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = viewModel.quantidade,
                onValueChange = viewModel::onQuantidadeChanged,
                label = { Text("Quantidade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.onSaveContagemClicked(onSuccess = onSuccessAction, onFailure = onFailureAction)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Produto")
            }
        }
        Spacer(modifier = Modifier.weight(4f))
    }
}

@Preview(showBackground = true)
@Composable
fun CadastroProdutoScreenPreview() {
    WsinventarioTheme {
        CadastroProdutoScreen(
            viewModel = CadastroViewModel(Application())
        )
    }
}
