package revolut.interview.exception;

public class SameSenderAndReceiverRequestException extends RuntimeException {
    public SameSenderAndReceiverRequestException() {
        super("One has passed an illegal transfer request - sender and receiver is the same subject");
    }
}
