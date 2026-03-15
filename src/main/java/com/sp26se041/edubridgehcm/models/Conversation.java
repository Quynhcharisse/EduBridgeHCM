package com.sp26se041.edubridgehcm.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status;

    @Column(name = "parent_email")
    String parentEmail;

    @Column(name = "counsellor_email")
    String counsellorEmail;
}
