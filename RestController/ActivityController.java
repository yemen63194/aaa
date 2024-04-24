package com.example.carecareforeldres.RestController;

import com.example.carecareforeldres.Entity.Activity;
import com.example.carecareforeldres.Entity.LikeDisliketRate;
import com.example.carecareforeldres.Entity.TypeActivity;
import com.example.carecareforeldres.Service.IActivityService;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/Activity")
public class ActivityController {
    IActivityService iActivityService;
    @GetMapping("/allActivity")
    public List<Activity> getActivity() {
        List<Activity> listActivitys = iActivityService.retrieveAllActivity();
        return listActivitys;
    }

    @GetMapping("/Activity/{id}")
    public Activity retrieveActivity(@PathVariable("id") Long id) {
        return iActivityService.retrieveActivity(id);
    }

    @PostMapping("/addActivity")
    public Activity addEtudiant(@RequestBody Activity b) {
        Activity Activity = iActivityService.addActivity(b);
        return Activity;
    }

    @PutMapping("/UpdateActivity")
    public Activity updateActivity(@RequestBody Activity e) {
        Activity Activity= iActivityService.updateActivity(e);
        return Activity;
    }

    @DeleteMapping("/RemoveActivity/{id}")
    public void removeActivity(@PathVariable("id") Long id) {
        iActivityService.removeActivity(id);
    }


    @PostMapping("/{idActivity}/{idPatient}")
    public ResponseEntity<?> registerPatientttToEvent(@PathVariable Long idActivity, @PathVariable int idPatient) {
        try {
            iActivityService.registerPatientToActivity(idActivity, idPatient);
            return ResponseEntity.ok("Inscription réussie");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/{idOrganisateur}")
    @ResponseBody
    public ResponseEntity<?> registerOrganisateurrrToActivity( @RequestBody Activity activity, @PathVariable int idOrganisateur) {
        try {
            iActivityService.registerOrganisateurToActivity(activity,idOrganisateur);
            return ResponseEntity.ok("Inscription réussie");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping ("/react/{activityId}/{patientId}")
    public ResponseEntity<String> reactToActivity(@PathVariable Long activityId, @PathVariable int patientId, @RequestParam  LikeDisliketRate reaction ) {
        try {
            iActivityService.reactToActivity(activityId, patientId, reaction);
            return ResponseEntity.ok("Réaction enregistrée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Une erreur est survenue : " + e.getMessage());
        }
    }

    @GetMapping("/qualityTrend")
    public Map<LocalDate, Map<TypeActivity, Long>> getQualityTrend() {
        return iActivityService.getQualityTrend();
    }
}
