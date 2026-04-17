package com.example.truekita.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.truekita.R

@Composable
fun RouteCard(
    name: String,
    rating: String,
    route: String,
    departure: String,
    availability: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(text = rating)
            Text(text = route)
            Text(text = departure)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                if (isAvailable) Color(0xFF21D84C) else Color.Red,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = availability)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, shape = RoundedCornerShape(20.dp)) {
                        Text(text = stringResource(R.string.view_route))
                    }
                    Button(onClick = {}, shape = RoundedCornerShape(20.dp)) {
                        Text(text = stringResource(R.string.request_trip))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    subtitle: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF8E4))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = subtitle, color = Color.Gray)
            }
            Text(text = status, color = Color(0xFF6B8E5A))
        }
    }
}