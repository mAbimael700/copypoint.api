package com.copypoint.api.domain.role;

import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreUserRole {

    @EmbeddedId
    private UserStoreRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;



}
