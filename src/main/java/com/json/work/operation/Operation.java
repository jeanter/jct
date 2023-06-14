package com.json.work.operation;



public interface Operation {
    public static enum OperationResult {
        SUCCEEDED,
        RETRY,
        LOCKED;
    }

    default OperationResult run(OperationHandler currentHandler) {
        return OperationResult.SUCCEEDED;
    }

}
