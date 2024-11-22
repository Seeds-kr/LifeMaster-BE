package com.example.LifeMaster_BE.TimeManager.Alarm;

import java.util.Random; /**
 * 따라 누르기 미션을 생성하는 기능을 제공합니다.
 */
public class Mission2 {

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
