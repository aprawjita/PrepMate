package com.example.PrepMate.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "course_units")
public class CourseUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitName;

    // This amazing annotation tells Spring Boot to automatically 
    // create a hidden side-table just to hold your list of topics!
    @ElementCollection
    @CollectionTable(name = "unit_topics", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "topic_name", length = 500) // length=500 just in case AI gives a long topic name
    private List<String> topics;

    // Empty constructor needed by Spring Boot
    public CourseUnit() {}

    public CourseUnit(String unitName, List<String> topics) {
        this.unitName = unitName;
        this.topics = topics;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
}