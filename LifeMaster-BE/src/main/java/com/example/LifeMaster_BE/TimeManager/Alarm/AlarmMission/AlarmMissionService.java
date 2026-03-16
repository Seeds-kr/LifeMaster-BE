package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum.RandomMissionType;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmRepository;
import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class AlarmMissionService {

    private final AlarmService alarmService;

    private final AlarmRepository alarmRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String LEVEL_HIGH = "상";
    public static final String LEVEL_MEDIUM = "중";
    public static final String LEVEL_LOW = "하";
    public static final String[] OPERATORS = {"+", "-", "*", "/"};

    private final Random random = new Random();

    public AlarmMissionService(AlarmService alarmService, AlarmRepository alarmRepository) {
        this.alarmService = alarmService;
        this.alarmRepository = alarmRepository;
    }

    // ========== 공통: level 문자열 → MissionLevel 변환 ==========

    private AlarmEntity.MissionLevel toMissionLevel(String level) {
        if (level == null) return null;
        return switch (level) {
            case LEVEL_HIGH -> AlarmEntity.MissionLevel.HIGH;
            case LEVEL_MEDIUM -> AlarmEntity.MissionLevel.MEDIUM;
            case LEVEL_LOW -> AlarmEntity.MissionLevel.LOW;
            default -> null;
        };
    }

    private AlarmEntity getAlarmOrThrow(Long alarmId) {
        return alarmService.findAlarmById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("Alarm " + alarmId + " not found"));
    }

    // ========== 수학 문제 ==========

    /**
     * 수학 문제를 생성하고, 해당 알람 엔티티에 문제/정답/난이도/타입을 저장합니다.
     *
     * @param alarmId 알람 ID
     * @param level   난이도 (상, 중, 하)
     * @return 생성된 문제(문자열, 정답, 난이도)
     */
    @Transactional
    public MathProblem generateMathProblem(Long alarmId, String level) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        if (alarm.getRandomMissionType() != RandomMissionType.MATH_PROBLEM) {
            alarm.clearMissionData();
        }

        // enum/영문 입력을 한글 난이도로 정규화
        String normalizedLevel = normalizeLevel(level);

        MathProblem problem = internalGenerateMathProblem(normalizedLevel);

        alarm.setRandomMissionType(RandomMissionType.MATH_PROBLEM);
        alarm.setMissionLevel(toMissionLevel(level)); // DB에는 enum 저장(LOW/MEDIUM/HIGH)
        alarm.setMathQuestion(problem.question);
        alarm.setMathAnswer(problem.correctAnswer);

        alarmService.saveAlarm(alarm);
        return problem;
    }

    private String normalizeLevel(String level) {
        if (level == null) return "중";

        String v = level.trim().toUpperCase();
        return switch (v) {
            case "HIGH", "상"   -> "상";
            case "MEDIUM", "중" -> "중";
            case "LOW", "하"    -> "하";
            default             -> "중";
        };
    }

    // 기존 generateMathProblem(String level)의 로직을 내부 메서드로 분리
    private MathProblem internalGenerateMathProblem(String level) {
        int num1, num2, correctAnswer = 0;
        String operator = OPERATORS[random.nextInt(OPERATORS.length)];
        String question = "";

        // 난이도에 따라 숫자 범위 설정
        if (LEVEL_HIGH.equals(level)) {
            if (operator.equals("*")) {
                num1 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                num2 = random.nextInt(20) + 1;   // (1-20)
            } else {
                num1 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                num2 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
            }
            // 난이도 상에서는 나눗셈은 제외
            while (operator.equals("/")) {
                operator = OPERATORS[random.nextInt(OPERATORS.length)];
            }
        } else if (LEVEL_MEDIUM.equals(level)) {
            num1 = random.nextInt(30) + 10;  // 두 자릿수 (10-39)
            num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
        } else {
            num1 = random.nextInt(10) + 1;
            num2 = random.nextInt(10) + 1;
        }

        // 연산자에 따라 문제 생성 및 정답 계산
        switch (operator) {
            case "+" -> {
                correctAnswer = num1 + num2;
                question = num1 + " + " + num2;
            }
            case "-" -> {
                // 항상 양수 결과 보장
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                correctAnswer = num1 - num2;
                question = num1 + " - " + num2;
            }
            case "*" -> {
                correctAnswer = num1 * num2;
                question = num1 + " * " + num2;
            }
            case "/" -> {
                if (num2 == 0) {
                    if (LEVEL_HIGH.equals(level)) {
                        num2 = random.nextInt(90) + 10;
                    } else if (LEVEL_MEDIUM.equals(level)) {
                        num2 = random.nextInt(10) + 1;
                    } else {
                        num2 = random.nextInt(10) + 1;
                    }
                }
                while (num1 % num2 != 0) {
                    if (LEVEL_HIGH.equals(level)) {
                        num1 = random.nextInt(90) + 10;
                        num2 = random.nextInt(90) + 10;
                    } else if (LEVEL_MEDIUM.equals(level)) {
                        num1 = random.nextInt(30) + 10;
                        num2 = random.nextInt(10) + 1;
                    } else {
                        num1 = random.nextInt(10) + 1;
                        num2 = random.nextInt(10) + 1;
                    }
                }
                correctAnswer = num1 / num2;
                question = num1 + " / " + num2;
            }
        }

        return new MathProblem(question, correctAnswer, level);
    }

    // ========== 문장 따라쓰기 ==========

    /**
     * 랜덤 문장을 생성하고, 알람 엔티티에 저장합니다.
     *
     * @param alarmId 알람 ID
     * @return 생성된 문장
     */
    @Transactional
    public String generateTypingSentence(Long alarmId) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        // 다른 타입이면 기존 미션 데이터 정리
        if (alarm.getRandomMissionType() != RandomMissionType.TYPING_SENTENCE) {
            alarm.clearMissionData();
        }

        String sentence = CreateRandomSentences.generateRandomSentence();

        alarm.setRandomMissionType(RandomMissionType.TYPING_SENTENCE);
        alarm.setMissionLevel(null);
        alarm.setTypingSentence(sentence);

        alarmService.saveAlarm(alarm);

        return sentence;
    }

    /**
     * 문장의 정답을 확인합니다. (DB에 저장된 문장 기준)
     *
     * @param alarmId   알람 ID
     * @param userInput 사용자가 입력한 문장
     * @return 통과 여부 메시지
     */
    @Transactional
    public String checkTypingAnswer(Long alarmId, String userInput) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        String targetSentence = alarm.getTypingSentence();
        if (targetSentence == null) {
            return "잘못된 접근, 문장 생성이 선행되어야 합니다.";
        }

        // --- 정답 ---
        if (targetSentence.equals(userInput)) {

            // 미션 초기화
            alarm.setRandomMissionType(null);
            alarm.setMissionLevel(null);

            alarm.setTypingSentence(null);
            alarm.setMathQuestion(null);
            alarm.setMathAnswer(null);
            alarm.setFollowClickGridJson(null);

            alarmRepository.save(alarm);

            return "문장: \"" + targetSentence + "\"\n입력: \"" + userInput + "\" (성공! 알람이 꺼졌습니다.)";
        }

        // --- 오답 ---
        return "문장: \"" + targetSentence + "\"\n입력: \"" + userInput + "\" (틀렸습니다. 다시 시도하세요.)";
    }

    // ========== 따라 누르기 ==========

    /**
     * 랜덤 클릭 그리드를 생성하고, 알람 엔티티에 JSON 형태로 저장합니다.
     *
     * @param alarmId 알람 ID
     * @param level   난이도 (상, 중, 하)
     * @return 생성된 5x5 클릭 그리드
     */
    @Transactional
    public int[][] generateFollowClickGrid(Long alarmId, String level) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        RandomMissionType current = alarm.getRandomMissionType();

        // 다른 타입이면 기존 미션 데이터 정리(수학/타이핑 찌꺼기 제거)
        if (current != RandomMissionType.FOLLOW_CLICK) {
            alarm.clearMissionData();
        }

        int[][] grid = internalGenerateFollowClickGrid(level);

        try {
            String json = objectMapper.writeValueAsString(grid);

            // 새 미션 적용(덮어쓰기)
            alarm.setRandomMissionType(RandomMissionType.FOLLOW_CLICK);
            alarm.setMissionLevel(toMissionLevel(level));
            alarm.setFollowClickGridJson(json);

            alarmService.saveAlarm(alarm);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("그리드 직렬화 실패", e);
        }

        return grid;
    }

    private int[][] internalGenerateFollowClickGrid(String level) {
        int[][] grid = new int[5][5];
        int totalClicks;

        if (LEVEL_HIGH.equals(level)) {
            totalClicks = 18;
        } else if (LEVEL_MEDIUM.equals(level)) {
            totalClicks = 12;
        } else {
            totalClicks = 6;
        }

        for (int i = 0; i < totalClicks; i++) {
            int row = random.nextInt(5);
            int col = random.nextInt(5);
            while (grid[row][col] == 1) {
                row = random.nextInt(5);
                col = random.nextInt(5);
            }
            grid[row][col] = 1;
        }

        return grid;
    }

    /**
     * 사용자의 입력 그리드와 생성된 그리드를 비교합니다. (DB에 저장된 그리드 기준)
     *
     * @param alarmId  알람 ID
     * @param userGrid 사용자가 입력한 그리드
     * @return 통과 여부 메시지
     */
    @Transactional
    public String checkFollowClickAnswer(Long alarmId, int[][] userGrid) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        String json = alarm.getFollowClickGridJson();
        if (json == null) {
            return "잘못된 접근, 그리드 생성이 선행되어야 합니다.";
        }

        try {
            int[][] generatedGrid = objectMapper.readValue(json, int[][].class);

            // 비교
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (generatedGrid[i][j] != userGrid[i][j]) {
                        return "틀렸습니다. 입력한 그리드와 일치하지 않습니다.";
                    }
                }
            }

            // ========== 정답이면 모든 미션 필드 초기화 ==========
            alarm.setRandomMissionType(null);
            alarm.setMissionLevel(null);

            alarm.setTypingSentence(null);
            alarm.setMathQuestion(null);
            alarm.setMathAnswer(null);
            alarm.setFollowClickGridJson(null);

            alarmRepository.save(alarm);

            return "정답입니다! 알람이 꺼졌습니다.";

        } catch (JsonProcessingException e) {
            throw new RuntimeException("그리드 역직렬화 실패", e);
        }
    }

    // ========== Helper Class ==========

    public static class MathProblem {
        public final String question;
        public final int correctAnswer;
        public final String level;

        public MathProblem(String question, int correctAnswer, String level) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.level = level;
        }
    }

    public AlarmMissionAnswerResponseDto getMissionQuestionAndAnswer(Long alarmId, Long memberId) {

        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new EntityNotFoundException("ALARM_NOT_FOUND"));

        Long ownerId = (alarm.getMember() != null) ? alarm.getMember().getId() : null;
        if (ownerId == null || !ownerId.equals(memberId)) {
            throw new SecurityException("NOT_OWNER");
        }

        RandomMissionType type = alarm.getRandomMissionType();

        // 미션이 없는 경우
        if (type == null) {
            return new AlarmMissionAnswerResponseDto(
                    alarm.getId(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return switch (type) {
            case MATH_PROBLEM -> new AlarmMissionAnswerResponseDto(
                    alarm.getId(),
                    type,
                    alarm.getMissionLevel(),
                    alarm.getMathQuestion(),     // question
                    alarm.getMathAnswer(),       // mathAnswer
                    null                         // payload 없음
            );

            case TYPING_SENTENCE -> new AlarmMissionAnswerResponseDto(
                    alarm.getId(),
                    type,
                    null,
                    null,                        // question=null
                    null,                        // mathAnswer=null
                    alarm.getTypingSentence()    // answerPayload에 문장
            );

            case FOLLOW_CLICK -> new AlarmMissionAnswerResponseDto(
                    alarm.getId(),
                    type,
                    alarm.getMissionLevel(),
                    null,                          // question=null
                    null,                          // mathAnswer=null
                    alarm.getFollowClickGridJson() // answerPayload에 그리드 JSON
            );
        };
    }

    public void updateAlarmStatus(Long alarmId, boolean status) {
        alarmService.updateAlarmStatus(alarmId, status);
    }
}
