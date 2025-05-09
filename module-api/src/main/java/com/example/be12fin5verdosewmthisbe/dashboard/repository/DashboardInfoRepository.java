package com.example.be12fin5verdosewmthisbe.dashboard.repository;

import com.example.be12fin5verdosewmthisbe.dashboard.model.DashboardInfo;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardInfoRepository extends JpaRepository<DashboardInfo, Long> {
    Optional<DashboardInfo> findByStoreIdAndStatus(Long storeId, DashboardInfo.InfoStatus status);
    Optional<DashboardInfo> findByStoreAndStatus(Store store, DashboardInfo.InfoStatus status);
}
