package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.components.SearchBarPlaceholder
import com.example.truekita.navigation.Screen

// Modelo simple para la lista de chats
data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

@Composable
fun ChatListScreen(navController: NavController) {
    // Lista de chats usando stringResource para cada campo
    val chats = listOf(
        ChatItem(
            stringResource(id = R.string.seller_alexis),
            stringResource(id = R.string.sample_message_1),
            "10:30 AM", 1
        ),
        ChatItem(
            stringResource(id = R.string.seller_nestor),
            stringResource(id = R.string.sample_message_2),
            "9:45 AM", 0
        ),
        ChatItem(
            stringResource(id = R.string.seller_abraham),
            stringResource(id = R.string.yesterday), "9:45"
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.chat), // Traducción de "Chat"
                showBack = false
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                SearchBarPlaceholder(
                    text = stringResource(id = R.string.search_user), // Traducción de "Buscar usuario..."
                    onSearchClick = { /* Lógica de filtrado */ }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(chats) { chat ->
                    ChatItemRow(chat) {
                        navController.navigate(Screen.ChatDetail.route)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: ChatItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D47A1)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = chat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF81C784)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}