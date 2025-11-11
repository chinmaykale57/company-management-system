package com.example.sellerhelp.tool.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tool_category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Tool> tools = new ArrayList<>();
}