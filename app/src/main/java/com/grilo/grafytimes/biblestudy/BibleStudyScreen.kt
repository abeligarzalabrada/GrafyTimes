package com.grilo.grafytimes.biblestudy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grilo.grafytimes.biblestudy.data.BibleStudy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleStudyScreen(
    modifier: Modifier = Modifier,
    viewModel: BibleStudyViewModel = viewModel()
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var currentEditingStudy by remember { mutableStateOf<BibleStudy?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Estudios Bíblicos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Lista de estudios
        AnimatedVisibility(
            visible = viewModel.bibleStudies.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.bibleStudies) { study ->
                    BibleStudyItem(
                        study = study,
                        onEditClick = {
                            currentEditingStudy = study
                            showAddEditDialog = true
                        },
                        onDeleteClick = {
                            viewModel.deleteBibleStudy(study.id)
                        }
                    )
                    Divider()
                }
            }
        }
        
        // Botón para añadir nuevo estudio
        Button(
            onClick = {
                currentEditingStudy = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir Nuevo Estudio")
        }
    }
    
    // Diálogo para añadir/editar estudio
    if (showAddEditDialog) {
        AddEditBibleStudyDialog(
            study = currentEditingStudy,
            onDismiss = { showAddEditDialog = false },
            onSave = { study ->
                viewModel.saveBibleStudy(study)
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun BibleStudyItem(
    study: BibleStudy,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val lastVisitText = if (study.lastVisitDate.isNotBlank()) {
        try {
            val dateTime = LocalDateTime.parse(study.lastVisitDate)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "Última visita: ${dateTime.format(formatter)}"
        } catch (e: Exception) {
            "Última visita: No disponible"
        }
    } else {
        "Sin visitas registradas"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = study.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = lastVisitText,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (study.contactInfo.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = study.contactInfo,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBibleStudyDialog(
    study: BibleStudy?,
    onDismiss: () -> Unit,
    onSave: (BibleStudy) -> Unit
) {
    var name by remember { mutableStateOf(study?.name ?: "") }
    var contactInfo by remember { mutableStateOf(study?.contactInfo ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (study == null) "Añadir Estudio Bíblico" else "Editar Estudio Bíblico") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Información de contacto / Notas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val updatedStudy = study?.copy(
                            name = name,
                            contactInfo = contactInfo
                        ) ?: BibleStudy(
                            name = name,
                            contactInfo = contactInfo,
                            createdAt = LocalDateTime.now().toString()
                        )
                        onSave(updatedStudy)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}