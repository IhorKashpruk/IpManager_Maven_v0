package Logic.Net;

/**
 * Created by Игорь on 21.08.2016.
 */
public enum STATUS{
    HOME_NETWORK("h"),
    FREE_NETWORK("n"),
    BUSY_NETWORK("z");

    String value;

    STATUS(String value) {
        this.value = value;
    }

    public static STATUS createStatus(String status) throws Exception {
        if(status == null)
            throw new Exception("String must be not null. {mth. getStatus(String)}");
        if(!status.equals("z") && !status.equals("h") && !status.equals("n"))
            throw new Exception("String must be 'z' || 'n' || 'h'. {mth. getStatus(String)}");
        return status.equals("h") ? HOME_NETWORK : status.equals("z") ? BUSY_NETWORK : FREE_NETWORK;
    }
    public String getValue(){return value;}
}

