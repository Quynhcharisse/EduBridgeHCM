package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.enums.Status;
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

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "hash_tag_json")
    Object hashTag;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "content_json")
    Object content;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "image_json")
    Object imageJson;

    String thumbnail;

    int totalPosition; // vị trí của hiển thị
    // ==> như là :FE sẽ cho người nhập post cho lựa trọn hiển thị vị trí

    @Column(name = "type_file")
    String typeFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_post")
    CategoryPost categoryPost;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "published_date")
    LocalDateTime publishedDate;

    @ManyToOne
    @JoinColumn(name = "author_id")
    Account author;
}

