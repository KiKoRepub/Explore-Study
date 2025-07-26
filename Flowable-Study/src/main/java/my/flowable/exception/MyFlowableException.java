package my.flowable.exception;


public class MyFlowableException extends RuntimeException {

    public MyFlowableException(String message) {
        super(message);
        System.err.println("this process throws MyFlowableException: " + message);
    }
}
