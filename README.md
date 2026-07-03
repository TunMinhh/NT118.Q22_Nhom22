<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: none;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<h1 align="center"><b>PHÁT TRIỂN ỨNG DỤNG TRÊN THIẾT BỊ DI ĐỘNG</b></h1>

<h2 align="center"><b>ỨNG DỤNG ĐẶT VÉ XEM PHIM</b></h2>

## BẢNG MỤC LỤC

* [Giới thiệu môn học](#gioithieumonhoc)
* [Giảng viên hướng dẫn](#giangvien)
* [Thành viên nhóm](#thanhvien)
* [Đồ án môn học](#doan)
* [Công nghệ sử dụng](#congnghe)
* [Chức năng hệ thống](#chucnang)
* [Cơ sở dữ liệu](#cosodulieu)
* [Thanh toán VNPay](#vnpay)
* [Cài đặt và triển khai](#caidat)

## GIỚI THIỆU MÔN HỌC

<a name="gioithieumonhoc"></a>

* **Tên môn học:** Phát triển ứng dụng trên thiết bị di động
* **Mã môn học:** NT118
* **Lớp học:** NT118.Q22

## GIẢNG VIÊN HƯỚNG DẪN

<a name="giangvien"></a>

* ThS. **Trần Hồng Nghi**

## THÀNH VIÊN NHÓM

<a name="thanhvien"></a>

| STT | MSSV | Họ và Tên | Github | Email |
| --- | --- | --- | --- | --- |
| 1 | 23520164 | Nguyễn Hữu Cảm | [nghuucam](https://github.com/nghuucam) | 23520164@gm.uit.edu.vn |
| 2 | 23520959 | Trần Tuấn Minh | [TunMinhh](https://github.com/TunMinhh) | 23520959@gm.uit.edu.vn |
| 3 | 23520537 | Phạm Duy Hoàng | [hoangpham1006](https://github.com/hoangpham1006) | 23520537@gm.uit.edu.vn |

## ĐỒ ÁN MÔN HỌC

<a name="doan"></a>

**Tên đề tài:** Ứng dụng đặt vé xem phim

**Mô tả:**

Ứng dụng đặt vé xem phim được xây dựng trên nền tảng Android, hỗ trợ người dùng xem danh sách phim, tìm kiếm và lọc phim, xem chi tiết phim, chọn rạp, chọn lịch chiếu, chọn ghế và tiến hành thanh toán vé trực tuyến thông qua VNPay.

Đồ án hướng đến việc mô phỏng quy trình đặt vé xem phim thực tế trên thiết bị di động, kết hợp giao diện hiện đại với cơ sở dữ liệu Firebase để quản lý phim, rạp, phòng chiếu, suất chiếu, vé và người dùng.

## CÔNG NGHỆ SỬ DỤNG

<a name="congnghe"></a>

* **Ngôn ngữ:** Kotlin
* **Giao diện:** Jetpack Compose
* **Kiến trúc ứng dụng:** Android Native App
* **Cơ sở dữ liệu:** Firebase Firestore
* **Xác thực người dùng:** Firebase Authentication
* **Backend xử lý thanh toán:** Firebase Cloud Functions
* **Thanh toán:** VNPay Sandbox
* **Công cụ phát triển:** Android Studio, Git, GitHub, Firebase Console

## CHỨC NĂNG HỆ THỐNG

<a name="chucnang"></a>

### Người dùng

* Đăng ký tài khoản
* Đăng nhập
* Đăng xuất
* Quên mật khẩu
* Xem và cập nhật thông tin cá nhân
* Xem lịch sử đặt vé

### Phim

* Xem danh sách phim
* Xem phim đang chiếu và phim sắp chiếu
* Tìm kiếm phim theo tên
* Lọc phim theo thể loại
* Lọc phim theo độ tuổi
* Xem chi tiết phim
* Xem thông tin mô tả, thời lượng, thể loại, độ tuổi, ngày khởi chiếu
* Đánh giá phim

### Rạp và lịch chiếu

* Xem danh sách rạp chiếu
* Xem thông tin rạp
* Xem các tiện ích của rạp
* Xem lịch chiếu theo phim
* Xem lịch chiếu theo rạp
* Chọn suất chiếu phù hợp

### Đặt vé

* Chọn phim
* Chọn rạp
* Chọn lịch chiếu
* Chọn ghế theo sơ đồ phòng chiếu
* Hiển thị ghế trống, ghế đã đặt và ghế đang chọn
* Tính tổng tiền vé theo số lượng ghế
* Tạo vé sau khi đặt thành công
* Lưu thông tin vé vào Firestore

### Thanh toán

* Tích hợp thanh toán VNPay Sandbox
* Tạo yêu cầu thanh toán thông qua Firebase Cloud Functions
* Chuyển người dùng sang trang thanh toán VNPay
* Nhận kết quả thanh toán trả về
* Cập nhật trạng thái vé sau thanh toán

## CƠ SỞ DỮ LIỆU

<a name="cosodulieu"></a>

## THANH TOÁN VNPAY

<a name="vnpay"></a>

Đồ án tích hợp VNPay Sandbox để mô phỏng quy trình thanh toán vé xem phim trực tuyến.

Quy trình xử lý:

1. Người dùng chọn ghế và xác nhận đặt vé.
2. Ứng dụng gửi thông tin đơn hàng lên Firebase Cloud Functions.
3. Cloud Functions tạo URL thanh toán VNPay với chữ ký bảo mật.
4. Người dùng được chuyển sang trang thanh toán VNPay.
5. VNPay trả kết quả giao dịch về callback URL.
6. Hệ thống cập nhật trạng thái thanh toán và lưu thông tin vé.

Các Cloud Functions chính:

* `createVnpayPayment`: Tạo URL thanh toán VNPay.
* `vnpayReturn`: Nhận kết quả trả về sau khi người dùng thanh toán.
* `vnpayIpn`: Xử lý IPN từ VNPay.

## CÀI ĐẶT VÀ TRIỂN KHAI

<a name="caidat"></a>

### Yêu cầu

* Android Studio
* JDK phù hợp với Gradle của dự án
* Android SDK
* Firebase Project
* Thiết bị Android hoặc Android Emulator

### Cài đặt ứng dụng Android

1. Clone repository:

```bash
git clone <repository-url>