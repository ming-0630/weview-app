package org.weviewapp.dto;

import lombok.*;
import org.weviewapp.enums.VoteType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private UUID commentId;
    private String text;
    private UserDTO user;
    private LocalDateTime dateCreated;
    private Integer votes;
    private VoteType currentUserVote;
}
