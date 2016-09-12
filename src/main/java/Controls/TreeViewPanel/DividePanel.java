package Controls.TreeViewPanel;

import Controls.CallBacks.ComboBoxCallbackStatus;
import Controls.MyLittleAlert;
import Controls.TreeViewManager;
import Logic.MyMath;
import Logic.Net.IP;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 30.08.2016.
 */
public class DividePanel {
    private TreeViewManager viewManager;
    private VBox mainBox;
    private List<Network> freeSpaces;
    private TreeItem<Network> lastItem;

    public DividePanel(TreeViewManager viewManager, VBox mainBox) {
        this.viewManager = viewManager;
        this.mainBox = mainBox;
        freeSpaces = null;
        createElements();
    }

    private void createElements(){
        for(int i = 2; i < 7; i++)
            mainBox.getChildren().get(i).setVisible(false);
        TextField sizeTextField = ((TextField)((HBox)((VBox)mainBox.getChildren().get(4)).getChildren().get(1)).getChildren().get(1));
        ComboBox<String> countComboBox = ((ComboBox<String>) ((HBox)((VBox)mainBox.getChildren().get(4)).getChildren().get(1)).getChildren().get(3));
        countComboBox.setValue("1");
        CheckBox checkBox = ((CheckBox)((VBox)mainBox.getChildren().get(4)).getChildren().get(2));

        //GridPane
        ComboBox<STATUS> statusComboBox = ((ComboBox<STATUS>) ((GridPane)((VBox)mainBox.getChildren().get(4)).getChildren().get(4)).getChildren().get(5));
        ComboBox<String> priorityComboBox = ((ComboBox<String>) ((GridPane)((VBox)mainBox.getChildren().get(4)).getChildren().get(4)).getChildren().get(6));
        TextField clientTextField = ((TextField) ((GridPane)((VBox)mainBox.getChildren().get(4)).getChildren().get(4)).getChildren().get(7));
        TextField typeTextField = ((TextField) ((GridPane)((VBox)mainBox.getChildren().get(4)).getChildren().get(4)).getChildren().get(8));
        DatePicker datePicker = ((DatePicker) ((GridPane)((VBox)mainBox.getChildren().get(4)).getChildren().get(4)).getChildren().get(9));
        Button applayButton = ((Button)((VBox)mainBox.getChildren().get(6)).getChildren().get(0));
        ListView<String> listView = ((ListView<String>)mainBox.getChildren().get(3));
        listView.setStyle("-fx-border-color: #5c5c5c;");


        clientTextField.setPromptText("Enter client...");
        typeTextField.setPromptText("Enter type of connection...");

        List<String> list = new ArrayList<>();
        for(int i = 1; i <=15; i++)
            list.add(String.valueOf(i));

        countComboBox.setItems(FXCollections.observableArrayList(list));
        countComboBox.getSelectionModel().select(0);


        try {
            statusComboBox.getItems().addAll(
                    STATUS.createStatus("z"),
                    STATUS.createStatus("n"),
                    STATUS.createStatus("h"));
            statusComboBox.setCellFactory(new ComboBoxCallbackStatus());
            statusComboBox.getSelectionModel().select(STATUS.HOME_NETWORK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String pattern = "dd.MM.yyyy";
        datePicker.setPromptText(pattern);
        datePicker.setValue(LocalDate.now());
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
        datePicker.setConverter(converter);


        priorityComboBox.getItems().addAll("1", "2", "3", "4", "5");
        priorityComboBox.getSelectionModel().select("1");

        countComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                countComboBox.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        sizeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                sizeTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        sizeTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            String str = sizeTextField.getText();
            if(str.isEmpty() || str.length() > 9 || str.equals("1") || str.equals("0"))
                return;
            if(checkBox.isSelected()) {
                int localCountIp = Integer.parseInt(str);
                int countDivided2 = MyMath.countDividedBy(localCountIp, 2);
                if (!MyMath.isDivideBy2Entirely(localCountIp)) {
                    sizeTextField.setText(String.valueOf((int) Math.pow(2, countDivided2 + 1)));
                }
            }
        });

        applayButton.setOnAction(event -> {
            if(sizeTextField.getText().isEmpty() || Integer.parseInt(sizeTextField.getText()) == 0 || Integer.parseInt(countComboBox.getValue()) == 0)
                return;
            int count = 0;
            int countDivide = Integer.parseInt(countComboBox.getValue());
            int size = Integer.parseInt(sizeTextField.getText());
            for(Network network: freeSpaces){
                count += network.getSize()/size;
            }

            if(count < countDivide){
                new MyLittleAlert(Alert.AlertType.ERROR, "Divide network", "You do not have enough addresses", "").showAndWait();
                return;
            }

            count = countDivide;
            List<Network> networks = new ArrayList<>();

            try {
            for(Network network: freeSpaces){
                int n = network.getSize()/size;
                Network network1 = null;
                int mask = MyMath.isDivideBy2Entirely(size) ? MyMath.countDividedBy(size, 2) : -1;
                while (countDivide  > 0 && n > 0){
                    if(network1 == null) {
                        network1 = network;
                        network1 = new Network(network1.getIp(), (byte)(32-mask), size, statusComboBox.getValue(), (byte)Integer.parseInt(priorityComboBox.getValue()),
                                clientTextField.getText(), typeTextField.getText(), null);
                            network1.setDate(datePicker.getConverter().toString(datePicker.getValue()));
                    }else
                    {
                        network1 = new Network(IP.moveIP(network1.getIp(), size), (byte)(32 - mask), size, statusComboBox.getValue(), (byte)Integer.parseInt(priorityComboBox.getValue()),
                                clientTextField.getText(), typeTextField.getText(), null);
                        network1.setDate(datePicker.getConverter().toString(datePicker.getValue()));
                    }
                    networks.add(network1);
                    n--;
                    countDivide--;
                }
                network1 = network;
                if(!IP.moveIP(network1.getIp(), network1.getSize()).equals(IP.moveIP(network.getIp(), network.getSize()))){
                    IP ip = IP.moveIP(network1.getIp(), network1.getSize());
                    int sizeComp = IP.moreOn(IP.moveIP(network.getIp(), network.getSize()), ip);
                    byte localMask = (byte) (MyMath.isDivideBy2Entirely(sizeComp) ? MyMath.countDividedBy(sizeComp, 2) : -1);
                    networks.add(new Network(ip, (byte)(32- localMask), sizeComp, STATUS.FREE_NETWORK, (byte)5, "", "", null));
                }
            }} catch (Exception e) {
                e.printStackTrace();
            }


            for (Network network: networks) {
                viewManager.getData().add(network);
            }
            viewManager.upload();
            setNetwork(lastItem);
        });

    }

    public void setNetwork(TreeItem<Network> item){
        lastItem = item;
        Label label = (Label) ((HBox) mainBox.getChildren().get(0)).getChildren().get(0);
        String url = item.getValue().getStatus() == STATUS.HOME_NETWORK ? "Icons/network.png" : item.getValue().getStatus() == STATUS.FREE_NETWORK ? "Icons/open_network.png" : "Icons/close_network.png";

        label.setGraphic(new ImageView(new Image(url)));
        label.setText(item.getValue().getIp().getIp() + "\t["+item.getValue().getMask()+"]\t{"+item.getValue().getSizeString()+"}");
        if(item.getValue().getStatus() != STATUS.HOME_NETWORK){
            ((HBox) mainBox.getChildren().get(0)).getChildren().get(1).setVisible(false);
            label.setText(label.getText() + "\n" + "You can divide only HOME_HETWORK");
            for(int i = 2; i < 7; i++)
                mainBox.getChildren().get(i).setVisible(false);
            freeSpaces = null;
        }else
        {
            ((HBox) mainBox.getChildren().get(0)).getChildren().get(1).setVisible(true);
            for(int i = 2; i < 7; i++)
                mainBox.getChildren().get(i).setVisible(true);

            List<Network> networkList = new ArrayList<>();
            TreeViewManager.findFreeSpace(item, networkList);
            freeSpaces = networkList;
            ListView<String> listView = ((ListView<String>)mainBox.getChildren().get(3));

            listView.getItems().clear();
            int countFreeNetworks = 0;
            for (Network network : networkList) {
                countFreeNetworks += network.getSize();
                try {
                    listView.getItems().add("["+network.getIp().getIp() + ", " + IP.moveIP(network.getIp(), network.getSize()).getIp()+")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Label countFreeNetworkLabel = (Label) ((HBox)((HBox) mainBox.getChildren().get(0)).getChildren().get(1)).getChildren().get(1);
            countFreeNetworkLabel.setText(String.valueOf(countFreeNetworks));
        }
    }

}
