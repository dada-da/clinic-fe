# Hệ Thống Quản Lý Phòng Khám - Clinic Management System

Ứng dụng desktop quản lý phòng khám được xây dựng bằng JavaFX, cung cấp giao diện người dùng để quản lý bệnh nhân, bác sĩ, lịch hẹn và hồ sơ y tế.

## 📋 Mô Tả Dự Án

Dự án này là một ứng dụng desktop client kết nối với REST API backend để quản lý các hoạt động của phòng khám. Ứng dụng cho phép:

- **Quản lý Bệnh nhân**: Xem, tìm kiếm, tạo mới, cập nhật và xóa thông tin bệnh nhân
- **Quản lý Bác sĩ**: Xem danh sách và thông tin chi tiết bác sĩ
- **Quản lý Lịch hẹn**: Tạo, xem, và hoàn thành lịch hẹn khám bệnh
- **Quản lý Hồ sơ Y tế**: Xem và tạo hồ sơ y tế cho các lịch hẹn

## 🏗️ Cấu Trúc Dự Án

```
clinic-fe/
├── pom.xml                          # File cấu hình Maven
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java     # Module descriptor (Java Platform Module System)
│       │   └── com/
│       │       └── clinic/
│       │           └── ui/
│       │               ├── Main.java                    # Entry point của ứng dụng
│       │               ├── controller/                  # Các controller (MVC pattern)
│       │               │   ├── AppointmentListViewController.java
│       │               │   ├── AppointmentTabController.java
│       │               │   └── PatientViewController.java
│       │               ├── model/                       # Data Transfer Objects (DTOs)
│       │               │   ├── AppointmentDTO.java
│       │               │   ├── DoctorDTO.java
│       │               │   ├── MedicalRecordDTO.java
│       │               │   └── PatientDTO.java
│       │               └── service/                     # Service layer
│       │                   └── ApiService.java          # Service giao tiếp với REST API
│       └── resources/
│           └── views/                                   # FXML view files
│               ├── appointment_list_view.fxml
│               ├── appointment_view.fxml
│               └── patient_view.fxml
└── target/                          # Thư mục build output (tự động tạo)
```

### Kiến Trúc

Dự án tuân theo mô hình **MVC (Model-View-Controller)**:

- **Model**: Các DTO classes trong package `com.clinic.ui.model` đại diện cho dữ liệu từ API
- **View**: Các file FXML trong `resources/views/` định nghĩa giao diện người dùng
- **Controller**: Các controller classes trong `com.clinic.ui.controller` xử lý logic và tương tác giữa View và Model
- **Service**: `ApiService` cung cấp các phương thức để giao tiếp với REST API backend

## 🛠️ Công Nghệ Sử Dụng

### Ngôn Ngữ & Platform
- **Java 21**: Ngôn ngữ lập trình chính
- **Java Platform Module System (JPMS)**: Module system của Java

### Framework & Libraries
- **JavaFX 22**: Framework để xây dựng giao diện desktop
  - `javafx-controls`: Các control UI cơ bản
  - `javafx-fxml`: Hỗ trợ FXML để định nghĩa UI
  - `javafx-base`: Base classes
  - `javafx-graphics`: Graphics và rendering

### JSON Processing
- **Jackson 2.20.1**: Thư viện xử lý JSON
  - `jackson-databind`: Serialization/Deserialization JSON
  - `jackson-datatype-jsr310`: Hỗ trợ Java 8 time API (LocalDateTime, etc.)

### HTTP Client
- **Java HTTP Client** (java.net.http): HTTP client tích hợp sẵn trong Java 11+ để giao tiếp với REST API

### Build Tool
- **Maven**: Quản lý dependencies và build project
- **JavaFX Maven Plugin**: Plugin để chạy JavaFX applications

## 📦 Yêu Cầu Hệ Thống

- **JDK 21** hoặc cao hơn
- **Maven 3.6+**
- **REST API Backend** đang chạy tại `http://localhost:8080/api`

## 🚀 Hướng Dẫn Chạy Dự Án

### 1. Kiểm Tra Yêu Cầu

Đảm bảo đã cài đặt:
```bash
java -version  # Kiểm tra Java version (cần Java 21+)
mvn -version   # Kiểm tra Maven version
```

### 2. Clone Repository

```bash
git clone <repository-url>
cd clinic-fe
```

### 3. Đảm Bảo Backend API Đang Chạy

Ứng dụng cần kết nối với REST API backend tại `http://localhost:8080/api`. Đảm bảo backend đã được khởi động trước khi chạy ứng dụng.

### 4. Build Project

```bash
mvn clean compile
```

### 5. Chạy Ứng Dụng

#### Cách 1: Sử dụng JavaFX Maven Plugin (Khuyến nghị)

```bash
mvn javafx:run
```

#### Cách 2: Chạy trực tiếp với Java

```bash
# Compile project
mvn clean package

# Chạy ứng dụng
java --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.clinic.ui.Main
```

**Lưu ý**: Cách 2 yêu cầu đã tải JavaFX SDK và chỉ định đường dẫn đến thư mục `lib` của JavaFX SDK.

### 6. Cấu Hình API Endpoint (Nếu cần)

Nếu backend API không chạy tại `localhost:8080`, cần chỉnh sửa biến `BASE_URL` trong file:
```
src/main/java/com/clinic/ui/service/ApiService.java
```

Thay đổi dòng:
```java
private static final String BASE_URL = "http://localhost:8080/api";
```

## 📝 Các Tính Năng Chính

### Quản Lý Bệnh Nhân
- Xem danh sách tất cả bệnh nhân
- Tìm kiếm bệnh nhân theo tên hoặc số CMND/CCCD
- Tạo mới bệnh nhân
- Cập nhật thông tin bệnh nhân
- Xóa bệnh nhân

### Quản Lý Lịch Hẹn
- Xem danh sách lịch hẹn
- Xem lịch hẹn hôm nay
- Tạo lịch hẹn mới
- Xem chi tiết lịch hẹn
- Hoàn thành lịch hẹn

### Quản Lý Hồ Sơ Y Tế
- Xem hồ sơ y tế theo lịch hẹn
- Tạo hồ sơ y tế mới

## 🔧 Phát Triển

### Thêm Tính Năng Mới

1. **Thêm DTO mới**: Tạo class trong `com.clinic.ui.model` nếu cần model mới
2. **Thêm API method**: Thêm method trong `ApiService.java` để gọi API endpoint mới
3. **Tạo View**: Tạo file FXML mới trong `resources/views/`
4. **Tạo Controller**: Tạo controller mới trong `com.clinic.ui.controller` và liên kết với FXML

### Build JAR File

Để tạo file JAR có thể chạy độc lập:

```bash
mvn clean package
```

File JAR sẽ được tạo trong thư mục `target/`.

**Lưu ý**: Đây là ứng dụng client frontend, cần có backend API đang chạy để ứng dụng hoạt động đầy đủ.

