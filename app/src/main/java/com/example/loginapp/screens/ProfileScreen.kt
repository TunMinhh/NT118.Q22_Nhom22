package com.example.loginapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.loginapp.R
import com.example.loginapp.auth.AuthUiState
import java.io.File



// Màn hình hồ sơ người dùng — hiển thị thông tin tài khoản và nút đăng xuất
@Composable
fun ProfileScreen(
    uiState: AuthUiState,
    onUpdateProfile: (name: String, phone: String, address: String, onComplete: (String?) -> Unit) -> Unit,
    onSignOutClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    // Dùng fallback nếu tên/email null hoặc rỗng (ví dụ đăng nhập ẩn danh)
    val safeDisplayName = uiState.displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_default_name)
    val safeEmail = uiState.userEmail?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_default_email)
    val safePhone = uiState.phoneNumber?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_phone_placeholder)
    val safeAddress = uiState.address?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_address_placeholder)

    val context = LocalContext.current
    // File lưu trữ ảnh cục bộ trong bộ nhớ trong của app
    val avatarFile = remember { File(context.filesDir, "profile_avatar.jpg") }

    // Trạng thái lưu model ảnh (có thể là Uri hoặc File)
    var currentAvatarModel by remember { 
        mutableStateOf<Any?>(if (avatarFile.exists()) avatarFile else null) 
    }

    // Launcher để mở thư viện ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy ảnh được chọn vào bộ nhớ trong để lưu vĩnh viễn (cục bộ)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    avatarFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                // Gán trực tiếp Uri để Coil cập nhật giao diện ngay lập tức
                currentAvatarModel = uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Trạng thái chỉnh sửa thông tin hồ sơ
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(uiState.displayName) { mutableStateOf(uiState.displayName.orEmpty()) }
    var editPhone by remember(uiState.phoneNumber) { mutableStateOf(uiState.phoneNumber.orEmpty()) }
    var editAddress by remember(uiState.address) { mutableStateOf(uiState.address.orEmpty()) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 20.dp)
            )



            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        if (currentAvatarModel != null) {
                            AsyncImage(
                                // Sử dụng ImageRequest để vô hiệu hóa cache bộ nhớ nếu dùng File
                                // Đảm bảo luôn lấy ảnh mới nhất nếu cùng tên file
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentAvatarModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.profile_avatar_desc),
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = CircleShape
                                    )
                                    .padding(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Icon Camera ở góc phải dưới
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Thay đổi ảnh đại diện",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = safeDisplayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = safeEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isEditing) {
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.profile_info_label_name),
                            value = safeDisplayName
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.profile_info_label_email),
                            value = safeEmail
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = stringResource(R.string.profile_info_label_phone),
                            value = safePhone
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = stringResource(R.string.profile_info_label_address),
                            value = safeAddress
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.profile_edit_button))
                        }
                    } else {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(stringResource(R.string.profile_info_label_name)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = safeEmail,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.profile_info_label_email)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text(stringResource(R.string.profile_info_label_phone)) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text(stringResource(R.string.profile_info_label_address)) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editName = uiState.displayName.orEmpty()
                                    editPhone = uiState.phoneNumber.orEmpty()
                                    editAddress = uiState.address.orEmpty()
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                Text(stringResource(R.string.profile_cancel_button))
                            }

                            Button(
                                onClick = {
                                    onUpdateProfile(editName, editPhone, editAddress) { error ->
                                        if (error == null) {
                                            isEditing = false
                                            android.widget.Toast.makeText(context, "Cập nhật hồ sơ thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Cập nhật thất bại: $error", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading && editName.isNotBlank()
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.profile_save_button))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nút Lịch sử đặt vé
            Button(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lịch sử đặt vé",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút Đăng Xuất — dùng Button với màu nền đỏ để chữ luôn hiển thị rõ
            Button(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.profile_sign_out),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Hàng hiển thị một cặp nhãn – giá trị kèm icon (dùng trong card thông tin)
@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


