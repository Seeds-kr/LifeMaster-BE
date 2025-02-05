package com.example.demo.timer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.time.Duration;
import java.time.LocalTime;

/**
 * 디지털 디톡스 타이머를 구현합니다.
 * - 누적된 잠금 시간 표시
 * - 잠금 해제까지 남은 시간 표시
 * - 원형 감소 애니메이션
 */
public class Timer {

    private LocalTime lockStartTime;
    private Duration lockDuration;
    private LocalTime unlockTime;
    private Duration totalLockedDuration = Duration.ZERO;

    private JFrame frame;
    private JLabel remainingTimeLabel;
    private JLabel totalLockedLabel;
    private TimerPanel timerPanel;

    public Timer() {
        // GUI 초기화
        frame = new JFrame("Digital Detox Timer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        // 라벨
        remainingTimeLabel = new JLabel("남은 시간: ", SwingConstants.CENTER);
        remainingTimeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(remainingTimeLabel, BorderLayout.NORTH);

        totalLockedLabel = new JLabel("누적 잠금 시간: 0분", SwingConstants.CENTER);
        totalLockedLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(totalLockedLabel, BorderLayout.SOUTH);

        // 타이머 패널
        timerPanel = new TimerPanel();
        frame.add(timerPanel, BorderLayout.CENTER);

        // 시작 버튼
        JButton startButton = new JButton("Start Digital Detox");
        startButton.setFont(new Font("Arial", Font.PLAIN, 18));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTimer(5); // 예제: 5분 타이머 시작
            }
        });
        frame.add(startButton, BorderLayout.WEST);

        // 종료 버튼
        JButton stopButton = new JButton("End Detox");
        stopButton.setFont(new Font("Arial", Font.PLAIN, 18));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                endTimer();
            }
        });
        frame.add(stopButton, BorderLayout.EAST);

        frame.setVisible(true);
    }

    /**
     * 디지털 디톡스 타이머를 시작합니다.
     * @param minutes 잠금 시간(분)
     */
    public void startTimer(int minutes) {
        lockStartTime = LocalTime.now();
        lockDuration = Duration.ofMinutes(minutes);
        unlockTime = lockStartTime.plus(lockDuration);
        timerPanel.startCountdown((int) lockDuration.getSeconds());
        updateRemainingTime();
    }

    /**
     * 디지털 디톡스를 종료하고 누적 시간을 갱신합니다.
     */
    public void endTimer() {
        if (lockStartTime != null) {
            Duration currentLockedDuration = Duration.between(lockStartTime, LocalTime.now());
            totalLockedDuration = totalLockedDuration.plus(currentLockedDuration);
            lockStartTime = null;
            remainingTimeLabel.setText("남은 시간: 잠금 해제 완료");
            totalLockedLabel.setText("누적 잠금 시간: " + totalLockedDuration.toMinutes() + "분");
            timerPanel.resetCountdown();
        }
    }

    /**
     * 남은 시간을 업데이트합니다.
     */
    private void updateRemainingTime() {
        Timer updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lockStartTime == null) {
                    ((Timer) e.getSource()).stop();
                    return;
                }

                Duration remainingDuration = Duration.between(LocalTime.now(), unlockTime);
                if (!remainingDuration.isNegative()) {
                    remainingTimeLabel.setText("남은 시간: " + remainingDuration.toMinutes() + "분 " + (remainingDuration.getSeconds() % 60) + "초");
                } else {
                    endTimer();
                }
            }
        });
        updateTimer.start();
    }

    /**
     * 타이머 패널(원형 감소 애니메이션).
     */
    private class TimerPanel extends JPanel {
        private int remainingSeconds = 0;
        private int totalSeconds = 0;

        public void startCountdown(int seconds) {
            this.totalSeconds = seconds;
            this.remainingSeconds = seconds;

            Timer countdownTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (remainingSeconds > 0) {
                        remainingSeconds--;
                        repaint();
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            countdownTimer.start();
        }

        public void resetCountdown() {
            this.remainingSeconds = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) - 40;

            g.setColor(Color.LIGHT_GRAY);
            g.fillOval((width - size) / 2, (height - size) / 2, size, size);

            if (remainingSeconds > 0) {
                double angle = 360.0 * remainingSeconds / totalSeconds;
                g.setColor(Color.BLUE);
                g.fillArc((width - size) / 2, (height - size) / 2, size, size, 90, -(int) angle);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Timer::new);
    }
}
