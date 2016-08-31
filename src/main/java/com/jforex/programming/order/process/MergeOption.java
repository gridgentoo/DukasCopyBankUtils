package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface MergeOption extends CommonOption {

    public CommonBuilder onRemoveSLReject(Consumer<IOrder> removeSLRejectAction);

    public CommonBuilder onRemoveTPReject(Consumer<IOrder> removeTPRejectAction);

    public CommonBuilder onRemoveSL(Consumer<IOrder> removedSLAction);

    public CommonBuilder onRemoveTP(Consumer<IOrder> removedTPAction);

    public CommonBuilder onMergeReject(Consumer<IOrder> mergeRejectAction);

    public CommonBuilder onMerge(Consumer<IOrder> mergedAction);

    public CommonBuilder onMergeClose(Consumer<IOrder> mergeClosedAction);
}
