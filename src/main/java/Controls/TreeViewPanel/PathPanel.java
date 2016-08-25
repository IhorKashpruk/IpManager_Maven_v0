package Controls.TreeViewPanel;

import Controls.TreeViewManager;
import Logic.Net.Network;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Created by Игорь on 23.08.2016.
 */
public class PathPanel {
    private HBox mainBox;
    private TreeViewManager viewManager;
    private TreeItem<Network> currentTreeItem;

    public PathPanel(HBox mainBox, TreeViewManager viewManager) {
        this.mainBox = mainBox;
        this.viewManager = viewManager;
        currentTreeItem = null;
    }

    public void setPath(TreeItem<Network> treeItem){
        if(treeItem == null || mainBox == null)
            return;
        currentTreeItem = treeItem;
        mainBox.getChildren().clear();

        while(treeItem.getParent() != null){
            HBox pane = new HBox();
            pane.setMaxHeight(26);
            pane.setMinHeight(26);
            pane.setPrefHeight(26);
            pane.setAlignment(Pos.CENTER);

            pane.getChildren().add(new Label(treeItem.getValue().getIp().getIp(), new ImageView(new Image("icons/network.png"))));
            pane.getChildren().add(new Separator(Orientation.VERTICAL));
            pane.getChildren().add(new Label(treeItem.getValue().getMaskString()));
            pane.getChildren().add(new ImageView(new Image("icons/right-arrow.png")));
            pane.hoverProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue)
                    pane.setStyle("-fx-background-color: white;");
                else
                    pane.setStyle("");
            });
            mainBox.getChildren().add(0, pane);
            pane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int n = mainBox.getChildren().indexOf(pane)+1;
                for(; n < mainBox.getChildren().size();) {
                    mainBox.getChildren().remove(n);
                }
                TreeItem<Network> localTreeItem = currentTreeItem;
                String ip = ((Label)((HBox)mainBox.getChildren().get(mainBox.getChildren().indexOf(pane))).getChildren().get(0)).getText();
                String mask = ((Label)((HBox)mainBox.getChildren().get(mainBox.getChildren().indexOf(pane))).getChildren().get(2)).getText();
                while(localTreeItem.getParent() != null) {
                    localTreeItem = localTreeItem.getParent();
                    if(localTreeItem.getValue().getIp().getIp().equals(ip) && localTreeItem.getValue().getMaskString().equals(mask)){
                        viewManager.getTreeView().getSelectionModel().clearSelection();
                        viewManager.getTreeView().getSelectionModel().select(localTreeItem);
                        break;
                    }
                }
            });
            treeItem = treeItem.getParent();
        }

    }
}
