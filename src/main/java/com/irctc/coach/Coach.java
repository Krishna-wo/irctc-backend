package com.irctc.coach;

import com.irctc.train.Train;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coaches",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"train_id", "coach_number"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "coach_number", nullable = false, length = 10)
    private String coachNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false)
    private CoachType coachType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
}