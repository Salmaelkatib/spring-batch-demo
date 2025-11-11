package cash.batch.exception;

public enum ErrorCodes {
    INTERNAL_ERROR(82000, "Internal Error"),
    SECURITY_ERROR(84000, "Security Error"),
    EXTERNAL_ERROR(83000, "External Error"),
    NOT_FOUND(82001, "Not Found"),
    INPUT_VALIDATION_FAILED(82002, "Input validation failed"),
    INVALID_REQUEST(82003, "Invalid Request");

    private final int code;
    private final String description;

    private ErrorCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}


