package org.weviewapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name="report_reason")
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ReportReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 60)
    private String name;
}
