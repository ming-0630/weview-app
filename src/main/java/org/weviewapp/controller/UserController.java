package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.entity.User;
import org.weviewapp.repository.UserRepository;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserRepository userRepository;
//    @GetMapping("/getProfilePic")
//    public ResponseEntity<byte[]> getImage(@RequestParam UUID id) throws MalformedURLException {
//        Optional<User> user = userRepository.findById(id);
//
//        String projectRoot = System.getProperty("user.dir");
//        String imagesFolderPath = projectRoot + "/images/";
//
//        if (user.isPresent()) {
//            try {
////                image = FileUtils.readFileToByteArray(new File(FILE_PATH_ROOT+user.get().getProfileImageDirectory()));
//                File img = new File(imagesFolderPath + user.get().getProfileImageDirectory());
//                byte[] bytes = Files.readAllBytes(img.toPath());
//                return ResponseEntity.ok().contentType(
//                        MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))
//                ).body(bytes);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
    @GetMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }
}
