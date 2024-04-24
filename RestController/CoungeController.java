package com.example.carecareforeldres.RestController;

import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.CoungeRepository;
import com.example.carecareforeldres.Repository.CuisinierRepository;
import com.example.carecareforeldres.Service.CoungeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/counge")
@CrossOrigin("*")
public class CoungeController {
    CoungeService coungeService;
    CoungeRepository coungeRepository;
    CuisinierRepository cuisinierRepository;

    @PutMapping("/{id}/etat")
    public void updateCoungeEtat(@PathVariable("id") Integer id, @RequestBody EtatCounger newEtat) {
        Logger.getLogger("//////////////////////");
        coungeService.updateCoungeEtat(id, newEtat);
    }

    @PostMapping("/add")
    public Counge ajouterCounge(@RequestBody Counge res) {
        Counge p1 = coungeService.add(res);
        return p1;
    }

    @GetMapping("/retrive_all_counge")
    public Map<String, Object> retrieveCoungeList() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Voici la liste");
        response.put("coungeList", coungeService.getAll());
        return response;
    }


    @GetMapping("/retrive_counge/{resId}")
    public Counge retrieveCounge(@PathVariable("resId") Integer resId) {

        return coungeRepository.findById(resId).get();
    }

    @PutMapping("/update_counge/{idCuisinier}")
    public Counge updateCounge(@RequestBody Counge counge,
                               @PathVariable("idCuisinier") Integer idCuisinier) {

        return coungeService.update(counge, idCuisinier);
    }

    @DeleteMapping("/delete_counge/{coungeId}")
    public void deleteCounge(@PathVariable("coungeId") Integer coungeId) {
        coungeService.remove(coungeId);
    }

    public boolean aPrisCongeDerniersQuatreMois(List<Counge> congés) {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime ilYaQuatreMois = maintenant.minusMonths(2);

        for (Counge congé : congés) {
            LocalDate dateDebutConge = congé.getDateDebut();
            LocalDateTime dateDebutLocalDateTime = dateDebutConge.atStartOfDay(); // Convertir LocalDate en LocalDateTime en ajoutant une heure arbitraire

            if (dateDebutLocalDateTime.isAfter(ilYaQuatreMois) && dateDebutLocalDateTime.isBefore(maintenant)) {
                return true;
            }
        }

        return false;
    }

    @PostMapping("/DemandeCoungeCuisine/{idCuisinier}")
    public ResponseEntity<?> DemandeCoungeCuisine(@PathVariable("idCuisinier") Integer CuisinierId, @RequestBody Counge counge) {
        try {
            Cuisinier cuisinier = cuisinierRepository.findById(CuisinierId).orElseThrow(() -> new NotFoundException("Cuisinier non trouvé"));
            counge.setEtatCounger(EtatCounger.EN_COUR);
            counge.setCuisinierC(cuisinier);
            counge.setDateAjout(LocalDate.now());
            List<Counge> congesDuCuisinier = cuisinier.getCounges();

            boolean aPrisConge;
            try {
                aPrisConge = aPrisCongeDerniersQuatreMois(congesDuCuisinier);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la vérification des congés du cuisinier");
            }

            LocalDate dateDebut;
            LocalDate dateFin;
            LocalDate currentDate = LocalDate.now();
            try {
                dateDebut = counge.getDateDebut();
                dateFin = counge.getDateFin();
                if (dateDebut == null || dateFin == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La date de début ou la date de fin du congé est null.");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la récupération des dates de congé");
            }

            int joursCongesCetteAnnee;
            int dureeNouvelleConge;
            try {
                joursCongesCetteAnnee = coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate);
                dureeNouvelleConge = (int) ChronoUnit.DAYS.between(dateDebut, dateFin);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du calcul de la durée des congés");
            }

            long differenceEnJours = ChronoUnit.DAYS.between(currentDate, dateDebut);

            if (counge.getDateFin().isBefore(counge.getDateDebut())) {
                if (differenceEnJours < 3) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La date de début du congé doit être au moins 3 jours après la date actuelle.");
                }
              //  return new ResponseEntity<>("La date de fin est antérieure à la date de début.", HttpStatus.BAD_REQUEST);

               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La date de fin est antérieure à la date de début.");
            } else {
                if (!aPrisConge) {
                    if (counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE) && cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUNGER_MATERNITE) == 0) {
                        coungeRepository.save(counge);
                        return ResponseEntity.ok("Coungé Ajouter avec succès");
                    }
                    if (counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUGER_PARENTALE) == 0) {
                        coungeRepository.save(counge);
                        return ResponseEntity.ok("Coungé Ajouter avec succès");
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tu a déjà pris un congé récemment");
                }

                if (joursCongesCetteAnnee + dureeNouvelleConge > 30) {
                    if (counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE) && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUNGER_MATERNITE) == 0) {
                        if (cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45) {
                            coungeRepository.save(counge);
                            return ResponseEntity.ok("Coungé Ajouter avec succès");
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le congé de maternité est limité à 45 jours pour les femmes");
                        }
                    }
                    if (counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUGER_PARENTALE) == 0) {
                        coungeRepository.save(counge);
                        return ResponseEntity.ok("Coungé Ajouter avec succès");
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le cuisinier aura plus de 30 jours de congé cette année après l'ajout de cette nouvelle demande de congé.");
                }

                coungeRepository.save(counge);
                return ResponseEntity.ok("Coungé Ajouter avec succès");
            }
        } catch (ErrorResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
}
}
