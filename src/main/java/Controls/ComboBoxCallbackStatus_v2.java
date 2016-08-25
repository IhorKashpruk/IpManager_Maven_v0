package Controls;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * Created by Administrator on 25.08.2016.
 */
public class ComboBoxCallbackStatus_v2 implements Callback<ListView<ImageView>, ListCell<ImageView>> {
    @Override
    public ListCell<ImageView> call(ListView<ImageView> param) {
        return new ListCell<ImageView>(){
            private final ImageView rectangle;
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                rectangle = new ImageView();
            }
            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    rectangle.setImage(item.getImage());
                    setGraphic(rectangle);
                }
            }
        };
    }
}
