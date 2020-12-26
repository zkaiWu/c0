package c0.navm;

public class Assembler {


    /**
     * 将整形转为byte
     * @param num
     * @return
     */
    public static byte[] int2Byte(int num) {
        byte[] bytes= new byte[4];
        for(int i=0; i<4; i++){
            bytes[3-i] = (byte)(num&0xff);
            num=num>>8;
        }
        return bytes;
    }


    /**
     * 将long转换为byte
     * @param num
     * @return
     */
    public static byte[] long2Byte(long num) {
        byte[] bytes = new byte[8];
        for(int i=0; i<8; i++){
            bytes[7-i] = (byte)(num&0xff);
            num=num>>8;
        }
        return bytes;
    }

    /**
     * 将字符转换为byte
     * @param ch
     * @return
     */
    public static byte char2Byte(char ch){
        byte by = (byte)(ch&0xff);
        return by;
    }
}
