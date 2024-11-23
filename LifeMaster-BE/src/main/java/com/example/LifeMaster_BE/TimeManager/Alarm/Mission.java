package com.example.demo.Alarm;

import java.util.Random;
import java.util.Scanner;

/**
 * 수학 문제 미션을 생성하고, 정답 맞추기를 처리하는 기능을 제공합니다.
 */
public class Mission {

    public static final String MATH_PROBLEM = "간단한 수학 문제 풀기";
    public static final String LEVEL_HIGH = "상";
    public static final String LEVEL_MEDIUM = "중";
    public static final String LEVEL_LOW = "하";

    public static final String[] OPERATORS = {"+", "-", "*", "/"};

    /**
     * 수학 문제 미션을 생성합니다.
     * @param level 난이도 (상, 중, 하)
     * @param scanner 사용자 입력을 받을 스캐너 객체
     * @return 수학 문제 미션을 맞추면 알람이 꺼지고, 틀리면 정답을 알려주고 다시 시도하도록 처리
     */
    public String createMathProblemWithAttempts(String level, Scanner scanner) {
        Random rand = new Random();
        int num1, num2, correctAnswer = 0;
        String operator = OPERATORS[rand.nextInt(OPERATORS.length)];
        int attempts = 0;
        int maxAttempts = 3;
        String question = "";

        if (LEVEL_HIGH.equals(level)) {
            num1 = rand.nextInt(50) + 50;
            num2 = rand.nextInt(50) + 50;
        } else if (LEVEL_MEDIUM.equals(level)) {
            num1 = rand.nextInt(30) + 20;
            num2 = rand.nextInt(30) + 20;
        } else {
            num1 = rand.nextInt(10) + 1;
            num2 = rand.nextInt(10) + 1;
        }

        switch (operator) {
            case "+":
                correctAnswer = num1 + num2;
                question = "문제: " + num1 + " + " + num2 + " = ?";
                break;
            case "-":
                correctAnswer = num1 - num2;
                question = "문제: " + num1 + " - " + num2 + " = ?";
                break;
            case "*":
                correctAnswer = num1 * num2;
                question = "문제: " + num1 + " * " + num2 + " = ?";
                break;
            case "/":
                if (num2 == 0) num2 = rand.nextInt(10) + 1; 
                correctAnswer = num1 / num2;
                question = "문제: " + num1 + " / " + num2 + " = ?";
                break;
        }

        while (attempts < maxAttempts) {
            System.out.println(question);
            System.out.print("정답을 입력하세요: ");
            int userAnswer = scanner.nextInt();

            if (userAnswer == correctAnswer) {
                return "정답입니다! 알람이 꺼집니다.";
            } else {
                attempts++;
                if (attempts < maxAttempts) {
                    System.out.println("틀렸습니다. 다시 시도하세요. (" + (maxAttempts - attempts) + "번 남음)");
                }
            }
        }

        return "정답을 맞추지 못했습니다. 정답은 " + correctAnswer + "입니다. 알람이 꺼집니다.";
    }
}



/**
 * 따라 누르기 미션을 생성하는 기능을 제공합니다.
 */
public class Mission {

    public static final String LEVEL_HIGH = "상";
    public static final String LEVEL_MEDIUM = "중";
    public static final String LEVEL_LOW = "하";

    /**
     * 따라 누르기 미션을 생성합니다.
     * @param level 난이도 (상, 중, 하)
     * @return 5x5 칸에서 랜덤 클릭 위치를 알려주는 미션
     */
    public String createFollowClick(String level) {
        Random rand = new Random();
        int totalClicks = 0;
        int[][] grid = new int[5][5];  // 5x5 그리드 (0은 클릭 안한 칸, 1은 클릭해야 할 칸)
        
        
        if (LEVEL_HIGH.equals(level)) {
            totalClicks = 20;  
        } else if (LEVEL_MEDIUM.equals(level)) {
            totalClicks = 15;
        } else {
            totalClicks = 10;  
        }

        for (int i = 0; i < totalClicks; i++) {
            int row = rand.nextInt(5);  
            int col = rand.nextInt(5);  
            while (grid[row][col] == 1) {
                row = rand.nextInt(5);
                col = rand.nextInt(5);
            }
            grid[row][col] = 1;  
        }

        StringBuilder missionText = new StringBuilder("미션: ");
        missionText.append(totalClicks).append("번 클릭해야 합니다. 5x5 그리드에서 클릭할 칸을 찾아 클릭하세요:\n");

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (grid[i][j] == 1) {
                    missionText.append("[X] ");  
                } else {
                    missionText.append("[ ] ");  
                }
            }
            missionText.append("\n");
        }

        return missionText.toString();  
    }
}
public class TypingMission {
    private static final String PLACEHOLDER_SENTENCE = "나의 꿈은 맑은 바람이 되어서";

    /**
     * 글 따라쓰기 미션을 생성합니다.
     * @return 따라쓰기 미션 결과
     */
    public void startTypingMission() {
        Scanner scanner = new Scanner(System.in);
        String targetSentence = getTargetSentence(); // 데이터베이스 연동 후 수정하기
        int maxAttempts = 3;

        System.out.println("타자 미션: 주어진 문장을 정확히 입력하세요!");
        System.out.println("문장: " + targetSentence);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.print("입력: ");
            String userInput = scanner.nextLine();

            if (targetSentence.equals(userInput)) {
                System.out.println("성공! 알람이 꺼졌습니다.");
                return;
            } else {
                System.out.println("틀렸습니다. 다시 시도하세요.");
                if (attempt == maxAttempts) {
                    System.out.println("최대 시도 횟수 초과! 미션 실패.");
                }
            }
        }
        System.out.println("정답은: " + targetSentence);
    }

    /**
     * 타자 연습 문장을 가져옵니다. (현재는 고정된 문장)
     * @return 연습 문장
     */
    private String getTargetSentence() {
        // 데이터베이스 연동하고 수정해야할 부분
        return PLACEHOLDER_SENTENCE;
    }
}
