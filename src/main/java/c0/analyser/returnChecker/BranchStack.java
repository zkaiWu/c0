package c0.analyser.returnChecker;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;
import c0.symbolTable.DType;
import c0.util.Pos;

import java.util.ArrayList;

public class BranchStack {

    private ArrayList<BranchBlock> branchBlocks;


    public BranchStack() {
        this.branchBlocks = new ArrayList<>();
    }


    public void addFnBranch(DType dType) {
        BranchBlock fnBranch = new BranchBlock(BranchType.FUNC, dType, 1);
        this.branchBlocks.add(fnBranch);
    }

    /**
     * 添加一个if分支
     */
    public void addIfBranch() {
        DType dType= this.branchBlocks.get(this.branchBlocks.size()-1).getReturnDType();
        this.branchBlocks.get(this.branchBlocks.size()-1).addCount();
        BranchBlock ifBranch = new BranchBlock(BranchType.IF, dType, 1);
        this.branchBlocks.add(ifBranch);
    }


    /**
     * 添加一个else分支
     */
    public void addElseBranch() {
        System.out.println(this.branchBlocks.size());
        DType dType= this.branchBlocks.get(this.branchBlocks.size()-1).getReturnDType();
        BranchBlock elseBrach  = new BranchBlock(BranchType.ELSE, dType, 1);
        this.branchBlocks.add(elseBrach);
    }


    /**
     * 添加一个while分支
     */
    public void addWhileBranch() {
        DType dType= this.branchBlocks.get(this.branchBlocks.size()-1).getReturnDType();
        this.branchBlocks.get(this.branchBlocks.size()-1).addCount();
        BranchBlock whileBranch = new BranchBlock(BranchType.WHILE, dType, 1);
        this.branchBlocks.add(whileBranch);
    }


    /**
     * 当前的分支有return语句
     * @param dType
     */
    public void returnOnce(DType dType, Pos pos) throws AnalyzeError {

        BranchBlock branchBlock = this.branchBlocks.get(this.branchBlocks.size()-1);

        if(dType != branchBlock.getReturnDType()) {
            throw new AnalyzeError(ErrorCode.ReturnTypeNotMatched, pos);
        }
        branchBlock.setCount2Zero();
    }

    /**
     * 这个分支退出，如果没有所有分支都覆盖到，则抛出异常，用于else，if，while等分支
     * 如果正常退出，则将其父节点的returnCount-1
     * @throws AnalyzeError
     */
    public void quitBranch(Pos pos) throws AnalyzeError {
        BranchBlock branchBlock = this.branchBlocks.get(this.branchBlocks.size()-1);
        this.branchBlocks.remove(this.branchBlocks.size()-1);
        if(branchBlock.getBranchType()!=BranchType.FUNC && branchBlock.getReturnCount()==0) {
            this.branchBlocks.get(this.branchBlocks.size()-1).subCount();
        }
    }


    /**
     * 一个函数退出，要区分其是否为void，不为void则强制需要参数
     * @param pos
     * @return
     * @throws AnalyzeError
     */
    public boolean quitFunc(Pos pos) throws  AnalyzeError {

        boolean needRet = false;
        BranchBlock branchBlock = this.branchBlocks.get(this.branchBlocks.size()-1);
        if(branchBlock.getReturnCount() !=0 && branchBlock.getReturnDType()!=DType.VOID) {
            throw new AnalyzeError(ErrorCode.NotAllBranchReturn, pos);
        }

        if(branchBlock.getReturnDType()==DType.VOID && branchBlock.getReturnCount()!=0) {
            needRet = true;
        }

        this.branchBlocks.remove(this.branchBlocks.size()-1);
        assert this.branchBlocks.isEmpty();
        return needRet;
    }
}
