package com.example.be12fin5verdosewmthisbe.menu_management.category.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.dto.CategoryDto;
import com.example.be12fin5verdosewmthisbe.menu_management.category.service.CategoryService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Category API", description = "메뉴 카테고리 관련 API")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
// TODO: 가게 ID 추가하면 가게 ID에 해당 하는 목록에서만 조회해야함
public class CategoryController {
    private final CategoryService categoryService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "카테고리 등록", description = "새로운 메뉴 카테고리를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 등록 성공"),
            @ApiResponse(responseCode = "5004", description = "이미 존재하는 카테고리입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/register")
    public BaseResponse<String> registerCategory(@RequestBody @Valid CategoryDto.requestDto dto, HttpServletRequest request) {
        log.info("register");
        categoryService.register(dto,getStoreId(request));
        return BaseResponse.success("Category registered successfully");
    }

    @Operation(summary = "카테고리 수정", description = "기존 메뉴 카테고리 이름을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "5002", description = "해당 카테고리를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/update")
    public BaseResponse<String> updateCategory(@RequestBody  @Valid CategoryDto.updateDto dto, HttpServletRequest request) {
        categoryService.update(dto.getId(), dto.getNewName(),dto.getOptionIds(),getStoreId(request));
        return BaseResponse.success("Category updated successfully");
    }

    @Operation(summary = "카테고리 삭제", description = "기존 메뉴 카테고리를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "5002", description = "해당 카테고리를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/delete")
    public BaseResponse<String> deleteCategory(@RequestBody @Valid  CategoryDto.deleteDto dto) {
        List<Long> ids = dto.getIds();

        for (Long id : ids) {
                categoryService.deleteCategory(id);
        }

        return BaseResponse.success("Categories deleted successfully");
    }

    @GetMapping("/getList")
    @Operation(summary = "카테고리 목록 조회", description = "키워드로 검색하거나 전체 카테고리를 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            @ApiResponse(responseCode = "5003", description = "카테고리 목록이 비어있습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public BaseResponse<Page<CategoryDto.CategoryResponseDto>> getCategoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        Page<CategoryDto.CategoryResponseDto> result = categoryService.getCategoryList(PageRequest.of(page, size), keyword,getStoreId(request));
        return BaseResponse.success(result);
    }

    @GetMapping("getPOSCategoryList")
    public BaseResponse<List<CategoryDto.CategoryResponseDto>> getCategoryList(HttpServletRequest request) {
        List<CategoryDto.CategoryResponseDto> result = categoryService.getPOSCategoryList(getStoreId(request));
        return BaseResponse.success(result);
    }


    @Operation(summary = "카테고리 상세 조회", description = "특정 이름의 메뉴 카테고리 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "5001", description = "카테고리 타입에 맞지 않는 잘못된 요청입니다."),
            @ApiResponse(responseCode = "5002", description = "해당 카테고리를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/detail")
    public BaseResponse<CategoryDto.responseDto> getCategoryDetail(@RequestParam Long id) {
        if (id == null) {
            return BaseResponse.error(ErrorCode.INVAILD_REQUEST);
        }
        Category category = categoryService.findById(id);
        return BaseResponse.success(CategoryDto.responseDto.from(category));
    }


    private Long getStoreId(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ATOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        Claims claims = jwtTokenProvider.getClaims(token);
        Long storeId = Long.valueOf(claims.get("storeId", String.class));
        return  storeId;
    }
}
