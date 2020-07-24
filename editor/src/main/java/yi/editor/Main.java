package yi.editor;

import yi.core.go.GoAnnotation;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameRules;
import yi.component.board.GameBoard;
import yi.editor.settings.Settings;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Settings.load();
        GoGameModel game = new GoGameModel(19, 19, GoGameRules.CHINESE);
        game.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(0, 1)
                .playMove(1, 1)
                .playMove(0, 2)
                .playMove(1, 2)
                .playMove(0, 3)
                .playMove(1, 3)
                .playMove(0, 4)
                .playMove(1, 4)
                .annotate(new GoAnnotation.Triangle(0, 0))
                .annotate(new GoAnnotation.Triangle(1, 0))
                .annotate(new GoAnnotation.Square(0, 1))
                .annotate(new GoAnnotation.Square(1, 1))
                .annotate(new GoAnnotation.Circle(0, 2))
                .annotate(new GoAnnotation.Circle(1, 2))
                .annotate(new GoAnnotation.Cross(0, 3))
                .annotate(new GoAnnotation.Cross(1, 3))
                .annotate(new GoAnnotation.Fade(0, 4))
                .annotate(new GoAnnotation.Fade(1, 4))
                .annotate(new GoAnnotation.Line(0, 5, 1, 5))
                .annotate(new GoAnnotation.Arrow(0, 6, 1, 6));

        GameBoard board = new GameBoard(Settings.getBoardSettings());
        board.setModel(game);
        board.setEditable(false);

        Scene scene = new Scene(board.getComponent(), 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}