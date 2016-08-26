package Controls;

import Controls.TreeViewPanel.EditNetworkPanel;
import Controls.TreeViewPanel.FindPanel;
import Controls.TreeViewPanel.PathPanel;
import Logic.MyMath;
import Logic.Net.IP;
import Logic.Net.Network;
import Logic.Net.STATUS;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import sun.nio.ch.Net;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by Игорь on 11.08.2016.
 */


public class TreeViewManager {
    /*Panels*/
    private PathPanel pathPanel;
    private EditNetworkPanel editNetworkPanel;
    private FindPanel findPanel;
    /*Members*/
    private TreeItem<Network> rootNode;
    private TreeView<Network> treeView;
    private List<Network> data;

    public void setEditNetworkPanel(EditNetworkPanel editNetworkPanel) {
        this.editNetworkPanel = editNetworkPanel;
    }

    public void setPathPanel(PathPanel pathPanel) {
        this.pathPanel = pathPanel;
    }

    public void setFindPanel(FindPanel findPanel) {
        this.findPanel = findPanel;
    }

    public FindPanel getFindPanel() {
        return findPanel;
    }

    public TreeItem<Network> getRootNode() {
        return rootNode;
    }

    public TreeViewManager(TreeView<Network> treeView, TreeItem<Network> rootNode, PathPanel pathPanel, EditNetworkPanel editNetworkPanel, FindPanel findPanel) {
        this.rootNode = rootNode;
        this.treeView = treeView;
        this.pathPanel = pathPanel;
        this.editNetworkPanel = editNetworkPanel;
        this.findPanel = findPanel;
        treeView.setRoot(rootNode);
        treeView.setCellFactory(e -> new CustomCell());

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if( this.editNetworkPanel != null && newValue != null)
                this.editNetworkPanel.setNetwork(newValue);
            if(this.pathPanel != null && newValue != null)
                this.pathPanel.setPath(newValue);
        });
        createMenu();
    }

    private void createMenu(){
        final ContextMenu contextMenu = new ContextMenu();
        Menu newItem = new Menu("New");
        MenuItem addHomeSiec = new MenuItem("Home network", new ImageView(new Image("Icons/network.png")));
        MenuItem addFreeSiec = new MenuItem("Free network", new ImageView(new Image("Icons/open_network.png")));
        MenuItem addBusySiec = new MenuItem("Busy network", new ImageView(new Image("Icons/close_network.png")));

        newItem.getItems().addAll(addHomeSiec, addFreeSiec, addBusySiec);
        contextMenu.setOnShowing(observable -> {
            TreeItem<Network> network = treeView.getSelectionModel().getSelectedItem();
            if(network == rootNode){
                new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "This is main node", "").showAndWait();
                return;
            }
            if(network != null){
                if(network.getValue().getStatus() != STATUS.HOME_NETWORK){
                    newItem.hide();
                    newItem.setDisable(true);
                }else newItem.setDisable(false);
            }
        });

//        addHomeSiec.setOnAction(event -> {
//            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.HOME_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.HOME_NETWORK).show();
//        });
//        addFreeSiec.setOnAction(event -> {
//            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.FREE_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.FREE_NETWORK).show();
//        });
//        addBusySiec.setOnAction(event -> {
//            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.BUSY_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.BUSY_NETWORK).show();
//        });


        MenuItem margeIntoOne = new MenuItem("Marge...");
        margeIntoOne.setOnAction(event -> {
            ObservableList<TreeItem<Network>> observableList = getTreeView().getSelectionModel().getSelectedItems();
            if(observableList.size() == 0){
                new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "Select item!", "").showAndWait();
                return;
            }
            if(observableList.size() == 1){
                new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge 1 network", "").showAndWait();
                return;
            }

            // Перевірити чи знаходяиться в одній підсети
            TreeItem<Network> parrent = observableList.get(0).getParent();
            int count = 0;
            for (TreeItem<Network> obj :
                    observableList) {
                if(obj.getParent() != parrent){
                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring","These networks must be in the same home network", "").showAndWait();
                    return;
                }
                if(obj.getValue().getStatus() == STATUS.HOME_NETWORK){
                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge with home network", "").showAndWait();
                    return;
                }
                count += obj.getValue().getSize();
            }

            if(!MyMath.isDivideBy2Entirely(count)){
                new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not create a network consisting of " + count +" computers.", "").showAndWait();
                return;
            }

            List<Network> list = new ArrayList<>();
            for (TreeItem<Network> item1 : observableList){
                list.add(item1.getValue());
            }
            Collections.sort(list, Network::comparatorForSort);

            for (Network s :
                    list) {
                System.out.println(s);
            }

            for (int i = 0; i < list.size()-1; i++){
                Network network = list.get(i);
                Network network2 = list.get(i+1);
                IP ip = null;
                try {
                    ip = IP.moveIP(network.getIp(), network.getSize());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!ip.equals(network2.getIp())){
                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "There is a space or other network between networks", "").showAndWait();
                    return;
                }
            }

            new MargeNetworkDialog(parrent,this, observableList).show();
        });

        MenuItem deleteItem = new MenuItem("Delete...");
        deleteItem.setOnAction(e -> {
            TreeItem<Network> siec = treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Information");
                alert.setHeaderText("Delete network: " + siec.getValue());
                alert.setContentText("Are you sure?");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK){
                    remove(siec);
                    siec.getParent().getChildren().remove(siec);
                    treeView.getSelectionModel().clearSelection();
//                    upload();
//                    selectItem(rootNode, parrentSiec.getValue());
                }
            }
        });

        contextMenu.getItems().addAll(newItem, margeIntoOne, new SeparatorMenuItem(), deleteItem);
        treeView.setContextMenu(contextMenu);
    }

    public TreeView<Network> getTreeView() {
        return treeView;
    }


    //
//    public TreeViewManager(TreeView treeView, HBox editPanel) throws Exception {
//        this.editPanel = editPanel;
//        final ContextMenu contextMenu = new ContextMenu();
//
//        Menu newItem = new Menu("New");
//        MenuItem addHomeSiec = new MenuItem("Home network", new ImageView(new Image("Icons/network.png")));
//        MenuItem addFreeSiec = new MenuItem("Free network", new ImageView(new Image("Icons/open_network.png")));
//        MenuItem addBusySiec = new MenuItem("Busy network", new ImageView(new Image("Icons/close_network.png")));
//
//        newItem.getItems().addAll(addHomeSiec, addFreeSiec, addBusySiec);
//        contextMenu.setOnShowing(observable -> {
//            TreeItem<Siec6> siec = (TreeItem<Siec6>) treeView.getSelectionModel().getSelectedItem();
//            if(siec == rootNode){
//                new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "This is main node", "").showAndWait();
//                return;
//            }
//            if(siec != null){
//                if(siec.getValue().getStatus() != null &&
//                        !siec.getValue().getStatus().equals("")){
//                    newItem.hide();
//                    newItem.setDisable(true);
//                }else newItem.setDisable(false);
//            }
//        });
//
//        addHomeSiec.setOnAction(event -> {
//            TreeItem<Siec6> siec = (TreeItem<Siec6>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.HOME_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.HOME_NETWORK).show();
//        });
//        addFreeSiec.setOnAction(event -> {
//            TreeItem<Siec6> siec = (TreeItem<Siec6>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.FREE_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.FREE_NETWORK).show();
//        });
//        addBusySiec.setOnAction(event -> {
//            TreeItem<Siec6> siec = (TreeItem<Siec6>) treeView.getSelectionModel().getSelectedItem();
//            if(siec != null){
//                new AddSiecDialog(siec, this, AddSiecDialog.NETWORK_TYPE.BUSY_NETWORK).show();
//            }else
//                new AddSiecDialog(rootNode, this, AddSiecDialog.NETWORK_TYPE.BUSY_NETWORK).show();
//        });
//
//        MenuItem margeIntoOne = new MenuItem("Marge...");
//        margeIntoOne.setOnAction(event -> {
//            ObservableList<TreeItem<Siec6>> observableList = getTreeView().getSelectionModel().getSelectedItems();
//            if(observableList.size() == 0){
//                new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "Select item!", "").showAndWait();
//                return;
//            }
//            if(observableList.size() == 1){
//                new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge 1 network", "").showAndWait();
//                return;
//            }
//
//            // Перевірити чи знаходяиться в одній підсети
//            TreeItem<Siec6> parrent = observableList.get(0).getParent();
//            int count = 0;
//            for (TreeItem<Siec6> obj :
//                    observableList) {
//                if(obj.getParent() != parrent){
//                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring","These networks must be in the same home network", "").showAndWait();
//                    return;
//                }
//                if(obj.getValue().getStatus() == null || obj.getValue().getStatus().equals("")){
//                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge with home network", "").showAndWait();
//                    return;
//                }
//                count += Integer.parseInt(obj.getValue().getCountIp());
//            }
//
//            if(!MyMath.isDivideBy2Entirely(count)){
//                new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not create a network consisting of " + count +" computers.", "").showAndWait();
//                return;
//            }
//
//            List<Siec6> list = new ArrayList<>();
//            for (TreeItem<Siec6> item1 : observableList){
//                list.add(item1.getValue());
//            }
//            Collections.sort(list, (o1, o2) ->
//                    Siec6.isBigger(o1.getAddress(), o2.getAddress()) ? 1 : -1);
//
//            for (Siec6 s :
//                    list) {
//                System.out.println(s);
//            }
//
//            for (int i = 0; i < list.size()-1; i++){
//                Siec6 siec6 = list.get(i);
//                Siec6 siec6_2 = list.get(i+1);
//                String str = Siec6.generatedIpSiec(siec6.getAddress(), Integer.parseInt(siec6.getCountIp()));
//                System.out.println("str = " + str + ", str2 = " + siec6_2.getAddress());
//                if(!str.equals(siec6_2.getAddress())){
//                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "There is a space or other network between networks", "").showAndWait();
//                    return;
//                }
//            }
//
//            new MargeSiecDialog(parrent,this, observableList).show();
//
//        });
//
//        MenuItem deleteItem = new MenuItem("Delete...");
//        deleteItem.setOnAction(e -> {
//            TreeItem<Siec6> siec = (TreeItem<Siec6>) treeView.getSelectionModel().getSelectedItem();
////            TreeItem<Siec6> parrentSiec = siec.getParent();
//            if(siec != null){
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle("Information");
//                alert.setHeaderText("Delete network: " + siec.getValue());
//                alert.setContentText("Are you sure?");
//                Optional<ButtonType> result = alert.showAndWait();
//                if(result.get() == ButtonType.OK){
//                    remove(siec);
//                    siec.getParent().getChildren().remove(siec);
//                    treeView.getSelectionModel().clearSelection();
////                    upload();
////                    selectItem(rootNode, parrentSiec.getValue());
//                }
//            }
//        });
//
//        contextMenu.getItems().addAll(newItem, new SeparatorMenuItem(), margeIntoOne, deleteItem);
//
//        rootNode.setExpanded(true);
//        this.treeView = treeView;
//        this.treeView.setRoot(rootNode);
//        treeView.setContextMenu(contextMenu);
//
//
//        treeView.setCellFactory(e -> new CustomCell());
//        ComboBox<ImageView> statusComboBox = (ComboBox<ImageView>) editPanel.getChildren().get(3);
//        statusComboBox.getItems().addAll(
//                new ImageView(new Image("Icons/plus.png")),
//                new ImageView(new Image("Icons/close_network.png")),
//                new ImageView(new Image("Icons/network.png"))
//        );
//        statusComboBox.setCellFactory(new ComboBoxCallback());
//
//        DatePicker datePicker = (DatePicker) editPanel.getChildren().get(7);
//        datePicker.setPromptText("yyyy-MM-dd".toLowerCase());
//        StringConverter converter = new StringConverter<LocalDate>() {
//            DateTimeFormatter dateFormatter =
//                    DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//            @Override
//            public String toString(LocalDate date) {
//                if (date != null) {
//                    return dateFormatter.format(date);
//                } else {
//                    return "";
//                }
//            }
//
//            @Override
//            public LocalDate fromString(String string) {
//                if (string != null && !string.isEmpty()) {
//                    return LocalDate.parse(string, dateFormatter);
//                } else {
//                    return null;
//                }
//            }
//        };
//        datePicker.setConverter(converter);
//
//        ComboBox<String> priorityComboBox = (ComboBox<String>) editPanel.getChildren().get(4);
//        priorityComboBox.getItems().addAll("1", "2", "3", "4", "5");
//
//        Button saveButton = (Button) editPanel.getChildren().get(editPanel.getChildren().size()-1);
//        saveButton.setOnAction(event -> {
//            if(currentEditSiec == null)
//                return;
//            int selectedItem = statusComboBox.getSelectionModel().getSelectedIndex();
//            currentEditSiec.setStatus(selectedItem == 0 ? "n" : selectedItem == 1 ? "z" : "");
//            currentEditSiec.setPriority(priorityComboBox.getSelectionModel().getSelectedItem());
//            currentEditSiec.setClient(((TextField)editPanel.getChildren().get(5)).getText());
//            currentEditSiec.setType(((TextField)editPanel.getChildren().get(6)).getText());
//            currentEditSiec.setDate(((DatePicker)editPanel.getChildren().get(7)).getValue().toString());
//
//        });
//        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue == null)
//                return;
//            currentEditSiec = ((TreeItem<Siec6>)newValue).getValue();
//            ((TextField)editPanel.getChildren().get(0)).setText(currentEditSiec.getAddress());
//            ((TextField)editPanel.getChildren().get(1)).setText(currentEditSiec.getMask());
//            ((TextField)editPanel.getChildren().get(2)).setText(currentEditSiec.getCountIp());
//            ((ComboBox<ImageView>) editPanel.getChildren().get(3)).getSelectionModel().select(
//                    currentEditSiec.getStatus() == null || currentEditSiec.getStatus().isEmpty() ?
//            2 : currentEditSiec.getStatus().equals("z") ? 1 : 0);
//            ((ComboBox<String>) editPanel.getChildren().get(4)).getSelectionModel().select(
//                    currentEditSiec.getPriority() == null || currentEditSiec.getPriority().isEmpty() ? 0 : Integer.parseInt(currentEditSiec.getPriority())-1);
//            ((TextField)editPanel.getChildren().get(5)).setText(currentEditSiec.getClient());
//            ((TextField)editPanel.getChildren().get(6)).setText(currentEditSiec.getType());
//            ((DatePicker)editPanel.getChildren().get(7)).setValue(((DatePicker)editPanel.getChildren().get(7)).getConverter().fromString(currentEditSiec.getDate()));
//        });
//    }
//
//
    public void setData(List<Network> Siec6List){

        data = Siec6List;
    }

    public List<Network> getData() {
        return data;
    }

    public void upload() {
        Collections.sort(data, Network::comparatorForSort);
        rootNode.getChildren().clear();
        try {
            for (Network siec :
                    data) {
                TreeItem<Network> empLeaf = new TreeItem<>(siec);
                TreeItem<Network> node = searchParent(rootNode, siec);
                node.getChildren().add(empLeaf);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private TreeItem<Network> searchParent(final TreeItem<Network> node, final Network network) throws Exception {
        for (TreeItem<Network> depNode : node.getChildren()){
            if(network.thisIsParrentNetwork(depNode.getValue())){
                return searchParent(depNode, network);
            }
        }
        return node;
    }

    public TreeItem<Network> find(final TreeItem<Network> startNode, Network network){
        for(final TreeItem<Network> depNode : startNode.getChildren()){
            if(depNode.getValue().equals(network)){
                return depNode;
            }else{
                TreeItem<Network> node = find(depNode, network);
            if(node != null)
                return node;
            }
        }
        return null;
    }

    public TreeItem<Network> find(TreeItem<Network> startNode, String column, String value){
        for(TreeItem<Network> depNode : startNode.getChildren()){
            if(depNode.getValue().getValue(column).equals(value)){
                return depNode;
            }else {
                TreeItem<Network> node = find(depNode, column, value);
                if(node != null)
                    return node;
            }
        }
        return null;
    }

    public void selectItems(TreeItem<Network> start, String column, String value){
        for(TreeItem<Network> depNode : start.getChildren()){
            if(!depNode.getValue().getValue(column).isEmpty()){
                if(depNode.getValue().getValue(column).equals(value))
                    treeView.getSelectionModel().select(depNode);
            }
            selectItems(depNode, column, value);
        }
    }
    public void selectItem(TreeItem<Network> start, Network network){
        for(TreeItem<Network> depNode : start.getChildren()){
            if(depNode.getValue() == network){
                treeView.getSelectionModel().select(depNode);
                return;
            }
            selectItem(depNode, network);
        }
    }

    public void remove(TreeItem<Network> start){
        for(TreeItem<Network> depNode : start.getChildren()){
            remove(depNode);
        }
        data.remove(start.getValue());
    }

    public static void findFreeSpace(TreeItem<Network> treeItem, List list){
        for (int i = 0; i < treeItem.getChildren().size(); i++) {
            findFreeSpace(treeItem.getChildren().get(i), list);
        }
        try {
        Network network = treeItem.getValue();
        if(treeItem.getChildren().size() > 0){
            if(!network.getIp().equals(treeItem.getChildren().get(0).getValue().getIp())){
                Network newNetwork = Network.betweenThem(network, treeItem.getChildren().get(0).getValue());
                if(newNetwork != null)
                    list.add(newNetwork);
            }
            for(int i = 0; i < treeItem.getChildren().size()-1; i++){
                Network currentNetwork = treeItem.getChildren().get(i).getValue();
                currentNetwork = new Network(IP.moveIP(currentNetwork.getIp(), currentNetwork.getSize()), (byte)-1, 1, STATUS.FREE_NETWORK, (byte)5, "", "", null);
                Network nextNetwork    =  treeItem.getChildren().get(i+1).getValue();

                Network newNetwork = Network.betweenThem(currentNetwork, nextNetwork);
                if(newNetwork != null){
                    list.add(newNetwork);
                }
            }

            Network lastNetwork = treeItem.getChildren().get(treeItem.getChildren().size()-1).getValue();
            lastNetwork = new Network(IP.moveIP(lastNetwork.getIp(), lastNetwork.getSize()), (byte)-1, 1, STATUS.FREE_NETWORK, (byte)5, "", "", null);
            Network nextNetworkStart = new Network(IP.moveIP(network.getIp(), network.getSize()), (byte)-1, 1, STATUS.FREE_NETWORK, (byte)5, "", "", null);
            Network newNetwork = Network.betweenThem(lastNetwork, nextNetworkStart);
            if(newNetwork != null){
                    list.add(newNetwork);
            }
        }else
        {
            if(network.getStatus() == STATUS.HOME_NETWORK){
                list.add(new Network(network));
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    public TreeItem<Siec6> getRootNode() {
//        return rootNode;
//    }
//
    class CustomCell extends TreeCell<Network> {

        final HBox cellBox = new HBox(10);
        final Label labelIpMasSize = new Label();
        final Label icon = new Label();
        final HBox twoBox = new HBox();
        final MyArc diagram = new MyArc();
        final ObservableList<PieChart.Data> datas = FXCollections.observableArrayList();

    public CustomCell() {
        twoBox.setPadding(new Insets(0,5,0,0));
        HBox.setHgrow(twoBox, Priority.ALWAYS);
        twoBox.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
        protected void updateItem(Network item, boolean empty) {
            super.updateItem(item, empty);

            // If the cell is empty we don't show anything.
            if (isEmpty() || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                labelIpMasSize.setText(item.getIp().getIp() + "\t[" + item.getMaskString() + "]\t{"+item.getSizeString()+"}");
                String url;
                if(item.getStatus() == STATUS.HOME_NETWORK){
                    url = "Icons/network.png";
                    cellBox.setStyle("-fx-border-color: darkgrey;");
                }else
                if(item.getStatus() == STATUS.FREE_NETWORK) {
                    cellBox.setStyle("");
                    url = "Icons/open_network.png";
                }else {
                    cellBox.setStyle("");
                    url = "Icons/close_network.png";
                }
                icon.setGraphic(new ImageView(new Image(url)));
                cellBox.getChildren().clear();
                cellBox.getChildren().addAll(icon, labelIpMasSize);

                if(item.getStatus() == STATUS.HOME_NETWORK) {
                    datas.clear();
                    List<Network> networks = new ArrayList<>();
                    TreeViewManager.findFreeSpace(getTreeItem(), networks);

                    double n = 0;
                    double totalSize = item.getSize();

                    for (Network network : networks) n += network.getSize();
                    double freePersent;
                    if(n == totalSize) {
                        diagram.setRedColor();
                        freePersent = 0;
                    }
                    else {
                        diagram.setGreenColor();
                        double inOnePersent = totalSize / 360.0f;
                        freePersent = n/inOnePersent;
                    }

                    diagram.setAngle(freePersent);
                    twoBox.setAlignment(Pos.CENTER_RIGHT);
                    twoBox.getChildren().clear();
                    twoBox.getChildren().add(diagram.getArc());
                    cellBox.getChildren().add(twoBox);
                }

                setGraphic(cellBox);
                setText(null);

            }
        }
    }
}
