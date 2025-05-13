package com.example.be12fin5verdosewmthisbe.store.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.model.dto.StoreDto;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import com.example.be12fin5verdosewmthisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double RADIUS_KM = 3.0;
    public String registerStore(StoreDto.RegistRequest dto, String emailUrl) {
        User user = userRepository.findByEmail(emailUrl).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND) );
        Store registerStore = dto.toEntity(user,dto.getLatitude(),dto.getLongitude());
        Store store =  storeRepository.save(registerStore);
        return  String.valueOf(store.getId());
    }

    public List<Store> getNearbyStoreIds(Long storeId) {
        double radiusInMeters = RADIUS_KM * 1000;
        return storeRepository.findNearbyStoresByStoreId(storeId, radiusInMeters);
    }


    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));
    }

}
        