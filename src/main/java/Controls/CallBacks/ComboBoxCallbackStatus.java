package Controls.CallBacks;


import Logic.Net.STATUS;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * Created by Игорь on 18.08.2016.
 */
public class ComboBoxCallbackStatus implements Callback<ListView<STATUS>, ListCell<STATUS>> {
    @Override
    public ListCell<STATUS> call(ListView<STATUS> param) {
        return new ListCell<STATUS>(){
            private final ImageView rectangle;
            {
//                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                rectangle = new ImageView();
            }
            @Override
            protected void updateItem(STATUS item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    String url = item == STATUS.BUSY_NETWORK ? "Icons/close_network.png" : item == STATUS.FREE_NETWORK ?
                            "Icons/open_network.png" : "Icons/network.png";
                    rectangle.setImage(new Image(url));
                    setGraphic(rectangle);
                    setText(item.toString());
                }
            }
        };
    }
}
