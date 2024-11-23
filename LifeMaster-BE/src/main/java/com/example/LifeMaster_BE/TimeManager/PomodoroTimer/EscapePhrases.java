package com.example.LifeMaster_BE.TimeManager.PomodoroTimer;

import java.util.Random;

public class EscapePhrases {

    //비상 탈출 랜덤 문장
    private static final String[] PHRASES = {
            "Focus brings success!",
            "Stay determined!",
            "One step at a time!",
            "Keep pushing forward!",
            "Believe in your potential!",
            "Never give up!",
            "Stay positive, work hard!",
            "Dream big and dare to fail!",
            "You are stronger than you think!",
            "Success is no accident!",
            "Consistency is key!",
            "Stay focused and never give up!",
            "Keep your eyes on the goal!",
            "Make today count!",
            "You can do hard things!",
            "Challenge yourself!",
            "Progress, not perfection!",
            "Believe you can, and you're halfway there!",
            "Do what you can, with what you have!",
            "Hard work beats talent!",
            "Every step forward counts!",
            "Push through the tough times!",
            "Your only limit is you!",
            "Success is built on discipline!",
            "Failure is part of success!",
            "The journey is worth it!",
            "Small steps lead to big changes!",
            "Stay committed to your goals!",
            "You are closer than you think!",
            "Keep going no matter what!"
    };

    // 랜덤으로 문장을 선택하는 메서드
    public static String getRandomPhrase() {
        Random random = new Random();
        return PHRASES[random.nextInt(PHRASES.length)];
    }
}

