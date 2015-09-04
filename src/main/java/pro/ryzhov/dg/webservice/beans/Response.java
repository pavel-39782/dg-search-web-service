package pro.ryzhov.dg.webservice.beans;

/**
 * @author Pavel Ryzhov
 */
public abstract class Response {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private double apiVersion;

    public void setApiVersion(double apiVersion) {
        this.apiVersion = apiVersion;
    }
}
