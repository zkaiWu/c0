package c0.analyser.BC;

import c0.tokenizer.TokenType;

import java.util.ArrayList;

public class BcBlock {

    ArrayList<Integer> breakList;
    ArrayList<Integer> continueList;


    public BcBlock() {
        breakList = new ArrayList<>();
        continueList = new ArrayList<>();
    }

    public void addBreak(int offset) {
        this.breakList.add(offset);
    }

    public void addContinue(int offset) {
        this.continueList.add(offset);
    }

    public ArrayList<Integer> getBreakList() {
        return this.breakList;
    }

    public ArrayList<Integer> getContinueList() {
        return this.continueList;
    }
}
