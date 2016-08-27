package Controls;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

/**
 * Created by Administrator on 26.08.2016.
 */
public class MyArc {
    private Arc arc;
    private Tooltip tooltip;

    public MyArc(Arc arc) {
        this.arc = arc;
    }

    public MyArc(double startAngle) {
        initialize();
        arc.setStartAngle(startAngle);
        arc.setLength(365.0f-startAngle);
    }

    public void setAngle(double startAngle){
        if(startAngle >= 360.0f) {
            tooltip.setText("This network is empty!");
            startAngle = 0.0f;
            setRedColor();
        }
        else {
            if(startAngle != 0.0f)
                tooltip.setText("This network is " + startAngle / 360 * 100 + "% free.");
            else
                tooltip.setText("This network is completely filled.");
            setGreenColor();
        }
        arc.setStartAngle(startAngle);
        arc.setLength(360.0f-startAngle);
    }

    public MyArc() {
        initialize();
    }

    public void setRedColor(){
        arc.setFill(Color.web("#F22613"));
    }

    public void setGreenColor(){
        arc.setFill(Color.web("#2ECC71"));
    }

    private void initialize(){
        arc = new Arc();
        tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: white;" +
                "-fx-background-insets: 0;" +
                "-fx-background-radius: 0 0 0 0;" +
                "-fx-padding: 0.333333em 0.666667em 0.333333em 0.666667em;" +
//                "-fx-font-family: Segoe UI Semilight, Segoe UI Light, Segoe UI, Helvetica, Arial, sans-serif;" +
                "-fx-font-size: 12;" +
                "-fx-text-fill: black;" +
                "-fx-border-color: #808080;" +
                "-fx-border-width: 2px;" +
                "-fx-effect: null;");
        arc.setFill(Color.LIGHTGREEN);
        arc.setCenterX(10);
        arc.setCenterY(10);
        arc.setRadiusX(5);
        arc.setRadiusY(5);
        arc.setType(ArcType.ROUND);
        Tooltip.install(arc, tooltip);
    }

    public Arc getArc(){
        return arc;
    }

}
