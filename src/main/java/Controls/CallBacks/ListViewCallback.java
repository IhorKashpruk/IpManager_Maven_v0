package Controls.CallBacks;

import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * Created by Administrator on 28.08.2016.
 */
public class ListViewCallback implements Callback<ListView<Network>, ListCell<Network>> {
    private boolean withText;
    public ListViewCallback(boolean withText) {
        this.withText = withText;
    }

    @Override

    public ListCell<Network> call(ListView<Network> param) {
        return new ListCell<Network>(){
            @Override
            protected void updateItem(Network item, boolean empty) {
                super.updateItem(item, empty);
                if (isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                if (item == null || empty) {
                    setText(null);
                } else {
                    HBox hBox = new HBox(10);
                    String text = item.getIp().getIp() + "\t[" + item.getMask() + "]\t{" + item.getSizeString() + "}";
                    if(withText)
                        text += "\t'" + item.getPriority() + "', '" + item.getClient() + "', '" + item.getTypeOfConnection() + "', '" + item.getDateString() + "'";
                    Label labelAddrMaskCount = new Label(text);
                    String url = item.getStatus() == STATUS.HOME_NETWORK ? "Icons/network.png" :
                            item.getStatus() == STATUS.BUSY_NETWORK ? "Icons/close_network.png" : "Icons/open_network.png";
                    Label image = new Label(null, new ImageView(new Image(url)));
                    hBox.getChildren().addAll(image, labelAddrMaskCount);
                    setText(null);
                    setGraphic(hBox);
                }
            }
        };
    }
}
