# Playbook: làm app maker mới

App nào cũng có phần maker giống nhau. Mỗi app mới chỉ khác:

1. **Định danh**: tên app, package, mã dự án.
2. **Đuôi API**: `APP_KEY` trên server (và máy chủ nếu khác).
3. **Giao diện**: theo Figma mới.
4. **Tính năng / màn hình thêm hoặc bỏ** (không bắt buộc).

Phần còn lại (luồng màn hình, logic maker, lưu trữ, xử lý lỗi, đa ngôn ngữ) có sẵn từ project mẫu và đã có test.

```
0. Chuẩn bị → 1. Tạo project → 2. API → 3. Giao diện → 4. Tính năng mới → 5. Nghiệm thu
              (script)        (probe)   (khi có Figma)  (nếu có)         (audit)
```

Figma hoặc danh sách tính năng có thể đến sau: làm bước 1, 2 trước, bước 3, 4 làm khi có. Tiến độ được ghi trong `docs/APP_BRIEF.md` của app mới, nên phiên làm việc sau biết đang dở ở đâu.

## Bộ công cụ

| File | Dùng để |
|---|---|
| `.claude/skills/maker-reskin/` | Skill hướng dẫn Claude làm đúng quy trình và đúng phạm vi |
| `scripts/new_app.py` | Sao chép project và đổi package, tên, theme, database, tên APK, `APP_KEY`, máy chủ |
| `scripts/probe_api.py` | Kiểm tra server thật có khớp định dạng app đang đọc không |
| `scripts/audit.py` | Tìm phần sót của app cũ, cho biết đã thay bao nhiêu ảnh / màu, kiểm tra trước phát hành |
| `references/api-adaptation.md` | Đổi API, sửa DTO / mapper |
| `references/ui-reskin.md` | Đổi giao diện theo Figma, luồng nghiệm thu |
| `references/new-feature.md` | Thêm màn hình / tính năng / thẻ Home, bỏ tính năng |
| `CLAUDE.md` | Quy ước dự án, Claude đọc tự động |
| `docs/APP_BRIEF.md` | Thông tin riêng của từng app; Claude đọc trước khi làm |

Bộ công cụ được sao chép sang project mới, nên app nào cũng có thể làm mẫu cho app sau.

## Bước 0: Chuẩn bị

| Thông tin | Ví dụ | Cần ở bước |
|---|---|---|
| Tên app | Anime OC Maker | 1 |
| Package | `com.conght.animeocmaker` | 1 |
| Mã dự án | ST279 | 1 |
| `APP_KEY` (đuôi API) | `ST279_AnimeOCMaker` | 1 |
| Máy chủ (nếu khác app mẫu) | `https://lvtglobal.tech/` | 1 |
| Tài liệu API / JSON mẫu (nếu khác định dạng) | file `.json`, link Postman | 2 |
| Link Figma (có `node-id` càng tốt) | `https://www.figma.com/design/...` | 3 |
| Danh sách tính năng / màn mới, tính năng bỏ | "Thêm màn Daily reward mở từ Home" | 4 |
| URL chính sách, email góp ý | | 5 |

`APP_KEY` quyết định cả endpoint `api/app/<APP_KEY>` lẫn thư mục ảnh `public/app/<APP_KEY>/`. Nếu server giống app mẫu thì đổi `APP_KEY` là đủ, không phải sửa code API.

## Bước 1: Tạo project

Chạy tại project mẫu. Lần đầu dùng máy nào thì xem mục [Windows và Linux](#windows-và-linux) để cài Python, JDK, Android SDK.

**Windows (PowerShell)**, lệnh dài gõ liền một dòng:

```powershell
python .claude\skills\maker-reskin\scripts\new_app.py --dest ..\anime-oc-maker --app-name "Anime OC Maker" --app-id com.conght.animeocmaker --app-key ST279_AnimeOCMaker --project-code ST279

cd ..\anime-oc-maker
git init
git add .
git update-index --chmod=+x gradlew
git commit -m "Tạo project từ template"
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

`git update-index --chmod=+x gradlew` ghi quyền chạy vào git, để sau này clone về Linux gõ được `./gradlew`. Windows không tự ghi quyền này.

**Linux / macOS** (Git Bash trên Windows cũng dùng được, đổi `python3` thành `python`):

```bash
python3 .claude/skills/maker-reskin/scripts/new_app.py --dest ../anime-oc-maker \
  --app-name "Anime OC Maker" --app-id com.conght.animeocmaker \
  --app-key ST279_AnimeOCMaker --project-code ST279

cd ../anime-oc-maker
git init && git add . && git commit -m "Tạo project từ template"
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Thêm `--base-url` / `--backup-url` nếu máy chủ khác, `--app-title "Anime OC\nMaker"` nếu logo xuống dòng khác tên app.

Hoặc để Claude chạy hộ (mở `claude` tại project mẫu):

```text
/maker-reskin Tạo app mới: tên "Anime OC Maker", package com.conght.animeocmaker,
APP_KEY ST279_AnimeOCMaker, mã ST279, thư mục ../anime-oc-maker. Build và test xong thì báo.
```

Sau đó mở `docs/APP_BRIEF.md` của project mới và điền những gì đã có. **Brief càng rõ, prompt càng ngắn.**

## Bước 2: API

```powershell
python .claude\skills\maker-reskin\scripts\probe_api.py      # Windows
```

```bash
python3 .claude/skills/maker-reskin/scripts/probe_api.py     # Linux / macOS
```

Nếu báo **KHỚP** thì xong bước này. Nếu **KHÔNG KHỚP**, mở `claude` trong project mới:

```text
/maker-reskin Làm giai đoạn API. Tài liệu API: <đường dẫn hoặc link>.
JSON mẫu thật: <đường dẫn file>. Viết test mapper theo JSON này trước.
```

Chưa có API thì đặt `USE_MOCK_DATA` thành `"true"` trong `app/build.gradle.kts` để làm giao diện bằng dữ liệu giả, nhớ tắt lại.

## Bước 3: Giao diện (khi có Figma)

Làm lần lượt từng prompt, mỗi lần chạy xong thì xem kết quả và commit.

**Ghép Figma với màn hình** (làm một lần, hoặc khi Figma cập nhật):

```text
/maker-reskin Figma của app: <link>. Ghi vào brief, rồi điền cột Figma node trong bảng màn hình.
Frame nào không ghép được với màn có sẵn thì liệt kê là màn mới và hỏi mình, đừng đoán.
```

**Token và component chung:**

```text
/maker-reskin Làm bước token và component chung theo Figma.
Lấy màu, font, kiểu nút, kiểu dialog. Chưa sửa từng màn hình.
Xong thì liệt kê những gì Figma không rõ để mình quyết định.
```

**Ảnh dùng chung:**

```text
/maker-reskin Xuất ảnh dùng chung từ Figma, ghi đè đúng tên file hiện có, ghi vào docs/assets.md.
Xong chạy audit.py và cho mình biết còn ảnh nào của app cũ, ảnh nào Figma không có.
```

**Từng màn hình** (lặp lại cho mỗi màn):

```text
/maker-reskin Làm màn Home theo node trong brief.
Giữ view id, không sửa ViewModel. Chạy app và so với ảnh Figma.
```

Thứ tự gợi ý: Home → Customize → Decor editor → Dialog → các màn còn lại.

## Bước 4: Tính năng / màn hình mới (nếu có)

Ghi mỗi tính năng một dòng vào bảng "Tính năng / màn hình mới" trong `docs/APP_BRIEF.md`: mở từ đâu, làm gì, lưu gì, Figma node (nếu có), dữ liệu từ API hay local. Sau đó làm **từng tính năng một**, luôn yêu cầu kế hoạch trước:

```text
/maker-reskin Làm tính năng "<tên>" trong brief. Lập kế hoạch trước (màn mẫu làm theo,
file tạo / sửa, dữ liệu từ đâu, chỗ chưa rõ), chưa sửa code.
```

Đọc kế hoạch, sửa nếu cần, rồi:

```text
Đồng ý kế hoạch. Làm đi, build + test sau khi xong, cập nhật brief.
```

Các yêu cầu hay gặp:

```text
/maker-reskin Thêm thẻ "<tên>" ở Home mở màn mới <mô tả>. Figma node <id>. Lập kế hoạch trước.
/maker-reskin App này bỏ tính năng Cosplay. Lập kế hoạch các file cần đổi trước, chưa sửa code.
/maker-reskin Thêm endpoint <url> cho màn <tên>. JSON mẫu: <file>. Viết test mapper trước.
```

Chưa có Figma cho màn mới thì Claude làm phần dữ liệu và logic trước, dựng bố cục tạm từ component sẵn có và ghi "chờ Figma". Khi có link thì làm lại giao diện của màn đó như bước 3.

## Bước 5: Nội dung và nghiệm thu

```text
/maker-reskin Làm giai đoạn nghiệm thu: sửa chữ theo chủ đề trong mọi ngôn ngữ,
chạy audit.py, build, test, rồi đi hết luồng trên máy ảo. Báo cáo những gì đã kiểm tra thật.
```

Trước khi phát hành:

```powershell
python .claude\skills\maker-reskin\scripts\audit.py --release      # Windows
.\gradlew.bat :app:assembleRelease
```

```bash
python3 .claude/skills/maker-reskin/scripts/audit.py --release     # Linux / macOS
./gradlew :app:assembleRelease
```

Lệnh này báo các mục cần xử lý: email / URL còn là `example.com`, `TODO(RELEASE)` (vd. `APP_KEY` mượn tạm), quảng cáo còn là stub, `USE_MOCK_DATA` đang bật. Kiểm tra thêm bằng tay: `versionCode`, icon launcher, keystore ký app, `google-services.json` đúng package (nếu dùng Firebase).

Bản release bật R8 (bỏ code thừa, đổi tên class), nên có lỗi chỉ xuất hiện ở bản release mà bản debug không có. Trước khi phát hành, cài **bản release đã ký** lên máy thật và đi hết luồng, nhất là lưu ảnh và Edit nhân vật đã lưu. Lưu `app/build/outputs/mapping/release/mapping.txt` của bản đó và tải lên Play Console (App bundle explorer → Downloads → ReTrace mapping file) để log crash đọc được.

## Cách viết prompt hiệu quả

- **Mỗi prompt một việc có điểm kết thúc rõ.** "Làm màn Home" tốt hơn "đổi hết giao diện", vì dễ kiểm tra và dễ sửa.
- **Đưa dữ liệu thật:** link Figma có `node-id`, JSON thật, ảnh chụp lỗi. Dữ liệu tốt hơn mô tả bằng lời.
- **Tính năng mới: kế hoạch trước, code sau.** Sửa kế hoạch rẻ hơn sửa code.
- **Nói điều không được làm:** "giữ view id", "không sửa ViewModel". Skill đã có sẵn, nhưng nhắc lại khi việc dễ vượt phạm vi.
- **Yêu cầu bằng chứng:** "chạy test và cho xem kết quả", "so với ảnh Figma".
- **Sửa theo triệu chứng cụ thể:** "nút Next cách mép phải 16dp, đang là 8dp" tốt hơn "chưa giống Figma".
- **Gặp lỗi lặp lại thì đưa vào kit:** thêm vào `references/` trong skill, các app sau sẽ không bị lại.

Tiết kiệm lượt dùng:

- `/clear` giữa các màn hình hoặc tính năng. Tiến độ đã nằm trong brief và git.
- `/effort medium` cho việc thông thường, tăng lên `high` với Customize, Decor editor, đổi API hoặc tính năng mới.

## Windows và Linux

Project và bộ công cụ chạy được trên cả hai. Các script Python chỉ dùng thư viện chuẩn, đường dẫn tự xử lý theo hệ điều hành.

### Khác nhau khi gõ lệnh

| Việc | Windows (PowerShell) | Linux / macOS | Git Bash trên Windows |
|---|---|---|---|
| Chạy Python | `python` (hoặc `py`) | `python3` | `python` |
| Build | `.\gradlew.bat ...` | `./gradlew ...` (hoặc `sh gradlew ...`) | `sh gradlew ...` |
| Đường dẫn | `..\ten-app` (`/` cũng được) | `../ten-app` | `../ten-app` |
| Lệnh dài | gõ liền một dòng, hoặc xuống dòng bằng dấu huyền `` ` `` | xuống dòng bằng `\` | xuống dòng bằng `\` |
| Nối lệnh | gõ từng dòng (PowerShell 5 không có `&&`) | `a && b` | `a && b` |
| Đặt biến môi trường | `$env:JAVA_HOME = "..."` | `export JAVA_HOME=...` | `export JAVA_HOME=...` |

Claude Code tự chọn lệnh theo máy đang chạy. Khi copy prompt mẫu thì không phải sửa gì.

### Cài đặt lần đầu

| Thành phần | Windows | Linux (Ubuntu / Debian) |
|---|---|---|
| Python 3.9+ | Cài từ python.org, đánh dấu "Add Python to PATH" | Thường có sẵn `python3`; thiếu thì `sudo apt install python3` |
| Git | Git for Windows (kèm Git Bash) | `sudo apt install git` |
| Android Studio + SDK | Bộ cài `.exe` | Giải nén bản `.tar.gz` (vd. `~/android-studio`) hoặc `sudo snap install android-studio --classic` |
| JDK 17+ | Dùng JDK đi kèm Android Studio | Dùng JDK đi kèm Android Studio, hoặc `sudo apt install openjdk-17-jdk` |

Build trong Android Studio (nút Run) thì không cần cấu hình gì thêm. Build bằng terminal thì cần `JAVA_HOME` và đường dẫn SDK:

```powershell
# Windows: đặt một lần trong System Properties → Environment Variables, hoặc tạm thời:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

```bash
# Linux: thêm vào ~/.bashrc rồi mở terminal mới (sửa đường dẫn theo nơi cài)
export JAVA_HOME="$HOME/android-studio/jbr"          # bản snap: /snap/android-studio/current/jbr
export ANDROID_HOME="$HOME/Android/Sdk"
```

Đường dẫn SDK nằm trong `local.properties` (`sdk.dir=...`), Android Studio tự tạo khi mở project. File này riêng từng máy và không đưa lên git.

### Làm qua lại giữa hai máy

- **Chuyển code bằng git** (push ở máy này, pull ở máy kia), đừng chép cả thư mục. Thư mục `build/`, `.gradle/`, `local.properties` là riêng từng máy.
- **Kiểu xuống dòng** do `.gitattributes` cố định: `gradlew` và file `.py` luôn là LF, `gradlew.bat` luôn là CRLF. Đừng xoá file này.
- **Quyền chạy `gradlew`** đã ghi trong git (`100755`). Project mới tạo trên Windows thì nhớ chạy `git update-index --chmod=+x gradlew` trước lần commit đầu (xem bước 1).
- **Thư mục OneDrive**: OneDrive không có bản cho Linux, và đồng bộ thư mục project giữa hai máy dễ làm hỏng `build/` hay `.git/`. Dùng git để chuyển code.
- Lỗi chỉ xuất hiện ở một máy thì xoá `build/`, `app/build/`, `.gradle/` ở máy đó rồi build lại.

## Xử lý sự cố

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| Ảnh trống nhưng JSON vẫn có | Server chặn ảnh hoặc sai `APP_KEY`, chạy `probe_api.py` |
| Bộ phận đè sai thứ tự | Sai `zIndex` hoặc đảo X-Y của `parts` trong mapper |
| Sticker / speech trống, nền vẫn có | Tên nhóm decor khác app mẫu (`stickers` / `sticker`), xem `references/api-adaptation.md` |
| Ảnh lỗi 401 dù API chạy được | Glide bỏ interceptor, xem `references/api-adaptation.md` |
| Lỗi `Unresolved reference: binding.xxx` | Layout mất view id, thêm lại |
| Hai ô None trong danh sách | Mapper tự thêm None, UI đã chèn sẵn |
| Test lỗi biên dịch sau khi thêm hàm vào interface | Các fake trong `app/src/test` chưa có hàm mới, xem `references/new-feature.md` |
| App crash khi mở sau khi thêm cột Room | Thiếu `Migration`, xem `references/new-feature.md` |
| Chỉ bản release lỗi (crash, dữ liệu trống, Edit mở nhân vật mặc định) | R8 đổi tên class mà Gson cần. Class mới đi qua Gson phải nằm trong `data/model/`, `data/remote/dto/` hoặc được thêm vào `app/src/main/keepRules/rules.keep` |
| Nút vẫn có vết mẻ gỗ | Hình vẽ bằng code trong `WoodButtonDrawable.kt` |
| Linux: `./gradlew: Permission denied` | Mất quyền chạy: `chmod +x gradlew`, rồi `git update-index --chmod=+x gradlew` và commit |
| Linux: `$'\r': command not found` hoặc `/bin/sh^M` khi chạy gradlew | `gradlew` bị CRLF (chép từ Windows thay vì dùng git): `sed -i 's/\r$//' gradlew`, kiểm tra `.gitattributes` còn đó |
| Linux: `python: command not found` | Dùng `python3` |
| `SDK location not found` | Thiếu `local.properties` hoặc `ANDROID_HOME`; mở project bằng Android Studio một lần, hoặc đặt `ANDROID_HOME` |
| `JAVA_HOME is not set` | Đặt `JAVA_HOME` theo mục [Windows và Linux](#windows-và-linux), hoặc build bằng Android Studio |
| Windows: `.\gradlew.bat` không nhận lệnh | Đang ở Git Bash: dùng `sh gradlew` |

## Giữ template luôn tốt

- Sửa lỗi chung ở một app thì đưa lại về project mẫu, nếu không các app sau sẽ lặp lại lỗi đó. Ví dụ: lỗi Edit ở My creation tạo bản mới thay vì ghi đè đã được sửa ở project mẫu (commit `c4851f4`).
- Tính năng mới dùng được cho nhiều app (không gắn chủ đề) cũng nên đưa về project mẫu, kèm cờ bật / tắt trong `AppConfig.kt` nếu không phải app nào cũng cần.
- Khi có đủ 2–3 app, cân nhắc tách `core/`, `data/` thành module dùng chung. Chưa cần làm sớm.
