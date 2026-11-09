# Maker app

App Android tạo nhân vật theo lớp ảnh: chọn bộ phận, trang trí, lưu, chia sẻ. Code base này được tái sử dụng cho nhiều app maker: mỗi app khác giao diện, `APP_KEY` (đuôi API) và có thể thêm / bớt vài màn hình. Thông tin riêng của app (định danh, Figma, API, tính năng mới, quyết định đã chốt) nằm ở `docs/APP_BRIEF.md`, đọc file này trước khi làm.

## Việc gì đọc gì

| Việc | Đọc |
|---|---|
| Tạo app mới, đổi giao diện, đổi API, thêm / bỏ màn hình | Skill `maker-reskin` (hướng dẫn cho người: `docs/NEW_APP_PLAYBOOK.md`) |
| Thêm màn / tính năng | `.claude/skills/maker-reskin/references/new-feature.md` |
| Áp Figma | `.claude/skills/maker-reskin/references/ui-reskin.md`, ảnh ↔ node ở `docs/assets.md` |
| Đổi API, DTO, mapper | `.claude/skills/maker-reskin/references/api-adaptation.md` |

`docs/prompts/` là lịch sử dựng app mẫu lần đầu, có chỗ đã cũ. Không dùng làm quy ước.

## Lệnh

```bash
sh gradlew :app:assembleDebug
sh gradlew :app:testDebugUnitTest
```

Người dùng làm trên cả Windows và Linux. Chọn lệnh theo máy đang chạy:

| | Linux / macOS | Windows (Git Bash) | Windows (PowerShell) |
|---|---|---|---|
| Gradle | `./gradlew` hoặc `sh gradlew` | `sh gradlew` | `.\gradlew.bat` |
| Python | `python3` | `python` | `python` |

Không xoá `.gitattributes` (giữ `gradlew` là LF) và không làm mất quyền chạy của `gradlew` trong git (`100755`).

## Kiến trúc

Kotlin, XML + ViewBinding (không dùng Compose), MVVM, single-activity + Navigation, Hilt, Room, DataStore, Retrofit, Glide.

```
core/      cấu hình, widget, tiện ích UI
data/      model, remote (API), local (Room, file), repository
di/        Hilt module
feature/   mỗi màn một package: Fragment + ViewModel
```

Luồng dữ liệu: `Fragment → ViewModel (StateFlow) → Repository → MakerRemoteDataSource`. Domain model trong `data/model/` là ranh giới: API đổi thì sửa DTO và mapper, không sửa màn hình.

Package đọc từ `namespace` trong `app/build.gradle.kts`; test ở `app/src/test/java/` cùng package.

## Dùng lại, đừng viết mới

- Màn: `BaseFragment<VB>`, `collectWhenStarted`, `navigateSafe`, `setOnSingleClickListener`, `applyStatusBarPadding`, `setLightStatusBar`, `showAppToast`.
- Dữ liệu mạng: `Resource` + `resourceFlow { }`, Retry bằng `reloadKey` + `flatMapLatest`, `retryWhenBackOnline`.
- Trạng thái: `ShimmerFrameLayout` / `SkeletonAdapter` khi tải, `ErrorStateView` khi lỗi, `NoInternetDialogFragment`.
- Danh sách phụ kiện: `noneFirst()`.
- Component: `WoodButton`, `RoundIconButton`, `PaperDialogContainer`, `ConfirmDialogs`, `LoadingDialogFragment`.
- Lưu / tải ảnh: `CreationRepository`, `registerGalleryDownload`, `shareImages`.

## Quy ước

- Không hardcode màu, kích thước, chữ trong layout. Dùng `@color`, `@dimen`, `@string`.
- Thêm chuỗi thì thêm vào **mọi** `values-*/strings.xml`.
- Thêm dimen thì xem `values-sw600dp/`, `values-sw800dp/`.
- Danh sách phụ kiện luôn có ô None ở đầu, do UI chèn qua `noneFirst()`, không lấy từ API.
- Data source ném exception khi lỗi, `resourceFlow` đổi thành `Resource.Error` để UI hiện Retry.
- Thêm hàm vào `MakerRemoteDataSource` thì thêm cả vào `MockDataSource`. Thêm hàm vào interface repository thì sửa các fake trong test (hoặc cho hàm implementation mặc định).
- Tham số điều hướng: hằng `ARG_*` trong companion của Fragment, khai báo `<argument>` trong `nav_main.xml`, đọc ở ViewModel qua `SavedStateHandle`.
- Đổi schema Room: tăng `version`, viết `Migration`. Không xoá dữ liệu người dùng.
- Bản release bật R8. Class đi qua Gson (JSON server, JSON lưu vào Room / `SavedStateHandle`) đặt trong `data/model/` hoặc `data/remote/dto/`; để ở chỗ khác thì thêm vào `app/src/main/keepRules/rules.keep`. Không đổi tên field của các class này: JSON đã lưu trên máy người dùng sẽ không đọc lại được.
- Thiết kế 360 × 800, 1px Figma = 1dp.
- Ảnh của app lưu ở internal storage. Chỉ nút Download mới ghi ra Gallery (`Pictures/<tên app>`).

Cấu hình app (`APP_KEY`, máy chủ, cờ mock, URL chính sách, cờ tính năng) nằm ở `core/common/AppConfig.kt` và `app/build.gradle.kts`.
