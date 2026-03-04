module TCPServer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.chatapp.server.ui to javafx.fxml;
    exports com.chatapp.server;
}