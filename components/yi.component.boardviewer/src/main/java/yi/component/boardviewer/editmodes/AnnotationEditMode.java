package yi.component.boardviewer.editmodes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Font;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.boardviewer.AnnotationRenderer;
import yi.component.boardviewer.GameBoardManager;
import yi.component.boardviewer.GameBoardSize;
import yi.core.go.Annotation;
import yi.core.go.AnnotationType;
import yi.core.go.editor.edit.AnnotationEdit;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Edit mode that manipulates annotation data on a {@link yi.core.go.GameNode}.
 */
public final class AnnotationEditMode extends AbstractEditMode {

    /**
     * Defines the behaviour for mouse drag across the board intersection.
     */
    private enum DragAction {
        /**
         * Each grid visited should have an annotation added there.
         */
        CREATE,

        /**
         * Each grid visited should have its annotation erased.
         */
        DELETE
    }

    /**
     * The internal game model does not differentiate between a letter label and a number label.
     * These are arbitrary differentiations made by the editor. Therefore we distinguish it here.
     */
    public enum LabelType {
        LETTER,
        NUMBER
    }

    private final AnnotationType typeToApply;

    // Internal
    private DragAction dragAction = DragAction.CREATE;
    private long mouseSessionId;

    // For label annotations only
    private static final String LETTER_LABEL_PROGRESSION = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private @Nullable LabelType labelType;
    private String nextLabelText;
    boolean isLabelEditOfDifferentType = false;

    /**
     * Instantiates an annotation edit mode that works with one specific type of
     * annotation. The type of annotation <b>must not</b> be a label. Use
     * {@link #AnnotationEditMode(LabelType)} constructor for label annotations.
     * <p/>
     * It is recommended to use the factory class {@link EditMode} to instantiate
     * edit modes rather than using this constructor.
     *
     * @param typeToApply Type of non-label annotation to work with.
     */
    public AnnotationEditMode(AnnotationType typeToApply) {
        this.typeToApply = Objects.requireNonNull(typeToApply);
        this.labelType = null;
    }

    /**
     * Instantiates an annotation edit mode specific to working with labelled
     * annotation type.
     * <p/>
     * It is recommended to use the factory class {@link EditMode} to instantiate
     * edit modes rather than using this constructor.
     *
     * @param labelType Type of label to work with.
     */
    public AnnotationEditMode(@NotNull LabelType labelType) {
        this(AnnotationType.LABEL);

        this.labelType = Objects.requireNonNull(labelType);
    }

    @Override
    public void renderGridCursor(GraphicsContext g, GameBoardManager manager, int x, int y) {
        if (manager.getGameModel().getCurrentNode().hasAnnotationAt(x, y)) {
            return;
        }

        Annotation temporary = null;

        if (AnnotationType.Companion.isPointAnnotation(typeToApply)) {
            temporary = Annotation.Companion.createFromType(typeToApply, x, y, -1, -1, getNextLabelText(manager));
        } else if (AnnotationType.Companion.isDirectionalAnnotation(typeToApply)) {
            if (directionalAnnoStartPositionDefined) {
                temporary = Annotation.Companion.createFromType(typeToApply, directionalAnnoFirstX, directionalAnnoFirstY, x, y, "");
            }
        } else {
            throw new IllegalStateException("Unsupported annotation type: " + typeToApply);
        }

        if (temporary != null) {
            g.setGlobalAlpha(0.5d);
            AnnotationRenderer.render(temporary, g, manager, getLabelFont(manager.size));
            g.setGlobalAlpha(1.0d);
        }
    }

    private Font getLabelFont(GameBoardSize size) {
        return AnnotationRenderer.getAndCacheLabelFont(size, this.getClass());
    }

    @Override
    public void onMousePress(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        generateNewMouseSessionId();

        var annotationHere = manager.getGameModel().getCurrentNode().getAnnotationAt(gridX, gridY);
        boolean createRatherThanDelete;
        isLabelEditOfDifferentType = false;

        if (annotationHere != null) {
            var itsType = annotationHere.getType();
            createRatherThanDelete = itsType != typeToApply;
            if (typeToApply == AnnotationType.LABEL) {
                isLabelEditOfDifferentType = isDifferentLabelType(annotationHere);
                createRatherThanDelete |= isLabelEditOfDifferentType;
            }
        } else {
            createRatherThanDelete = true;
        }

        if (createRatherThanDelete) {
            dragAction = DragAction.CREATE;
            maybeCreateAnnotation(manager, gridX, gridY, isLabelEditOfDifferentType);
        } else {
            dragAction = DragAction.DELETE;
            removeAnnotationAt(manager, gridX, gridY, mouseSessionId);
        }
    }

    @Override
    public void onMouseDrag(MouseButton button, GameBoardManager manager, int gridX, int gridY) {
        // TODO: Maybe allow re-positioning a directional annotation if it's dragged at either end?

        if (AnnotationType.Companion.isPointAnnotation(typeToApply)) {
            if (dragAction == DragAction.CREATE) {
                maybeCreateAnnotation(manager, gridX, gridY, isLabelEditOfDifferentType);
            } else if (dragAction == DragAction.DELETE) {
                removeAnnotationAt(manager, gridX, gridY, mouseSessionId);
            } else {
                throw new IllegalStateException("Unsupported drag action: " + dragAction.name());
            }
        }
    }

    @Override
    public void onMouseRelease(MouseButton button, GameBoardManager manager, int cursorX, int cursorY) {
        generateNewMouseSessionId();

        if (typeToApply == AnnotationType.LABEL) {
            computeNextLabelText(manager);
        }
    }

    /*
        Directional annotations require two clicks, so it's not guaranteed that an annotation will be
        created by calling this method once.
     */
    private int directionalAnnoFirstX; // The first co-ordinate to create a directional annotation.
    private int directionalAnnoFirstY;
    private boolean directionalAnnoStartPositionDefined = false;
    
    private void maybeCreateAnnotation(GameBoardManager manager, int gridX, int gridY,
                                       boolean isLabelEditOfDifferentType) {
        Annotation annotationHere = manager.getGameModel().getCurrentNode().getAnnotationAt(gridX, gridY);

        if (annotationHere != null && annotationHere.getType() == typeToApply
            && typeToApply != AnnotationType.LABEL && isLabelEditOfDifferentType) {
            return; // Same annotation. Letting the logic fall through introduces too much overhead.
        }

        // First clear whatever annotation that is already here.
        // Only want one annotation per intersection.
        removeAnnotationAt(manager, gridX, gridY, mouseSessionId);

        if (AnnotationType.Companion.isPointAnnotation(typeToApply)) {
            createPointAnnotation(manager, typeToApply, gridX, gridY, nextLabelText, mouseSessionId);
        } else if (AnnotationType.Companion.isDirectionalAnnotation(typeToApply)) {
            if (!directionalAnnoStartPositionDefined) {
                directionalAnnoFirstX = gridX;
                directionalAnnoFirstY = gridY;
                directionalAnnoStartPositionDefined = true;
            } else {
                assert directionalAnnoFirstX >= 0 && directionalAnnoFirstY >= 0
                        : String.format("Invalid directional annotation starting point: (%d, %d).",
                        directionalAnnoFirstX, directionalAnnoFirstY);

                if (gridX != directionalAnnoFirstX || gridY != directionalAnnoFirstY) {
                    maybeCreateDirectionalAnnotation(manager, typeToApply, directionalAnnoFirstX,
                            directionalAnnoFirstY, gridX, gridY, mouseSessionId);
                }
                resetDirectionalAnnotationCreateStep();
            }
        }
    }

    private static void createPointAnnotation(GameBoardManager manager, AnnotationType annotationType,
                                              int gridX, int gridY, String nextLabelText, long sessionId) {
        if (!AnnotationType.Companion.isPointAnnotation(annotationType)) {
            throw new IllegalArgumentException("Not a point annotation: " + annotationType.name());
        }

        var annotationText = nextLabelText == null ? "" : nextLabelText;
        Annotation pointAnno = Annotation.Companion.createFromType(annotationType, gridX, gridY, -1, -1, annotationText);
        createAnnotation(manager, pointAnno, sessionId);
    }

    private static void maybeCreateDirectionalAnnotation(GameBoardManager manager, AnnotationType annotationType,
                                                         int startX, int startY, int endX, int endY, long sessionId) {
        if (!AnnotationType.Companion.isDirectionalAnnotation(annotationType)) {
            throw new IllegalArgumentException("Not a directional annotation: " + annotationType.name());
        }

        var directionalAnno = Annotation.Companion.createFromType(annotationType, startX, startY, endX, endY, "");
        createAnnotation(manager, directionalAnno, sessionId);
    }

    private static void removeAnnotationAt(GameBoardManager manager, int gridX, int gridY, long sessionId) {
        Annotation annotation = manager.getGameModel().getCurrentNode().getAnnotationAt(gridX, gridY);

        if (annotation != null) {
            var currentNode = manager.getGameModel().getCurrentNode();
            var deletionEdit = AnnotationEdit.Companion.forRemoval(currentNode, annotation, sessionId);

            manager.edit.submit(deletionEdit);
        }
    }

    private static void createAnnotation(GameBoardManager manager, Annotation annotation, long sessionId) {
        var currentNode = manager.getGameModel().getCurrentNode();
        var additionEdit = AnnotationEdit.Companion.forNew(currentNode, annotation, sessionId);
        manager.edit.submit(additionEdit);
    }

    private void resetDirectionalAnnotationCreateStep() {
        directionalAnnoStartPositionDefined = false;
        directionalAnnoFirstX = -1;
        directionalAnnoFirstY = -1;
    }

    private boolean isDifferentLabelType(Annotation annotationHere) {
        if (annotationHere.getType() == AnnotationType.LABEL) {
            var labelAnnotation = (Annotation.Label) annotationHere;
            String labelText = labelAnnotation.getText();
            boolean wantToCreateNumericalLabel = isNumericalLabel(nextLabelText);
            boolean annotationIsNumerical = isNumericalLabel(labelText);

            return wantToCreateNumericalLabel != annotationIsNumerical;
        }
        throw new IllegalStateException("Should not be checking label types if annotation is not a label");
    }

    private boolean isNumericalLabel(String labelText) {
        try {
            Integer.parseInt(labelText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getNextLabelText(GameBoardManager manager) {
        if (labelType == null)
            return "";

        if (nextLabelText == null) {
            computeNextLabelText(manager);
        }

        return nextLabelText;
    }

    private void computeNextLabelText(GameBoardManager manager) {
        assertLabelTypeNotNull();

        if (labelType == LabelType.LETTER) {
            var annoTexts =
                    manager.getGameModel().getCurrentNode().getAnnotations()
                    .stream()
                    .filter(anno -> anno instanceof Annotation.Label)
                    .map(anno -> ((Annotation.Label) anno).getText())
                    .filter(text -> !NumberUtils.isCreatable(text))
                    .collect(Collectors.toSet());

            int step = 0;
            String candidateText;

            // TODO: This is not ideal. Annotation labels should ideally correspond to a base 26 format so that
            //       if the annotation exceeds letter Z, we append AA, AB ... AZ, then AAA, AAB ... etc.

            do {
                candidateText = String.valueOf(LETTER_LABEL_PROGRESSION.charAt(step));
                if (annoTexts.contains(candidateText)) {
                    ++step;
                } else {
                    break;
                }
            } while (step < LETTER_LABEL_PROGRESSION.length());

            nextLabelText = candidateText;
        }
        else if (labelType == LabelType.NUMBER) {
            var maxNumber =
                manager.getGameModel().getCurrentNode().getAnnotations()
                    .stream()
                    .filter(anno -> anno instanceof Annotation.Label)
                    .map(anno -> ((Annotation.Label) anno).getText())
                    .filter(NumberUtils::isCreatable)
                    .map(Integer::parseInt)
                    .max((a, b) -> a > b ? a : (a.equals(b)) ? 0 : -1);

            nextLabelText = String.valueOf(1 + maxNumber.orElse(0));
        } else throw new IllegalStateException("Unimplemented label type: " + Objects.requireNonNull(labelType).name());
    }

    private void generateNewMouseSessionId() {
        long sessionId;

        do {
            sessionId = ThreadLocalRandom.current().nextLong();
        } while (mouseSessionId == sessionId);

        mouseSessionId = sessionId;
    }

    private void assertLabelTypeNotNull() {
        if (labelType == null)
            throw new IllegalStateException("Bad internal state. Should not be retrieving label text if labelType is null.");
    }

    /**
     *
     * @return Type of annotation being created or removed.
     */
    public AnnotationType getTypeToApply() {
        return typeToApply;
    }

    /**
     * Result of this return type is null if {@link #getTypeToApply()} is
     * not equal to {@link AnnotationType#LABEL}.
     *
     * @return Type of label being created or removed.
     */
    public Optional<LabelType> getLabelType() {
        return Optional.ofNullable(labelType);
    }
}
