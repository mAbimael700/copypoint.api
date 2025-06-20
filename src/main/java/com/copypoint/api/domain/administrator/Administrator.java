package com.copypoint.api.domain.administrator;

import com.copypoint.api.domain.administratorRole.AdministratorRole;
import com.copypoint.api.domain.role.Role;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "administrators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrator {

    @EmbeddedId
    private AdministratorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AdministratorStatus status;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Builder.Default
    @OneToMany(mappedBy = "administrator", fetch = FetchType.LAZY)
    private List<AdministratorRole> administratorRoles = new ArrayList<>();
}
