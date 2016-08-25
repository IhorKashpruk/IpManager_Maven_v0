import Controls.TreeViewManager;
import Controls.TreeViewPanel.EditNetworkPanel;
import Controls.TreeViewPanel.FindPanel;
import Controls.TreeViewPanel.PathPanel;
import Logic.CSVManager;
import Logic.Net.IP;
import Logic.Net.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Игорь on 22.08.2016.
 */
public class Controller {
    @FXML
    private MenuItem openFileItem;
    @FXML
    private HBox pathPanell;
    @FXML
    private HBox findPanel;
    @FXML
    private HBox editPanel;
    @FXML
    private TreeView<Network> treeView;

    private FindPanel findPanelClass;
    private EditNetworkPanel editPanelClass;
    private PathPanel pathPanelClass;
    private List<Network> list;
    private TreeViewManager viewManager;

    @FXML
    public void initialize(){

        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TreeItem<Network> rootNode = null;
        try {
            rootNode = new TreeItem<>(new Network("255.255.255.255", "-1", "0", "h", "1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewManager = new TreeViewManager(treeView, rootNode, null, null, null);
        editPanelClass = new EditNetworkPanel(editPanel, viewManager);
        pathPanelClass = new PathPanel(pathPanell, viewManager);
        viewManager.setEditNetworkPanel(editPanelClass);
        viewManager.setPathPanel(pathPanelClass);
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
                findPanelClass = new FindPanel(viewManager, findPanel);
                viewManager.setFindPanel(findPanelClass);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void saveFile(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        File selectedFile = fileChooser.showSaveDialog(null);
        if(selectedFile != null) {
            CSVManager csvManager = new CSVManager(selectedFile.getPath(), ';');
            try {
                csvManager.writeData(list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void click(ActionEvent actionEvent){

    }

}
