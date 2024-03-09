package org.study.labs.cg.computergraphics;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TriangleDrawer extends Application {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    private TextField xField1, yField1, xField2, yField2;
    private Button drawButton;
    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        primaryStage.setResizable(false);

        canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawCoordinateSystem(gc);
        root.setCenter(canvas);

        HBox inputBox = createInputBox();
        root.setBottom(inputBox);

        MenuBar menuBar = createMenu();
        root.setTop(menuBar);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setTitle("Triangle Drawer");
        primaryStage.show();
    }

    private void drawCoordinateSystem(GraphicsContext gc) {
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

        for (int i = 50; i <= 350; i += 50) {
            gc.strokeLine(WIDTH / 2 + i, HEIGHT / 2 - 3, WIDTH / 2 + i, HEIGHT / 2 + 3);
            gc.strokeLine(WIDTH / 2 - i, HEIGHT / 2 - 3, WIDTH / 2 - i, HEIGHT / 2 + 3);

            double textWidth = gc.getFont().getSize() * String.valueOf(i).length() * 0.6;
            double textHeight = gc.getFont().getSize();
            gc.fillText(String.valueOf(i), WIDTH / 2 + i - textWidth / 2, HEIGHT / 2 + textHeight + 3);
            gc.fillText(String.valueOf(-i), WIDTH / 2 - i - textWidth / 2, HEIGHT / 2 + textHeight + 3);
        }

        for (int i = 50; i <= 250; i += 50) {
            gc.strokeLine(WIDTH / 2 - 3, HEIGHT / 2 - i, WIDTH / 2 + 3, HEIGHT / 2 - i);
            gc.strokeLine(WIDTH / 2 - 3, HEIGHT / 2 + i, WIDTH / 2 + 3, HEIGHT / 2 + i);

            double textHeight = gc.getFont().getSize() * 0.6;
            double distanceFromYAxis = 10;

            gc.fillText(String.valueOf(i), WIDTH / 2 + distanceFromYAxis, HEIGHT / 2 - i + textHeight / 2);
            gc.fillText(String.valueOf(-i), WIDTH / 2 + distanceFromYAxis, HEIGHT / 2 + i + textHeight / 2);
        }
    }

    private HBox createInputBox() {
        xField1 = new TextField();
        yField1 = new TextField();
        xField2 = new TextField();
        yField2 = new TextField();

        drawButton = new Button("Draw");

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(
                new Label("X1:"), xField1,
                new Label("Y1:"), yField1,
                new Label("X2:"), xField2,
                new Label("Y2:"), yField2,
                drawButton
        );
        return inputBox;
    }

    private void showDrawTriangleDialog() {
        // Створення діалогового вікна
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Draw triangle");
        dialog.setHeaderText("Enter triangle coordinates, color and vertices form");

        ButtonType drawButtonType = new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(drawButtonType, ButtonType.CANCEL);

        TextField x1Field = new TextField();
        x1Field.setPromptText("X1");
        TextField y1Field = new TextField();
        y1Field.setPromptText("Y1");
        TextField x2Field = new TextField();
        x2Field.setPromptText("X2");
        TextField y2Field = new TextField();
        y2Field.setPromptText("Y2");

        ColorPicker fillColorPicker = new ColorPicker(Color.GREEN);
        ColorPicker vertexColorPicker = new ColorPicker(Color.BLACK);

        ComboBox<String> vertexShapeComboBox = new ComboBox<>();
        vertexShapeComboBox.getItems().addAll("Circle", "Square");
        vertexShapeComboBox.setValue("Circle");

        dialog.getDialogPane().setContent(new VBox(10,
                new HBox(5, new Label("X1(Max - 350):"), x1Field, new Label("Y1(Max - 250):"), y1Field),
                new HBox(5, new Label("X2(Max - 350):"), x2Field, new Label("Y2(Max - 250):"), y2Field),
                new Label("Fill color:"), fillColorPicker,
                new Label("Vertices color:"), vertexColorPicker,
                new Label("Vertices form:"), vertexShapeComboBox
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

                    x1 = Math.max(0, Math.min(350, x1));
                    y1 = Math.max(0, Math.min(250, y1));
                    x2 = Math.max(0, Math.min(350, x2));
                    y2 = Math.max(0, Math.min(250, y2));

                    double x3 = (x1 + x2) / 2 + (y2 - y1) * Math.sqrt(3) / 2;
                    double y3 = (y1 + y2) / 2 + (x1 - x2) * Math.sqrt(3) / 2;

                    x3 = Math.max(0, Math.min(350, x3));
                    y3 = Math.max(0, Math.min(250, y3));

                    Color fillColor = fillColorPicker.getValue();
                    Color vertexColor = vertexColorPicker.getValue();
                    String vertexShape = vertexShapeComboBox.getValue();

                    drawTriangle(x1, y1, x2, y2, x3, y3, fillColor, vertexColor, vertexShape);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Something goes wrong.. check your input data!");
                }
            }
        });
    }

    private void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3,
                              Color fillColor, Color vertexColor, String vertexShape) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(fillColor);
        gc.setStroke(vertexColor);

        gc.beginPath();
        gc.moveTo(convertX(x1), convertY(y1));
        gc.lineTo(convertX(x2), convertY(y2));
        gc.lineTo(convertX(x3), convertY(y3));
        gc.closePath();
        gc.fill();
        gc.stroke();

        if (vertexShape.equals("Circle")) {
            drawVertexCircle(gc, x1, y1, vertexColor);
            drawVertexCircle(gc, x2, y2, vertexColor);
            drawVertexCircle(gc, x3, y3, vertexColor);
        } else if (vertexShape.equals("Square")) {
            drawVertexSquare(gc, x1, y1, vertexColor);
            drawVertexSquare(gc, x2, y2, vertexColor);
            drawVertexSquare(gc, x3, y3, vertexColor);
        }
    }

    private void drawVertexSquare(GraphicsContext gc, double x, double y, Color vertexColor) {
        double size = 5;
        gc.setFill(vertexColor);
        gc.fillRect(convertX(x) - size / 2, convertY(y) - size / 2, size, size);
    }

    private void drawVertexCircle(GraphicsContext gc, double x, double y, Color vertexColor) {
        double size = 5;
        gc.setFill(vertexColor);
        gc.fillOval(convertX(x) - size / 2, convertY(y) - size / 2, size, size);
    }

    private double convertX(double x) {
        return WIDTH / 2 + x;
    }

    private double convertY(double y) {
        return HEIGHT / 2 - y;
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();
        Menu functionMenu = new Menu("Functions");

        MenuItem drawTriangleItem = new MenuItem("Draw triangle");
        MenuItem cleanCoordinateSystemItem = new MenuItem("Clear");
        functionMenu.getItems().add(drawTriangleItem);
        functionMenu.getItems().add(cleanCoordinateSystemItem);

        drawTriangleItem.setOnAction(event -> showDrawTriangleDialog());
        cleanCoordinateSystemItem.setOnAction(event -> cleanCoordinateSystem());

        menuBar.getMenus().add(functionMenu);
        return menuBar;
    }

    private void cleanCoordinateSystem() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        drawCoordinateSystem(gc);
    }

    public static void main(String[] args) {
        launch(args);
    }
}