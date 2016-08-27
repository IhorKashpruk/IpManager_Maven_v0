package Logic.Net;

import Logic.MyMath;
import com.opencsv.bean.CsvBind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Игорь on 21.08.2016.
 */
public class Network {

    private final static String[] columns =
            {"ip", "mask", "size", "status", "priority",
                    "client", "typeOfConnection", "date"};
    private IP ip;
    private byte mask;
    private int size;
    private STATUS status;
    private byte priority;
    private String client;
    private String typeOfConnection;
    private Date date;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static void setDateFormat(SimpleDateFormat dateFormat){
        if(dateFormat != null) {
            Network.dateFormat = dateFormat;
        }
    }

    public String getValue(String column){
        switch (column){
            case "ip": return ip.getIp();
            case "mask": return getMaskString();
            case "size": return getSizeString();
            case "status": return getStatusString();
            case "priority": return getPriorityString();
            case "client": return client;
            case "typeOfConnection": return typeOfConnection;
            case "date": return getDateString();
        }
        return "";
    }

    public void setValue(String column, String value) throws Exception {
        switch (column){
            case "ip":              ip = new IP(value); break;
            case "mask":            mask = Byte.valueOf(value); break;
            case "size":            size = Byte.valueOf(value); break;
            case "status":          status = STATUS.createStatus(value); break;
            case "priority":        priority = Byte.parseByte(value); break;
            case "client":          client = value; break;
            case "typeOfConnection":typeOfConnection = value; break;
            case "date":            setDate(value); break;
        }
    }

    public static String[] getColumns() {
        return columns;
    }

    public Network(IP ip, byte mask, int size, STATUS status, byte priority, String client, String typeOfConnection, Date date) {
        this.ip = ip;
        this.mask = mask;
        this.size = size;
        this.status = status;
        this.priority = priority;
        this.client = client;
        this.typeOfConnection = typeOfConnection;
        this.date = date;
    }

    public Network(String ip, String mask, String size, String status, String priority, String client,
                   String typeOfConnection, String date) throws Exception {
        byte localMask;
        if(mask.equals(""))
            localMask = -1;
        else
            localMask = Byte.parseByte(mask);
        if(localMask > 32 || localMask < -1)
            throw new Exception("Mask must be 0-32 or -1 if there is no mask. {ctr. Network(...)}");
        byte localPriority = Byte.parseByte(priority);
        if(localPriority > 5 || localPriority < 1)
            throw new Exception("Priority must be 1-5. {ctr. Network(...)}");
        int localSize = Integer.parseInt(size);
        if(localSize < 0)
            throw new Exception("Size must be > 0. {ctr. Network(...)}");

        this.ip = new IP(ip);
        this.mask = localMask;
        this.size = localSize;
        this.status = STATUS.createStatus(status);
        this.priority = localPriority;
        this.client = client;
        this.typeOfConnection = typeOfConnection;
        if(date != null && !date.isEmpty())
            this.date = dateFormat.parse(date);
        else
            this.date = null;
    }

    public Network(String ip, String mask, String size, String status, String priority) throws Exception {
        this(ip, mask, size, status, priority, null, null, null);
    }
    public Network(Network obj) throws Exception {
        this.ip = new IP(obj.getIp());
        this.mask = obj.getMask();
        this.size = obj.getSize();
        this.status = STATUS.createStatus(obj.getStatusString());
        this.priority = obj.getPriority();
        this.client = obj.getClient();
        this.typeOfConnection = obj.getTypeOfConnection();
        this.setDate(obj.getDateString());
    }

    public IP getIp() {
        return ip;
    }

    public void setIp(IP ip) {
        this.ip = ip;
    }
    public void setIp(String ip) throws Exception {
        this.ip = new IP(ip);
    }

    public byte getMask() {
        return mask;
    }
    public String getMaskString() { return String.valueOf(mask); };

    public void setMask(byte mask) {
        this.mask = mask;
    }
    public void setMask(String mask) {
        this.mask = Byte.valueOf(mask);
    }

    public int getSize() {
        return size;
    }
    public String getSizeString() {return String.valueOf(size);}

    public void setSize(int size) {
        this.size = size;
    }
    public void setSize(String size) {
        this.size = Integer.valueOf(size);
    }

    public STATUS getStatus() {
        return status;
    }
    public String getStatusString() {return status.getValue();}

    public void setStatus(STATUS status) {
        this.status = status;
    }
    public void setStatus(String status) throws Exception {
        this.status = STATUS.createStatus(status);
    }

    public byte getPriority() {
        return priority;
    }
    public String getPriorityString() {return String.valueOf(priority);}

    public void setPriority(byte priority) {
        this.priority = priority;
    }
    public void setPriority(String priority) {
        this.priority = Byte.valueOf(priority);
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTypeOfConnection() {
        return typeOfConnection;
    }

    public void setTypeOfConnection(String typeOfConnection) {
        this.typeOfConnection = typeOfConnection;
    }

    public Date getDate() {
        return date;
    }
    public String getDateString() {return date == null ? "" : dateFormat.format(date); }

    public void setDate(Date date) {
        this.date = date;
    }
    public void setDate(String date) throws ParseException {
        if(date == null || date.isEmpty())
            this.date = null;
        else
            this.date = dateFormat.parse(date);
    }

    public boolean thisIsParrentNetwork(Network network) throws Exception {
        if(network == null)
            return false;
        if(this.equals(network))
            return false;
        IP thisLastIp = IP.moveIP(this.ip, this.size);
        IP otherLastIp = IP.moveIP(network.ip, network.size);
        if((ip.equals(network.getIp()) || ip.isBiggerThan(network.ip)) &&
                (otherLastIp.isBiggerThan(thisLastIp) || thisLastIp.equals(otherLastIp))){
            return true;
        }
        return false;
    }

    public int comparatorForSort(Network network) /*throws Exception*/ {
        if(network == null)
            return -1;
//            throw new Exception("Network must be not null. {mth. isBiggerThan(Network}");
        try {
            if(this.status == STATUS.HOME_NETWORK && network.status == STATUS.HOME_NETWORK)
            {
                return this.size == network.size ? (this.ip.isBiggerThan(network.ip) ? 1 : -1) : this.size > network.size ? -1 : 1;
            }
            if(this.status == STATUS.HOME_NETWORK || network.status == STATUS.HOME_NETWORK){
                return this.status == STATUS.HOME_NETWORK ? -1 : 1;
            }

            return this.ip.isBiggerThan(network.ip) ? 1 : -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
//
//        if(this.size > network.size)
//            return -1;
//        if(this.size < network.size)
//            return 1;
//        if(this.ip.equals(network.ip)){
//            return this.status == STATUS.HOME_NETWORK ? -1 : 1;
//        }
//        int result = 0;
//        try {
//           result = this.ip.isBiggerThan(network.ip) ? 1 : -1;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
    }

    public static Network betweenThem(final Network first, final Network second){
        if(first.equals(second))
            return null;
        int distancesIp = IP.moreOn(second.getIp(), first.getIp());
        if(distancesIp > 0) {
            int size = MyMath.isDivideBy2Entirely(distancesIp) ? (32-MyMath.countDividedBy(distancesIp,2)) : -1;
            try {
                return new Network(new IP(first.getIp()), (byte)size, distancesIp, STATUS.FREE_NETWORK, (byte)5, "", "", null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Network network = (Network) o;

        if (mask != network.mask) return false;
        if (size != network.size) return false;
        return ip != null ? ip.equals(network.ip) : network.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (int) mask;
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return "Network{" +
                "ip=" + ip.getIp() +
                ", mask=" + mask +
                ", size=" + size +
                ", status=" + status +
                ", priority=" + priority +
                ", client='" + client + '\'' +
                ", typeOfConnection='" + typeOfConnection + '\'' +
                ", date=" + getDateString() +
                '}';
    }
}
