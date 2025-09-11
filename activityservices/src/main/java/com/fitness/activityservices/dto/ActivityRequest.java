package com.fitness.activityservices.dto;

import com.fitness.activityservices.model.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;


@Data
public class ActivityRequest {

    private String id;
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String,Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
