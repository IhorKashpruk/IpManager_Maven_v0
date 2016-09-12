import Controls.CallBacks.ListViewCallback;
import Controls.MyLittleAlert;
import Controls.TreeViewManager;
import Controls.TreeViewPanel.DividePanel;
import Controls.TreeViewPanel.EditNetworkPanel;
import Controls.TreeViewPanel.FindPanel;
import Controls.TreeViewPanel.PathPanel;
import Logic.CSVManager;
import Logic.Net.IP;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import sun.nio.ch.Net;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Игорь on 22.08.2016.
 */
public class Controller {
    @FXML
    private ListView<Network> listViewFreeNet;
    @FXML
    private MenuItem openFileItem;
    @FXML
    private HBox pathPanell;
    @FXML
    private HBox findPanel;
    @FXML
    private HBox findPanel2;
    @FXML
    private VBox dividePanel;
    @FXML
    private HBox editPanel;
    @FXML
    private TreeView<Network> treeView;
    @FXML
    private TreeView<Network> treeView2;
    @FXML
    private HBox editPanel2;
    @FXML
    private Button applaySave;
    @FXML
    private HBox applayBox;

    private String path;
    private FindPanel findPanelClass;
    private EditNetworkPanel editPanelClass;
    private PathPanel pathPanelClass;

    private EditNetworkPanel editPanelClass2;
    private FindPanel findPanelClass2;
    private DividePanel dividePanelClass2;
    private List<Network> list;
    private TreeViewManager viewManager;
    private TreeViewManager viewManager2;
    private Network currentSelectNetwork = null;


    @FXML
    public void initialize(){
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewFreeNet.setCellFactory(new ListViewCallback(false));

        listViewFreeNet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null)
                return;
            if(viewManager2.getRootNode() != null && !currentSelectNetwork.equals(viewManager2.getRootNode().getValue())){
                Optional<ButtonType> answer = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "", "Save changes?", "").showAndWait();
                if(answer.get() == ButtonType.OK)
                    updateMainTree();
            }
            try {
                viewManager2.setRootNode(new TreeItem<>(new Network(newValue)));
                currentSelectNetwork = newValue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewManager2.setData(new ArrayList<>());
            viewManager2.getFindPanel().setRealData(viewManager2.getData());
        });

        TreeItem<Network> rootNode = null;
        try {
            rootNode = new TreeItem<>(new Network("255.255.255.255", "-1", "0", "h", "1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewManager = new TreeViewManager(treeView, rootNode, null, null, null);
        editPanelClass = new EditNetworkPanel(editPanel, viewManager);
        pathPanelClass = new PathPanel(pathPanell, viewManager);
        findPanelClass = new FindPanel(viewManager, findPanel);
        viewManager.setFindPanel(findPanelClass);
        viewManager.setEditNetworkPanel(editPanelClass);
        viewManager.setPathPanel(pathPanelClass);
        viewManager.setFindPanel(findPanelClass);
        viewManager.setFreeNetworkList(listViewFreeNet);

        viewManager2 = new TreeViewManager(treeView2, null, null, null, null);
        viewManager2.setCellFactoryV2();
        editPanelClass2 = new EditNetworkPanel(editPanel2, viewManager2);
        findPanelClass2 = new FindPanel(viewManager2, findPanel2);
        dividePanelClass2 = new DividePanel(viewManager2, dividePanel);
        viewManager2.setEditNetworkPanel(editPanelClass2);
        viewManager2.setFindPanel(findPanelClass2);
        viewManager2.setDividePanel(dividePanelClass2);
    }

    private void updateMainTree(){
        if(viewManager2.getRootNode() == null)
            return;
        if(!viewManager2.getRootNode().getValue().equals(currentSelectNetwork)){
            List<Network> list = new ArrayList<>();
            TreeViewManager.findFreeSpace(viewManager2.getRootNode(), list);
            if(list.size() > 0){
                Optional<ButtonType> answer = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "", "You have free space in you network", "Continue?").showAndWait();
                if(answer.get() != ButtonType.OK)
                    return;
            }
            viewManager.getData().remove(currentSelectNetwork);
            viewManager.getData().add(viewManager2.getRootNode().getValue());
            for(Network network: viewManager2.getData()){
                viewManager.getData().add(network);
            }
            viewManager.upload();
            viewManager.updateFreeNetworks();
            viewManager2.getData().clear();
            viewManager2.setRootNode(null);
        }else
        {
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "There is nothing to change", "").showAndWait();
            return;
        }
    }

    @FXML
    public void openFile(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open .csv file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV File", "*.csv")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            CSVManager csvManager = new CSVManager(selectedFile.getPath(), ';');
            list = null;
            try {
                list = csvManager.readData();
                viewManager.setData(list);
                viewManager.upload();
                viewManager.getFindPanel().setRealData(viewManager.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
            path = selectedFile.getPath();
        }
    }
    @FXML
    public void saveFile(ActionEvent actionEvent){
        if(path == null){
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Save file...", "You have nothing to save.", "").showAndWait();
            return;
        }
        if(viewManager.getFindPanel().getStatus() != STATUS.HOME_NETWORK){
            Optional<ButtonType> result = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "Save file...", "You save only " + viewManager.getFindPanel().getStatus(),
                    "All other data will be overwritten.\nAre you sure?").showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }

        CSVManager csvManager = new CSVManager(path, ';');
        try {
            csvManager.writeData(viewManager.getData());
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Save file as...", "Save complited succesfully.","").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

}

    @FXML
    public void saveFileAs(ActionEvent actionEvent){
        if(viewManager.getData() == null){
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Save file as...", "You have nothing to save.", "").showAndWait();
            return;
        }
        if(viewManager.getFindPanel().getStatus() != STATUS.HOME_NETWORK){
            new MyLittleAlert(Alert.AlertType.INFORMATION, "Save file...", "You save only " + viewManager.getFindPanel().getStatus(), "").showAndWait();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        File selectedFile = fileChooser.showSaveDialog(null);
        if(selectedFile != null) {
            CSVManager csvManager = new CSVManager(selectedFile.getPath(), ';');
            try {
                csvManager.writeData(viewManager.getData());

                new MyLittleAlert(Alert.AlertType.INFORMATION, "Save file as...", "Save complited succesfully.","").showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void applaySaveChange(ActionEvent actionEvent){
        updateMainTree();
    }

}
