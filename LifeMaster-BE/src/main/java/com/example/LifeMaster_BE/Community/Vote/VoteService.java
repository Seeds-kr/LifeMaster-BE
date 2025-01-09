package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteService {
    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final PollOptionRepository pollOptionRepository;

    public VoteService(PollRepository voteRepo, VoteRepository voteRepository, PollOptionRepository pollOptionRepository) {
        this.pollRepository = voteRepo;
        this.voteRepository = voteRepository;
        this.pollOptionRepository = pollOptionRepository;
    }

    public VoteEntity.Poll createPoll(String title, LocalDateTime endDate, List<String> options) {
        VoteEntity.Poll poll = new VoteEntity.Poll();
        poll.setTitle(title);
        poll.setEndDate(endDate);

        List<VoteEntity.PollOption> pollOptions = options.stream()
                .map(content -> {
                    VoteEntity.PollOption option = new VoteEntity.PollOption();
                    option.setContent(content);
                    option.setPoll(poll);
                    return option;
                }).collect(Collectors.toList());

        poll.setOptions(pollOptions);
        return pollRepository.save(poll);
    }

    public void castVote(Long pollId, Long optionId, String userId) {
        if (voteRepository.existsByPollIdAndUserId(pollId, userId)) {
            throw new IllegalArgumentException("이미 투표한 사용자입니다.");
        }

        VoteEntity.PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("투표 항목이 존재하지 않습니다."));

        option.setVotes(option.getVotes() + 1);
        pollOptionRepository.save(option);

        VoteEntity.Vote vote = new VoteEntity.Vote();
        vote.setPoll(option.getPoll());
        vote.setUserId(userId);
        voteRepository.save(vote);
    }

    public Map<String, Double> getPollResults(Long pollId) {
        List<VoteEntity.PollOption> options = pollOptionRepository.findByPollId(pollId);
        int totalVotes = options.stream().mapToInt(VoteEntity.PollOption::getVotes).sum();

        return options.stream().collect(Collectors.toMap(
                VoteEntity.PollOption::getContent,
                option -> totalVotes == 0 ? 0.0 : (double) option.getVotes() / totalVotes * 100
        ));
    }
}
