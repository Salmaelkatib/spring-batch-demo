package cash.batch.registeration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceDTO {
    private String serviceName;
    private String serviceDescription;
    private String ip;
    private Integer port;
    List<JobDTO> jobs;
}
