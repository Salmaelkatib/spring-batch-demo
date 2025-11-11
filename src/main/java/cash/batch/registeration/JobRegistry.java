package cash.batch.registeration;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Job_Registry")
@Data
public class JobRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)"
            , nullable = false, unique = true)
    private String jobName;

    @Column(columnDefinition = "VARCHAR(255)")
    private String jobDescription;

    @Column(columnDefinition = "VARCHAR(255)", unique = true, nullable = false)
    private String path;

    @Column(columnDefinition = "VARCHAR(255)")
    private String cronExpression;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdated;

    @ManyToOne
    @JoinColumn(name = "service_id",  nullable = false)
    private ServiceRegistry serviceRegistry;

}
