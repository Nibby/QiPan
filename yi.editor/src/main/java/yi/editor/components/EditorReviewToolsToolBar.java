package yi.editor.components;

import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import yi.component.shared.Property;
import yi.component.shared.PropertyListener;
import yi.component.shared.component.YiStyleClass;
import yi.component.shared.component.YiToggleButton;
import yi.component.shared.utilities.GuiUtilities;
import yi.editor.EditorWindow;
import yi.editor.framework.action.EditorAction;
import yi.editor.framework.action.EditorSeparatorAction;
import yi.editor.framework.action.EditorToolAction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primary toolbar for {@link EditorWindow} that displays a set of supported editing tools
 * and other options.
 */
public class EditorReviewToolsToolBar extends ToolBar {

    private final Property<EditorTool> selectedTool = new Property<>(EditorTool.PLAY_MOVE);

    private final ToggleGroup toolButtonGroup;
    private final ToggleGroup toolMenuGroup;

    private YiToggleButton toolPlayMove; // Effectively final and non-null because of assertions in constructor
    private final List<EditorToolAction> editToolActions;

    public EditorReviewToolsToolBar() {
        toolButtonGroup = new ToggleGroup();
        toolMenuGroup = new ToggleGroup();

        editToolActions = Arrays.stream(EditorTool.values())
                                .map(value -> {
                                    var action = value.createAction(toolButtonGroup, toolMenuGroup);
                                    action.setUserObject(value);
                                    action.setComponentCompact(true);
                                    return action;
                                })
                                .sorted(Comparator.comparingDouble(EditorAction::getMenuPosition))
                                .collect(Collectors.toList());

        for (EditorToolAction action : editToolActions) {
            YiToggleButton button = action.getAsComponent();
            assert button != null : "Editor tool button shouldn't be null";
            EditorTool editorTool = (EditorTool) action.getUserObject().orElseThrow();
            button.getStyleClass().add("button-style2");
            button.setFocusTraversable(false);

            if (editorTool == EditorTool.PLAY_MOVE) {
                this.toolPlayMove = button;
            }
        }

        assert this.toolPlayMove != null : "No \"Play Move\" tool was found when constructing toolbar";

        getItems().add(GuiUtilities.createDynamicSpacer());
        addReviewTools();
        getItems().add(GuiUtilities.createDynamicSpacer());

        getStyleClass().add(YiStyleClass.BACKGROUND_BLACK_60_PERCENT.getName());
    }

    public void setContentForLayout(EditorPerspective layout) {
        var showIt = layout == EditorPerspective.REVIEW;
        setVisible(showIt);
        setManaged(showIt);

        if (layout == EditorPerspective.COMPACT) {
            toolPlayMove.setSelected(true); // This view is mainly for browsing
        }
    }

    private void addReviewTools() {
        for (EditorAction action : EditorAction.sorted(getAllActions())) {
            getItems().add(action.getAsComponent());
        }
    }

    public void addSelectedToolChangeListener(PropertyListener<EditorTool> listener) {
        this.selectedTool.addListener(listener);
    }

    public List<? extends EditorAction> getAllActions() {
        var allActions = new ArrayList<EditorAction>(editToolActions);
        allActions.addAll(getDividers());
        return allActions;
    }

    private Collection<? extends EditorAction> getDividers() {
        var dividers = new ArrayList<EditorAction>();
        dividers.add(new EditorSeparatorAction().setInMenuBar(EditorMainMenuType.TOOLS, 0.005));
        dividers.add(new EditorSeparatorAction().setInMenuBar(EditorMainMenuType.TOOLS, 0.025));
        dividers.add(new EditorSeparatorAction().setInMenuBar(EditorMainMenuType.TOOLS, 0.095));
        return dividers;
    }
}
