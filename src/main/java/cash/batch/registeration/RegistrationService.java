package cash.batch.registeration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RestTemplate restTemplate;

    @Value("${orchestrator.registration.url}")
    private String serviceRegistrationUrl;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int serverPort;

    @Value("${service.description}")
    private String serviceDescription;

    @Value("${cronExp}")
    private String cronExp;

    public void registerService() {
        try {
            ServiceDTO body = getRegisterServiceDTO();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ServiceDTO> requestEntity = new HttpEntity<>(body, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                    serviceRegistrationUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            log.info("Service registration response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error registering service: {}", e.getMessage(), e);
        }
    }

    private ServiceDTO getRegisterServiceDTO() {
        String hostAddress = "localhost";

        // jobs list
        List<JobDTO> jobs = List.of(
                new JobDTO(
                        "importStudents",
                        "Process student data",
                        "/demo/jobs/importStudents/",
                        cronExp
                ),
                new JobDTO(
                        "importTeachers",
                        "Process teacher data",
                        "/demo/jobs/importTeachers/",
                        cronExp
                )
        );

        // Build request body
        return new ServiceDTO(
                serviceName,
                serviceDescription,
                hostAddress,
                serverPort,
                jobs
        );
    }
}
