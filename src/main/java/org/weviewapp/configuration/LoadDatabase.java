package org.weviewapp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.entity.Role;
import org.weviewapp.entity.User;
import org.weviewapp.repository.ProductImageRepository;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.RoleRepository;
import org.weviewapp.repository.UserRepository;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Configuration
class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductImageRepository productImageRepository;

    @Bean
    CommandLineRunner initDatabase() {
        List<Runnable> initializationTasks = new ArrayList<>();

        // Initialize Role
        if (roleRepository.count() <= 0) {
            initializationTasks.add(() -> {
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_USER").build()));
            });
            initializationTasks.add(() -> {
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
            });
            initializationTasks.add(() -> {
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_BUSINESS").build()));
            });
        }

        // Initialize User
        Role roles = roleRepository.findByName("ROLE_USER").get();
        if (userRepository.count() <= 0) {
            initializationTasks.add(() -> {
                log.info("Preloading " + userRepository.save(new User(
                        UUID.randomUUID(),
                        "megumi",
                        "Megumi Katou",
                        "$2a$12$Tg/wZsB6nBJIelVzo3SebeCg.MFQlegT6/F2Naa9S5vcW3JTibLdO",
                        "ProfilePic_6f47c27e-2679-4890-b830-52c6bcd9c9ec.jpg",
                        Collections.singleton(roles)
                )));
            });
        }

        // Initialize Product
        if (productRepository.count() <= 0) {
            UUID id = UUID.randomUUID();
            Product p = Product.builder()
                    .id(id)
                    .name("Test Product")
                    .category(Product.Category.SMARTPHONES)
                    .description("Description is here!")
                    .releaseYear(Year.parse("2018"))
                    .build();

            initializationTasks.add(() -> {
                log.info("Preloading " + productRepository.save(p));
            });

            // Initialize Product Images
            if (productImageRepository.count() <= 0) {
                initializationTasks.add(() -> {
                    log.info("Preloading " + productImageRepository.save(
                            new ProductImage(
                                    UUID.randomUUID(),
                                    p,
                                    "PRODUCT_IMG_86b37a2d-5f27-4df9-a73c-92ebe4146a71.png"
                            )
                    ));
                });
            }
        }

        return args -> {
            for (Runnable task : initializationTasks) {
                task.run();
            }
        };
    }
}
