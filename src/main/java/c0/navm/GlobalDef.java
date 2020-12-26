package c0.navm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GlobalDef {
    // 是否为常量？非零值视为真
    private char isConst;
    // 按字节顺序排列的变量值
    private ArrayList<Character> value;

    public GlobalDef() {
        this.isConst = 0;
        this.value = new ArrayList<>();
    }

    public GlobalDef(char isConst) {
        this.isConst = isConst;
        this.value = new ArrayList<>();
    }

    public GlobalDef(char isConst, int valueLen) {
        this.isConst = isConst;
        this.value = new ArrayList<>();
        for(int i=0; i<valueLen; i++ ){
            this.value.add((char)0);
        }
    }

    public  GlobalDef(char isConst, String varValue) {
        this.isConst = isConst;
        this.value = new ArrayList<>();
        for(int i=0; i<varValue.length(); i++) {
            this.value.add(varValue.charAt(i));
        }
    }

    public char getIsConst() {
        return isConst;
    }

    public void setIsConst(char isConst) {
        this.isConst = isConst;
    }

    public ArrayList<Character> getValue() {
        return value;
    }

    public void setValue(ArrayList<Character> value) {
        this.value = value;
    }


    /**
     * 写入二进制文件
     * @param output
     * @throws IOException
     */
    public void toAssemble(List<Byte> byteList) throws IOException {
        byte by = Assembler.char2Byte(this.isConst);
        byteList.add(by);
        byte []bytes = Assembler.int2Byte(this.value.size());
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        for(Character ch: this.value) {
            byteList.add(Assembler.char2Byte(ch));
        }
        return;
    }
}
