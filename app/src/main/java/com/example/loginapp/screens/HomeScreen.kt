package com.example.loginapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginapp.ui.theme.LoginAppTheme

@Composable
fun HomeScreen(
	displayName: String?,
	userEmail: String?,
	infoMessage: String?,
	onSignOutClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White)
			.padding(20.dp),
		verticalArrangement = Arrangement.Center
	) {
		Text(
			text = "Đăng nhập thành công",
			fontSize = 28.sp,
			color = Color(0xFFB33A3A)
		)

		Spacer(modifier = Modifier.height(20.dp))

		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2F2))
		) {
			Column(modifier = Modifier.padding(16.dp)) {
				Text(text = "Xin chào ${displayName?.takeIf { it.isNotBlank() } ?: "bạn"}!")
				Spacer(modifier = Modifier.height(8.dp))
				Text(text = "Email: ${userEmail ?: "Chưa có dữ liệu"}")
				Spacer(modifier = Modifier.height(8.dp))
				Text(text = "Bước tiếp theo: tạo Home thật để đọc phim từ Firestore.")
			}
		}

		if (!infoMessage.isNullOrBlank()) {
			Spacer(modifier = Modifier.height(16.dp))
			Text(
				text = infoMessage,
				color = Color(0xFF2E7D32)
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		Button(
			onClick = onSignOutClick,
			modifier = Modifier.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB33A3A))
		) {
			Text(text = "ĐĂNG XUẤT", color = Color.White)
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
	LoginAppTheme {
		HomeScreen(
			displayName = "Hoàng Trung",
			userEmail = "hoangtrung.dev@gmail.com",
			infoMessage = "Tạo tài khoản thành công.",
			onSignOutClick = {}
		)
	}
}

