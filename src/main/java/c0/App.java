package c0;


import c0.analyser.Analyser;
import c0.analyser.SymbolIter;
import c0.navm.OoFile;
import c0.tokenizer.StringIter;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.tokenizer.Tokenizer;


import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App<scanner> {
    public static void main(String[] args) {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("output");
        var debugFileName = "debug.txt";

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        PrintStream debugOutput;
        debugOutput = System.out;
//        if (outputFileName.equals("-")) {
//            debugOutput = System.out;
//        } else {
//            try {
//                debugOutput = new PrintStream(new FileOutputStream(debugFileName));
//            } catch (FileNotFoundException e) {
//                System.err.println("Cannot open output file.");
//                e.printStackTrace();
//                System.exit(2);
//                return;
//            }
//        }


        DataOutputStream output;
        try {
            output = new DataOutputStream(new FileOutputStream(outputFileName));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open output file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        Scanner scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = new Tokenizer(iter);
//        try {
//            Token temp = null;
//            while((temp=tokenizer.nextToken()).getTokenType()!=TokenType.EOF) {
//                output.println(temp);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }


        var symbolIter = new SymbolIter(tokenizer);
        var analyse = new Analyser(symbolIter);

        try {
            OoFile ooFile = analyse.analyse();
            ooFile.writeDebug(debugOutput);
            ooFile.toAssemble(output);
        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            e.printStackTrace();
            System.exit(-1);
        }
    }



    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }
}
