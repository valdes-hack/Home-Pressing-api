package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    @Query(value = "SELECT * FROM client WHERE tel_client = fn_normaliser_telephone(:tel)", nativeQuery = true)
    Optional<Client> findByTelephoneNormalise(@Param("tel") String tel);

    List<Client> findTop20ByTelClientContainingOrNomClientContainingIgnoreCaseOrderByNomClientAsc(String tel, String nom);

    List<Client> findTop50ByOrderByNomClientAsc();
    Optional<Client> findByNomClientIgnoreCase(String nomClient);
    Optional<Client> findByTelClient(String telClient);
}
