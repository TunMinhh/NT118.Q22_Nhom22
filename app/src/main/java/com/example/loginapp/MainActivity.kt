package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.loginapp.ui.theme.LoginAppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextDecoration


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginAppTheme {
                LoginScreen()
            }
        }
        }
    }


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreen() {
    // Biến lưu email
    var email by remember { mutableStateOf("") }
    // Biến lưu mật khẩu
    var password by remember { mutableStateOf("") }

    // Sắp xếp các thành phần theo chiều dọc
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Hiển thị banner phía trên
        Image(
            painter = painterResource(id = R.drawable.login_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(50.dp)) // Khoảng cách giữa banner và ô nhập email, sdt

            // ô nhập email, sdt
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email hoặc số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(15.dp)) // Khoảng cách giữa ô nhập email,sdt và password

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(25.dp)) // Khoảng cách giữa ô nhập mật khẩu và nút đăng nhập

            // Nút đăng nhập
            Button(
                onClick = {
                    // xử lý khi bấm đăng nhập
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB33A3A)
                )
            ) {
                Text("ĐĂNG NHẬP", fontSize = 18.sp, color=Color.White)
            }

            Spacer(modifier = Modifier.height(25.dp)) // Khoảng cách giữa nút đăng nhập và nút quên mật khẩu

            TextButton(
                onClick = {
                    // xử lý khi bấm quên mật khẩu
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text (
                    text = "Quên mật khẩu?",
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(25.dp)) // Khoảng cách giữa nút quên mật khẩu và nút đăng ký tài khoản

            // Nút đăng ký tài khoản
            OutlinedButton(
                onClick = {
                        // chuyển sang trang đăng ký
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {

                Text(
                    text = "Đăng ký tài khoản CGV",
                    fontSize = 18.sp
                )
            }



        }
    }


}