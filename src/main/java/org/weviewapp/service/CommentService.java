package org.weviewapp.service;

import org.weviewapp.dto.CommentDTO;
import org.weviewapp.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    List<CommentDTO> mapToCommentDTO(List<Comment> comments);
    void deleteComment(UUID commentId);
}
