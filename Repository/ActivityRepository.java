package com.example.carecareforeldres.Repository;

import com.example.carecareforeldres.Entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity,Long> {
    @Query("SELECT a.dateActivity, a.typeActivity, COUNT(a) FROM Activity a WHERE a.dateActivity BETWEEN :startDate AND :endDate GROUP BY a.dateActivity, a.typeActivity")
    List<Object[]> findQualityTrendByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);



}




