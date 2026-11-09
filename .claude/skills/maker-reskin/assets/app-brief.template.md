# App brief: {{app_name}}

Nguồn sự thật của app này. Skill `maker-reskin` đọc file này trước khi làm bất cứ việc gì. Mục nào chưa có thì để trống, điền dần khi có (Figma, tính năng mới có thể đến sau).

## Định danh (script đã điền)

| Mục | Giá trị |
|---|---|
| Tên app | {{app_name}} |
| Mã dự án | {{project_code}} |
| Package / applicationId | `{{app_id}}` |
| Tiền tố class / theme | `{{class_prefix}}` |
| APP_KEY trên server (đuôi API) | `{{app_key}}` |
| Máy chủ chính | {{base_url}} |
| Máy chủ dự phòng | {{backup_url}} |

`APP_KEY` quyết định cả endpoint dữ liệu `api/app/<APP_KEY>` lẫn thư mục ảnh `public/app/<APP_KEY>/`. Đổi đuôi API = đổi `APP_KEY` trong `core/common/AppConfig.kt`.

## Bạn cần điền

### Thiết kế

- Figma URL:
- Page chứa màn hình:
- Chủ đề app (vd. anime, mèo, công chúa):
- Danh từ thay cho "D&D" trong chữ hiển thị:
- Font chính (tên + nguồn, cần giấy phép dùng thương mại):

### API

- Định dạng: `giống app mẫu` / `khác` (chạy `probe_api.py` nếu chưa chắc)
- Tài liệu API (đường dẫn file hoặc link):
- JSON mẫu thật (đường dẫn file):
- Có cần token / header không:
- Endpoint thêm cho tính năng mới (nếu có):

### Phát hành

- URL chính sách bảo mật:
- Email góp ý:
- Ngôn ngữ (mặc định en, hi, es, fr, pt, in):
- Quảng cáo / Firebase (có / không, mã gì):

## Tính năng / màn hình mới

Những thứ app mẫu chưa có. Mỗi dòng một việc; Claude lập kế hoạch và hỏi trước khi làm (xem `references/new-feature.md` trong skill).

| Tính năng / màn | Mô tả (mở từ đâu, làm gì, lưu gì) | Figma node | Dữ liệu (API / local) | Trạng thái |
|---|---|---|---|---|
| | | | | |

## Bỏ hoặc đổi so với app mẫu

Tính năng có sẵn nhưng app này không dùng hoặc hoạt động khác. Để trống nghĩa là giữ nguyên.

-

## Bảng màn hình ↔ Figma

Điền node id khi bắt đầu giai đoạn giao diện. Trạng thái: `chưa` / `đang làm` / `xong` / `chờ Figma`.

| Màn | Layout | Figma node | Trạng thái |
|---|---|---|---|
| Splash | `fragment_splash` | | |
| Language | `fragment_language`, `item_language` | | |
| Intro | `fragment_intro`, `item_intro_page` | | |
| Permission | `fragment_permission`, `item_permission_row` | | |
| Home | `fragment_home` | | |
| Setting | `fragment_setting` | | |
| Chọn template | `fragment_template_select`, `item_template` | | |
| Customize + Move | `fragment_customize`, `view_move_panel`, `item_part`, `item_none_part`, `item_dice_part`, `item_color_dot`, `item_category_tab` | | |
| Decor editor | `fragment_decor_editor`, `item_decor_cell`, `item_none_decor`, `item_sub_tab`, `item_font_chip`, `item_color_chip` | | |
| Successful | `fragment_success` | | |
| My creation + Detail | `fragment_my_creation`, `fragment_creation_detail`, `item_creation` | | |
| Cosplay | `fragment_cosplay_start`, `fragment_customize` (mode COSPLAY), `dialog_cosplay_score`, `dialog_instruction` | | |
| Lucky roll | `fragment_lucky_roll` | | |
| Dialog | `view_paper_dialog`, `dialog_confirm` (Exit / Reset / Random / Delete / No internet), `dialog_color_picker`, `dialog_rate_us`, `dialog_pack_name`, `dialog_loading` | | |
| Trạng thái tải / lỗi | `view_error_state`, `item_skeleton_*`, `view_skeleton_image` | | |

Màn mới thêm dòng vào cuối bảng này.

## Quyết định đã chốt

Ghi lại quyết định không suy ra được từ code, để phiên làm việc sau không phải hỏi lại.

-
