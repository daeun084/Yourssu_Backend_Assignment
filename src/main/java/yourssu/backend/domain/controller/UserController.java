package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.service.UserService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    @PostMapping("/sign-up")
    public ApiResponse signUp(@RequestBody UserRequest.SignUpDto signUpDto){
        return ApiResponse.SuccessResponse(SuccessStatus.SIGN_UP_SUCCESS, userService.signUp(signUpDto));
    }

    @DeleteMapping("/withdrawal")
    public ApiResponse withdrawal(@RequestBody UserRequest.WithDrawalDto withDrawalDto){
        userService.withdrawal(withDrawalDto);
        return ApiResponse.SuccessResponse(SuccessStatus.WITHDRAWAL_SUCCESS);
    }
}
