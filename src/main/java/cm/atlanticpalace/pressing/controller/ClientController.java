package cm.atlanticpalace.pressing.controller;

import cm.atlanticpalace.pressing.domain.entity.Client;
import cm.atlanticpalace.pressing.repository.ClientRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public record CreerClientRequest(
            @NotBlank(message = "Le nom du client est obligatoire") String nomClient,
            @NotBlank(message = "Le numéro de téléphone à 9 chiffres est obligatoire") String telClient
    ) {}

    @GetMapping
    public List<Client> lister() {
        return clientRepository.findTop50ByOrderByNomClientAsc();
    }

    @GetMapping("/recherche")
    public List<Client> rechercher(@RequestParam("q") String query) {
        String q = query.trim();
        return clientRepository.findByTelephoneNormalise(q)
                .map(List::of)
                .orElseGet(() -> clientRepository
                        .findTop20ByTelClientContainingOrNomClientContainingIgnoreCaseOrderByNomClientAsc(q, q));
    }

    @PostMapping
    public Client creer(@Valid @RequestBody CreerClientRequest request) {
        Client client = new Client();
        client.setNomClient(request.nomClient().trim());
        client.setTelClient(request.telClient().trim());
        client.setPointsFidelite(0);
        // Le trigger trg_client_before_insert_tel normalise automatiquement en +237XXXXXXXXX (RG9, CI2)
        return clientRepository.save(client);
    }
}
