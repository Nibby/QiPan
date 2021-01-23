package yi.component.shared.component.modal;

public enum CloseTrigger {

    /**
     * The interaction was successful and subsequent actions (if any) should be
     * carried out.
     */
    OKAY,

    /**
     * The interaction was dismissed because the user no longer wish to continue
     * some operation.
     */
    CANCEL,
    ;

    /**
     * @return The {@link CloseTrigger} value corresponding to the primary action button.
     */
    public static CloseTrigger getPrimaryTrigger() {
        return OKAY;
    }
}
