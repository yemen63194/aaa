package com.example.carecareforeldres.Repository;

import com.example.carecareforeldres.Entity.Plat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatRepository extends JpaRepository<Plat,Integer> {
    @Query("SELECT COALESCE(SUM(i.calorie), 0) FROM Ingredient i JOIN i.plats p JOIN p.repas r WHERE r.patient.idpatient = :userId ")
    float calculateCaloriesConsumedByUserToday(@Param("userId") Integer userId);

    @Query("SELECT p FROM Plat p WHERE p.cuisinier.restaurantC.idRestaurant = :restoId ")
    List<Plat> listePlatRestaurantPar(@Param("restoId") Integer restoId);


}
