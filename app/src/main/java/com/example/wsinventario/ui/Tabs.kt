package com.example.wsinventario.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wsinventario.data.ContagemItem
import com.example.wsinventario.data.Produto

@Composable
fun ContagensTab(
    productsCount: Int, 
    totalQuantity: Int,
    contagemList: List<ContagemItem>,
    onContagemClick: (ContagemItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                InfoColumn(title = "PRODUTOS", value = productsCount.toString(), modifier = Modifier.weight(1f))
                InfoColumn(title = "QUANTIDADE TOTAL", value = totalQuantity.toString(), modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (contagemList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("Nenhum produto na contagem", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Text("Inicie agora incluindo um produto na contagem", textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn {
                items(contagemList) { contagem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContagemClick(contagem) } 
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = contagem.codigoDeBarras, modifier = Modifier.weight(1f))
                        Text(text = contagem.nome, modifier = Modifier.weight(1f))
                        Text(text = contagem.quantidade.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ProdutosTab(
    productList: List<Produto>,
    onProductClick: (Produto) -> Unit
) {
    if (productList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum produto cadastrado.")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(productList) { product ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onProductClick(product) }
                ) {
                    Text(text = product.codigoDeBarras, modifier = Modifier.weight(1f))
                    Text(text = product.nome, modifier = Modifier.weight(1f))
                    Text(text = product.quantidade.toString(), modifier = Modifier.weight(0.5f))
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
    }
}
