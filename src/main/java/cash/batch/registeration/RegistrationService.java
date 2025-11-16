package cash.batch.registeration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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

    @Value("${cron.expression.students}")
    private String cronExpStudents;
    @Value("${cron.expression.teachers}")
    private String cronExpTeachers;

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
        }catch (HttpClientErrorException e) {
            log.info("Service registration failed: {}", e.getResponseBodyAsString());
        }
        catch (RestClientException e){
            log.error("Error registering service", e.getMessage());
        }
        catch (Exception e) {
            log.error("Error registering service: {}", e.getMessage());
        }
    }

    private ServiceDTO getRegisterServiceDTO() {
        String hostAddress = "localhost";

        // jobs list
        List<JobDTO> jobs = List.of(
                new JobDTO(
                        "importStudents",
                        "Process student data",
                        cronExpStudents
                ),
                new JobDTO(
                        "importTeachers",
                        "Process teacher data",
                        cronExpTeachers
                )
        );

        // Build request body
        return new ServiceDTO.ServiceDTOBuilder()
                .serviceName(serviceName)
                .ip(hostAddress)
                .serviceDescription(serviceDescription)
                .path("/" + serviceName + "/jobs/")
                .jobs(jobs)
                .port(serverPort)
                .build();
    }
}
