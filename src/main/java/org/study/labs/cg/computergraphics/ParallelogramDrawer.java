package org.study.labs.cg.computergraphics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;

public class ParallelogramDrawer extends Application {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 1000;
    private boolean isMoving = false;
    private double pivotX, pivotY;
    private Color selectedColor;

    private TextField xField1, yField1, xField2, yField2, xField3, yField3, xField4, yField4;
    private double x1Field1 = 50, y1Field1 = 50;
    private double x1Field2 = 150, y1Field2 = 150;
    private double x1Field3 = 330, y1Field3 = 150;
    private double x1Field4 = 230, y1Field4 = 50;

    private boolean isImageSaved = false;
    private Button drawButton;
    private Canvas canvas;
    private int step = 50;
    GraphicsContext gc;
    Timeline timeline;

    //Початок виконання програми
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        primaryStage.setResizable(false);

        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        drawCoordinateSystem(gc);
        root.setCenter(canvas);
        drawGrid(gc);

        HBox inputBox = createInputBox();
        root.setBottom(inputBox);

        MenuBar menuBar = createMenu();
        root.setTop(menuBar);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setTitle("Parallelogram Drawer");
        primaryStage.show();
    }

    //Малювання координатних прямих
    private void drawCoordinateSystem(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        gc.setStroke(Color.BLACK);

        gc.strokeLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        gc.strokeLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);

        gc.strokeLine(WIDTH / 2, 0, WIDTH / 2 - 5, 10);
        gc.strokeLine(WIDTH / 2, 0, WIDTH / 2 + 5, 10);
        gc.strokeLine(WIDTH, HEIGHT / 2, WIDTH - 10, HEIGHT / 2 - 5);
        gc.strokeLine(WIDTH, HEIGHT / 2, WIDTH - 10, HEIGHT / 2 + 5);

        gc.setFill(Color.NAVY);
        gc.fillText("Y", WIDTH / 2 + 10, 10);
        gc.fillText("X", WIDTH - 10, HEIGHT / 2 - 10);

        gc.setFill(Color.NAVY);
        double zeroPointSize = 5;
        gc.fillOval(WIDTH / 2 - zeroPointSize / 2, HEIGHT / 2 - zeroPointSize / 2, zeroPointSize, zeroPointSize);

        for (int i = step; i <= 450; i += step) {
            gc.strokeLine(WIDTH / 2 + i, HEIGHT / 2 - 3, WIDTH / 2 + i, HEIGHT / 2 + 3);
            gc.strokeLine(WIDTH / 2 - i, HEIGHT / 2 - 3, WIDTH / 2 - i, HEIGHT / 2 + 3);

            double textWidth = gc.getFont().getSize() * String.valueOf(i).length() * 0.6;
            double textHeight = gc.getFont().getSize();
            gc.fillText(String.valueOf(i), WIDTH / 2 + i - textWidth / 2, HEIGHT / 2 + textHeight + 3);
            gc.fillText(String.valueOf(-i), WIDTH / 2 - i - textWidth / 2, HEIGHT / 2 + textHeight + 3);
        }

        for (int i = step; i <= 450; i += step) {
            gc.strokeLine(WIDTH / 2 - 3, HEIGHT / 2 - i, WIDTH / 2 + 3, HEIGHT / 2 - i);
            gc.strokeLine(WIDTH / 2 - 3, HEIGHT / 2 + i, WIDTH / 2 + 3, HEIGHT / 2 + i);

            double textHeight = gc.getFont().getSize() * 0.6;
            double distanceFromYAxis = 10;

            gc.fillText(String.valueOf(i), WIDTH / 2 + distanceFromYAxis, HEIGHT / 2 - i + textHeight / 2);
            gc.fillText(String.valueOf(-i), WIDTH / 2 + distanceFromYAxis, HEIGHT / 2 + i + textHeight / 2);
        }
    }

    //Створення меню для введення даних параделограма
    private HBox createInputBox() {
        xField1 = new TextField();
        yField1 = new TextField();
        xField2 = new TextField();
        yField2 = new TextField();
        xField3 = new TextField();
        yField3 = new TextField();
        xField4 = new TextField();
        yField4 = new TextField();

        drawButton = new Button("Draw");

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(
                new Label("X1:"), xField1,
                new Label("Y1:"), yField1,
                new Label("X2:"), xField2,
                new Label("Y2:"), yField2,
                new Label("X3:"), xField3,
                new Label("Y3:"), yField3,
                new Label("X4:"), xField4,
                new Label("Y4:"), yField4,
                drawButton
        );
        return inputBox;
    }

    //Створення меню для введення даних параделограма
    private void showDrawParallelogramDialog() {
        // Створення діалогового вікна
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Draw parallelogram");
        dialog.setHeaderText("Enter parallelogram coordinates, color and vertices form");

        ButtonType drawButtonType = new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(drawButtonType, ButtonType.CANCEL);

        TextField x1Field = new TextField();
        x1Field.setPromptText("X1");
        x1Field.setText(String.valueOf(x1Field1)); // Значення за замовчуванням
        TextField y1Field = new TextField();
        y1Field.setPromptText("Y1");
        y1Field.setText(String.valueOf(y1Field1)); // Значення за замовчуванням
        TextField x2Field = new TextField();
        x2Field.setPromptText("X2");
        x2Field.setText(String.valueOf(x1Field2)); // Значення за замовчуванням
        TextField y2Field = new TextField();
        y2Field.setPromptText("Y2");
        y2Field.setText(String.valueOf(y1Field2)); // Значення за замовчуванням
        TextField x3Field = new TextField();
        x3Field.setPromptText("X3");
        x3Field.setText(String.valueOf(x1Field3)); // Значення за замовчуванням
        TextField y3Field = new TextField();
        y3Field.setPromptText("Y3");
        y3Field.setText(String.valueOf(y1Field3)); // Значення за замовчуванням
        TextField x4Field = new TextField();
        x4Field.setPromptText("X4");
        x4Field.setText(String.valueOf(x1Field4)); // Значення за замовчуванням
        TextField y4Field = new TextField();
        y4Field.setPromptText("Y4");
        y4Field.setText(String.valueOf(y1Field4)); // Значення за замовчуванням

        ColorPicker fillColorPicker = new ColorPicker(Color.hsb(228, 0.76, 0.75, 0.80));

        dialog.getDialogPane().setContent(new VBox(10,
                new HBox(5, new Label("X1(Max - 350):"), x1Field, new Label("Y1(Max - 250):"), y1Field),
                new HBox(5, new Label("X2(Max - 350):"), x2Field, new Label("Y2(Max - 250):"), y2Field),
                new HBox(5, new Label("X3(Max - 350):"), x3Field, new Label("Y3(Max - 250):"), y3Field),
                new HBox(5, new Label("X4(Max - 350):"), x4Field, new Label("Y4(Max - 250):"), y4Field),
                new Label("Fill color:"), fillColorPicker
        ));


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == drawButtonType) {
                return new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    double x1 = Double.parseDouble(x1Field.getText());
                    double y1 = Double.parseDouble(y1Field.getText());
                    double x2 = Double.parseDouble(x2Field.getText());
                    double y2 = Double.parseDouble(y2Field.getText());
                    double x3 = Double.parseDouble(x3Field.getText());
                    double y3 = Double.parseDouble(y3Field.getText());
                    double x4 = Double.parseDouble(x4Field.getText());
                    double y4 = Double.parseDouble(y4Field.getText());

                    if ((x1 > 450 || x1 < -450) || (x2 > 450 || x2 < -450)
                            || (x3 > 450 || x3 < -450) || x4 > 450 || x4 < -450) {
                        showAlert("x values must be less than or equal to +/- 450");
                    } else if ((y1 > 450 || y1 < -200) || (y2 > 450 || y2 < -200)
                            || (y3 > 450 || y3 < -200) || y4 > 450 || y4 < -200) {
                        showAlert("y values must be less than or equal to 450, and negative to -200");
                    } else {

                        x1Field1 = x1;
                        x1Field2 = x2;
                        x1Field3 = x3;
                        x1Field4 = x4;
                        y1Field1 = y1;
                        y1Field2 = y2;
                        y1Field3 = y3;
                        y1Field4 = y4;

                        Color fillColor = fillColorPicker.getValue();
                        selectedColor = fillColor;

                        drawParallelogram(x1, y1, x2, y2, x3, y3, x4, y4, fillColor);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Something goes wrong.. check your input data!");
                }
            }
        });
    }

    //Функція для малювання фігури
    private void drawParallelogram(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4,
                                   Color fillColor) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(fillColor);

        gc.beginPath();
        gc.moveTo(convertX(x1), convertY(y1));
        gc.lineTo(convertX(x2), convertY(y2));
        gc.lineTo(convertX(x3), convertY(y3));
        gc.lineTo(convertX(x4), convertY(y4));
        gc.closePath();
        gc.fill();
        gc.stroke();

        if (!isImageSaved) {
            saveInitialParallelogramToFile("initial_parallelogram.png");
        }
    }

    //Переведення значення пікселів на площині в значення координат - X
    private double convertX(double x) {
        return WIDTH / 2 + x;
    }

    //Переведення значення пікселів на площині в значення координат - Y
    private double convertY(double y) {
        return HEIGHT / 2 - y;
    }

    //Основне меню з функціями
    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();
        Menu functionMenu = new Menu("Functions");

        MenuItem drawParallelogramItem = new MenuItem("Draw parallelogram");
        MenuItem moveParallelogramItem = new MenuItem("Move parallelogram");
        MenuItem stopParallelogramItem = new MenuItem("Stop");
        MenuItem cleanCoordinateSystemItem = new MenuItem("Clear");
        MenuItem expandItem = new MenuItem("Expand");
        MenuItem reduceItem = new MenuItem("Reduce");
        functionMenu.getItems().add(expandItem);
        functionMenu.getItems().add(reduceItem);
        functionMenu.getItems().add(drawParallelogramItem);
        functionMenu.getItems().add(moveParallelogramItem);
        functionMenu.getItems().add(stopParallelogramItem);
        functionMenu.getItems().add(cleanCoordinateSystemItem);

        stopParallelogramItem.setOnAction(event -> stopMoving());
        expandItem.setOnAction(actionEvent -> expandCoordinateSystem(gc));
        reduceItem.setOnAction(actionEvent -> reduceCoordinateSystem(gc));
        drawParallelogramItem.setOnAction(event -> showDrawParallelogramDialog());
        cleanCoordinateSystemItem.setOnAction(event -> cleanCoordinateSystem());
        moveParallelogramItem.setOnAction(event -> moveParallelogram());


        menuBar.getMenus().add(functionMenu);
        return menuBar;
    }

    //Меню для запуску анімації руху та вибору вершини
    private void moveParallelogram() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Move parallelogram");
        dialog.setHeaderText("Choose pivot vertex and click Move/Stop to control figure action");

        ButtonType moveButtonType = new ButtonType("Move");
        dialog.getDialogPane().getButtonTypes().addAll(moveButtonType);

        ComboBox<String> pivotVertexComboBox = new ComboBox<>();
        pivotVertexComboBox.getItems().addAll("Vertex 1", "Vertex 2", "Vertex 3", "Vertex 4");
        pivotVertexComboBox.setValue("Vertex 1");

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Pivot vertex:"), pivotVertexComboBox
        ));

        // Обробник кнопки "Move"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == moveButtonType) {
                String pivotVertex = pivotVertexComboBox.getValue();
                switch (pivotVertex) {
                    case "Vertex 1":
                        startMoving(x1Field1, y1Field1);
                        break;
                    case "Vertex 2":
                        startMoving(x1Field2, y1Field2);
                        break;
                    case "Vertex 3":
                        startMoving(x1Field3, y1Field3);
                        break;
                    case "Vertex 4":
                        startMoving(x1Field4, y1Field4);
                        break;
                }
                timeline = new Timeline(new KeyFrame(Duration.millis(50), event -> {
                    isMoving = true;
                    moveParallelogram(1);
                    cleanCoordinateSystem();
                    drawParallelogram(x1Field1, y1Field1, x1Field2, y1Field2, x1Field3, y1Field3, x1Field4, y1Field4, selectedColor);
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
                if (isMoving) {
                    timeline.play();
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    //Допоміжні змінні
    private double totalRotation = 0.0;
    private boolean isMatrixSaved = false;

    //Функція що використовує афінні перетворення для руху паралелограма
    private void moveParallelogram(double angle) {
        if (!isMoving) {
            return;
        }

        // Вибір вершини для обертання
        double pivotX = this.pivotX;
        double pivotY = this.pivotY;

        // Перетворення в радіани
        double angleRad = Math.toRadians(-angle);
        double sinTheta = Math.sin(angleRad);
        double cosTheta = Math.cos(angleRad);

        // Створення матриці афінного перетворення
        double[][] transformationMatrix = {
                {cosTheta, -sinTheta, pivotX * (1 - cosTheta) + pivotY * sinTheta},
                {sinTheta, cosTheta, pivotY * (1 - cosTheta) - pivotX * sinTheta},
                {0, 0, 1}
        };

        // Збереження матриці-результату у файл
        if (!isMatrixSaved) {
            saveMatrixResultToFile("transformation_matrix.txt", transformationMatrix);
            isMatrixSaved = true;
        }

        // Перетворення координат вершин паралелограма
        double[] points = {x1Field1, y1Field1, x1Field2, y1Field2, x1Field3, y1Field3, x1Field4, y1Field4};
        double[] newPoints = new double[points.length];

        for (int i = 0; i < points.length; i += 2) {
            double x = points[i];
            double y = points[i + 1];
            newPoints[i] = transformationMatrix[0][0] * x + transformationMatrix[0][1] * y + transformationMatrix[0][2];
            newPoints[i + 1] = transformationMatrix[1][0] * x + transformationMatrix[1][1] * y + transformationMatrix[1][2];
        }

        // Оновлення координат вершин
        x1Field1 = newPoints[0];
        y1Field1 = newPoints[1];
        x1Field2 = newPoints[2];
        y1Field2 = newPoints[3];
        x1Field3 = newPoints[4];
        y1Field3 = newPoints[5];
        x1Field4 = newPoints[6];
        y1Field4 = newPoints[7];

        // Перевірка чи зроблено повний оберт паралелограма
        totalRotation += angle;
        if (totalRotation >= 360 || totalRotation <= -360) {
            // Зменшення розміру паралелограма на 30% після кожного повного оберту
            double scaleFactor = 0.95;
            x1Field1 = pivotX + scaleFactor * (x1Field1 - pivotX);
            y1Field1 = pivotY + scaleFactor * (y1Field1 - pivotY);
            x1Field2 = pivotX + scaleFactor * (x1Field2 - pivotX);
            y1Field2 = pivotY + scaleFactor * (y1Field2 - pivotY);
            x1Field3 = pivotX + scaleFactor * (x1Field3 - pivotX);
            y1Field3 = pivotY + scaleFactor * (y1Field3 - pivotY);
            x1Field4 = pivotX + scaleFactor * (x1Field4 - pivotX);
            y1Field4 = pivotY + scaleFactor * (y1Field4 - pivotY);
            // Зменшення totalRotation на 360 градусів
            if (totalRotation > 0) {
                totalRotation -= 360;
            } else {
                totalRotation += 360;
            }
        }
    }

    //Збереження матриці коофіцієнтів у файл
    private void saveMatrixResultToFile(String fileName, double[][] transformationMatrix) {
        StringBuilder matrixResult = new StringBuilder();
        matrixResult.append("Transformation matrix:\n");
        for (double[] row : transformationMatrix) {
            matrixResult.append("[");
            for (double value : row) {
                matrixResult.append(value).append(", ");
            }
            matrixResult.delete(matrixResult.length() - 2, matrixResult.length()); // Видаляємо останню кому та пробіл
            matrixResult.append("]\n");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(matrixResult);
        } catch (IOException e) {
            throw new RuntimeException("Can't save matrix result", e);
        }
    }

    //Малювання розмітки
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        for (double x = 21; x < gc.getCanvas().getWidth(); x += 20) {
            gc.strokeLine(x, 0, x, gc.getCanvas().getHeight());
        }
        for (double y = 21; y < gc.getCanvas().getHeight(); y += 20) {
            gc.strokeLine(0, y, gc.getCanvas().getWidth(), y);
        }
    }

    //Динамічне збільшення значення координат
    private void expandCoordinateSystem(GraphicsContext gc) {
        step += 10;
        drawCoordinateSystem(gc);
        drawGrid(gc);
    }

    //Динамічне зменшення значення координат
    private void reduceCoordinateSystem(GraphicsContext gc) {
        step -= 10;
        drawCoordinateSystem(gc);
        drawGrid(gc);
    }

    //Очищення координатної площини
    private void cleanCoordinateSystem() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        drawCoordinateSystem(gc);
        drawGrid(gc);
    }

    //Початку руху та визначення опорної вершин
    private void startMoving(double pivotX, double pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        isMoving = true;
    }

    //Припинення руху паралелограма
    private void stopMoving() {
        isMoving = false;
        if (timeline != null) {
            timeline.stop();
        }
    }

    //Користувацька помилка - неправильні дані
    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid coordinates value");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    //Збереження початкового вигляду паралелограма у вигляді зображення в файл
    private void saveInitialParallelogramToFile(String fileName) {
        WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
        File file = new File(fileName);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            isImageSaved = true;

        } catch (IOException e) {
            throw new RuntimeException("Can't save parallelogram to file", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
