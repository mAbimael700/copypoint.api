package com.copypoint.api.domain.attachment;

import com.copypoint.api.domain.saleprofile.SaleProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attachments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ ESTO ESTÁ BIEN
    @OneToMany(mappedBy = "attachment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();  // <- Lista, no objeto singular

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private AttachmentFileType fileType;

    private Integer pages;

    private Integer copies;

    @Column(name = "uploaded_by", length = 20)
    private String uploadedBy;

    private Boolean active;


}
