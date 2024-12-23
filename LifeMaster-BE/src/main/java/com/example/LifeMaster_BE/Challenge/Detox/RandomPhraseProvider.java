package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class RandomPhraseProvider {

    private final List<String> phrases = Arrays.asList(
            "Persistence is the key to success.",
            "Discipline equals freedom.",
            "Stay focused, stay strong.",
            "You are capable of amazing things.",
            "One step at a time leads to success.",
            "Hard work beats talent when talent doesn't work hard.",
            "Dream big and dare to fail.",
            "Success is no accident.",
            "Small steps every day lead to big changes.",
            "Don't stop until you're proud.",
            "Great things take time.",
            "Action is the foundational key to all success.",
            "Believe you can and you're halfway there.",
            "Mistakes are proof that you're trying.",
            "The best way to predict the future is to create it.",
            "Opportunities don't happen, you create them.",
            "It always seems impossible until it's done.",
            "Success usually comes to those who are too busy to be looking for it.",
            "Your limitation—it's only your imagination.",
            "Push yourself, because no one else is going to do it for you.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Dream it. Wish it. Do it.",
            "Success doesn't just find you. You have to go out and get it.",
            "The key to success is to focus on goals, not obstacles.",
            "You don't have to be great to start, but you have to start to be great.",
            "Don't watch the clock; do what it does. Keep going.",
            "Someday is not a day of the week.",
            "What you get by achieving your goals is not as important as what you become by achieving your goals.",
            "Motivation gets you going; discipline keeps you growing.",
            "Doubt kills more dreams than failure ever will.",
            "You are your only limit."
    );

    public String getRandomPhrase() {
        Random random = new Random();
        return phrases.get(random.nextInt(phrases.size()));
    }
}

