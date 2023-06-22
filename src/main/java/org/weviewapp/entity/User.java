package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
        @Id
        @Column(name="user_id")
        private UUID id;

        @Column(name="email")
        private String email;

        @Column(name="username")
        private String username;

        @Column(name="password")
        private String password;

        @Column(name="image_dir")
        private String profileImageDirectory;

        @Column(name="is_verified")
        private Boolean isVerified;

        @Column(name="points")
        private Integer points;

        @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        @JoinTable(name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
        private Set<Role> roles;

        @JsonManagedReference
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        @Nullable
        @OneToMany(mappedBy = "user")
        private List<Vote> votes = new ArrayList<>();

        @JsonManagedReference
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        @Nullable
        @OneToMany(mappedBy = "user")
        private List<Comment> comments = new ArrayList<>();
}
