package com.example.wsinventario.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.viewmodel.CadastroViewModel
import com.example.wsinventario.viewmodel.CadastroViewModelFactory
import com.example.wsinventario.ui.theme.WsinventarioTheme

class NovoProdutoActivity : ComponentActivity() {
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

    DisposableEffect(Unit) {
        viewModel.clearForm()
        onDispose { }
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.eanInput,
                onValueChange = viewModel::onEanChanged,
                label = { Text("EAN (Código de Barras)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.nomeInput,
                onValueChange = viewModel::onNomeChanged,
                label = { Text("NOME") },
                modifier = Modifier.fillMaxWidth()
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
                Button(onClick = {
                    viewModel.createProdutoNoCatalogo(
                        onSuccess = {
                            Toast.makeText(context, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                            activity?.setResult(Activity.RESULT_OK)
                            activity?.finish()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("ADICIONAR")
                }
            }
        }
    }
}
