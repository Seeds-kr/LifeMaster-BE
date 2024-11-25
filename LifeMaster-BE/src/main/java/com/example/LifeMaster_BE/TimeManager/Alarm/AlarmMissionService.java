package com.example.LifeMaster_BE.TimeManager.Alarm;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AlarmMissionService {

    public static final String LEVEL_HIGH = "상";
    public static final String LEVEL_MEDIUM = "중";
    public static final String LEVEL_LOW = "하";
    public static final String[] OPERATORS = {"+", "-", "*", "/"};

    private final Random random = new Random();

    // ========== 수학 문제 ==========

    /**
     * 수학 문제를 생성합니다.
     *
     * @param level 난이도 (상, 중, 하)
     * @return 생성된 문제와 정답
     */
    public MathProblem generateMathProblem(String level) {
        int num1, num2, correctAnswer = 0;
        String operator = OPERATORS[random.nextInt(OPERATORS.length)];
        String question = "";

        // 난이도에 따라 숫자 범위 설정
        if (LEVEL_HIGH.equals(level)) {
            // 상: 두 자릿수 덧셈, 뺄셈, 곱셈, 나눗셈
            if(operator.equals("*")){
                num1 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                num2 = random.nextInt(20) + 1;  // (1-20)
            }
            else{
                num1 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                num2 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
            }
            // 난이도 상에서는 나눗셈은 제외
            while (operator.equals("/")) {
                operator = OPERATORS[random.nextInt(OPERATORS.length)];
            }
        } else if (LEVEL_MEDIUM.equals(level)) {
            // 중: 두 자릿수 덧셈, 뺄셈, 한 자릿수 곱셈, 나눗셈
            num1 = random.nextInt(30) + 10;  // 두 자릿수 (10-39)
            num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
        } else {
            // 하: 한 자릿수 덧셈, 뺄셈
            num1 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
            num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
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
                // 나눗셈의 경우 num2가 0이면 다시 랜덤 숫자 선택
                if (num2 == 0){
                    if (LEVEL_HIGH.equals(level)) {
                        // 상: 두 자릿수 덧셈, 뺄셈, 곱셈, 나눗셈
                        num2 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                    } else if (LEVEL_MEDIUM.equals(level)) {
                        // 중: 두 자릿수 덧셈, 뺄셈, 한 자릿수 곱셈, 나눗셈
                        num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
                    } else {
                        // 하: 한 자릿수 덧셈, 뺄셈
                        num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
                    }
                }
                // 나눗셈은 나누어떨어지는 숫자만 사용
                while (num1 % num2 != 0) {
                    if (LEVEL_HIGH.equals(level)) {
                        // 상: 두 자릿수 덧셈, 뺄셈, 곱셈, 나눗셈
                        num1 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                        num2 = random.nextInt(90) + 10;  // 두 자릿수 (10-99)
                    } else if (LEVEL_MEDIUM.equals(level)) {
                        // 중: 두 자릿수 덧셈, 뺄셈, 한 자릿수 곱셈, 나눗셈
                        num1 = random.nextInt(30) + 10;  // 두 자릿수 (10-39)
                        num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
                    } else {
                        // 하: 한 자릿수 덧셈, 뺄셈
                        num1 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
                        num2 = random.nextInt(10) + 1;   // 한 자릿수 (1-9)
                    }
                }
                correctAnswer = num1 / num2;
                question = num1 + " / " + num2;
            }
        }

        return new MathProblem(question, correctAnswer);
    }

    /**
     * 수학 문제의 정답을 확인합니다.
     *
     * @param problem 생성된 문제
     * @param userAnswer 사용자가 입력한 정답
     * @return 통과 여부 메시지
     */
    public String checkMathProblemAnswer(MathProblem problem, int userAnswer) {
        if (problem == null) {
            return "잘못된 접근, 문제 생성이 선행되어야 합니다.";
        }
        return userAnswer == problem.correctAnswer
                ? "문제: " + problem.question + " = " + userAnswer + " (정답입니다!)"
                : "문제: " + problem.question + " = " + userAnswer + " (틀렸습니다. 정답은 " + problem.correctAnswer + "입니다.)";
    }

    // ========== 문장 따라쓰기 ==========

    /**
     * 랜덤 문장을 생성합니다.
     *
     * @return 생성된 문장
     */
    public String generateTypingSentence() {
        return CreateRandomSentences.generateRandomSentence();  // RandomSentences에서 문장을 가져옴
    }

    /**
     * 문장의 정답을 확인합니다.
     *
     * @param targetSentence 생성된 문장
     * @param userInput 사용자가 입력한 문장
     * @return 통과 여부 메시지
     */
    public String checkTypingAnswer(String targetSentence, String userInput) {
        if (targetSentence == null) {
            return "잘못된 접근, 문장 생성이 선행되어야 합니다.";
        }
        return targetSentence.equals(userInput)
                ? "문장: \"" + targetSentence + "\"\n입력: \"" + userInput + "\" (성공! 알람이 꺼졌습니다.)"
                : "문장: \"" + targetSentence + "\"\n입력: \"" + userInput + "\" (틀렸습니다. 다시 시도하세요.)";
    }

    // ========== 따라 누르기 ==========

    /**
     * 랜덤 클릭 그리드를 생성합니다.
     *
     * @param level 난이도 (상, 중, 하)
     * @return 생성된 5x5 클릭 그리드
     */
    public int[][] generateFollowClickGrid(String level) {
        int[][] grid = new int[5][5];
        int totalClicks;

        // 난이도에 따라 클릭 수 설정
        if (LEVEL_HIGH.equals(level)) {
            totalClicks = 18;
        } else if (LEVEL_MEDIUM.equals(level)) {
            totalClicks = 12;
        } else {
            totalClicks = 6;
        }

        // 랜덤 클릭 위치 설정
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
     * 사용자의 입력 그리드와 생성된 그리드를 비교합니다.
     *
     * @param generatedGrid 생성된 그리드
     * @param userGrid 사용자가 입력한 그리드
     * @return 통과 여부 메시지
     */
    public String checkFollowClickAnswer(int[][] generatedGrid, int[][] userGrid) {
        if (generatedGrid == null) {
            return "잘못된 접근, 그리드 생성이 선행되어야 합니다.";
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (generatedGrid[i][j] != userGrid[i][j]) {
                    return "틀렸습니다. 입력한 그리드와 일치하지 않습니다.";
                }
            }
        }
        return "정답입니다! 알람이 꺼졌습니다.";
    }

    // ========== Helper Class ==========

    public static class MathProblem {
        public final String question;
        public final int correctAnswer;

        public MathProblem(String question, int correctAnswer) {
            this.question = question;
            this.correctAnswer = correctAnswer;
        }
    }
}
