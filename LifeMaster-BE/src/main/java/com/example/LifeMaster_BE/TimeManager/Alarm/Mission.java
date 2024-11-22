package com.example.LifeMaster_BE.TimeManager.Alarm;

import java.util.Random;
import java.util.Scanner; /**
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
