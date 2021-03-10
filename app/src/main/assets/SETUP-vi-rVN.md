# Trợ giúp thiết lập

Việc thiết lập FairEmail khá đơn giản. Bạn sẽ cần phải thêm ít nhất một tài khoản để nhận email và ít nhất một danh tính nếu bạn muốn gửi email. Thiết lập nhanh sẽ thêm một tài khoản và một danh tính trong một lần thiết lập cho đa số các nhà cung cấp lớn.

## Các yêu cầu

Yêu cầu kết nối internet để thiết lập các tài khoản và danh tính.

## Thiết lập nhanh

Chỉ cần chọn nhà cung cấp phù hợp hoặc *Nhà cung cấp khác* và nhập tên, địa chỉ email và mật khẩu của bạn và nhấn *Kiểm tra*.

Việc này sẽ được đối với đa số nhà cung cấp email.

Nếu thiết lập nhanh không được, bạn sẽ cần thiết lập một tài khoản và một danh tính thủ công, hãy xem hướng dẫn ở dưới.

## Thiết lập tài khoản - để nhận email

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Select a provider from the list, enter the username, which is mostly your email address and enter your password. Tap *Check* to let FairEmail connect to the email server and fetch a list of system folders. After reviewing the system folder selection you can add the account by tapping *Save*.

Nếu nhà cung cấp của bạn không ở trong danh sách các nhà cung cấp, có hàng nghìn nhà cung cấp, chọn *Tùy chỉnh*. Nhập tên miền, ví dụ *gmail.com* và nhấn *Lấy cài đặt*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). For more about this, please see [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Thiết lập danh tính - để gửi email

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Enter the name you want to appear in the from address of the emails you send and select a linked account. Tap *Save* to add the identity.

If the account was configured manually, you likely need to configure the identity manually too. Enter the domain name, for example *gmail.com* and tap *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Xem [câu hỏi thường gặp này](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) về việc sử dụng bí danh.

## Cấp quyền - để truy cập thông tin liện hệ

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Thiết lập tối ưu hoá pin - để nhận email liên tục

Trên các phiên bản Android gần đây, Android sẽ cho các ứng dụng ngủ khi màn hình tắt được một thời gian để giảm sử dụng pin. Nếu bạn muốn nhận email mới mà không có độ trễ, bạn nên tắt tối ưu hoá pin cho FairEmail. Nhấn *Quản lý* và làm theo hướng dẫn.

## Câu hỏi hoặc vấn đề

Nếu bạn có câu hỏi hoặc vấn đề, hãy [xem chỗ này](https://github.com/M66B/FairEmail/blob/master/FAQ.md) để tìm trợ giúp.