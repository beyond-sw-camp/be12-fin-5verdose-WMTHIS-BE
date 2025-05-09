package com.example.be12fin5verdosewmthisbe.menu_management.option.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.dto.OptionDto;
import com.example.be12fin5verdosewmthisbe.menu_management.option.service.OptionService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Option API", description = "메뉴 옵션 관련 API")
@RestController
@RequestMapping("/api/option")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "옵션 등록", description = "새로운 메뉴 옵션을 등록하고, 각 재고별 사용 수량을 설정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "옵션 등록 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": true, \"message\": \"Option registered successfully\", \"data\": null}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"잘못된 요청 형식입니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "5002", description = "카테고리 정보 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"해당 ID의 카테고리를 찾을 수 없습니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\", \"data\": null}")))
    })
    @PostMapping("/register")
    public BaseResponse<String> registerOption(@RequestBody @Valid OptionDto.RegisterRequestDto requestDto, HttpServletRequest request) {
        optionService.registerOption(requestDto, getStoreId(request));
        return BaseResponse.success("옵션이 성공적으로 등록되었습니다.");
    }


    @Operation(summary = "옵션 수정", description = "기존 메뉴 옵션의 정보 (이름, 가격, 카테고리) 및 재고별 사용 수량을 수정합니다. 요청에 없는 재고 ID의 사용 수량 정보는 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "옵션 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": true, \"message\": \"Option updated successfully\", \"data\": null}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"잘못된 요청 형식입니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "5002", description = "카테고리 정보 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"해당 ID의 카테고리를 찾을 수 없습니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "4001", description = "옵션 정보 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"해당 ID의 옵션을 찾을 수 없습니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\", \"data\": null}")))
    })
    @PutMapping
    public BaseResponse<String> updateOption(@RequestBody @Valid OptionDto.UpdateRequestDto requestDto, HttpServletRequest request) {
        optionService.updateOption(requestDto,getStoreId(request));
        return BaseResponse.success("옵션이 성공적으로 수정되었습니다.");
    }


    @Operation(summary = "옵션 삭제", description = "주어진 ID의 옵션을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "옵션 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": true, \"message\": \"Option deleted successfully\", \"data\": null}"))),
            @ApiResponse(responseCode = "4001", description = "옵션 정보 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"해당 ID의 옵션을 찾을 수 없습니다.\", \"data\": null}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\", \"data\": null}")))
    })
    @PostMapping("/delete/batch")
    public BaseResponse<String> deleteOptions(@RequestBody List<Long> optionIds) {
        optionService.deleteOptions(optionIds); // 내부에서 반복 삭제 처리
        return BaseResponse.success("Options deleted successfully");
    }

    @Operation(summary = "옵션 목록 조회 (페이지네이션)", description = "등록된 옵션 목록을 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "옵션 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\", \"data\": null}")))
    })
    @GetMapping("/list")
    public BaseResponse<Page<OptionDto.ResponseDto>> getOptionList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "페이지 정보 (기본: page=0, size=10, sort=name,asc)", schema = @Schema(implementation = Pageable.class))
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable, HttpServletRequest request) {

        Page<Option> optionPage;

        if (keyword != null && !keyword.isBlank()) {
            optionPage = optionService.searchOptionsByKeyword(keyword, pageable, getStoreId(request));
        } else {
            optionPage = optionService.findAllOptions(pageable, getStoreId(request));
        }

        Page<OptionDto.ResponseDto> dtoPage = optionPage.map(option -> new OptionDto.ResponseDto(
                option.getId(),
                option.getName()
        ));

        return BaseResponse.success(dtoPage);
    }


    @Operation(summary = "ID로 옵션 조회", description = "옵션 ID를 통해 특정 옵션을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "옵션 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "옵션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class, defaultValue = "{\"success\": false, \"message\": \"옵션을 찾을 수 없습니다.\", \"data\": null}")))
    })
    @GetMapping("/{optionId}")
    public BaseResponse<OptionDto.DetailResponseDto> getOptionById(
            @Parameter(description = "옵션 ID", required = true, example = "1")
            @PathVariable Long optionId) {
        Option option = optionService.findOptionWithValuesById(optionId);
        return BaseResponse.success(OptionDto.DetailResponseDto.from(option));
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