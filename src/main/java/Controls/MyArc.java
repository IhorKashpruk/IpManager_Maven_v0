package Controls;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

/**
 * Created by Administrator on 26.08.2016.
 */
public class MyArc {
    private Arc arc;

    public MyArc(Arc arc) {
        this.arc = arc;
    }

    public MyArc(double startAngle) {
        initialize();
        arc.setStartAngle(startAngle);
        arc.setLength(365.0f-startAngle);
    }

    public void setAngle(double startAngle){
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
        arc.setFill(Color.LIGHTGREEN);
        arc.setCenterX(10);
        arc.setCenterY(10);
        arc.setRadiusX(5);
        arc.setRadiusY(5);
        arc.setType(ArcType.ROUND);
    }

    public Arc getArc(){
        return arc;
    }

}
