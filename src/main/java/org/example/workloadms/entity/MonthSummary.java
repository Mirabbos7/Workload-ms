package org.example.workloadms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "month_summary")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthSummary extends BaseEntity {

    @Column(name = "month_value")
    private int month;

    @Column(name = "duration_in_minutes")
    private int durationInMinutes;
}
