package com.iyr.ian.itag;

public class StoreOp {
    public final StoreOpType op;
    public final ITagInterface tag;

    public StoreOp(StoreOpType op, ITagInterface tag) {
        this.op = op;
        this.tag = tag;
    }
}
