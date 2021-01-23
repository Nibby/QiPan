package yi.component.shared.component.modal;

public enum ActionType {

    /**
     * The interaction was successful and subsequent actions (if any) should be
     * carried out.
     */
    PRIMARY,

    /**
     * The interaction was dismissed because the user no longer wish to continue
     * some operation.
     */
    SECONDARY,
    ;

    /**
     * @return The {@link ActionType} value corresponding to the primary action button.
     */
    public static ActionType getPrimaryTrigger() {
        return PRIMARY;
    }
}
