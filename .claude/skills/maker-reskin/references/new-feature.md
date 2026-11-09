# Thêm màn hình / tính năng mới

Đọc file này khi app mới cần thứ mà app mẫu chưa có: màn mới, thẻ mới ở Home, endpoint mới, đổi luồng, hoặc bỏ một tính năng.

Đây là việc **vượt ranh giới reskin**, nên chỉ làm khi người dùng đã yêu cầu và đã ghi vào mục "Tính năng / màn hình mới" trong `docs/APP_BRIEF.md`. Luôn **lập kế hoạch trước** (danh sách file sẽ tạo / sửa, chỗ nào chưa rõ), chờ người dùng đồng ý rồi mới viết code.

## Trước khi viết code

1. Đọc mô tả trong brief và Figma node của màn (nếu đã có). Thiếu Figma thì dựng bố cục tạm từ token và component sẵn có, ghi "chờ Figma" vào cột trạng thái. Không bịa giao diện chi tiết.
2. Tìm màn có sẵn giống nhất để làm theo, đừng tự nghĩ cấu trúc mới:

| Cần làm | Làm theo |
|---|---|
| Màn danh sách tải từ API, có Retry | `feature/maker/template/` (`TemplateSelectViewModel`) |
| Màn chỉnh nhân vật / dùng lại Customize với chế độ khác | `CustomizeMode` trong `feature/maker/customize/` (Cosplay là ví dụ) |
| Màn xem ảnh + Share / Download / Delete | `feature/creation/CreationDetailFragment` |
| Màn kết quả sau khi lưu | `feature/success/` |
| Dialog xác nhận | `ConfirmDialogs` + `ConfirmDialogFragment` (kết quả qua `setFragmentResultListener`) |
| Dialog tuỳ biến | `feature/dialog/PackNameDialogFragment` + `BaseDialogFragment` |
| Game có đồng hồ / chấm điểm | Cosplay: `CustomizeViewModel` (mode COSPLAY), `CosplayScorer` |
| Random kết quả rồi Edit | `feature/luckyroll/` |
| Lưu cài đặt | `SettingsRepository` + `SettingsDataStore` |

3. Trả lời: dữ liệu lấy từ đâu (API có sẵn, endpoint mới, Room, DataStore, tự sinh)? Màn mở từ đâu, đóng thì về đâu, truyền tham số gì?

## Checklist: thêm một màn

Thứ tự này giữ cho build luôn xanh sau từng bước.

**1. Dữ liệu** (bỏ qua nếu dùng repository sẵn có)

- Model mới đặt trong `data/model/` (domain model, không chứa annotation Gson).
- Bản release bật R8: class nào đi qua Gson (lưu JSON vào Room / `SavedStateHandle`, truyền qua navigation) mà không nằm trong `data/model/` hoặc `data/remote/dto/` thì thêm vào `app/src/main/keepRules/rules.keep`, nếu không bản release sẽ đọc / ghi JSON sai dù bản debug chạy đúng.
- Hàm mới của repository trả `Flow<Resource<T>>` bằng `resourceFlow { ... }` nếu tải từ mạng.
- Interface mới trong `data/repository/`, bind ở `di/RepositoryModule.kt` (`@Binds @Singleton`).
- Thêm hàm vào interface sẵn có (`CreationRepository`, `MakerRemoteDataSource`...) sẽ làm hỏng các fake trong `app/src/test`. Hoặc sửa hết các fake, hoặc cho hàm mới một implementation mặc định trong interface (xem `CreationRepository.update`).

**2. API mới** (nếu cần endpoint)

```
ApiService (endpoint) → dto/ApiDtos.kt (field nullable) → ApiMapper (DTO → model)
→ MakerRemoteDataSource (hàm mới) → ApiRemoteDataSource + MockDataSource → Repository
```

- Đường dẫn ghép từ `AppConfig.APP_KEY` / `AppConfig.ASSET_PATH` như các endpoint cũ, không viết cứng mã app.
- Thêm hàm vào `MakerRemoteDataSource` thì **phải** thêm vào cả `MockDataSource` (để `USE_MOCK_DATA = true` vẫn chạy).
- Lỗi thì ném exception, không trả rỗng. Viết test mapper bằng JSON thật trước (xem `references/api-adaptation.md`).

**3. ViewModel** — `feature/<tên>/<Tên>ViewModel.kt`

- `@HiltViewModel` + `@Inject constructor`, nhận repository chứ không tự tạo.
- Expose `StateFlow` (`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`); sự kiện một lần (điều hướng, toast) qua `Channel` + `receiveAsFlow()`.
- Tham số điều hướng đọc từ `SavedStateHandle[ARG_...]`; trạng thái cần sống qua xoay màn / process death cũng để trong `SavedStateHandle`.
- Màn tải mạng: `reloadKey` + `flatMapLatest` để Retry, và `retryWhenBackOnline(networkMonitor, ...)` để tự tải lại khi có mạng (xem `TemplateSelectViewModel`).

**4. Layout** — `res/layout/fragment_<tên>.xml`

- Chỉ dùng `@color`, `@dimen`, `@string`, style `Text.*`. Kích thước theo Figma 1px = 1dp, thêm vào `values/dimens.xml` và kiểm tra `values-sw600dp/`, `values-sw800dp/` (chạy `scripts/gen_tablet_dimens.py` nếu cần).
- Dùng component chung: `WoodButton`, `RoundIconButton`, `ImageButton` + `ImageIconButton`, `PaperDialogContainer`, `ShimmerFrameLayout` / `SkeletonView`, `ErrorStateView`.
- Có đủ trạng thái: đang tải (skeleton), lỗi (`ErrorStateView` + Retry), rỗng (nếu là danh sách).
- Danh sách phụ kiện: ô None đứng đầu qua `noneFirst()`.

**5. Fragment** — `feature/<tên>/<Tên>Fragment.kt`

```kotlin
@AndroidEntryPoint
class FooFragment : BaseFragment<FragmentFooBinding>(FragmentFooBinding::inflate) {
    private val viewModel: FooViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.content.applyStatusBarPadding()
        setLightStatusBar(false)                       // true nếu nền sáng
        binding.btnBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.errorState.setOnRetryClickListener { viewModel.retry() }
        collectWhenStarted(viewModel.state, ::render)
    }

    companion object {
        const val ARG_FOO_ID = "fooId"                 // trùng tên <argument> trong nav_main.xml
    }
}
```

- Bấm nút: `setOnSingleClickListener` (chống bấm đúp). Điều hướng: `navigateSafe(R.id.action_..., bundle)`.
- Toast: `showAppToast(R.string...)`. Lưu ảnh ra máy: `registerGalleryDownload` + `showDownloadSuccessToast`.
- Fragment chỉ hiển thị và chuyển sự kiện; logic để trong ViewModel.

**6. Điều hướng** — `res/navigation/nav_main.xml`

- Thêm `<fragment>` với `<argument>` (có `defaultValue`), và `<action>` từ màn gọi tới, dùng cùng bộ anim `slide_in_right` / `slide_out_left` / `slide_in_left` / `slide_out_right` như các action khác.
- Màn cần mạng mà mở từ Home thì đi qua `navigateOnline` trong `HomeFragment` như các thẻ khác.

**7. Chữ** — thêm chuỗi vào `values/strings.xml` **và mọi** `values-*/strings.xml` (hi, es, fr, pt, in), dịch tự nhiên. `audit.py` báo lỗi nếu thiếu.

**8. Ảnh** — xuất từ Figma theo quy tắc trong `references/ui-reskin.md`, không dùng URL Figma trong code.

**9. Test** — `app/src/test/java/<package>/feature/<tên>/<Tên>ViewModelTest.kt`: dùng fake repository (xem `SuccessViewModelTest`, `TemplateSelectViewModelTest`), kiểm tra Loading → Success, Error → Retry, và sự kiện điều hướng.

**10. Cập nhật tài liệu**

- Thêm dòng vào bảng màn hình trong `docs/APP_BRIEF.md` (layout, Figma node, trạng thái).
- Thêm bước vào "Đi hết luồng khi nghiệm thu" trong `references/ui-reskin.md` nếu tính năng nên có ở các app sau.

## Các trường hợp khác

**Thêm thẻ ở Home**: view mới trong `fragment_home.xml` (ảnh `img_home_*`), `setOnSingleClickListener` + action trong `HomeFragment`, action trong `<fragment homeFragment>` của `nav_main.xml`.

**Bỏ một tính năng** (vd. app không có Cosplay): ẩn hoặc xoá thẻ ở Home và action vào màn đó trước; chỉ xoá Fragment / ViewModel / layout / chuỗi / test khi người dùng muốn gọn code. Build + test sau khi xoá. Ghi vào "Quyết định đã chốt".

**Thêm cột / bảng Room**: tăng `version` trong `AppDatabase`, viết `Migration` và thêm vào `DatabaseModule` (`.addMigrations(...)`). Không dùng `fallbackToDestructiveMigration`: người dùng sẽ mất ảnh đã lưu.

**Thêm thư viện**: thêm vào `gradle/libs.versions.toml` rồi `app/build.gradle.kts`. Hỏi người dùng trước (quảng cáo, Firebase, billing thường có yêu cầu riêng của từng app).

**Thêm tham số cấu hình riêng từng app** (bật / tắt tính năng, mã quảng cáo): để trong `AppConfig.kt` như `LUCKY_ROLL_REQUIRES_AD`, ghi vào brief.

## Đưa ngược về app mẫu

Tính năng mới có ích cho các app sau (không gắn với chủ đề riêng) thì đề xuất người dùng đưa về project mẫu, để lần sau chỉ cần bật thay vì làm lại.
