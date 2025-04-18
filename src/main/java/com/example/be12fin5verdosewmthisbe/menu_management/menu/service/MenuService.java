package com.example.be12fin5verdosewmthisbe.menu_management.menu.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.menu_management.category.repository.CategoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto.MenuDto;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto.MenuRegisterDto;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto.MenuUpdateDto;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.RecipeRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeRepository recipeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void registerMenu(MenuRegisterDto.MenuCreateRequestDto dto,Long storeId) {


        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        // 중복 검사
        Optional<Menu> duplicate = menuRepository.findByStoreIdAndName(storeId, dto.getName());
        if(duplicate.isPresent()) {
            throw new CustomException(ErrorCode.MENU_ALREADY_EXIST);
        }


        // 1. 카테고리 조회 (nullable 허용)
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }
        // 2. 메뉴 생성
        Menu menu = Menu.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .store(store)
                .category(category)  // null일 수도 있음
                .build();

        menuRepository.save(menu);

        // 3. 재료(Recipe) 연결
        List<Recipe> recipes = dto.getIngredients().stream().map(ingredientDto -> {
            StoreInventory storeInventory = storeInventoryRepository.findById(ingredientDto.getStoreInventoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND));

            return Recipe.builder()
                    .menu(menu)
                    .storeInventory(storeInventory)
                    .quantity(ingredientDto.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        recipeRepository.saveAll(recipes);
    }

    public Menu findById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }
    public Page<MenuDto.MenuListResponseDto> findAllMenus(Pageable pageable, String keyword,Long storeId) {

        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        Page<Menu> result = null;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = menuRepository.findByStoreId(storeId,pageable);
        } else {
            result = menuRepository.findByStoreIdAndNameContaining(storeId, keyword, pageable);
        }


        if (result.isEmpty()) {
            throw new CustomException(ErrorCode.MENU_NOT_FOUND);
        }

        return result.map(this::convertToMenuListResponseDto);
    }

    private MenuDto.MenuListResponseDto convertToMenuListResponseDto(Menu menu) {
        List<Recipe> recipes = menu.getRecipes();

        // 카테고리가 null일 수 있으므로 안전하게 처리
        String categoryName = (menu.getCategory() != null) ? menu.getCategory().getName() : "카테고리 없음";

        // 재료가 없을 경우
        if (recipes.isEmpty()) {
            return MenuDto.MenuListResponseDto.builder()
                    .id(menu.getId())
                    .name(menu.getName())
                    .category(categoryName)
                    .ingredients("재료 없음")
                    .build();
        }

        // 사용량이 가장 큰 재료 찾기
        Recipe maxRecipe = recipes.stream()
                .filter(r -> r.getStoreInventory() != null && r.getQuantity() != null)
                .max(Comparator.comparing(Recipe::getQuantity))
                .orElse(null);

        String ingredientSummary = "재료 정보 없음";

        if (maxRecipe != null) {
            String name = maxRecipe.getStoreInventory().getName();
            BigDecimal quantity = maxRecipe.getQuantity();
            String unit = maxRecipe.getStoreInventory().getUnit();

            int otherCount = (int) recipes.stream()
                    .filter(r -> r.getStoreInventory() != null)
                    .map(r -> r.getStoreInventory().getName())
                    .distinct()
                    .count() - 1;

            ingredientSummary = String.format(
                    "%s %s%s%s",
                    name,
                    quantity.stripTrailingZeros().toPlainString(),
                    unit,
                    (otherCount > 0 ? String.format(" 외 %d종", otherCount) : "")
            );
        }

        return MenuDto.MenuListResponseDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .category(categoryName)
                .ingredients(ingredientSummary)
                .build();
    }

    public MenuDto.MenuDetailResponseDto getMenuDetail(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("해당 메뉴를 찾을 수 없습니다."));

        List<Recipe> recipes = menu.getRecipes();

        List<MenuDto.IngredientInfoDto> ingredients = recipes.stream()
                .map(recipe -> {
                    StoreInventory inventory = recipe.getStoreInventory();
                    return MenuDto.IngredientInfoDto.builder()
                            .storeInventoryId(inventory.getStoreinventoryId())
                            .name(inventory.getName())
                            .quantity(recipe.getQuantity())
                            .unit(inventory.getUnit())
                            .build();
                }).toList();

        Long categoryId = (menu.getCategory() != null) ? menu.getCategory().getId() : null;

        return MenuDto.MenuDetailResponseDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .categoryId(categoryId)
                .price(menu.getPrice())
                .ingredients(ingredients)
                .build();
    }


    public void deleteMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            throw new IllegalArgumentException("메뉴 ID 리스트가 비어있습니다.");
        }

        for (Long menuId : menuIds) {
            menuRepository.deleteById(menuId);
        }
    }
    @Transactional
    public void updateMenu(Long menuId, MenuUpdateDto.RequestDto dto,Long storeId) {


        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        // 중복 검사
        Optional<Menu> duplicate = menuRepository.findByStoreIdAndName(storeId, dto.getName());
        if(duplicate.isPresent() && !duplicate.get().getId().equals(menuId)) {
            throw new CustomException(ErrorCode.MENU_ALREADY_EXIST);
        }

        // 1. 메뉴 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 2. 카테고리 조회 및 변경
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        menu.setCategory(category);

        // 3. 기본 정보 수정
        menu.setName(dto.getName());
        menu.setPrice(dto.getPrice());

        // 4. 기존 레시피 삭제 (orphanRemoval=true이므로 그냥 clear로 충분)
        menu.getRecipes().clear();

        // 5. 새 레시피 추가
        for (MenuRegisterDto.MenuCreateRequestDto.IngredientDto ingredientDto : dto.getIngredients()) {
            StoreInventory inventory = storeInventoryRepository.findById(ingredientDto.getStoreInventoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

            Recipe recipe = Recipe.builder()
                    .menu(menu)
                    .storeInventory(inventory)
                    .quantity(ingredientDto.getQuantity())
                    .build();

            menu.getRecipes().add(recipe);
        }

    }
}