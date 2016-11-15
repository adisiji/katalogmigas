package neobyte.scode.webflip.adapter.scrolllistener;

/**
 * Created by neobyte on 11/10/2016.
 */

public class Preconditions {
    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkIfPositive(int number, String message) {
        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
