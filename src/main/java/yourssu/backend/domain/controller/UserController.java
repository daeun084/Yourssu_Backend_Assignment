package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.service.UserService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    @PostMapping("sign-up")
    public ApiResponse signUp(@RequestBody UserRequest.SignUpDto signUpDto){
        return ApiResponse.SuccessResponse(SuccessStatus.SIGN_UP_SUCCESS, userService.signUp(signUpDto));
    }
}
