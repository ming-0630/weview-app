package org.weviewapp.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class User {
        @Id
        private @Getter @Setter UUID userId;
        private @Getter @Setter String userEmail;
        private @Getter @Setter String userPassword;
}
