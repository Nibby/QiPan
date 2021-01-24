package yi.component.shared;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class UITestHelper {

    private UITestHelper() {

    }

    public static void onFxThread(Consumer<AtomicBoolean> task) throws InterruptedException {
        var doneFlag = new AtomicBoolean(false);

        Platform.runLater(() -> task.accept(doneFlag));

        while (!doneFlag.get()) {
            Thread.sleep(500);
        }
    }

}
