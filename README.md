# Character Maker (face-maker)

App Android tạo nhân vật theo lớp ảnh: chọn bộ phận và màu, trang trí (nền, sticker, bong bóng thoại, chữ), lưu, chia sẻ. Code base này được dùng làm **template** cho nhiều app maker: mỗi app khác giao diện, `APP_KEY` (đuôi API) và có thể thêm / bớt vài màn hình. Thông tin riêng của app này nằm ở [`docs/APP_BRIEF.md`](docs/APP_BRIEF.md).

## Tính năng

| Tính năng | Mô tả |
|---|---|
| Maker | Chọn nhân vật gốc, đổi bộ phận và màu, random, di chuyển / zoom / xoay / lật |
| Decor editor | Nền (màu, ảnh, pack), sticker, speech bubble, chữ với nhiều font |
| Cosplay | Dựng lại nhân vật mẫu trong 10 phút, chấm điểm theo % khớp |
| Lucky roll | Random một nhân vật hoàn chỉnh rồi chỉnh sửa tiếp |
| My creation | Hai tab Character / Design, chọn nhiều, xoá, tải về, chia sẻ, Add to WhatsApp / Telegram, Edit nhân vật đã lưu |
| Khác | Đa ngôn ngữ (en, hi, es, fr, pt, in), onboarding, rate us, màn hình lỗi và Retry khi mất mạng |

## Công nghệ

- Kotlin, XML + ViewBinding (không dùng Compose)
- MVVM, single-activity + Navigation
- Hilt, Room, DataStore, Retrofit + OkHttp, Glide, Coroutines / Flow
- `minSdk` 24, `compileSdk` / `targetSdk` 37

## Cấu trúc

```
app/src/main/java/<package>/   (package = namespace trong app/build.gradle.kts)
├── core/      cấu hình, widget tự vẽ, tiện ích UI
├── data/      model, remote (API), local (Room, file), repository
├── di/        Hilt module
└── feature/   mỗi màn một package: Fragment + ViewModel
```

Luồng dữ liệu:

```
Fragment → ViewModel (StateFlow) → Repository → MakerRemoteDataSource → ApiService + ApiMapper
```

Domain model trong `data/model/` là ranh giới: API đổi thì chỉ sửa DTO và mapper, không sửa màn hình.

## Chạy và build

Chạy được trên Windows, Linux và macOS. Yêu cầu: Android Studio bản mới, JDK 17 trở lên (dùng JDK đi kèm Android Studio là được), Python 3.9+ nếu dùng các script trong `.claude/skills/maker-reskin/scripts/`.

**Android Studio:** mở thư mục project, chờ Gradle Sync, chọn cấu hình **app** và một thiết bị, bấm Run.

**Terminal** (cần `JAVA_HOME`, xem cách đặt cho từng hệ điều hành ở [`docs/NEW_APP_PLAYBOOK.md`](docs/NEW_APP_PLAYBOOK.md#windows-và-linux)):

```bash
# Linux / macOS
./gradlew :app:assembleDebug        # build APK debug
./gradlew :app:testDebugUnitTest    # chạy unit test
```

```powershell
# Windows (PowerShell)
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Git Bash trên Windows dùng `sh gradlew ...`. Chuyển code giữa hai máy bằng git, không chép cả thư mục; `.gitattributes` giữ `gradlew` ở dạng LF để Linux chạy được.

## Cấu hình

| Mục | Nơi đặt |
|---|---|
| Máy chủ chính, máy chủ dự phòng, cờ `USE_MOCK_DATA` | `app/build.gradle.kts` (`buildConfigField`) |
| `APP_KEY` (đuôi API), đường dẫn ảnh, URL chính sách, email góp ý, cờ tính năng | `app/src/main/java/<package>/core/common/AppConfig.kt` |

- API chỉ dùng GET, không cần token. Endpoint `api/app/<APP_KEY>`, ảnh ở `public/app/<APP_KEY>/`. Máy chủ chính lỗi hoặc quá 6 giây thì tự chuyển sang máy chủ dự phòng.
- Đặt `USE_MOCK_DATA` thành `"true"` để làm giao diện bằng dữ liệu giả khi chưa có API. Nhớ tắt lại trước khi phát hành.
- Ảnh của app lưu trong internal storage. Chỉ nút Download mới ghi ra Gallery (`Pictures/<tên app>`).

## Quy ước

- Không hardcode màu, kích thước, chữ trong layout: dùng `@color`, `@dimen`, `@string`.
- Thêm chuỗi thì thêm vào **mọi** `values-*/strings.xml`.
- Danh sách phụ kiện luôn có ô None ở đầu, do UI chèn bằng `noneFirst()`.
- Thiết kế 360 × 800, 1px Figma = 1dp.

Chi tiết hơn nằm ở [`CLAUDE.md`](CLAUDE.md).

## Tạo app mới từ template

Dùng khi làm app maker mới: đổi giao diện, đuôi API, thêm / bớt màn hình. Bộ công cụ nằm ở `.claude/skills/maker-reskin/`.

```bash
# Linux / macOS
python3 .claude/skills/maker-reskin/scripts/new_app.py --dest ../ten-app --app-name "Ten App" \
  --app-id com.conght.tenapp --app-key STxxx_TenApp --project-code STxxx
```

```powershell
# Windows (PowerShell)
python .claude\skills\maker-reskin\scripts\new_app.py --dest ..\ten-app --app-name "Ten App" --app-id com.conght.tenapp --app-key STxxx_TenApp --project-code STxxx
```

Xem hướng dẫn từng bước, prompt mẫu và cách thêm tính năng trong [`docs/NEW_APP_PLAYBOOK.md`](docs/NEW_APP_PLAYBOOK.md).

## Tài liệu

| File | Nội dung |
|---|---|
| [`docs/APP_BRIEF.md`](docs/APP_BRIEF.md) | Định danh, Figma, API, bảng màn hình, quyết định đã chốt của app này |
| [`docs/NEW_APP_PLAYBOOK.md`](docs/NEW_APP_PLAYBOOK.md) | Quy trình làm app maker mới, thêm tính năng, prompt mẫu |
| [`.claude/skills/maker-reskin/references/`](.claude/skills/maker-reskin/references) | Chi tiết đổi API, đổi giao diện, thêm màn hình |
| [`docs/assets.md`](docs/assets.md) | Danh sách ảnh ↔ node Figma |
| [`docs/prompts/`](docs/prompts) | Lịch sử các bước dựng app lần đầu (có chỗ đã cũ) |
| [`docs/licenses/`](docs/licenses) | Giấy phép font (SIL OFL) |

## Trước khi phát hành

- Chạy `audit.py --release` (`python3` trên Linux, `python` trên Windows) (cần `origin.json`, tức chỉ chạy trong app do `new_app.py` tạo).
- Thay `FEEDBACK_EMAIL` còn là `example.com` trong `AppConfig.kt`.
- `APP_KEY` đang mượn `ST247_ChibiDollDressUp` để test (`TODO(RELEASE)`): đổi sang mã thật, kiểm tra máy chủ.
- Kiểm tra `archivesName` (tên file APK) trong `app/build.gradle.kts` đúng tên app.
- Bản release **bật R8** (bỏ code thừa, đổi tên class): file nhỏ hơn khoảng một nửa. Lỗi do R8 chỉ xuất hiện ở bản release, nên luôn cài bản release đã ký lên máy thật và đi hết luồng trước khi phát hành. Lưu `app/build/outputs/mapping/release/mapping.txt` của mỗi bản và tải lên Play Console để đọc được log crash.
- Đặt `USE_MOCK_DATA` về `false`, kiểm tra `versionCode`, icon launcher và keystore ký app.
