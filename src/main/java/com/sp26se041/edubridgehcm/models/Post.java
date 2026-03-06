package com.sp26se041.edubridgehcm.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "post")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(name = "content_body")
    String contentBody;

    @Column(name = "category")
    String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_scope")
    String targetScope;

    int priority; //Dùng để sắp xếp độ ưu tiên hiển thị

    @Column(name = "is_pinned")
    boolean isPinned;

    @Column(name = "published_date")
    LocalDateTime publishedDate;

    @Column(name = "is_active")
    Boolean isActive; //Cho phép ẩn hiện bài viết

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "image_json")
    String imageJson;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
}

