package cash.batch.registeration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRegistryRepository extends JpaRepository<ServiceRegistry, Long> {
    ServiceRegistry findByServiceName(String serviceName);
}
