package cash.batch.registeration;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ServiceDTO {
    private String serviceName;
    private String serviceDescription;
    private String ip;
    private Integer port;
    private String path;
    private List<JobDTO> jobs;
}
