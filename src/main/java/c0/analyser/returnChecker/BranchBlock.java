package c0.analyser.returnChecker;

import c0.symbolTable.DType;

public class BranchBlock {

    // 标记这个分支是什么种类
    private BranchType branchType;
    //return的类型
    private DType returnDType;
    //需要有几个return
    private int returnCount;

    public BranchBlock(BranchType branchType, DType dType, int returnCount) {
        this.branchType = branchType;
        this.returnCount = returnCount;
        this.returnDType = dType;
    }


    public int addCount() {
        this.returnCount++;
        return this.returnCount;
    }

    public int subCount() {
        this.returnCount--;
        if(this.returnCount<=0) this.returnCount = 0;
        return this.returnCount;
    }

    public int setCount2Zero() {
        this.returnCount=0;
        return this.returnCount;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public void setBranchType(BranchType branchType) {
        this.branchType = branchType;
    }

    public DType getReturnDType() {
        return returnDType;
    }

    public void setReturnDType(DType returnDType) {
        this.returnDType = returnDType;
    }

    public int getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(int returnCount) {
        this.returnCount = returnCount;
    }
}
