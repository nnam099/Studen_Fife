# Studen_Fife — Sổ Sinh Viên (Student Life)

Ứng dụng Android hiện tại là **100% Kotlin + Jetpack Compose** cho UI, với backend NestJS và PostgreSQL. Code demo Java/XML cũ đã được loại bỏ khỏi nhánh làm việc chính. Bản snapshot trước khi xóa được lưu tại branch `legacy-demo-backup` (commit `4d970e4`).

## Yêu cầu

- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 17
- Android SDK 34

## Mở project

1. Android Studio → **File → Open**
2. Chọn thư mục gốc repo (clone về máy)
3. Đợi Gradle sync (lần đầu có thể mất vài phút)

## Chạy app

- Chọn emulator hoặc thiết bị thật (API 24+)
- Run `app`

Build debug từ command line:

```bash
./gradlew assembleDebug
```

Gradle ưu tiên JDK 17: wrapper dùng `JAVA_HOME` nếu đã đặt, nếu chưa thì tự tìm JDK 17 trong các vị trí cài đặt phổ biến. Nếu máy không có JDK 17, cài JDK 17 hoặc đặt `JAVA_HOME` tới JDK 17 trước khi build.

Đăng ký hoặc đăng nhập bằng tài khoản backend thật. Base URL mặc định cho emulator là `http://10.0.2.2:3000/api/v1/` và có thể override bằng `-PbackendBaseUrl=...`.

## Cấu trúc màn hình hiện tại

| Màn hình | Mô tả |
|----------|--------|
| Login/Register | Gọi backend thật, lưu token mã hóa |
| Onboarding | Compose placeholder |
| Home/Dashboard | Compose placeholder, có refresh session |
| Thêm chi tiêu | Compose placeholder |
| Ngân sách | Compose placeholder |
| Deadline | Compose placeholder |
| Báo cáo | Compose placeholder |
| Cài đặt | Compose placeholder |

## Sơ đồ luồng

Xem file: [`docs/app_flow_diagram.svg`](docs/app_flow_diagram.svg)

## Nguồn thiết kế

- Nghiệp vụ: `Tai_lieu_nghiep_vu_QuanLySinhVien.docx`
- UX/UI prompt: `Stitch_Prompt_UX_UI.md`
- Chi tiết giao diện: thư mục `ui/`

## Sprint 1

- Retrofit + OkHttp kết nối API auth
- Access token và refresh token lưu bằng `EncryptedSharedPreferences`
- Navigation shell gồm 8 màn hình Compose
- Backend và database chạy bằng Docker Compose

## Nhóm phát triển

| STT | Họ tên |
|:---:|--------|
| 1 | Lương Minh Khôi |
| 2 | Võ Nguyễn Nhật Nam |
| 3 | Hồ Mạnh Danh |
| 4 | Trần Minh Quang |
| 5 | Trần Quốc Tuấn |
