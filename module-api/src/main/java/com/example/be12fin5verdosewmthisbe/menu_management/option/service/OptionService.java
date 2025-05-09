package com.example.be12fin5verdosewmthisbe.menu_management.option.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto.MenuRegisterDto;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.dto.OptionDto;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionService {

    private final OptionRepository optionRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void registerOption(OptionDto.RegisterRequestDto request,Long storeId) {


        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        // 중복 체크
        Optional<Option> duplicate = optionRepository.findByStoreIdAndName(storeId,request.getName());
        if(duplicate.isPresent()) {
            throw new CustomException(ErrorCode.OPTION_ALREADY_EXIST);
        }
        Set<Long> uniqueStoreInventoryIds = new HashSet<>();
        for (OptionDto.InventoryQuantityDto ingredient : request.getInventoryQuantities()) {
            if (!uniqueStoreInventoryIds.add(ingredient.getInventoryId())) {
                throw new CustomException(ErrorCode.DUPLICATE_INGREDIENT_IN_RECIPE);
            }
        }

        Option option = Option.builder()
                .name(request.getName())
                .store(store)
                .price(request.getPrice())
                .build();

        List<OptionValue> optionValues = request.getInventoryQuantities().stream()
                .map(iq -> {
                    StoreInventory inventory = storeInventoryRepository.findById(iq.getInventoryId())
                            .orElseThrow(() -> new IllegalArgumentException("해당 재고 없음 ID: " + iq.getInventoryId()));

                    return OptionValue.builder()
                            .option(option)
                            .storeInventory(inventory)
                            .quantity(iq.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        option.setOptionValueList(optionValues); // 양방향 설정
        optionRepository.save(option);
    }

    @Transactional
    public void updateOption(OptionDto.UpdateRequestDto request, Long storeId) {


        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        Option option = optionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다. ID: " + request.getOptionId()));

        // 중복 체크
        Optional<Option> duplicate = optionRepository.findByStoreIdAndName(storeId, request.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(request.getOptionId())) {
            throw new CustomException(ErrorCode.OPTION_ALREADY_EXIST);
        }
        Set<Long> uniqueStoreInventoryIds = new HashSet<>();
        for (OptionDto.InventoryQuantityDto ingredient : request.getInventoryQuantities()) {
            if (!uniqueStoreInventoryIds.add(ingredient.getInventoryId())) {
                throw new CustomException(ErrorCode.DUPLICATE_INGREDIENT_IN_RECIPE);
            }
        }

        // 이름, 가격 업데이트
        option.setName(request.getName());
        option.setPrice(request.getPrice());

        // 기존 OptionValue 초기화
        option.getOptionValueList().clear();

        // 새 OptionValue 리스트 생성
        List<OptionValue> newValues = request.getInventoryQuantities().stream()
                .map(iq -> {
                    StoreInventory inventory = storeInventoryRepository.findById(iq.getInventoryId())
                            .orElseThrow(() -> new IllegalArgumentException("재고 없음 ID: " + iq.getInventoryId()));

                    return OptionValue.builder()
                            .option(option)
                            .storeInventory(inventory)
                            .quantity(iq.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        option.getOptionValueList().addAll(newValues); // 양방향 설정
    }



    public void deleteOptions(List<Long> optionIds) {
        List<Option> options = optionRepository.findAllById(optionIds);

        // 존재하지 않는 ID가 있을 경우 예외 처리 (선택사항)
        if (options.size() != optionIds.size()) {
            throw new CustomException(ErrorCode.OPTION_NOT_FOUND);
        }

        optionRepository.deleteAll(options);
    }

    public Page<Option> findAllOptions(Pageable pageable,Long storeId) {

        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));
        return optionRepository.findByStoreId(storeId,pageable);
    }
    @Transactional(readOnly = true)
    public Option findOptionWithValuesById(Long optionId) {
        return optionRepository.findByIdWithOptionValues(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다. ID: " + optionId));
    }
    public Page<Option> searchOptionsByKeyword(String keyword, Pageable pageable, Long storeId) {

        // 가게 정보 체크
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));
        return optionRepository.findByStoreIdAndNameContaining(storeId,keyword, pageable);
    }

}