package org.weviewapp.Repository;

import org.springframework.data.repository.CrudRepository;
import org.weviewapp.Entity.User;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
}
