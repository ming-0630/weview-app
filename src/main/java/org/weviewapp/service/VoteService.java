package org.weviewapp.service;

import org.weviewapp.entity.Vote;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;

import java.util.UUID;

public interface VoteService {
    public Vote vote(VoteOn voteOn, UUID id, UUID userId, VoteType voteType);

    public VoteType getCurrentUserVote(VoteOn voteOn, UUID id, UUID userId);

    public int getTotalUpvotes(VoteOn voteOn, UUID id);

    public int getTotalDownvotes(VoteOn voteOn, UUID id);

}
