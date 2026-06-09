package com.chubb.assessment.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<PolicyEntity, UUID>, JpaSpecificationExecutor<PolicyEntity> {

    // @Modifying
    // @Query("UPDATE PolicyEntity p SET p.flaggedForReview = true WHERE p.id IN :ids")
    // int flagForReview(@Param("ids") List<UUID> ids);

    @Query("SELECT p.status AS status, COUNT(p) AS total FROM PolicyEntity p GROUP BY p.status")
    List<StatusCount> countGroupedByStatus();

    @Query("SELECT p.lineOfBusiness AS lineOfBusiness, SUM(p.premiumAmount) AS total "
            + "FROM PolicyEntity p GROUP BY p.lineOfBusiness")
    List<LineOfBusinessPremium> totalPremiumGroupedByLineOfBusiness();

    long countByExpiryDateBetween(LocalDate from, LocalDate to);
}
