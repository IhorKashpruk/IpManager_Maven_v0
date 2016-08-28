package Controls;

import Controls.Dialogs.AddNetworkDialog;
import Controls.Dialogs.MargeNetworkDialog;
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
            if(newValue == null)
                return;
            if( this.editNetworkPanel != null)
                this.editNetworkPanel.setNetwork(newValue);
            if(this.pathPanel != null)
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

        addHomeSiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.HOME_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.HOME_NETWORK).show();
        });
        addFreeSiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.FREE_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.FREE_NETWORK).show();
        });
        addBusySiec.setOnAction(event -> {
            TreeItem<Network> siec = (TreeItem<Network>) treeView.getSelectionModel().getSelectedItem();
            if(siec != null){
                new AddNetworkDialog(siec, this, STATUS.BUSY_NETWORK).show();
            }else
                new AddNetworkDialog(rootNode, this, STATUS.BUSY_NETWORK).show();
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
            ObservableList<TreeItem<Network>> networks = treeView.getSelectionModel().getSelectedItems();
            List<TreeItem<Network>> list = networks.subList(0, networks.size());
            for(int i = list.size()-1; i >= 0; i--) {
                TreeItem<Network> network = list.get(i);
                if (network != null) {
                    Optional<ButtonType> result = new MyLittleAlert(Alert.AlertType.CONFIRMATION, "Delete network", "Delete " + network.getValue(), "Are you sure?").showAndWait();
                    if (result.get() == ButtonType.OK) {
                        remove(network);
                        network.getParent().getChildren().remove(network);
//                    treeView.getSelectionModel().clearSelection();
                    }
                }
            }
        });
        contextMenu.getItems().addAll(newItem, margeIntoOne, new SeparatorMenuItem(), deleteItem);
        treeView.setContextMenu(contextMenu);
    }

    public TreeView<Network> getTreeView() {
        return treeView;
    }

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
