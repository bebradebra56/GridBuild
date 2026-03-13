package com.gridibuild.sfobud.ui.screens.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gridibuild.sfobud.data.local.entity.ContactEntity
import com.gridibuild.sfobud.ui.components.*
import com.gridibuild.sfobud.ui.theme.*
import com.gridibuild.sfobud.viewmodel.ContactsViewModel

@Composable
fun ContactsScreen(navController: NavController) {
    val vm: ContactsViewModel = viewModel()
    val contacts by vm.contacts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editContact by remember { mutableStateOf<ContactEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar("Contacts", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Orange, contentColor = Color.White) {
                Icon(Icons.Filled.PersonAdd, "Add Contact")
            }
        }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.Contacts,
                    title = "No contacts",
                    subtitle = "Add contractors, electricians, and suppliers",
                    actionLabel = "Add Contact",
                    onAction = { showDialog = true }
                )
            }
        } else {
            val grouped = contacts.groupBy { it.role }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (role, list) ->
                    item {
                        Text(
                            if (role.isBlank()) "Other" else role,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Orange,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(list, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onEdit = { editContact = contact },
                            onDelete = { vm.deleteContact(contact) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog || editContact != null) {
        ContactDialog(
            contact = editContact,
            roles = vm.roles,
            onDismiss = { showDialog = false; editContact = null },
            onSave = { name, role, phone, email, company, notes ->
                if (editContact != null) vm.updateContact(editContact!!.copy(name = name, role = role, phone = phone, email = email, company = company, notes = notes))
                else vm.addContact(name, role, phone, email, company, notes)
                showDialog = false; editContact = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    contact: ContactEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val roleColor = when (contact.role) {
        "Master" -> Orange; "Electrician" -> BrightYellow; "Plumber" -> SaturatedBlue
        "Carpenter" -> Color(0xFF795548); "Painter" -> WarmRed; "Delivery" -> Turquoise
        "Store" -> ProgressGreen; "Designer" -> Color(0xFFE91E63); else -> GrayBeige
    }
    val initials = contact.name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = roleColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (contact.role.isNotBlank()) Text(contact.role, fontSize = 12.sp, color = roleColor)
                if (contact.company.isNotBlank()) Text(contact.company, fontSize = 12.sp, color = OnSurfaceVariant)
                if (contact.phone.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(contact.phone, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (contact.phone.isNotBlank()) {
                    IconButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))) },
                        modifier = Modifier.size(36.dp)
                    ) { Icon(Icons.Filled.Call, null, tint = ProgressGreen, modifier = Modifier.size(20.dp)) }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.MoreVert, null, modifier = Modifier.size(18.dp)) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Edit, null) })
                        DropdownMenuItem(text = { Text("Delete", color = WarmRed) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = WarmRed) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDialog(
    contact: ContactEntity?,
    roles: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var role by remember { mutableStateOf(contact?.role ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var company by remember { mutableStateOf(contact?.company ?: "") }
    var notes by remember { mutableStateOf(contact?.notes ?: "") }
    var expandedRole by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(if (contact == null) "New Contact" else "Edit Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Person, null) })
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expandedRole, onExpandedChange = { expandedRole = it }) {
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedRole) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Filled.Work, null) })
                    ExposedDropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        roles.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { role = it; expandedRole = false }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Phone, null) })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Email, null) })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Business, null) })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { onSave(name, role, phone, email, company, notes) }, modifier = Modifier.weight(1f), enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Save") }
                }
            }
        }
    }
}
