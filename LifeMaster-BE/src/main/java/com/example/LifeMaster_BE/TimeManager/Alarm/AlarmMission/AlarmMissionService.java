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

        RandomMissionType current = alarm.getRandomMissionType();

        // 이미 다른 종류의 미션이 존재하면 수학 문제 생성 금지
        if (current != null &&
                current != RandomMissionType.NONE &&
                current != RandomMissionType.MATH_PROBLEM) {

            return new MathProblem(
                    "이미 다른 미션이 생성되어 있습니다.",
                    -1,
                    level
            );
        }

        // 수학 문제 생성
        MathProblem problem = internalGenerateMathProblem(level);

        // 미션 저장
        alarm.setRandomMissionType(RandomMissionType.MATH_PROBLEM);
        alarm.setMissionLevel(toMissionLevel(level));
        alarm.setMathQuestion(problem.question);
        alarm.setMathAnswer(problem.correctAnswer);

        alarmService.saveAlarm(alarm);

        return problem;
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

    /**
     * 수학 문제의 정답을 확인합니다. (DB에 저장된 문제/정답 기준)
     *
     * @param alarmId    알람 ID
     * @param userAnswer 사용자가 입력한 정답
     * @return 통과 여부 메시지
     */
    @Transactional
    public String checkMathProblemAnswer(Long alarmId, int userAnswer) {
        AlarmEntity alarm = getAlarmOrThrow(alarmId);

        String question = alarm.getMathQuestion();
        Integer correct = alarm.getMathAnswer();

        if (question == null || correct == null) {
            return "잘못된 접근, 먼저 수학 문제를 생성해야 합니다.";
        }

        // ---- 정답 처리 ----
        if (userAnswer == correct) {

            // 미션 초기화
            alarm.setRandomMissionType(RandomMissionType.NONE);
            alarm.setMissionLevel(null);

            alarm.setTypingSentence(null);
            alarm.setMathQuestion(null);
            alarm.setMathAnswer(null);
            alarm.setFollowClickGridJson(null);

            alarmRepository.save(alarm); // 업데이트 저장

            return "문제: " + question + " = " + userAnswer + " (정답입니다!)";
        }

        // ---- 오답 처리 ----
        return "문제: " + question + " = " + userAnswer +
                " (틀렸습니다. 정답은 " + correct + "입니다.)";
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

        RandomMissionType current = alarm.getRandomMissionType();

        // 현재 미션 타입 확인
        if (current != null &&
                current != RandomMissionType.NONE &&
                current != RandomMissionType.TYPING_SENTENCE) {

            return "이미 다른 미션이 생성되어 있습니다.";
        }

        // 랜덤 문장 생성
        String sentence = CreateRandomSentences.generateRandomSentence();

        // 미션 적용
        alarm.setRandomMissionType(RandomMissionType.TYPING_SENTENCE);
        alarm.setMissionLevel(null);         // 난이도 없음
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
            alarm.setRandomMissionType(RandomMissionType.NONE);
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

        // 🔒 이미 다른 종류의 미션이 걸려 있으면 생성 불가
        if (current != null &&
                current != RandomMissionType.NONE &&
                current != RandomMissionType.FOLLOW_CLICK) {

            throw new IllegalStateException("이미 다른 미션이 생성되어 있습니다.");
        }

        int[][] grid = internalGenerateFollowClickGrid(level);

        try {
            String json = objectMapper.writeValueAsString(grid);

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
            alarm.setRandomMissionType(RandomMissionType.NONE);
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

    public void updateAlarmStatus(Long alarmId, boolean status) {
        alarmService.updateAlarmStatus(alarmId, status);
    }
}
