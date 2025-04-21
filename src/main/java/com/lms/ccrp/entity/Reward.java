package com.lms.ccrp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String rewardDescription;

    private long numberOfPoints;

    private String imgUrl;

    private boolean isActive;
}
