package Controls;

import Controls.CallBacks.ComboBoxCallbackStatus_v2;
import Controls.Dialogs.AddNetworkDialog;
import Controls.Dialogs.MargeNetworkDialog;
import Controls.TreeViewPanel.DividePanel;
import Controls.TreeViewPanel.EditNetworkPanel;
import Controls.TreeViewPanel.FindPanel;
import Controls.TreeViewPanel.PathPanel;
import Logic.MyMath;
import Logic.Net.IP;
import Logic.Net.Network;
import Logic.Net.STATUS;
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

import java.util.*;

/**
 * Created by Игорь on 11.08.2016.
 */


public class TreeViewManager {
    /*Panels*/
    private PathPanel pathPanel;
    private EditNetworkPanel editNetworkPanel;
    private FindPanel findPanel;
    private ListView<Network> freeNetworkList;
    private DividePanel dividePanel = null;

    /*Members*/
    private TreeItem<Network> rootNode;
    private TreeView<Network> treeView;
    private List<Network> data;


    public void setDividePanel(DividePanel dividePanel) {
        this.dividePanel = dividePanel;
    }

    public void setFreeNetworkList(ListView<Network> freeNetworkList) {
        this.freeNetworkList = freeNetworkList;
    }
    public void updateFreeNetworks(){
        if(freeNetworkList != null)
        {
            List<Network> list = new ArrayList<>();
            for(Network network: data){
                if (network.getStatus() == STATUS.FREE_NETWORK)
                    list.add(network);
            }
            freeNetworkList.getItems().clear();
            freeNetworkList.setItems(FXCollections.observableArrayList(list));
        }
    }

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
        this.data = null;
        this.rootNode = rootNode;
        this.treeView = treeView;
        this.pathPanel = pathPanel;
        this.editNetworkPanel = editNetworkPanel;
        this.findPanel = findPanel;
        freeNetworkList = null;
        treeView.setRoot(rootNode);
        treeView.setCellFactory(e -> new CustomCell());
//        treeView.setFocusTraversable(false);

        treeView.setOnMouseClicked(event -> {
            TreeItem<Network> selectItem = treeView.getSelectionModel().getSelectedItem();
            if(selectItem != null){
                if( this.editNetworkPanel != null)
                    this.editNetworkPanel.setNetwork(selectItem);
                if(this.pathPanel != null)
                    this.pathPanel.setPath(selectItem);
                if(this.dividePanel != null)
                    this.dividePanel.setNetwork(selectItem);
            }
        });
        createMenu();
    }

    public void setRootNode(TreeItem<Network> rootNode) {
        this.rootNode = rootNode;
        this.treeView.setRoot(this.rootNode);
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
//            if(network == rootNode){
//                new MyLittleAlert(Alert.AlertType.INFORMATION, "Information", "This is main node", "").showAndWait();
//                return;
//            }
            if(network != null){
                if(network.getValue().getStatus() != STATUS.HOME_NETWORK){
                    newItem.hide();
                    newItem.setDisable(true);
                }else newItem.setDisable(false);
            }
        });

        addHomeSiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.HOME_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.HOME_NETWORK).show();
            updateFreeNetworks();
        });
        addFreeSiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.FREE_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.FREE_NETWORK).show();
            updateFreeNetworks();
        });
        addBusySiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.BUSY_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.BUSY_NETWORK).show();
            updateFreeNetworks();
        });


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
            STATUS status = observableList.get(0).getValue().getStatus();
            for (TreeItem<Network> obj :
                    observableList) {
                if(obj.getParent() != parrent){
                    new MyLittleAlert(Alert.AlertType.WARNING, "Waring","These networks must be in the same home network", "").showAndWait();
                    return;
                }
                if(obj.getValue().getStatus() == STATUS.HOME_NETWORK ){
                    if(status != STATUS.HOME_NETWORK) {
                        new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge with home network", "").showAndWait();
                        return;
                    }
                }
                else
                {
                    if(status == STATUS.HOME_NETWORK) {
                        new MyLittleAlert(Alert.AlertType.WARNING, "Waring", "You can not marge with home network", "").showAndWait();
                        return;
                    }
                }
                count += obj.getValue().getSize();
                status = obj.getValue().getStatus();
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
            ObservableList<TreeItem<Network>> networks = treeView.getSelectionModel().getSelectedItems();
            List<TreeItem<Network>> list = networks.subList(0, networks.size());
            System.out.println(list.size());
            for(int i = list.size()-1; i >= 0; i--) {
                System.out.println(i);
                TreeItem<Network> network = list.get(i);
                if(network == rootNode){
                    new MyLittleAlert(Alert.AlertType.INFORMATION, "Delete network", "You can not delete root network!", "").showAndWait();
                    return;
                }
                if (network != null) {
                    Optional<ButtonType> result = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "Delete network", "Delete " + network.getValue(), "Are you sure?").showAndWait();
                    if (result.get() == ButtonType.OK) {
                        remove(network);
                        network.getParent().getChildren().remove(network);
//                    treeView.getSelectionModel().clearSelection();
                    }
                }
            }
            updateFreeNetworks();
        });
        contextMenu.getItems().addAll(newItem, margeIntoOne, new SeparatorMenuItem(), deleteItem);
        treeView.setContextMenu(contextMenu);
    }

    public TreeView<Network> getTreeView() {
        return treeView;
    }

    public void setData(List<Network> Siec6List){
        data = Siec6List;
        updateFreeNetworks();
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
            selectItems(depNode, column, value);
        }
        if(!start.getValue().getValue(column).isEmpty()){
            if(start.getValue().getValue(column).equals(value))
                treeView.getSelectionModel().select(start);
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
    public static void findFreeNetwork(TreeItem<Network> startNode, List list){
        for(TreeItem<Network> depNode : startNode.getChildren()){
          findFreeNetwork(depNode, list);
        }
        if(startNode.getValue().getStatus() == STATUS.FREE_NETWORK)
            list.add(startNode.getValue());
    }

    public void setCellFactoryV2(){
        treeView.setCellFactory(e->new CustomCellV_2(this));
    }

    public void setCellFactoryV1(){
        treeView.setCellFactory(e->new CustomCell());
    }

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

                    diagram.setGreenColor();
                    double inOnePersent = totalSize / 360.0f;
                    freePersent = n/inOnePersent;

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

    class CustomCellV_2 extends TreeCell<Network> {

        final HBox cellBox = new HBox(10);
        final Label labelIpMasSize = new Label();
        final ComboBox<ImageView> icon = new ComboBox<>();
        final HBox twoBox = new HBox();
        final MyArc diagram = new MyArc();
        final ObservableList<PieChart.Data> datas = FXCollections.observableArrayList();
        final TreeViewManager treeViewManager;

        public CustomCellV_2(TreeViewManager treeViewManager) {
            this.treeViewManager = treeViewManager;
            twoBox.setPadding(new Insets(0,5,0,0));
            HBox.setHgrow(twoBox, Priority.ALWAYS);
            cellBox.setAlignment(Pos.CENTER_LEFT);
            twoBox.setMaxWidth(Double.MAX_VALUE);
            icon.setCellFactory(new ComboBoxCallbackStatus_v2());
            icon.setMaxWidth(35);
            icon.setItems(FXCollections.observableArrayList(
                    new ImageView(new Image("icons/close_network.png")),
                    new ImageView(new Image("icons/open_network.png")),
                    new ImageView(new Image("icons/network.png"))));
            icon.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if(!Objects.equals(oldValue, newValue) && oldValue.intValue() == 2 && getTreeItem().getChildren().size() > 0){
//                    Optional<ButtonType> result = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "Attention",
//                            "This network consists of " + getTreeItem().getChildren().size() + " nodes!!!",
//                            "Are you sure? Remove them?").showAndWait();
//                    if(result.get() == ButtonType.NO || result.get() == ButtonType.CANCEL || result.get() == ButtonType.CLOSE)
//                        return;
                    for (int i = 0; i < getTreeItem().getChildren().size(); i++) {
                        treeViewManager.remove(getTreeItem().getChildren().get(i));
                    }
                    getTreeItem().getChildren().clear();
                }
                getItem().setStatus(newValue.intValue() == 0 ? STATUS.BUSY_NETWORK : newValue.intValue() == 1 ? STATUS.FREE_NETWORK : STATUS.HOME_NETWORK);
                updateItem(getItem(), false);
                getTreeView().getSelectionModel().clearSelection();
                getTreeView().requestFocus();
            });
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
                if(item.getStatus() == STATUS.HOME_NETWORK){
                    icon.getSelectionModel().select(2);
                    cellBox.setStyle("-fx-border-color: darkgrey;");
                }else
                if(item.getStatus() == STATUS.FREE_NETWORK) {
                    cellBox.setStyle("");
                    icon.getSelectionModel().select(1);
                }else {
                    cellBox.setStyle("");
                    icon.getSelectionModel().select(0);
                }
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

                    diagram.setGreenColor();
                    double inOnePersent = totalSize / 360.0f;
                    freePersent = n/inOnePersent;

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
