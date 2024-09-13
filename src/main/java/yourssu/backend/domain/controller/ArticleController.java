package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.security.TokenDto;
import yourssu.backend.common.security.UserPrincipal;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.dto.request.UserRequest;
import yourssu.backend.domain.service.ArticleService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/article")
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping()
    public ApiResponse postArticle(@RequestBody ArticleRequest.ArticleDto articleDto,
                                   @AuthenticationPrincipal UserPrincipal userprincipal){
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_POST_SUCCESS, articleService.postArticle(articleDto, userprincipal));
    }

    @PatchMapping("/{articleId}")
    public ApiResponse patchArticle(@RequestBody ArticleRequest.ArticleDto articleDto,
                                    @PathVariable(name = "articleId") Long articleId,
                                    @AuthenticationPrincipal UserPrincipal userprincipal){
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_PATCH_SUCCESS, articleService.patchArticle(articleDto, articleId, userprincipal));
    }

    @DeleteMapping("/{articleId}")
    public ApiResponse deleteArticle(@PathVariable(name = "articleId") Long articleId,
                                     @AuthenticationPrincipal UserPrincipal userprincipal){
        articleService.deleteArticle(articleId, userprincipal);
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_DELETE_SUCCESS);
    }
}
