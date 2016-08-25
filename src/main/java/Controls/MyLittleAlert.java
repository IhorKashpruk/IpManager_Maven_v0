package Controls;

/**
 * Created by Administrator on 25.08.2016.
 */
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Created by Игорь on 18.08.2016.
 */
public class MyLittleAlert {
    private Alert window;

    public MyLittleAlert(Alert.AlertType type, String title, String handlerText, String contextText) {
        window = new Alert(type);
        window.setTitle(title);
        window.setHeaderText(handlerText);
        window.setContentText(contextText);
    }

    public Optional<ButtonType> showAndWait(){
        return window.showAndWait();
    }
}