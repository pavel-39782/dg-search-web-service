package pro.ryzhov.dg.webservice.beans;

/**
 * @author Pavel Ryzhov
 */
public class ErrorResponse extends Response {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private String errorMessage;

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
