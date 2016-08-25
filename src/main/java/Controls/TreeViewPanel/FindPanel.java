package Controls.TreeViewPanel;

import Controls.ComboBoxCallbackStatus;
import Controls.TreeViewManager;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import sun.nio.ch.Net;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Игорь on 22.08.2016.
 */
public class FindPanel {
    private TreeViewManager viewManager;
    private List<Network> realData;
    private HBox mainBox;
    public FindPanel(TreeViewManager viewManager, HBox mainBox) {
        this.viewManager = viewManager;
        this.realData = viewManager.getData();
        this.mainBox = mainBox;

        ComboBox<STATUS> firstComboBox = (ComboBox<STATUS>)mainBox.getChildren().get(0);
        try {
            firstComboBox.getItems().addAll(
                    STATUS.createStatus("z"),
                    STATUS.createStatus("n"),
                    STATUS.createStatus("h"));
            firstComboBox.getSelectionModel().select(STATUS.HOME_NETWORK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        firstComboBox.setCellFactory(new ComboBoxCallbackStatus());
        firstComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != newValue){
                if(newValue != STATUS.HOME_NETWORK) {
                    List<Network> list = new ArrayList<Network>();
                    for(Network network: realData)
                        if(network.getStatus() == newValue)
                            list.add(network);
                    viewManager.setData(list);
                    viewManager.upload();
                }else {
                    viewManager.setData(realData);
                    viewManager.upload();
                }
            }
        });

        ComboBox<String> secondComboBox = (ComboBox<String>)mainBox.getChildren().get(1);
        secondComboBox.getItems().clear();
        List<String> columns = new ArrayList<>();
        for (String str: Network.getColumns())
            if(!str.equals("status"))
                columns.add(str);
        secondComboBox.getItems().addAll(columns);

        TextField findInput = (TextField) mainBox.getChildren().get(2);
        secondComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue){
                case "ip": findInput.setPromptText("Enter ip..."); break;
                case "mask": findInput.setPromptText("Enter mask..."); break;
                case "size": findInput.setPromptText("Enter size..."); break;
                case "priority": findInput.setPromptText("Enter priority{1-5}..."); break;
                case "client": findInput.setPromptText("Enter client..."); break;
                case "typeOfConnection": findInput.setPromptText("Enter type of connection..."); break;
                case "date": findInput.setPromptText("Enter date{23.10.1994}..."); break;
                default: findInput.setPromptText("Enter text...");
            }
        });

        findInput.textProperty().addListener((observable, oldValue, newValue) -> {
            viewManager.getTreeView().getSelectionModel().clearSelection();
            viewManager.selectItems(viewManager.getRootNode(), secondComboBox.getSelectionModel().getSelectedItem(), findInput.getText());
        });

    }

    public void setStatus(STATUS status){
        ((ComboBox<STATUS>)mainBox.getChildren().get(0)).getSelectionModel().select(status);
    }
    public STATUS getStatus(){
        return ((ComboBox<STATUS>)mainBox.getChildren().get(0)).getSelectionModel().getSelectedItem();
    }
}
