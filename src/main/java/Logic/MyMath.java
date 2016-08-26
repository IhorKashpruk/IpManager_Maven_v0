package Logic;

/**
 * Created by Administrator on 26.08.2016.
 */
public class MyMath {
    public static int countDividedBy(int arg, int number){
        if(arg < 0 || number < 0)
            return -1;
        int count = 0;
        while(arg >= number){
            arg /= number;
            count++;
        }
        return count;
    }

    public static boolean isDivideBy2Entirely(int arg){
        if(arg < 2)
            return false;
        while(arg > 1){
            if(arg % 2 > 0)
                return false;
            arg /= 2;
        }
        return true;
    }
}