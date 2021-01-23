package yi.component.shared.component.modal;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.component.YiStyleClass;

public class YiModalAlertPane extends YiAbstractModalPane {

    private final Label title = new Label();
    private final TextArea bodyMessage = new TextArea();

    private double prefWidth = -1;
    private double prefHeight = -1;

    {
        title.getStyleClass().addAll(
            YiStyleClass.FONT_WEIGHT_BOLD.getName(),
            YiStyleClass.FONT_SIZE_16.getName()
        );

        bodyMessage.getStyleClass().addAll(
            YiStyleClass.DARK_SCROLL_PANE_CONTAINER.getName(),
            YiStyleClass.BACKGROUND_DARK_SECONDARY.getName(),
            YiStyleClass.BACKGROUND_RADIUS_0.getName(),
            YiStyleClass.BORDER_INSETS_0.getName(),
            YiStyleClass.FOREGROUND_DARK.getName(),
            YiStyleClass.PADDING_10.getName()
        );
        bodyMessage.setEditable(false);
        bodyMessage.setWrapText(true);
    }

    public YiModalAlertPane(@Nullable String title,
                            @Nullable String message) {
        this(title, message, new ModalActionButton[0], null);

        var okButton = ModalActionButton.createOkayButton();
        setControlButtons(okButton, ModalActionButton.createCancelButton());
        setDefaultControlButton(okButton);
    }

    public YiModalAlertPane(@Nullable String title,
                            @Nullable String message,
                            @NotNull ModalActionButton[] buttons,
                            @Nullable ModalActionButton defaultButton) {
        setTitle(title);
        setBodyMessage(message);
        setControlButtons(buttons);
        setDefaultControlButton(defaultButton);
    }

    @Override
    protected @NotNull Pane createContent() {
        var container = new BorderPane();
        container.setTop(title);
        container.setCenter(bodyMessage);
        if (prefWidth > 0 && prefHeight > 0) {
            container.setPrefSize(prefWidth, prefHeight);
        } else {
            container.setPrefSize(400, 180);
        }
        return container;
    }

    @Override
    public boolean isStrictModal() {
        return true;
    }

    @Override
    public boolean isContentDimmed() {
        return true;
    }

    public String getTitle() {
        return title.getText();
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getBodyMessage() {
        return bodyMessage.getText();
    }

    public void setBodyMessage(String bodyMessage) {
        this.bodyMessage.setText(bodyMessage);
    }

    public void setPrefSize(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be > 0");
        }
        this.prefWidth = width;
        this.prefHeight = height;
    }
}
