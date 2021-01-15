package c0.analyser.BC;


import java.util.ArrayList;
import java.util.Stack;

/**
 * 这是一个为break和continue所做的类
 */
public class BcStack {

    Stack<BcBlock> bcStack;



    public BcStack() {
        this.bcStack = new Stack<>();
    }


    /**
     * 对于while增加一个块
     */
    public void addBlock() {
        this.bcStack.push(new BcBlock());
    }

    /**
     * 增加break的offset
     * @param offset
     */
    public void addBreakOffset(int offset) {
        BcBlock bcBlock = this.bcStack.peek();
        bcBlock.addBreak(offset);
    }

    /**
     * 增加continue的offset
     * @param offset
     */
    public void addContinueOffset(int offset) {
        BcBlock bcBlock = this.bcStack.peek();
        bcBlock.addContinue(offset);
    }

    /**
     * 获取当前的break列表
     * @return
     */
    public ArrayList<Integer> getCurBreakList() {
        BcBlock bcBlock = this.bcStack.peek();
        return bcBlock.getBreakList();
    }


    /**
     * 获取当前的continue 列表
     * @return
     */
    public ArrayList<Integer> getCurContinueList() {
        BcBlock bcBlock = this.bcStack.peek();
        return bcBlock.getContinueList();
    }



    /**
     * 在while离开时，清除一个BcBlock
     */
    public void removeBlock() {
        this.bcStack.pop();
    }


}
