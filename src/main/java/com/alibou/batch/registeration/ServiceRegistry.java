package com.alibou.batch.registeration;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Service_Registry")
@Data
public class ServiceRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)"
            , nullable = false, unique = true)
    private String serviceName;

    @Column(columnDefinition = "VARCHAR(255)")
    private String serviceDescription;

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'localhost'")
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdated;

    @Column(unique = true,  nullable = false)
    private String port;
}
