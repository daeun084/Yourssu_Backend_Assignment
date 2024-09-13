package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.CommentRequest;
import yourssu.backend.domain.service.CommentService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping()
    public ApiResponse postComment(@RequestBody CommentRequest.PostCommentDto commentDto,
                                   @AuthenticationPrincipal UserPrincipal userprincipal){
        return ApiResponse.SuccessResponse(SuccessStatus.COMMENT_POST_SUCCESS, commentService.postComment(commentDto, userprincipal));
    }

    @PatchMapping("/{commentId}")
    public ApiResponse patchComment(@RequestBody CommentRequest.PatchCommentDto commentDto,
                                    @PathVariable(name = "commentId") Long commentId,
                                    @AuthenticationPrincipal UserPrincipal userprincipal){
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_PATCH_SUCCESS, commentService.patchComment(commentDto, commentId, userprincipal));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse deleteComment(@PathVariable(name = "commentId") Long commentId,
                                     @AuthenticationPrincipal UserPrincipal userprincipal){
        commentService.deleteComment(commentId, userprincipal);
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_DELETE_SUCCESS);
    }
}
