package com.apimock.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Project {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
