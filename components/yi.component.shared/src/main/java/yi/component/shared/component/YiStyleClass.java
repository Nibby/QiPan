package yi.component.shared.component;

import javafx.scene.Node;

/**
 * Collection of CSS style classes that is provided by every {@link Skin}
 * to be used in {@link Node#getStyleClass()}.
 */
public enum YiStyleClass {

    BACKGROUND_RADIUS_0("bg-radius-0"),

    BACKGROUND_TRANSPARENT("bg-transparent"),
    BACKGROUND_DARK("bg-dark"),
    BACKGROUND_DARK_SECONDARY("bg-dark-secondary"),
    BACKGROUND_BLACK_60_PERCENT("bg-black-60"),

    FOREGROUND_DARK("fg-dark"),
    FOREGROUND_DARK_SECONDARY("fg-dark-secondary"),
    FOREGROUND_LIGHT("fg-light"),

    FONT_WEIGHT_BOLD("font-weight-bold"),
    FONT_WEIGHT_NORMAL("font-weight-normal"),

    FONT_SIZE_16("font-size-16"),
    FONT_SIZE_14("font-size-14"),

    PREFERRED_HEIGHT_28("pref-height-28"),

    BORDER_INSETS_0("border-insets-0"),
    PADDING_10("padding-10"),


    /**
     * Applied to the parent container that will contain a scroll pane as a child, and
     * applies dark color scheme to it.
     */
    DARK_SCROLL_PANE_CONTAINER("dark-scroll-pane-container"),

    MODAL_CONTENT_CONTAINER("modal-content-container"),
    DEFAULT_BUTTON("button-default")
    ;

    private final String cssClassName;

    YiStyleClass(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public String getName() {
        return cssClassName;
    }
}
