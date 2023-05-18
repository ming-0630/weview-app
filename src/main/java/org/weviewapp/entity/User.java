package org.weviewapp.entity;

import jakarta.persistence.*;
import lombok.*;

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
        private @Getter @Setter UUID id;

        @Column(name="user_email")
        private @Getter @Setter String email;

        @Column(name="user_username")
        private @Getter @Setter String username;

        @Column(name="user_password")
        private @Getter @Setter String password;

        @Column(name="user_image_dir")
        private @Getter @Setter String profileImageDirectory;

        @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        @JoinTable(name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
        private Set<Role> roles;
}
