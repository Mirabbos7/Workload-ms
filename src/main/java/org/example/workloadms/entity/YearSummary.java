package org.example.workloadms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "year_summary")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class YearSummary extends BaseEntity {

    @Column(name = "year_value")
    private int year;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "year_id")
    private List<MonthSummary> monthSummary;

}


