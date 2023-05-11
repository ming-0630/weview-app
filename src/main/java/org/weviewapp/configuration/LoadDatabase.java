package org.weviewapp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weviewapp.Entity.User;
import org.weviewapp.Repository.UserRepository;

import java.util.UUID;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new User(UUID.randomUUID(), "Bilbo Baggins", "burglar")));
            log.info("Preloading " + repository.save(new User(UUID.randomUUID(), "Frodo Baggins", "thief")));
        };
    }
}
