package org.weviewapp.configuration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.entity.Role;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ProductCategory;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.RoleRepository;
import org.weviewapp.repository.UserRepository;

import java.time.Year;
import java.util.*;

@Component
@RequiredArgsConstructor
public class InitDatabase implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(InitDatabase.class);
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Override
    public void run(String... args) throws Exception {
        // Initialize Role
        if (roleRepository.count() <= 0) {
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_USER").build()));
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
                log.info("Preloading " + roleRepository.save(Role.builder().name("ROLE_BUSINESS").build()));
        }

        // Initialize User
        Role roles = roleRepository.findByName("ROLE_USER").get();
        if (userRepository.count() <= 0) {
                log.info("Preloading " + userRepository.save(new User(
                        UUID.randomUUID(),
                        "megumi",
                        "Megumi Katou",
                        "$2a$12$Tg/wZsB6nBJIelVzo3SebeCg.MFQlegT6/F2Naa9S5vcW3JTibLdO",
                        "ProfilePic_6f47c27e-2679-4890-b830-52c6bcd9c9ec.jpg",
                        Collections.singleton(roles)
                )));
            }

        // Initialize Product
        if (productRepository.count() <= 0) {
            ProductCategory[] categories = ProductCategory.values();
            Random random = new Random();
            for(int i = 0; i <= 50; i++ ) {
                Product p = new Product();

                p.setId(UUID.randomUUID());
                char c = (char)(random.nextInt(26) + 'a');
                p.setName(c + ": Test Product " + i);

                p.setCategory(ProductCategory.SMARTPHONES);
                p.setCategory(categories[random.nextInt(1, categories.length)]);
                p.setDescription("Description is here!");

                int currentYear = Year.now().getValue();
                int randomYear = random.nextInt(currentYear - 2000 + 1) + 2000;
                p.setReleaseYear(Year.parse(String.valueOf(randomYear)));

                ProductImage newImage = new ProductImage();
                newImage.setId(UUID.randomUUID());
                newImage.setProduct(p);

                switch (p.getCategory()) {
                    case MUSIC -> {
                        List<String> musicImage = new ArrayList<>();
                        musicImage.add("PRODUCT_IMG_9d8c272a-f1b5-4bb0-bac7-0dc76d88bc65.png");
                        musicImage.add("PRODUCT_IMG_fc772fcc-88f7-40a1-9cb3-9f2edd67a841.png");
                        musicImage.add("PRODUCT_IMG_70b8d218-72ba-4bec-98fa-69ce70801750.png");

                        newImage.setImageDirectory(musicImage.get(random.nextInt(musicImage.size())));
                        p.getImages().add(newImage);
                    }
                    case COMPUTERS -> {
                        List<String> computerImage = new ArrayList<>();
                        computerImage.add("PRODUCT_IMG_f60dcc58-3230-442d-933c-67854bfbb061.png");
                        computerImage.add("PRODUCT_IMG_3132d2f2-be5e-42b5-b6c5-36fd5bb60bc2.png");
                        computerImage.add("PRODUCT_IMG_c1c0dc33-13f0-4ccf-89cf-cf04a85662f1.png");

                        newImage.setImageDirectory(computerImage.get(random.nextInt(computerImage.size())));
                        p.getImages().add(newImage);
                    }
                    case SMARTPHONES -> {
                        List<String> smartphoneImage = new ArrayList<>();
                        smartphoneImage.add("PRODUCT_IMG_86b37a2d-5f27-4df9-a73c-92ebe4146a71.png");
                        smartphoneImage.add("PRODUCT_IMG_ab471e8c-c1f9-4baf-963a-3d1d4e386278.png");
                        smartphoneImage.add("PRODUCT_IMG_c00a724f-7fbc-4a65-a762-8a4d93e011dd.png");

                        newImage.setImageDirectory(smartphoneImage.get(random.nextInt(smartphoneImage.size())));
                        p.getImages().add(newImage);
                    }
                    case HOMEAPPLIANCES -> {
                        List<String> homeImage = new ArrayList<>();
                        homeImage.add("PRODUCT_IMG_d6f6470d-d106-4625-aaeb-86b3e528582c.png");
                        homeImage.add("PRODUCT_IMG_0520b7ad-8c1a-4620-a69a-406b496786a6.png");
                        homeImage.add("PRODUCT_IMG_53f4ed65-80af-4e6d-be8d-908acd7f22cb.png");

                        newImage.setImageDirectory(homeImage.get(random.nextInt(homeImage.size())));
                        p.getImages().add(newImage);
                    }
                }
                productRepository.save(p);
            }
            log.info("Preloaded random 50 products");
        }
        }
}
