package com.example.LifeMaster_BE.Community.Vote;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;

    private final UserVoteRepository userVoteRepository;

    private final MemberRepository memberRepository;

    public VoteService(PollRepository voteRepo, PollOptionRepository pollOptionRepository, UserVoteRepository userVoteRepository,MemberRepository memberRepository) {
        this.pollRepository = voteRepo;
        this.pollOptionRepository = pollOptionRepository;
        this.userVoteRepository = userVoteRepository;
        this.memberRepository = memberRepository;
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

    @Transactional
    public void castVote(Long pollId, Long optionId, String userId) {
        // 🔹 0. 현재 로그인 유저
        Long currentMemberId = getCurrentMemberIdOrNull();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        MemberEntity member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 1. poll 조회
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표 항목이 존재하지 않습니다."));

        // 2. 투표 종료 여부
        if (poll.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이 투표는 이미 종료되었습니다.");
        }

        // 3. 중복 투표 체크 (UserVote 기준)
        if (userVoteRepository.existsByPoll_IdAndMember_Id(pollId, currentMemberId)) {
            throw new IllegalArgumentException("이미 투표한 사용자입니다.");
        }

        // 4. 옵션이 해당 poll에 속하는지 확인
        VoteEntity.PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("투표 옵션이 존재하지 않습니다."));

        if (!option.getPoll().getId().equals(pollId)) {
            throw new IllegalArgumentException("해당 투표 옵션은 올바른 투표에 속하지 않습니다.");
        }

        // 5. 옵션의 투표 수 증가
        option.setVotes(option.getVotes() + 1);
        pollOptionRepository.save(option);

        // 6. 🔥 UserVote 테이블에 기록 저장
        UserVote userVote = UserVote.builder()
                .member(member)
                .poll(poll)
                .option(option)
                .build();

        userVoteRepository.save(userVote);
    }

    public Map<String, Map<String, Object>> getPollResults(Long pollId) {
        // 1. 주어진 pollId에 대한 모든 옵션 가져오기
        List<VoteEntity.PollOption> options = pollOptionRepository.findByPollId(pollId);
        int totalVotes = options.stream().mapToInt(VoteEntity.PollOption::getVotes).sum();

        // 2. 결과를 Map으로 반환
        return options.stream().collect(Collectors.toMap(
                VoteEntity.PollOption::getContent,
                option -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("votes", option.getVotes()); // 투표 수
                    result.put("percentage", totalVotes == 0 ? 0.0 : (double) option.getVotes() / totalVotes * 100); // 비율
                    return result;
                }
        ));
    }

    public Map<String, Object> getPollDetails(Long pollId) {
        // 1) 투표 정보 조회
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        // 2) 만료 여부
        boolean isExpired = poll.getEndDate().isBefore(LocalDateTime.now());

        // 3) 옵션/집계
        List<VoteEntity.PollOption> options = pollOptionRepository.findByPollId(pollId);
        int totalVotes = options.stream().mapToInt(VoteEntity.PollOption::getVotes).sum();

        // (A) 현재 로그인 사용자의 투표 옵션 ID 조회 (없으면 null)
        Long currentMemberId = getCurrentMemberIdOrNull();
        Long myVotedOptionId;
        if (currentMemberId != null) {
            myVotedOptionId = userVoteRepository
                    .findMyOptionId(pollId, currentMemberId)
                    .orElse(null);
        } else {
            myVotedOptionId = null;
        }

        //LinkedHashMap으로 순서 보장
        List<Map<String, Object>> optionDetails = options.stream().map(option -> {
            Map<String, Object> optionData = new LinkedHashMap<>();
            optionData.put("optionId", option.getId());              // id
            optionData.put("content", option.getContent());          // 내용
            optionData.put("votes", option.getVotes());              // 투표 수
            optionData.put("votePercentage", totalVotes > 0
                    ? (option.getVotes() * 100.0 / totalVotes) : 0.0); // 비율

            return optionData;
        }).collect(Collectors.toList());

        // 4) 응답 구성도 순서 유지하려면 LinkedHashMap 사용
        Map<String, Object> pollDetails = new LinkedHashMap<>();
        pollDetails.put("title", poll.getTitle());
        pollDetails.put("isExpired", isExpired);
        pollDetails.put("totalVotes", totalVotes);
        pollDetails.put("options", optionDetails);

        //(B) 요구사항: myVotedOptionId 추가 (미참여 시 null)
        pollDetails.put("myVotedOptionId", myVotedOptionId);

        return pollDetails;
    }

    public VoteEntity.Poll updatePollTitle(Long pollId, String title) {
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        poll.setTitle(title);
        return pollRepository.save(poll);
    }

    public VoteEntity.PollOption updatePollOption(Long pollId, Long optionId, String content) {
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        VoteEntity.PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("투표 항목을 찾을 수 없습니다."));

        option.setContent(content);
        return pollOptionRepository.save(option);
    }

    public VoteEntity.PollOption addPollOption(Long pollId, String content) {
        // 1. 주어진 pollId로 투표를 조회
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표가 존재하지 않습니다."));

        // 2. 동일한 내용의 항목이 이미 존재하는지 확인
        boolean isDuplicate = poll.getOptions().stream()
                .anyMatch(option -> option.getContent().equalsIgnoreCase(content));
        if (isDuplicate) {
            throw new IllegalArgumentException("동일한 내용의 항목이 이미 존재합니다.");
        }

        // 3. 새 투표 항목 생성 및 저장
        VoteEntity.PollOption newOption = new VoteEntity.PollOption();
        newOption.setPoll(poll);
        newOption.setContent(content);
        newOption.setVotes(0);

        return pollOptionRepository.save(newOption);
    }

    public void deletePoll(Long pollId) {
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        // 투표 옵션들 삭제
        pollOptionRepository.deleteAll(poll.getOptions());

        // 투표 삭제
        pollRepository.delete(poll);
    }

    public void deleteExpiredPolls() {
        LocalDateTime now = LocalDateTime.now();
        List<VoteEntity.Poll> expiredPolls = pollRepository.findByEndDateBefore(now);
        pollRepository.deleteAll(expiredPolls);
    }

    public void deletePollOption(Long pollId, Long optionId) {
        // 1. 해당 pollId가 유효한지 확인
        VoteEntity.Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        // 2. 해당 optionId가 유효하고, poll에 속해 있는지 확인
        VoteEntity.PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("투표 옵션을 찾을 수 없습니다."));

        if (!option.getPoll().getId().equals(pollId)) {
            throw new IllegalArgumentException("해당 투표 옵션은 올바른 투표에 속하지 않습니다.");
        }

        // 3. 삭제
        pollOptionRepository.delete(option);
    }

    public List<Map<String, Object>> getAllPollsWithStatus() {
        List<VoteEntity.Poll> polls = pollRepository.findAll(); // 모든 투표 목록 조회

        // 현재 시간을 기준으로 투표 상태를 판단
        LocalDateTime now = LocalDateTime.now();

        return polls.stream().map(poll -> {
            Map<String, Object> pollStatus = new HashMap<>();

            pollStatus.put("pollId", poll.getId());
            pollStatus.put("title", poll.getTitle());
            pollStatus.put("endDate", poll.getEndDate());

            // 투표가 종료되었는지 여부를 판단
            if (poll.getEndDate().isBefore(now)) {
                pollStatus.put("status", "만료됨");
            } else {
                pollStatus.put("status", "진행중");
            }

            return pollStatus;
        }).collect(Collectors.toList());
    }


    private Long getCurrentMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) return null;

        // principal: CustomUserDetails
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // (보호용 fallback) 혹시 다른 타입이 들어오는 경우 대비
        if (principal instanceof MemberEntity me) return me.getId();

        return null;
    }
}
