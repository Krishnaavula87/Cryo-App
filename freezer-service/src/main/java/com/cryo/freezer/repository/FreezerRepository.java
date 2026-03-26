package com.cryo.freezer.repository;

import com.cryo.freezer.entity.Freezer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FreezerRepository extends JpaRepository<Freezer, Long> {

    List<Freezer> findByOwnerUserId(String ownerUserId);

    Optional<Freezer> findByFreezerIdAndOwnerUserId(String freezerId, String ownerUserId);

    // new method for fallback when ownerUserId is not available
    Optional<Freezer> findByFreezerId(String freezerId);

    Optional<Freezer> findByPoNumber(String poNumber);

    boolean existsByPoNumber(String poNumber);

    // ✅ NEW: Find active freezers needed for polling
    List<Freezer> findByStatusAndS3UrlIsNotNull(Freezer.FreezerStatus status);

}


