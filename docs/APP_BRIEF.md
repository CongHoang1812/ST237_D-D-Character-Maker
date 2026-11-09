# App brief: D&D Character Maker

Brief của **project mẫu**. Mọi app maker sau được tạo từ project này bằng `new_app.py`; script không chép file này mà tạo brief mới từ `.claude/skills/maker-reskin/assets/app-brief.template.md`.

## Định danh

| Mục | Giá trị |
|---|---|
| Tên app | D&D Character Maker |
| Mã dự án | ST237 |
| Package / applicationId | `com.fantasy.oc.maker.customize` |
| Tiền tố class / theme | `FaceMaker` (`FaceMakerApp`, `Theme.FaceMaker`, `Widget.FaceMaker.*`) |
| Tên database | `face_maker.db` |
| APP_KEY trên server (đuôi API) | `ST247_ChibiDollDressUp` (**mượn tạm** để test, xem "Quyết định đã chốt") |
| Máy chủ chính | https://lvtglobal.tech/ |
| Máy chủ dự phòng | https://lvt-api-tech.io.vn/ |

## Thiết kế

- Figma URL: https://www.figma.com/design/QDVSAOgH3rY4DuocNmN8nM/ST237_D-D-Character-Maker--Copy-
- fileKey: `QDVSAOgH3rY4DuocNmN8nM`, page `ui` (page `lfs` là ảnh store listing, không code)
- Chủ đề: quán rượu D&D, khung giấy rách, nút gỗ
- Font chính: Londrina Solid (OFL). Font editor: xem `docs/assets.md`
- Ảnh đã xuất ↔ node: `docs/assets.md`

## API

- Định dạng: chuẩn của server LVT (`api/app/<APP_KEY>` + `public/app/<APP_KEY>/`), xem `.claude/skills/maker-reskin/references/api-adaptation.md`
- Token / header: không cần, chỉ GET
- Random, cosplay, lucky roll: server không có endpoint, app tự random (`CharacterRandomizer`) và ghép ảnh (`CharacterImageComposer`)

## Phát hành

- URL chính sách bảo mật: https://sites.google.com/view/d-d-character-maker/home
- Email góp ý: chưa có (`feedback@example.com`)
- Ngôn ngữ: en, hi, es, fr, pt, in
- Quảng cáo: chưa tích hợp (`TODO(ADS)` trong `ConfirmDialogFragment`)

## Tính năng

Đây là bộ tính năng chuẩn mà mọi app sinh ra đều có sẵn:

| Tính năng | Ghi chú |
|---|---|
| Onboarding | Splash → Language (lần đầu) → Intro 1–3 → Permission → Home |
| Maker | Chọn template → Customize (bộ phận, màu, random, move / zoom / xoay / lật) → Decor editor → Successful |
| Decor editor | Nền (màu, ảnh, pack), sticker, speech bubble, chữ nhiều font |
| Cosplay | Dựng lại nhân vật mẫu trong 10 phút, chấm điểm theo % khớp, sao theo điểm |
| Lucky roll | Random nhân vật hoàn chỉnh → Edit |
| My creation | Tab Character / Design, chọn nhiều, xoá, tải về, chia sẻ, Add to WhatsApp / Telegram, Edit nhân vật (ghi đè bản cũ) |
| Setting | Ngôn ngữ, Rate us, Share app, Privacy |
| Trạng thái chung | Skeleton khi tải, `ErrorStateView` + Retry, tự tải lại khi có mạng, dialog No internet |

## Bảng màn hình ↔ Figma

| Màn | Layout | Figma node | Trạng thái |
|---|---|---|---|
| Splash | `fragment_splash` | `3:572` | xong |
| Language | `fragment_language`, `item_language` | `3:605` (lần đầu), `3:505` (từ Setting) | xong |
| Intro | `fragment_intro`, `item_intro_page` | `3:668`, `3:692`, `3:717` | xong |
| Permission | `fragment_permission`, `item_permission_row` | `3:741` | xong |
| Home | `fragment_home` | `3:438` | xong |
| Setting | `fragment_setting` | `3:468` | xong |
| Chọn template | `fragment_template_select`, `item_template` | `3:278` | xong |
| Customize + Move | `fragment_customize`, `view_move_panel`, `item_part`, `item_none_part`, `item_dice_part`, `item_color_dot`, `item_category_tab` | `3:335`, `8:24` | xong |
| Decor editor | `fragment_decor_editor`, `item_decor_cell`, `item_none_decor`, `item_sub_tab`, `item_font_chip`, `item_color_chip` | `3:773`, `3:843`, `3:913`, `3:993`, `3:1155` | xong |
| Successful | `fragment_success` | `3:1109`, `3:223` (cosplay) | xong |
| My creation + Detail | `fragment_my_creation`, `fragment_creation_detail`, `item_creation` | `3:1229`, `3:1243`, `3:1257`, `3:1359`, `3:1437`, `3:1515`, `3:1583`, `3:1608` | xong |
| Cosplay | `fragment_cosplay_start`, `fragment_customize` (mode COSPLAY), `dialog_cosplay_score`, `dialog_instruction` | `3:41`, `3:77`, `3:132` | xong |
| Lucky roll | `fragment_lucky_roll` | `3:11`, `3:24` | xong |
| Dialog | `view_paper_dialog`, `dialog_confirm`, `dialog_color_picker`, `dialog_rate_us`, `dialog_pack_name`, `dialog_loading` | `69:34`, `69:59`, `69:73`, `69:87`, `69:101`, `69:339` | xong |
| Trạng thái tải / lỗi | `view_error_state`, `item_skeleton_*`, `view_skeleton_image` | không có trong Figma, dựng từ design system | xong |

## Quyết định đã chốt

- `APP_KEY` đang là `ST247_ChibiDollDressUp` vì chưa có dữ liệu của ST237 trên server. Có mã thật thì đổi trong `AppConfig.kt` (đã đánh dấu `TODO(RELEASE)`, `audit.py --release` sẽ báo).
- Package `com.fantasy.oc.maker.customize` được đổi tay (commit `ba82166`), không qua `new_app.py`. Tên class / theme / database vẫn giữ `FaceMaker` / `face_maker.db`.
- App đúng là ST237 D&D Character Maker. Tên file APK (`archivesName`) là `ST237_DnD_Character_Maker` + version + ngày (viết `DnD` vì `&` trong tên file dễ lỗi shell). Tên `ST279_Anime_OC_Maker_Avatar_Creator` trước đó chỉ là mượn tạm.
- `app/google-services.json` đã có nhưng plugin Google Services chưa được áp dụng nên chưa có tác dụng. `new_app.py` không chép file này sang app mới.
- Ảnh lưu ở internal storage; chỉ nút Download ghi ra Gallery (`Pictures/<tên app>`).
- Bản release bật R8 (`optimization { enable = true }`, từ 2026-11-08): APK release từ 10,1 MB xuống 4,7 MB. Class đi qua Gson được giữ tên trong `app/src/main/keepRules/rules.keep` (đã kiểm tra bằng `seeds.txt` / `usage.txt`). Bản release đầu tiên bật R8 cần test trên máy thật: Maker, Decor, lưu, Edit nhân vật đã lưu từ bản cũ, Cosplay, Lucky roll.
- Edit từ My creation ghi đè đúng nhân vật đó (truyền `creationId` sang Customize), không tạo bản mới.
- Cosplay Start giữ đề hiện tại khi về Home rồi vào lại, kể cả sau khi đã chơi xong; chỉ nút Random mới đổi đề. Đề giữ trong bộ nhớ nên app bị kill thì ra đề mới.
- `docs/prompts/` là lịch sử dựng app lần đầu, một số chi tiết đã cũ; tài liệu hiện hành là `CLAUDE.md`, brief này và skill `maker-reskin`.
