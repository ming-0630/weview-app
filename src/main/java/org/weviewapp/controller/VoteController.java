package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.service.VoteService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/voting")
public class VoteController {
    @Autowired
    private VoteService voteService;

    @PostMapping("/vote")
    public ResponseEntity<?> voteReview(
            @RequestParam(required = false) String reviewId,
            @RequestParam(required = false) String commentId,
            @RequestParam String userId,
            @RequestParam Integer voteType
    ) {
        if(reviewId.isEmpty() && !commentId.isEmpty()) {
            voteService.vote(VoteOn.COMMENT, UUID.fromString(commentId), UUID.fromString(userId), VoteType.values()[voteType]);
        }

        if(!reviewId.isEmpty() && commentId.isEmpty()) {
            voteService.vote(VoteOn.REVIEW, UUID.fromString(reviewId), UUID.fromString(userId), VoteType.values()[voteType]);
        }

        if(reviewId.isEmpty() && commentId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing param! Must have reviewId OR commentId.");
        }

        if(!reviewId.isEmpty() && !commentId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error! Both commentId and reviewId are present.");
        }

        Map<String, Object> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getVotes")
    public int getVotes(  @RequestParam(required = false) String reviewId,
                          @RequestParam(required = false) String commentId) {

        if(reviewId.isEmpty() && !commentId.isEmpty()) {
            UUID id = UUID.fromString(commentId);
            return voteService.getTotalUpvotes(VoteOn.COMMENT, id) - voteService.getTotalDownvotes(VoteOn.COMMENT, id);
        }

        if(!reviewId.isEmpty() && commentId.isEmpty()) {
            UUID id = UUID.fromString(reviewId);
            return voteService.getTotalUpvotes(VoteOn.REVIEW, id) - voteService.getTotalDownvotes(VoteOn.REVIEW, id);
        }

        if(reviewId.isEmpty() && commentId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing param! Must have reviewId OR commentId.");
        }

        if(!reviewId.isEmpty() && !commentId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error! Both commentId and reviewId are present.");
        }
        return 0;
    }

}
