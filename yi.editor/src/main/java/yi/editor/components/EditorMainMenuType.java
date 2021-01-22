package yi.editor.components;

import org.jetbrains.annotations.NotNull;
import yi.component.shared.i18n.TextResource;
import yi.editor.framework.EditorHelper;
import yi.editor.framework.EditorTextResources;

public enum EditorMainMenuType {

    FILE(0, EditorTextResources.MENU_FILE),
    EDIT(1, EditorTextResources.MENU_EDIT),
    TOOLS(2, EditorTextResources.MENU_TOOLS),
    NAVIGATE(3, EditorTextResources.MENU_NAVIGATE),
    VIEW(4, EditorTextResources.MENU_VIEW),
    HELP(5, EditorTextResources.MENU_HELP),

    DEBUG(99, EditorTextResources.MENU_DEBUG) {
        @Override
        public boolean isVisible() {
            return EditorHelper.isDebugMode();
        }
    },
    TESTING(100, EditorTextResources.MENU_TESTING) {
        @Override
        public boolean isVisible() {
            return EditorHelper.isRunningAsTest();
        }
    }
    ;

    private final TextResource name;
    private final int order;

    EditorMainMenuType(int order, @NotNull TextResource name) {
        this.order = order;
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public String getLocalisedName() {
        return name.getLocalisedText();
    }

    public boolean isVisible() {
        return true;
    }

    public TextResource getName() {
        return name;
    }
}
