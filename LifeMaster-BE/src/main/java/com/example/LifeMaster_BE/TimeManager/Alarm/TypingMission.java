package com.example.LifeMaster_BE.TimeManager.Alarm;

import java.util.Random;
import java.util.Scanner;


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