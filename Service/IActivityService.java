package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.Entity.Activity;
import com.example.carecareforeldres.Entity.LikeDisliketRate;
import com.example.carecareforeldres.Entity.TypeActivity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IActivityService {
    List<Activity> retrieveAllActivity();

    Activity addActivity(Activity a);

    Activity updateActivity(Activity a);

    Activity retrieveActivity(Long idActivity);

    void removeActivity(Long idActivity);


    ResponseEntity<?> registerPatientToActivity(Long idActivity, int idPatient);

    ResponseEntity<?> registerOrganisateurToActivity(Activity activity, int idOrganisateur);
    public Map<LocalDate, Map<TypeActivity, Long>> getQualityTrend() ;

    public void reactToActivity(Long activityId, int patientId, LikeDisliketRate reactionType) ;
//    Map<Activity, Integer> assignRatingsAndBadges();

  //  List<Activity> getTopThreeActivitiesByBadge();
    }
