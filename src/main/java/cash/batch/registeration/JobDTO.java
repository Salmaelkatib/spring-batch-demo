package cash.batch.registeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
    private String jobName;
    private String jobDescription;
    private String path;  //to trigger a job
    private String cronExpression;
}
