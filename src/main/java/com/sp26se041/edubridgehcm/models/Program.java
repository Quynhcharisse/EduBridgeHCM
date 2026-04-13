package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.FeeUnit;
import com.sp26se041.edubridgehcm.enums.LanguageInstruction;
import com.sp26se041.edubridgehcm.enums.ProgramCategory;
import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "program")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @ManyToOne
    @JoinColumn(name = "curriculum_id")
    Curriculum curriculum;

    // --- NHÓM TRƯỜNG PHỤC VỤ SEARCH & SO SÁNH (Ý THẦY) --

    @Column(name = "graduation_standard", columnDefinition = "TEXT")
    String graduationStandard;

    @Column(name = "language_of_instruction", columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    LanguageInstruction languageOfInstruction; // Vietnamese, English, Bilingual

    @Column(name = "program_category")
    @Enumerated(EnumType.STRING)
    ProgramCategory programCategory;

    // --- NHÓM TÀI CHÍNH GỐC ---

    @Column(name = "base_tuition_fee")
    BigDecimal baseTuitionFee;

    @Column(name = "fee_unit")
    @Enumerated(EnumType.STRING)
    FeeUnit feeUnit; // "Year", "Semester" -> Để so sánh "giá theo năm"

    // --- QUẢN TRỊ ---
    @Column(name = "target_student_description", columnDefinition = "TEXT")
    String targetStudentDescription;

    @Enumerated(EnumType.STRING)
    Status status;

    @OneToMany(mappedBy = "program")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CampusProgramOffering> campusProgramOfferingList;
}
