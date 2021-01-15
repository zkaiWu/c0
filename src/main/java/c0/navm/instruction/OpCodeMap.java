package c0.navm.instruction;

import java.util.HashMap;
import java.util.Map;

public class OpCodeMap {

    private static Map<InstructionType ,Character> opCodeMap;


    //指令给人看转换为指令给机器看
    static {
        opCodeMap  = new HashMap<>();
        opCodeMap.put(InstructionType.Nop, (char)0x00);
        opCodeMap.put(InstructionType.Push, (char)0x01);
        opCodeMap.put(InstructionType.Pop, (char)0x02);
        opCodeMap.put(InstructionType.PopN, (char)0x03);
        opCodeMap.put(InstructionType.Dup, (char)0x04);
        opCodeMap.put(InstructionType.LocA, (char)0x0a);
        opCodeMap.put(InstructionType.ArgA, (char)0x0b);
        opCodeMap.put(InstructionType.GlobA, (char)0x0c);
        opCodeMap.put(InstructionType.Load8, (char)0x10);
        opCodeMap.put(InstructionType.Load16, (char)0x11);
        opCodeMap.put(InstructionType.Load32, (char)0x12);
        opCodeMap.put(InstructionType.Load64, (char)0x13);
        opCodeMap.put(InstructionType.Store8, (char)0x14);
        opCodeMap.put(InstructionType.Store16, (char)0x15);
        opCodeMap.put(InstructionType.Store32, (char)0x16);
        opCodeMap.put(InstructionType.Store64, (char)0x17);
        opCodeMap.put(InstructionType.Alloc, (char)0x18);
        opCodeMap.put(InstructionType.Free, (char)0x19);
        opCodeMap.put(InstructionType.StackAlloc, (char)0x1a);
        opCodeMap.put(InstructionType.AddI, (char)0x20);
        opCodeMap.put(InstructionType.SubI, (char)0x21);
        opCodeMap.put(InstructionType.MulI, (char)0x22);
        opCodeMap.put(InstructionType.DivI, (char)0x23);
        opCodeMap.put(InstructionType.AddF, (char)0x24);
        opCodeMap.put(InstructionType.SubF, (char)0x25);
        opCodeMap.put(InstructionType.MulF, (char)0x26);
        opCodeMap.put(InstructionType.DivF, (char)0x27);
        opCodeMap.put(InstructionType.DivU, (char)0x28);
        opCodeMap.put(InstructionType.ShL, (char)0x29);
        opCodeMap.put(InstructionType.ShR, (char)0x2a);
        opCodeMap.put(InstructionType.And, (char)0x2b);
        opCodeMap.put(InstructionType.Or, (char)0x2c);
        opCodeMap.put(InstructionType.XOr, (char)0x2d);
        opCodeMap.put(InstructionType.Not, (char)0x2e);
        opCodeMap.put(InstructionType.CmpI, (char)0x30);
        opCodeMap.put(InstructionType.CmpU, (char)0x31);
        opCodeMap.put(InstructionType.CmpF, (char)0x32);
        opCodeMap.put(InstructionType.NegI, (char)0x34);
        opCodeMap.put(InstructionType.NegF, (char)0x35);
        opCodeMap.put(InstructionType.ItoF, (char)0x36);
        opCodeMap.put(InstructionType.FtoI, (char)0x37);
        opCodeMap.put(InstructionType.ShrL, (char)0x38);
        opCodeMap.put(InstructionType.SetLt, (char)0x39);
        opCodeMap.put(InstructionType.SetGt, (char)0x3a);
        opCodeMap.put(InstructionType.Br, (char)0x41);
        opCodeMap.put(InstructionType.BrFalse, (char)0x42);
        opCodeMap.put(InstructionType.BrTrue, (char)0x43);
        opCodeMap.put(InstructionType.Call, (char)0x48);
        opCodeMap.put(InstructionType.Ret, (char)0x49);
        opCodeMap.put(InstructionType.CallName, (char)0x4a);
        opCodeMap.put(InstructionType.ScanI, (char)0x50);
        opCodeMap.put(InstructionType.ScanC, (char)0x51);
        opCodeMap.put(InstructionType.ScanF, (char)0x52);
        opCodeMap.put(InstructionType.PrintI, (char)0x54);
        opCodeMap.put(InstructionType.PrintC, (char)0x55);
        opCodeMap.put(InstructionType.PrintF, (char)0x56);
        opCodeMap.put(InstructionType.PrintS, (char)0x57);
        opCodeMap.put(InstructionType.PrintLn, (char)0x58);
        opCodeMap.put(InstructionType.Panic, (char)0xfe);
    }


    public static char getCode(InstructionType instructionType) {
        return opCodeMap.get(instructionType);
    }

    public static void main(String[] args) {
        System.out.println((int)getCode(InstructionType.CallName));
    }
}
