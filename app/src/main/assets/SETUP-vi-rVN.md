# Trợ giúp thiết lập

Việc thiết lập FairEmail khá đơn giản. Bạn sẽ cần phải thêm ít nhất một tài khoản để nhận email và ít nhất một danh tính nếu bạn muốn gửi email. Thiết lập nhanh sẽ thêm một tài khoản và một danh tính trong một lần thiết lập cho đa số các nhà cung cấp lớn.

## Các yêu cầu

Yêu cầu kết nối internet để thiết lập các tài khoản và danh tính.

## Thiết lập nhanh

Chỉ cần chọn nhà cung cấp phù hợp hoặc *Nhà cung cấp khác* và nhập tên, địa chỉ email và mật khẩu của bạn và nhấn *Kiểm tra*.

Việc này sẽ được đối với đa số nhà cung cấp email.

Nếu thiết lập nhanh không được, bạn sẽ cần thiết lập một tài khoản và một danh tính thủ công, hãy xem hướng dẫn ở dưới.

## Thiết lập tài khoản - để nhận email

Để thêm một tài khoản, nhấn *Thiết lập thủ công và thêm tuỳ chọn*, nhấn *Tài khoản* và nhấn nút 'cộng' ở phía dưới và chọn IMAP (hoặc POP3). Chọn một nhà cung cấp từ danh sách, nhập tên người dùng (phần lớn là địa chỉ email của bạn) và nhập mật khẩu của bạn. Nhấn *Kiểm tra* để cho FairEmail kết nối đến máy chủ email và lấy danh sách các thư mục hệ thống. Sau khi xem xét sự lựa chọn thư mục hệ thống, bạn có thể thêm tài khoản bằng cách nhấn *Lưu*.

Nếu nhà cung cấp của bạn không ở trong danh sách các nhà cung cấp, có hàng nghìn nhà cung cấp, chọn *Tùy chỉnh*. Nhập tên miền, ví dụ *gmail.com* và nhấn *Lấy cài đặt*. Nếu nhà cung cấp của bạn hỗ trợ [tự động khám phá](https://tools.ietf.org/html/rfc6186), FairEmail sẽ điền vào tên máy chủ và số cổng, nếu không thì hãy kiểm tra hướng dẫn thiết lập của nhà cung cấp của bạn để tìm tên máy chủ IMAP đúng, số cổng và giao thức mã hoá (SSL/TLS hoặc STARTTLS). Để biết thêm về điều này, hãy xem [chỗ này](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Thiết lập danh tính - để gửi email

Tương tự, để thêm một danh tính, nhấn *Thiết lập thủ công và thêm tuỳ chọn*, nhấn *Danh tính* và nhấn nút 'cộng' ở phía dưới. Nhập tên bạn muốn xuất hiện trong địa chỉ 'từ' của các email bạn gửi và chọn một tài khoản được liên kết. Nhấn *Lưu* để thêm danh tính.

Nếu tài khoản được thiết lập thủ công, có khả năng bạn cũng cần thiết lập danh tính thủ công. Nhập tên miền, ví dụ *gmail.com* và nhấn *Lấy cài đặt*. Nếu nhà cung cấp của bạn hỗ trợ [tự động khám phá](https://tools.ietf.org/html/rfc6186), FairEmail sẽ điền vào tên máy chủ và số cổng, nếu không thì hãy kiểm tra hướng dẫn thiết lập của nhà cung cấp của bạn để tìm tên máy chủ SMTP đúng, số cổng và giao thức mã hoá (SSL/TLS hoặc STARTTLS).

Xem [câu hỏi thường gặp này](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) về việc sử dụng bí danh.

## Cấp quyền - để truy cập thông tin liên hệ

Nếu bạn muốn tra cứu địa chỉ email, hiện ảnh liên hệ, v.v., bạn sẽ cần cấp quyền đọc thông tin liên hệ cho FairEmail. Chỉ cần nhấn *Cấp* và chọn *Cho phép*.

## Thiết lập tối ưu hoá pin - để nhận email liên tục

Trên các phiên bản Android gần đây, Android sẽ cho các ứng dụng ngủ khi màn hình tắt được một thời gian để giảm sử dụng pin. Nếu bạn muốn nhận email mới mà không có độ trễ, bạn nên tắt tối ưu hoá pin cho FairEmail. Nhấn *Quản lý* và làm theo hướng dẫn.

## Câu hỏi hoặc vấn đề

Nếu bạn có câu hỏi hoặc vấn đề, hãy [xem chỗ này](https://github.com/M66B/FairEmail/blob/master/FAQ.md) để tìm trợ giúp.