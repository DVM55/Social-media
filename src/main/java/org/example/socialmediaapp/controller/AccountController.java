package org.example.socialmediaapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.ChangePasswordRequest;
import org.example.socialmediaapp.dto.req.UpdateAccountRequest;
import org.example.socialmediaapp.dto.req.UpdateAvatarRequest;
import org.example.socialmediaapp.dto.res.*;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.service.AccountService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private  final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        List<Account> accounts = accountService.getAccounts(page, size);

        return ResponseEntity.ok(
                ApiResponse.<List<Account>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy danh sách nguười dùng thành công")
                        .data(accounts)
                        .build()
        );
    }


    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfilePersonalResponse>> getProfilePersonal() {
        ProfilePersonalResponse response = accountService.getProfilePersonal();
        return ResponseEntity.ok(
                ApiResponse.<ProfilePersonalResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy thông tin cá nhân thành công")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<ProfileUserResponse>> getProfileUser(@PathVariable Long id) {
        ProfileUserResponse response = accountService.getProfileUser(id);
        return ResponseEntity.ok(
                ApiResponse.<ProfileUserResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy thông tin cá nhân thành công")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccountById(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa tài khoản thành công")
                        .data(null)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> updateAccountLock(
            @PathVariable Long id) {

        boolean locked = accountService.updateLockStatus(id);

        String message = locked
                ? "Khóa tài khoản thành công"
                : "Mở khóa tài khoản thành công";

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UpdateAccountResponse>> updateAccountInfo(
            @Valid @RequestBody UpdateAccountRequest body) {
        UpdateAccountResponse response = accountService.updateAccount(body);
        return ResponseEntity.ok(
                ApiResponse.<UpdateAccountResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Cập nhật thông tin tài khoản thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest body) {
        accountService.changePassword(body);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Đổi mật khẩu thành công")
                        .data(null)
                        .build()
        );
    }

    @PutMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> updateAvatarUrl(
            @ModelAttribute UpdateAvatarRequest body
    ) {
        Map<String, String> response = accountService.updateAvatarUrl(body);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Cập nhật ảnh đại diện thành công")
                        .data(response)
                        .build()
        );
    }


    @GetMapping("/search")
    public ResponseEntity<PagingResponse<AccountResponse>> searchAccounts(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String keyword
    ) {
        var response = accountService.queryAccountWithFollowStatus(lastId, limit, keyword);
        return ResponseEntity.ok(response);
    }

}
