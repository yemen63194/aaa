package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.*;
import com.example.carecareforeldres.demo.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityService implements  IActivityService {
    ActivityRepository activityRepository;
    PatientRepository patientRepository;

    UserRepository userRepository;

    EmailService emailService;

    LikeDislikeActivityRepository likeDislikeActivityRepository;

    OrganisateurRepository organisateurRepository;
    @Override
    public List<Activity> retrieveAllActivity() {
        return activityRepository.findAll();
    }

    @Override
    public Activity addActivity(Activity s) {
        return activityRepository.save(s);
    }

    @Override
    public Activity updateActivity(Activity s) {
        return activityRepository.save(s);
    }

    @Override
    public Activity retrieveActivity(Long idActivity) {
        return activityRepository.findById(idActivity).get();
    }

    @Override
    public void removeActivity(Long idActivity) {
        activityRepository.deleteById(idActivity);
    }

    @Override
    public ResponseEntity<?> registerPatientToActivity(Long idActivity, int idPatient) {
        Activity activity = activityRepository.findById(idActivity).get();
        Patient patient = patientRepository.findById(idPatient).get();
        int idH = patient.getUser();
        User user = userRepository.findById(idH).orElse(null);

        if(activity.getPatienttts().contains(patient)){
            throw new RuntimeException("Vous êtes déjà inscrit à cette activité");
        }
        activity.getPatienttts().add(patient);
        activityRepository.save(activity);

        String userEmail = user.getEmail();
        log.info("////////////////"+userEmail);
        String subject = "Confirmation d'inscription a l'activité";
        String message = "Vous avez été inscrit avec succès a activite. Merci!";
        try {
            emailService.sendEmail(userEmail, subject, message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
            // Gérer l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'e-mail de confirmation.");
        }
        return ResponseEntity.ok().body("{\"message\": \"Votre demande a été bien prise en compte, un Email de confirmation vous a été envoyé.\"}");
    }





    @Override
    public ResponseEntity<?> registerOrganisateurToActivity(Activity activity, int idOrganisateur) {
        Organisateur organisateur = organisateurRepository.findById(idOrganisateur).get();
        int idH = organisateur.getUser();
        User user = userRepository.findById(idH).orElse(null);
        activity.setOrganisateur(organisateur);
        activityRepository.save(activity);
        String userEmail = user.getEmail();
        log.info("////////////////"+userEmail);
        String subject = "Confirmation d'inscription a l'activité";
        String message = "Vous avez été inscrit avec succès a activite. Merci!";
        try {
            emailService.sendEmail(userEmail, subject, message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
            // Gérer l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'e-mail de confirmation.");
        }
        return ResponseEntity.ok().body("{\"message\": \"Votre demande d'organisation a été bien prise en compte, un Email de confirmation vous a été envoyé.\"}");

    }




    // @Override
    // public Activity registerPatientToActivity(Long idActivity, int idPatient) {
    //  Activity activity = activityRepository.findById(idActivity).get();
    //  Patient patient = patientRepository.findById(idPatient).get();

    //  activity.setPatienttt(patient);
    // activityRepository.save(activity);

    //   return activity;
    // }




    @Override
    public Map<LocalDate, Map<TypeActivity, Long>> getQualityTrend() {
        LocalDate startDate = LocalDate.now().minusDays(300000); // Par exemple, obtenir la tendance pour les 30 derniers jours
        LocalDate endDate = LocalDate.now();

        Map<LocalDate, Map<TypeActivity, Long>> qualityTrend = new HashMap<>();
        List<Object[]> qualityData = activityRepository.findQualityTrendByDateRange(startDate, endDate);

        for (Object[] data : qualityData) {
            LocalDate entryDate = (LocalDate) data[0];
            TypeActivity typeActivity = (TypeActivity) data[1];
            Long activityCount = (Long) data[2]; // Nombre d'occurrences de cette activité pour cette date

            if (!qualityTrend.containsKey(entryDate)) {
                qualityTrend.put(entryDate, new HashMap<>());
            }

            Map<TypeActivity, Long> qualityCounts = qualityTrend.get(entryDate);
            qualityCounts.put(typeActivity, activityCount);
        }

        return qualityTrend;
    }

    @Override
    public void reactToActivity(Long activityId, int patientId, LikeDisliketRate reactionType) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        Patient patient = patientRepository.findById(patientId).orElse(null);

        if (activity != null && patient != null) {
            LikeDislikeActivity existingReaction = likeDislikeActivityRepository.findByActivityAndPatient(activity, patient);

            if (existingReaction != null) {
                if (existingReaction.getReactionType() == reactionType) {
                    return;
                } else {
                    // Si le patient change sa réaction, mettre à jour le compteur
                    if (reactionType == LikeDisliketRate.LIKE) {
                        activity.setLikeActivity(activity.getLikeActivity() + 1);
                        activity.setDislikeActivity(activity.getDislikeActivity() - 1);
                    } else {
                        activity.setLikeActivity(activity.getLikeActivity() - 1);
                        activity.setDislikeActivity(activity.getDislikeActivity() + 1);
                    }
                    existingReaction.setReactionType(reactionType);
                    likeDislikeActivityRepository.save(existingReaction);
                }
            } else {
                LikeDislikeActivity newReaction = new LikeDislikeActivity();
                newReaction.setActivity(activity);
                newReaction.setPatient(patient);
                newReaction.setReactionType(reactionType);
                likeDislikeActivityRepository.save(newReaction);

                if (reactionType == LikeDisliketRate.LIKE) {
                    activity.setLikeActivity(activity.getLikeActivity() + 1);
                } else {
                    activity.setDislikeActivity(activity.getDislikeActivity() + 1);
                }
            }
            activityRepository.save(activity);
            patientRepository.save(patient);

        }
    }




}
