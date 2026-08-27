# Sổ Sinh Viên — Student Life 

Ứng dụng demo **Quản lý chi tiêu & thời gian cho sinh viên**, mở bằng Android Studio. Dữ liệu mock để visualize giao diện và luồng nghiệp vụ.

## Yêu cầu

- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 17
- Android SDK 34

## Mở project

1. Android Studio → **File → Open**
2. Chọn thư mục `d:\nam4\aicourse\src`
3. Đợi Gradle sync (lần đầu có thể mất vài phút)

## Chạy app

- Chọn emulator hoặc thiết bị thật (API 24+)
- Run `app`

### Tài khoản demo

| Email | Mật khẩu |
|-------|----------|
| `demo@truong.edu.vn` | `demo1234` |

Hoặc đăng ký tài khoản mới (validation cơ bản, không lưu server).

## Cấu trúc màn hình

| Màn hình | Mô tả |
|----------|--------|
| Splash → Auth | Đăng nhập / Đăng ký |
| Onboarding | 3 bước cấu hình (giờ ngủ, học/làm, sở thích) |
| Trang chủ | Số dư, ngân sách, nhắc nhở, sắp tới |
| Chi tiêu | Ngân sách, giao dịch, thống kê, danh mục |
| Lịch | Lịch tháng, sự kiện/công việc, đề xuất AI |
| Trợ lý | Chat demo với mock phản hồi |
| Cài đặt | Tài khoản, nhắc nhở, dark mode, đăng xuất |

## Sơ đồ luồng

Xem file: [`docs/app_flow_diagram.svg`](docs/app_flow_diagram.svg)

## Nguồn thiết kế

- Nghiệp vụ: `Tai_lieu_nghiep_vu_QuanLySinhVien.docx` (thư mục cha `aicourse`)
- UX/UI prompt: `Stitch_Prompt_UX_UI.md`
- Chi tiết giao diện: thư mục `ui/`

## Ghi chú demo

- Không kết nối backend / database
- OCR, AI, thông báo là mock UI
- Mọi thao tác "Lưu" hiển thị Toast và quay lại màn trước

## Nhóm phát triển

| STT | Họ tên |
|:---:|--------|
| 1 | Lương Minh Khôi |
| 2 | Võ Nguyễn Nhật Nam |
| 3 | Hồ Mạnh Danh |
| 4 | Trần Minh Quang |
| 5 | Trần Quốc Tuấn |
