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

import java.util.List;

@RequiredArgsConstructor
@Service
public class StoreService {
    // Your code here
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

    public List<Long> getNearbyStoreIds(Long storeId) {
        Store target = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게가 존재하지 않습니다."));

        double lat = target.getLatitude();
        double lng = target.getLongitude();

        double latDiff = kmToLat(RADIUS_KM);
        double lngDiff = kmToLng(RADIUS_KM, lat);

        double minLat = lat - latDiff;
        double maxLat = lat + latDiff;
        double minLng = lng - lngDiff;
        double maxLng = lng + lngDiff;

        return storeRepository.findNearbyStoreIds(storeId, minLat, maxLat, minLng, maxLng);
    }

    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));
    }

    private double kmToLat(double km) {
        return km / 111.0;
    }

    private double kmToLng(double km, double latitude) {
        return km / (111.320 * Math.cos(Math.toRadians(latitude)));
    }

}
        