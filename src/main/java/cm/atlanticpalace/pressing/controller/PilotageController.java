package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.repository.PressingStoredProcedureRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pilotage/dg")
@PreAuthorize("hasRole('DG')")
public class PilotageController {

    private final PressingStoredProcedureRepository spRepository;

    public PilotageController(PressingStoredProcedureRepository spRepository) {
        this.spRepository = spRepository;
    }

    @GetMapping("/rapport-consolide")
    public List<Map<String, Object>> rapportConsolide(
            @RequestParam(value = "dateDebut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(value = "dateFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) {
        LocalDate debut = (dateDebut != null) ? dateDebut : LocalDate.now().minusDays(30);
        LocalDate fin = (dateFin != null) ? dateFin : LocalDate.now();

        return spRepository.rapportPilotageDg(debut, fin);
    }
}
