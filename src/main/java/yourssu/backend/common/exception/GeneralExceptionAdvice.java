package yourssu.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.status.ErrorStatus;

@RestControllerAdvice(annotations = {RestController.class})
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { GeneralException.class })
    protected ResponseEntity<ApiResponse<String>> handleException(GeneralException e) {
        ApiResponse<String> response = ApiResponse.ErrorResponse((ErrorStatus) e.getBaseErrorCode());
        HttpStatus status = e.getReasonHttpStatus().getHttpStatus() != null
                ? e.getReasonHttpStatus().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(response, status);
    }
}
