package yourssu.backend.domain.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yourssu.backend.common.response.ApiResponse;
import yourssu.backend.common.status.SuccessStatus;
import yourssu.backend.domain.dto.request.ArticleRequest;
import yourssu.backend.domain.service.ArticleService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/article")
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping()
    public ApiResponse postArticle(@RequestBody ArticleRequest.ArticleDto articleDto){
        return ApiResponse.SuccessResponse(SuccessStatus.ARTICLE_POST_SUCCESS, articleService.postArticle(articleDto));
    }
}
