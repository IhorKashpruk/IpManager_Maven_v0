package Controls.TreeViewPanel;

import Controls.CallBacks.ComboBoxCallbackStatus_v2;
import Controls.MyLittleAlert;
import Controls.TreeViewManager;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Created by Игорь on 23.08.2016.
 */
public class EditNetworkPanel {
    private HBox mainBox;
    private final TreeViewManager viewManager;
    private TreeItem<Network> currentTreeItem;

    public EditNetworkPanel(HBox mainBox, TreeViewManager viewManager) {
        this.mainBox = mainBox;
        this.viewManager = viewManager;
        ComboBox<ImageView> comboBoxStatus = (ComboBox<ImageView>)mainBox.getChildren().get(3);
        comboBoxStatus.getItems().clear();
        comboBoxStatus.setItems(FXCollections.observableArrayList(
                new ImageView(new Image("icons/close_network.png")),
                new ImageView(new Image("icons/open_network.png")),
                new ImageView(new Image("icons/network.png"))));
        comboBoxStatus.setCellFactory(new ComboBoxCallbackStatus_v2());

        ComboBox<String> comboBoxPriority = (ComboBox<String>)mainBox.getChildren().get(4);
        comboBoxPriority.getItems().clear();
        comboBoxPriority.getItems().addAll("1", "2", "3", "4", "5");

        String pattern = "dd.MM.yyyy";
        ((DatePicker)mainBox.getChildren().get(7)).setPromptText(pattern);
        StringConverter converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern(pattern);
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        ((DatePicker)mainBox.getChildren().get(7)).setConverter(converter);
        Button saveButton = ((Button)mainBox.getChildren().get(9));
        saveButton.setGraphic(new ImageView(new Image("icons/save.png")));
        saveButton.setOnAction(event -> {
            if(currentTreeItem == null)
                return;
            Network network = currentTreeItem.getValue();
            int index = comboBoxStatus.getSelectionModel().getSelectedIndex();
            STATUS status = index == 0 ? STATUS.BUSY_NETWORK : index == 1 ? STATUS.FREE_NETWORK : STATUS.HOME_NETWORK;
            if(network.getStatus() != status) {
                if (network.getStatus() == STATUS.HOME_NETWORK && currentTreeItem.getChildren().size() > 0) {
                    Optional<ButtonType> result = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "Attention",
                            "This network consists of " + currentTreeItem.getChildren().size() + " nodes!!!",
                            "Are you sure? Remove them?").showAndWait();
                    if(result.get() == ButtonType.NO)
                        return;
                    for (int i = 0; i < currentTreeItem.getChildren().size(); i++) {
                        viewManager.remove(currentTreeItem.getChildren().get(i));
                    }
                    currentTreeItem.getChildren().clear();
                }
            }
            network.setStatus(status);
            network.setPriority(((ComboBox<String>)mainBox.getChildren().get(4)).getSelectionModel().getSelectedItem());
            network.setClient(((TextField)mainBox.getChildren().get(5)).getText());
            network.setTypeOfConnection(((TextField)mainBox.getChildren().get(6)).getText());
            DatePicker datePicker = (DatePicker)mainBox.getChildren().get(7);
            try {
                network.setDate(datePicker.getConverter().toString(datePicker.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Information",
                    "New data was added!", "").showAndWait();
            if(viewManager.getFindPanel().getStatus() != STATUS.HOME_NETWORK){
                STATUS lastStatus = viewManager.getFindPanel().getStatus();
                viewManager.getFindPanel().setStatus(STATUS.HOME_NETWORK);
                viewManager.getFindPanel().setStatus(lastStatus);
            }
            Network localNetwork = currentTreeItem.getValue();
            currentTreeItem.setValue(null);
            currentTreeItem.setValue(localNetwork);
        });
    }

    public void setNetwork(TreeItem<Network> treeItem){
        currentTreeItem = treeItem;
        Network currentNetwork = currentTreeItem.getValue();

        if(currentNetwork == null)
            return;
        ((TextField)mainBox.getChildren().get(0)).setText(currentNetwork.getIp().getIp());
        ((TextField)mainBox.getChildren().get(1)).setText(currentNetwork.getMaskString());
        ((TextField)mainBox.getChildren().get(2)).setText(currentNetwork.getSizeString());
        ((ComboBox)mainBox.getChildren().get(3)).getSelectionModel().select((currentNetwork.getStatus() == STATUS.BUSY_NETWORK) ?
                    0 : ((currentNetwork.getStatus() == STATUS.FREE_NETWORK) ? 1 : 2));
        ((ComboBox)mainBox.getChildren().get(4)).getSelectionModel().select(currentNetwork.getPriority()-1);
        ((TextField)mainBox.getChildren().get(5)).setText(currentNetwork.getClient());
        ((TextField)mainBox.getChildren().get(6)).setText(currentNetwork.getTypeOfConnection());
        ((DatePicker)mainBox.getChildren().get(7)).setValue(((DatePicker)mainBox.getChildren().get(7)).getConverter().fromString(currentNetwork.getDateString()));
    }

    private void showAllert(){

    }

}
