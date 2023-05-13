package org.weviewapp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.weviewapp.repository.RoleRepository;

@Configuration
class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
    @Autowired
    RoleRepository roleRepository;

//    @Bean
//    CommandLineRunner initDatabase(UserRepository repository) {
//        Role roles = roleRepository.findByName("ROLE_USER").get();
//
//        return args -> {
//            log.info("Preloading " + repository.save(new User(UUID.randomUUID(),"user1", "Bilbo Baggins", "$2a$12$Tg/wZsB6nBJIelVzo3SebeCg.MFQlegT6/F2Naa9S5vcW3JTibLdO", Collections.singleton(roles))));
//            log.info("Preloading " + repository.save(new User(UUID.randomUUID(), "user2","Frodo Baggins", "$2a$12$Tg/wZsB6nBJIelVzo3SebeCg.MFQlegT6/F2Naa9S5vcW3JTibLdO", Collections.singleton(roles))));
//            log.info("Preloading " + repository.save(new User(UUID.randomUUID(), "user3","user", "$2a$12$Tg/wZsB6nBJIelVzo3SebeCg.MFQlegT6/F2Naa9S5vcW3JTibLdO", Collections.singleton(roles))));
//        };
//    }
}
