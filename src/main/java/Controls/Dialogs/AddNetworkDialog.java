package Controls.Dialogs;

import Controls.TreeViewManager;
import Logic.MyMath;
import Logic.Net.IP;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Игорь on 12.08.2016.
 */
public class AddNetworkDialog {

    private final Stage dialog;
    private TreeItem<Network> item;
    private TreeViewManager manager;
    private VBox mainBox;
    private TextField labelIp;
    private TextField labelMask;
    private TextField labelCountIp;
    private Label labelLog;
    private List<Network> networkList;
    private Label labelIconLog;
    private STATUS networkType;

    public AddNetworkDialog(TreeItem<Network> item, TreeViewManager manager, STATUS type) {
        networkType = type;
        networkList = new ArrayList<>();
        this.manager = manager;
        this.item = item;
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(null);

        createElement();

        Scene dialogScene = new Scene(mainBox, 900, 260);
        String iconPath = networkType == STATUS.HOME_NETWORK ?
                "Icons/network.png" : networkType == STATUS.BUSY_NETWORK ?
                "Icons/close_network.png" : "Icons/open_network.png";
        dialog.getIcons().add(new Image(iconPath));
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.setTitle("Add "+networkType);
        dialog.initStyle(StageStyle.UTILITY);
        dialogScene.getStylesheets().add("styles/main.css");
    }

    private void createElement(){
        HBox topBox = new HBox(5);
        topBox.setAlignment(Pos.CENTER);
        mainBox = new VBox();

        ListView<String> leftNetwork = new ListView<>();
        leftNetwork.setStyle("-fx-border-color: #5c5c5c;");
        leftNetwork.setMinHeight(80);
        VBox leftMainBox = new VBox();
        VBox.setVgrow(leftMainBox, Priority.ALWAYS);

        // додаю вільні місця
        Label textFreeAddres = new Label("Available address:");
        textFreeAddres.setPadding(new Insets(0,0,0,10));
        textFreeAddres.setFont(new Font("System", 14));
        leftMainBox.getChildren().addAll(textFreeAddres, leftNetwork);

        TreeViewManager.findFreeSpace(item, networkList);
        System.out.println(networkList.get(0));

        for (Network network : networkList) {
            try {
                leftNetwork.getItems().add("["+network.getIp().getIp() + ", " + IP.moveIP(network.getIp(), network.getSize()).getIp()+")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        leftNetwork.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                labelIp.setText(newValue.substring(1, newValue.indexOf(','))));

        mainBox.setStyle("-fx-background-color: white;");
        HBox.setHgrow(mainBox, Priority.ALWAYS);
        VBox ridthMainBox = new VBox();
        ridthMainBox.setStyle("-fx-background-color: white;");
        VBox.setVgrow(ridthMainBox, Priority.ALWAYS);
        Label text = new Label("Current network: " + item.getValue().getIp().getIp()+ ", available IP addresses: ");
        text.setPadding(new Insets(10,10,10,10));
        text.setFont(new Font("System", 14));
        text.setAlignment(Pos.CENTER);
        text.setMaxWidth(Double.MAX_VALUE);
        int countIp = 0;

        for (Network aNetworkList : networkList) countIp += aNetworkList.getSize();

        Label labelCountIpTitle = new Label(String.valueOf(countIp));
        labelCountIpTitle.setMaxWidth(Double.MAX_VALUE);
        labelCountIpTitle.setAlignment(Pos.CENTER);
        labelCountIpTitle.setPadding(new Insets(10,10,10,10));
        labelCountIpTitle.setFont(new Font("System", 14));

        topBox.getChildren().addAll(text, labelCountIpTitle);

        HBox centerBox = new HBox(5);
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.setPadding(new Insets(10,10,10,10));

        labelIp = new TextField();
        labelIp.setPromptText("Network address..."); labelIp.setMinWidth(110);

        labelMask = new TextField();
        labelMask.setPromptText("Mask..."); labelMask.setMinWidth(50);
        labelCountIp = new TextField();
        labelCountIp.setPromptText("Count ip..."); labelCountIp.setMinWidth(60);

        ComboBox<String> labelPriority = new ComboBox<>();
        labelPriority.setMinWidth(60);
        labelPriority.setMinHeight(25);
        labelPriority.getItems().addAll("1", "2", "3", "4", "5");
        labelPriority.getSelectionModel().select(4);

        TextField labelClient = new TextField();
        labelClient.setPromptText("Enter client name...");
        labelClient.setMinWidth(200);
        TextField labelType = new TextField();
        labelType.setPromptText("Enter type of connection...");
        labelType.setMinWidth(180);
        DatePicker datePicker = new DatePicker();
        String pattern = "dd.MM.yyyy";
        datePicker.setMinWidth(80);
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

        centerBox.getChildren().addAll(labelIp, labelMask, labelCountIp, labelPriority, labelClient, labelType, datePicker);

        HBox logBox = new HBox(10);
        ImageView imageView = new ImageView();
        labelIconLog = new Label("", imageView);
        labelIconLog.setPadding(new Insets(0,5,0,10));
        labelLog = new Label();

        logBox.getChildren().addAll(labelIconLog, labelLog);

        HBox bottonBox = new HBox(5);
        bottonBox.setAlignment(Pos.CENTER_RIGHT);
        bottonBox.setPadding(new Insets(10,10,10,10));
        Button buttonOk = new Button("Ok");
        Button buttonCancel = new Button("Cancel");

        buttonCancel.setOnAction(event -> dialog.close());


        bottonBox.getChildren().addAll(buttonCancel, buttonOk);


        // Listeners
        // lableIp
        labelIp.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() != 0){
                int len = newValue.length();
                char symbol = newValue.charAt(len-1);
                if((symbol < '0' || symbol > '9') && symbol != '.'){
                    labelIp.replaceText(len-1, len, "");
                }
            }
            isGood();
        });

        labelMask.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(labelMask.getText().isEmpty())
                return;
            if(Integer.parseInt(labelMask.getText()) > 32){
                labelMask.setText("32");
            }
            int localMask = Integer.parseInt(labelMask.getText());
            if(!labelCountIp.getText().equals(String.valueOf(Math.pow(2, 32 - localMask))))
                labelCountIp.setText(String.valueOf((int)Math.pow(2, 32-localMask)));
        });
        labelMask.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                labelMask.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        labelCountIp.focusedProperty().addListener((observable, oldValue, newValue) ->{
            String str = labelCountIp.getText();
            if(str.isEmpty() || str.length() > 9 || str.equals("1") || str.equals("0"))
                return;
            int localCountIp = Integer.parseInt(str);

            int countDivided2 = MyMath.countDividedBy(localCountIp, 2);
            if(MyMath.isDivideBy2Entirely(localCountIp)){
                labelMask.setText(String.valueOf(32 - countDivided2));
            }else {
                switch (networkType){
                    case HOME_NETWORK:
                        break;
                    default:
                        labelCountIp.setText(String.valueOf((int)Math.pow(2, countDivided2+1)));
                        labelMask.setText(String.valueOf(32 - countDivided2-1));
                }
            }
            isGood();
        });
        labelCountIp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                labelCountIp.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // buttonOk
        buttonOk.setOnAction(event -> {
            if(!isGood()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText("Enter the correct data!");
                alert.showAndWait();
                return;
            }

            // Після перевірок
            String networkId =  networkType == STATUS.FREE_NETWORK ? "n" : networkType == STATUS.BUSY_NETWORK ? "z" : "h";
            Network siec6 = null;
            try {
                siec6 = new Network(labelIp.getText(), labelMask.getText(), labelCountIp.getText(),
                        networkId, labelPriority.getSelectionModel().getSelectedItem(), labelClient.getText(), labelType.getText(), datePicker.getConverter().toString(datePicker.getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            manager.getData().add(siec6);
            item.getValue().setStatus(STATUS.HOME_NETWORK);
            manager.upload();
            manager.selectItem(manager.getRootNode(), siec6);
            dialog.close();
        });


        ridthMainBox.getChildren().addAll(topBox, new Separator(Orientation.HORIZONTAL),
                centerBox, new Separator(Orientation.HORIZONTAL),
                logBox, new Separator(Orientation.HORIZONTAL));
        mainBox.getChildren().addAll(ridthMainBox, leftMainBox,new Separator(Orientation.HORIZONTAL), bottonBox);
    }

    private boolean isGood(){

        if(labelIp.getText().isEmpty()){
            showError("Ip is empty!", labelIp);
            return false;
        }
        if(labelCountIp.getText().isEmpty()){
            showError("Count ip is empty!", labelCountIp);
            return false;
        }

        // Перевірка на правильність
        // IP
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(labelIp.getText());


        try {
            if (matcher.find()) {
                String ipAddress = matcher.group();
                String status = networkType == STATUS.BUSY_NETWORK ? "z" : networkType == STATUS.HOME_NETWORK ? "h" : "n";
                Network newNetwork = new Network(ipAddress, labelMask.getText(), labelCountIp.getText(), status, "1", "", "", null);
                boolean isInto = false;
                for (Network network : networkList) {
                    System.out.println(network);
                    System.out.println(newNetwork);
                    if (newNetwork.thisIsParrentNetwork(network)) {
                        isInto = true;
                        break;
                    }
                }
                if (!isInto) {
                    showError("You haven't enough ip address!!", labelIp);
                    return false;
                }
            } else {
                showError("You haven't enough ip address!!", labelIp);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        showSuccess();
        return true;
    }

    public void show(){
        dialog.show();
    }

    private void showError(String message, TextField textField){
        String error_style = "-fx-border-color: lightcoral;-fx-border-width: 2px;";
        if(textField != null)
            textField.setStyle(error_style);
        labelIconLog.setGraphic(new ImageView(new Image("Icons/error.png")));

        labelLog.setText(message);
    }
    private void showSuccess(){
        labelIconLog.setGraphic(new ImageView(new Image("Icons/success.png")));
        labelCountIp.setStyle("");
        labelIp.setStyle("");
        labelMask.setStyle("");
    }

}