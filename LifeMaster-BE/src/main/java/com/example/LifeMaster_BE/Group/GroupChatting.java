import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.WebSocket;
import java.time.LocalTime;
import java.util.concurrent.CompletionStage;
import org.json.JSONObject;  // JSON 파싱을 위해 필요 (org.json 라이브러리 추가 필요)

public class GroupChatting extends Application {

    private boolean isChatOn = false; // 그룹 채팅 On/Off 상태
    private WebSocket webSocket;
    private ListView<String> chatListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // WebSocket 연결
        connectToWebSocket();

        // 레이아웃 생성
        BorderPane root = new BorderPane();

        // 그룹 채팅 리스트뷰 생성
        chatListView = new ListView<>();
        chatListView.setPrefSize(400, 300);

        // 플로팅 버튼 생성
        Button floatingButton = new Button("채팅 On/Off");
        floatingButton.setStyle("-fx-font-size: 14px; -fx-background-color: #6fa8dc; -fx-text-fill: white; -fx-border-radius: 20px; -fx-background-radius: 20px;");

        // 플로팅 버튼 동작 정의
        floatingButton.setOnAction(e -> {
            isChatOn = !isChatOn;
            if (isChatOn) {
                root.setCenter(chatListView);
            } else {
                root.setCenter(null);
            }
        });

        // 플로팅 버튼을 하단 중앙에 배치
        StackPane floatingButtonContainer = new StackPane(floatingButton);
        floatingButtonContainer.setStyle("-fx-alignment: bottom-right; -fx-padding: 20px;");
        root.setBottom(floatingButtonContainer);

        // Scene 설정 및 Stage 표시
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("그룹 채팅");
        primaryStage.show();
    }

    /**
     * WebSocket 서버에 연결합니다.
     */
    private void connectToWebSocket() {
        webSocket = WebSocket.newBuilder()
                .uri(URI.create("ws://localhost:8080/chat-websocket")) // 서버 주소
                .buildAsync()
                .join();
        
        webSocket.request(1); // 메시지 요청
        webSocket.setListener(new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                javafx.application.Platform.runLater(() -> {
                    // JSON 파싱
                    JSONObject json = new JSONObject(data.toString());
                    String sender = json.getString("sender");
                    String time = json.getString("time");
                    String content = json.getString("content");

                    // 채팅 목록에 추가
                    chatListView.getItems().add(String.format("[%s] %s: %s", time, sender, content));
                });

                webSocket.request(1);
                return null;
            }
        });
    }

    /**
     * WebSocket을 통해 메시지를 전송합니다.
     */
    private void sendMessage(String sender, String content) {
        // JSON 형식으로 변환하여 전송
        JSONObject json = new JSONObject();
        json.put("sender", sender);
        json.put("time", LocalTime.now().toString());  // 실제 서버에서 시간을 설정하는 것이 바람직함
        json.put("content", content);

        webSocket.sendText(json.toString(), true);
    }
}
