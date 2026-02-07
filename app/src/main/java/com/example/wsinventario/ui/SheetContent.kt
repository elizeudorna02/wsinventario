package com.example.wsinventario.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wsinventario.viewmodel.CadastroViewModel

@Composable
fun NovoProdutoSheetContent(
    viewModel: CadastroViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Novo Produto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = viewModel.nome,
            onValueChange = viewModel::onNomeChanged,
            label = { Text("NOME PRODUTO") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.codigoDeBarras,
            onValueChange = viewModel::onCodigoDeBarrasChanged,
            label = { Text("CÓDIGO BARRAS") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) {
                Text("CANCELAR")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("ADICIONAR")
            }
        }
    }
}

@Composable
fun EditarProdutoSheetContent(
    viewModel: CadastroViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Editar Cadastro do Produto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = viewModel.nome,
            onValueChange = viewModel::onNomeChanged,
            label = { Text("NOME PRODUTO") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.codigoDeBarras,
            onValueChange = viewModel::onCodigoDeBarrasChanged,
            label = { Text("CÓDIGO BARRAS") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) {
                Text("CANCELAR")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("SALVAR")
            }
        }
    }
}

@Composable
fun ProductOptionsSheetContent(
    onNovaContagem: () -> Unit,
    onEditarCadastro: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        ListItem(
            headlineContent = { Text("Editar cadastro") },
            leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onEditarCadastro)
        )
        ListItem(
            headlineContent = { Text("Nova contagem") },
            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onNovaContagem)
        )
    }
}

@Composable
fun CadastroSheetContent(
    viewModel: CadastroViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Nova Contagem", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = viewModel.nome,
            onValueChange = viewModel::onNomeChanged,
            label = { Text("NOME PRODUTO") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.codigoDeBarras,
            onValueChange = viewModel::onCodigoDeBarrasChanged,
            label = { Text("CÓDIGO BARRAS") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("QUANTIDADE", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { viewModel.decrementQuantidade() }, 
                modifier = Modifier.padding(end = 8.dp).size(56.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrementar")
            }
            OutlinedTextField(
                value = viewModel.quantidade,
                onValueChange = viewModel::onQuantidadeChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
            OutlinedButton(
                onClick = { viewModel.incrementQuantidade() }, 
                modifier = Modifier.padding(start = 8.dp).size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Incrementar")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) {
                Text("CANCELAR")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("SALVAR")
            }
        }
    }
}
