package org.weviewapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.weviewapp.enums.ReportAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name="report")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Report {
    @Id
    @Column(name="report_id")
    private UUID id;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "report_report_reasons",
            joinColumns = @JoinColumn(name = "report_id", referencedColumnName = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "report_reason_id", referencedColumnName = "id"))
    private List<ReportReason> reportReasons;

    @Column(name="description")
    private String description;

    @OneToOne
    @JoinColumn(name = "review_id", referencedColumnName = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User reporter;

    @Column(name="action")
    private ReportAction action;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_updated", nullable = false)
    private LocalDateTime dateUpdated;

    @PrePersist
    protected void onCreate() {
        if(dateCreated == null) {
            dateUpdated = dateCreated = LocalDateTime.now();
            return;
        }
        dateUpdated = dateCreated;
    }

    @PreUpdate
    protected void onUpdate() {
        dateUpdated = LocalDateTime.now();
    }

}


