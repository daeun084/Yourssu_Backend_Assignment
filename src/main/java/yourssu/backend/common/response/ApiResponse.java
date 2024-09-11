package yourssu.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import yourssu.backend.common.status.ErrorStatus;
import yourssu.backend.common.status.SuccessStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "result", "message", "data"})
public class ApiResponse<T> {
    private final int code;
    private final String result;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static <T>ApiResponse<T> SuccessResponse(SuccessStatus status, T data) {
        return new ApiResponse<>(status.getCode(), "SUCCESS", status.getMessage(), data);
    }

    public static ApiResponse SuccessResponse(SuccessStatus status) {
        return new ApiResponse<>(status.getCode(), "SUCCESS", status.getMessage(), null);
    }

    public static <T>ApiResponse<T> ErrorResponse(ErrorStatus status, T data) {
        return new ApiResponse<>(status.getCode(), "FAILURE", status.getMessage(), data);
    }

    public static ApiResponse ErrorResponse(ErrorStatus status) {
        return new ApiResponse<>(status.getCode(), "FAILURE", status.getMessage(), null);
    }
}
