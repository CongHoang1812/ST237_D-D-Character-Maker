---
name: maker-reskin
description: Tạo app maker mới từ code base này và hoàn thiện nó (Android, Kotlin, XML, Hilt). Dùng skill này mỗi khi người dùng muốn clone, nhân bản, reskin, đổi tên, làm app maker mới hoặc làm app "giống app này"; khi cần áp thiết kế Figma mới vào các màn hình có sẵn, đổi màu / font / ảnh / nút theo chủ đề mới; khi cần đổi APP_KEY (đuôi API), máy chủ, nối API mới hoặc sửa DTO / mapper; khi thêm màn hình, thêm tính năng, thêm thẻ ở Home hoặc bỏ tính năng của app maker; khi kiểm tra app có còn sót tên, ảnh, chữ của app cũ không, kể cả khi người dùng không nhắc đến skill. Use for "reskin", "clone this maker app", "new maker app", "swap the UI and API", "add a screen to the maker app".
---

# Maker reskin

Mục tiêu là ra app mới nhanh mà **không đụng vào phần đã chạy đúng**. Mặc định tính năng, luồng màn hình và logic giữ nguyên; chỉ ba thứ thay đổi: định danh, nguồn dữ liệu, giao diện. Màn hình / tính năng mới là phần cộng thêm, làm sau cùng và theo quy trình riêng.

Lý do của ranh giới này: các màn hình chỉ đọc domain model và view id. Giữ nguyên hai thứ đó thì có thể thay toàn bộ API và giao diện mà không phải sửa ViewModel hay test logic. Mỗi lần vượt ranh giới là mất lợi thế này, nên hãy dừng lại và hỏi nếu một yêu cầu buộc phải làm vậy.

## Xác định đang ở giai đoạn nào

Đọc `docs/APP_BRIEF.md` rồi chọn việc tiếp theo:

| Tình trạng | Việc cần làm |
|---|---|
| Đang ở project mẫu, người dùng muốn app mới | Giai đoạn 1: tạo project |
| Có brief, chưa kiểm tra API | Giai đoạn 2: API |
| API xong, có Figma, `audit.py` báo còn nhiều ảnh / màu cũ | Giai đoạn 3: giao diện |
| Brief có mục "Tính năng / màn hình mới" chưa xong | Giai đoạn 4: tính năng mới |
| Mọi thứ trên đã xong | Giai đoạn 5: nội dung và nghiệm thu |

Lệnh trong skill viết dạng bash. Người dùng làm trên cả Windows và Linux, nên chọn theo máy đang chạy: Python là `python3` trên Linux / macOS, `python` trên Windows; Gradle là `sh gradlew` (Linux, macOS, Git Bash) hoặc `.\gradlew.bat` (PowerShell). Khi đưa lệnh cho người dùng tự chạy mà không rõ họ đang ở máy nào, ghi cả hai.

Người dùng yêu cầu rõ một giai đoạn thì làm đúng giai đoạn đó. Figma có thể đến muộn: chưa có link thì làm API và phần logic của tính năng mới trước, không đoán giao diện. Chỉ hỏi thông tin mà giai đoạn hiện tại thực sự cần. Ghi quyết định mới vào mục "Quyết định đã chốt" trong brief.

## Giai đoạn 1: Tạo project

Chạy `scripts/new_app.py` từ project nguồn. Script sao chép sang thư mục mới (bỏ qua `build/`, `.idea/`, APK release, `google-services.json`, tài liệu riêng của app nguồn), đổi package, tiền tố class và theme, tên database, tên app trong mọi ngôn ngữ, tên file APK, `APP_KEY`, máy chủ, rồi tạo brief. Project nguồn không bị sửa.

Cần: tên app, package, `APP_KEY`, mã dự án, thư mục đích. Máy chủ để trống thì giữ như app nguồn.

```bash
python3 .claude/skills/maker-reskin/scripts/new_app.py --dest ../ten-app --app-name "Ten App" \
  --app-id com.conght.tenapp --app-key STxxx_TenApp --project-code STxxx
```

Sau đó vào project mới, chạy `sh gradlew :app:assembleDebug :app:testDebugUnitTest` và báo kết quả. Khi người dùng `git init` project mới trên Windows, nhắc chạy `git update-index --chmod=+x gradlew` trước lần commit đầu để clone về Linux vẫn chạy được `./gradlew`. Phải có mốc xanh này trước khi đổi gì khác, để lỗi về sau chắc chắn do thay đổi mới gây ra. Đề xuất người dùng commit mốc này. Nếu người dùng đã đưa sẵn Figma, tài liệu API hay danh sách tính năng mới thì điền luôn vào brief.

## Giai đoạn 2: API

Chạy `scripts/probe_api.py`:

- **KHỚP**: chỉ cần đổi cấu hình (`APP_KEY` quyết định cả endpoint `api/app/<APP_KEY>` lẫn thư mục ảnh `public/app/<APP_KEY>/`). Không sửa DTO hay mapper, chuyển sang giai đoạn sau.
- **KHÔNG KHỚP** hoặc có tài liệu API khác: đọc `references/api-adaptation.md` rồi sửa theo đúng phạm vi trong đó.

Kiểm tra API thật bằng probe và unit test, không chỉ dựa vào tài liệu. App mẫu từng gặp tài liệu ghi ngược ý nghĩa của `parts` và server chặn ảnh dù JSON vẫn trả 200.

## Giai đoạn 3: Giao diện

Đọc `references/ui-reskin.md`. Làm theo thứ tự: token, component, ảnh dùng chung, rồi từng màn hình. Thứ tự này giúp phần lớn màn hình đã gần đúng trước khi phải chỉnh từng màn.

Với Figma: nạp skill `figma-design-to-code` trước khi gọi `get_design_context`, và coi code Figma sinh ra chỉ là tài liệu tham khảo.

Mỗi lần chỉ làm một màn. Cập nhật cột trạng thái trong brief sau mỗi màn để phiên sau biết đang dở ở đâu.

## Giai đoạn 4: Tính năng / màn hình mới

Đọc `references/new-feature.md`. Với mỗi mục trong "Tính năng / màn hình mới" của brief:

1. Lập kế hoạch: màn mẫu sẽ làm theo, file tạo / sửa, dữ liệu lấy từ đâu, chỗ chưa rõ. **Chờ người dùng đồng ý** rồi mới viết code.
2. Làm theo checklist (dữ liệu → ViewModel → layout → Fragment → navigation → chữ → test), build + test sau mỗi tính năng.
3. Cập nhật bảng màn hình và trạng thái trong brief.

Bỏ tính năng cũng đi theo giai đoạn này: lập kế hoạch trước, ẩn lối vào trước khi xoá code.

## Giai đoạn 5: Nội dung và nghiệm thu

1. Chạy `scripts/audit.py`, xử lý hết mục LỖI.
2. Sửa chữ theo chủ đề mới trong `values/strings.xml` và **mọi** `values-*/strings.xml`. Dịch tự nhiên theo từng ngôn ngữ, không thay từ máy móc, vì `D&D` trong bản dịch có thứ tự từ khác nhau.
3. Chạy `sh gradlew :app:assembleDebug :app:testDebugUnitTest`.
4. Nếu có thiết bị hoặc máy ảo, chạy app bằng skill `run` và đi hết luồng trong `references/ui-reskin.md` (cộng luồng của tính năng mới). Build xanh không chứng minh giao diện đúng. Không có thiết bị thì nói rõ là chưa kiểm tra trên máy.
5. Trước khi phát hành, chạy `scripts/audit.py --release`. Bản release bật R8, nên nhắc người dùng cài bản release đã ký lên máy thật và đi lại luồng (nhất là Download và Edit nhân vật đã lưu), giữ `app/build/outputs/mapping/release/mapping.txt` của bản đó để tải lên Play Console. Tính năng mới có lưu / đọc JSON bằng Gson thì kiểm tra class đó đã nằm trong `data/model/`, `data/remote/dto/` hoặc `app/src/main/keepRules/rules.keep`.

Khi báo cáo, nêu rõ: đã chạy lệnh nào, kết quả thật, phần nào chưa kiểm chứng.

## Ranh giới

Được đổi tự do:

- `res/` (giữ view id, xem `references/ui-reskin.md`)
- `core/widget/` phần vẽ, giữ nguyên API công khai
- `core/common/AppConfig.kt`, cấu hình trong `app/build.gradle.kts`
- `data/remote/` khi đổi API

Được làm khi đã ghi trong brief và người dùng đồng ý kế hoạch (giai đoạn 4):

- Thêm package `feature/<tên>/`, layout, destination và action trong `nav_main.xml`, chuỗi, ảnh, test
- Thêm model, repository, hàm data source, endpoint cho tính năng mới

Phải hỏi trước khi đổi:

- Hành vi của màn có sẵn: `data/model/` đang dùng, `MakerRemoteDataSource`, repository, ViewModel, luồng trong `nav_main.xml`
- Thêm thư viện, đổi cách lưu trữ, đổi schema Room
- Sửa test có sẵn để cho pass. Test đỏ thường là dấu hiệu đã vượt ranh giới; riêng test mapper thì được cập nhật khi API thực sự đổi định dạng.

## Giữ bộ công cụ đúng

Sửa lỗi chung (không gắn với chủ đề) ở một app thì đề xuất đưa về project mẫu. Gặp lỗi lặp lại thì ghi vào `references/` để app sau không lặp lại.
