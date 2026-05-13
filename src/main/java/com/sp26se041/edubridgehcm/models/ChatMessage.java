package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_message")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "sender_name")
    String senderName;

    @Column(name = "receiver_name")
    String receiverName;

    @Column(name = "campus_id")
    int campusId;

    @Column(name = "school_name")
    String schoolName;

    @Column(name = "campus_name")
    String campusName;

    @Column(name = "message")
    String message;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "files")
    Object files;

    @Column(name = "conversation_id")
    Long conversationId;

    @Column(name = "timestamp",nullable = false)
    LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status;

}
