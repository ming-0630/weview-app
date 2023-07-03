package org.weviewapp.service;

import org.weviewapp.entity.Vote;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;

import java.util.UUID;

public interface VoteService {
    Vote vote(VoteOn voteOn, UUID id, UUID userId, VoteType voteType);
    VoteType getCurrentUserVote(VoteOn voteOn, UUID id, UUID userId);
    int getTotalUpvotes(VoteOn voteOn, UUID id);
    int getTotalDownvotes(VoteOn voteOn, UUID id);
    int getUserTotalUpvotes(UUID userId);
    int getUserTotalDownvotes(UUID userId);

}
