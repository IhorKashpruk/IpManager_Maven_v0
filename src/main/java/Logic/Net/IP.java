package Logic.Net;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Игорь on 21.08.2016.
 */
public class IP {
    private byte[] data = new byte[4];

    public IP(byte[] data) throws Exception {
        if(data.length != 4)
            throw new Exception("Size must be 4. {ctr. Ip(byte[])}");
        System.arraycopy(data, 0, this.data, 0, 4);
    }

    public boolean isGoodIp(String ip){
        if(ip == null)
            return false;
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);

        return matcher.find();
    }

    public IP(String ip) throws Exception {
        if(ip == null)
            throw new Exception("String must be not null. {ctr. Ip(String)}");
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);

        if (matcher.find()) {
            String localIp = matcher.group();
            String[] byteValues = localIp.split("\\.");
            for(int i = 0; i < byteValues.length; i++){
                byteValues[i] = String.valueOf(Integer.parseInt(byteValues[i])-128);
            }
            for(int i = 0; i < 4; i++)
                data[i] = Byte.parseByte(byteValues[i].trim());
        }
        else
            throw new Exception("Error string, must be(0-255.0-255.0-255.0-255). {ctr. Ip(String)}");
    }

    public IP(IP ip) throws Exception {
        this(ip.data);
    }

    public String getIp(){
        String str = String.valueOf(((int)data[0])+128) + ".";
        for(int i = 1; i < 4; i++) {
            str += String.valueOf(((int)data[i])+128);
            if(i != 3) str += ".";
        }
        return str;
    }

    public IP plus(int count){
        while(count > 0){
            int size = 1;
            if(data[3] == 127){
                if(data[2] == 127){
                    if(data[1] == 127){
                        if(data[0] == 127){
                            data[0] = -128;
                        }else
                            data[0] += size;
                        data[1] = -128;
                    }else
                        data[1] += size;
                    data[2] = -128;
                }else
                    data[2] += size;
                data[3] = -128;
            }else {
                size = 127 - data[3];
                size = count-size < 0 ? count : size;
                data[3] += size;
            }
            count -= size;
        }
        return this;
    }

    public IP minus(int count){
        while(count > 0){
            int size = 1;
            if(data[3] == -128){
                if(data[2] == -128){
                    if(data[1] == -128){
                        if(data[0] == -128){
                            data[0] = 127;
                        }else
                            data[0] -= size;
                        data[1] = 127;
                    }else
                        data[1] -= size;
                    data[2] = 127;
                }else
                    data[2] -= size;
                data[3] = 127;
            }else {
                size = data[3];
                size = count-(size+128) < 0 ? count : size+128;
                data[3] -= size;
            }
            count -= size;
        }
        return this;
    }

    public boolean isBiggerThan(IP ip) throws Exception {
        if(ip == null)
            throw new Exception("Ip must be not null. {mth. isBigger(Ip}");
        for(int i = 0; i < 4; i++) {
            if (data[i] > ip.data[i])
                return true;
            if(data[i] < ip.data[i])
                return false;
        }
        return false;
    }

    public static IP moveIP(final IP ip, int size) throws Exception {
        IP result = new IP(ip);
        if(size > 0)
            result.plus(size);
        else
            result.minus(size);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IP ip = (IP) o;

        return Arrays.equals(data, ip.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
