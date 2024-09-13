package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.request.CommentRequest;
import yourssu.backend.domain.service.CommentService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping()
    public ApiResponse postComment(@RequestBody CommentRequest.CommentDto commentDto){
        return ApiResponse.SuccessResponse(SuccessStatus.COMMENT_POST_SUCCESS, commentService.postComment(commentDto));
    }
}
