package org.study.labs.cg.computergraphics;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;


public class FractalViewer extends Application {

    //Змінні загального використання
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Canvas canvas;
    private GraphicsContext gc;

    private double minX = -2.5;
    private double minY = -1.5;
    private double maxX = 1.5;
    private double maxY = 1.5;
    private int maxIterations = 100;

    private double constant = 0.0;

    private double scale = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private String selectedFractal = null;
    private Color defaultMandelbrotColor = Color.RED;
    private Color defaultBrownianColor = Color.BLACK;


    //Основна функція програми
    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        primaryStage.setResizable(false);

        Menu fractalMenu = createMenu(primaryStage);

        MenuBar menuBar = new MenuBar(fractalMenu);

        createMouseListeners();

        HBox menuBox = new HBox(menuBar);

        VBox root = new VBox(menuBox, canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fractal Viewer");
        primaryStage.show();
    }

    //Функція для створення вікна вводу даних побудови фракталів
    private void showFractalInputDialog(String fractalType) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fractal Parameters Input");
        dialog.setHeaderText("Enter parameters for " + fractalType);
        selectedFractal = fractalType;

        ButtonType drawButtonType = new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(drawButtonType, ButtonType.CANCEL);

        TextField minXField = new TextField(Double.toString(minX));
        TextField minYField = new TextField(Double.toString(minY));
        TextField maxXField = new TextField(Double.toString(maxX));
        TextField maxYField = new TextField(Double.toString(maxY));
        TextField maxIterationsField = new TextField(Integer.toString(maxIterations));
        TextField constantField = new TextField(Double.toString(constant));
        constantField.setDisable(fractalType.equals("Brownian"));
        ColorPicker fillColorPicker = new ColorPicker(Color.BLUE);
        ColorPicker lineColorPicker = new ColorPicker(Color.RED);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Min X:"),
                minXField,
                new Label("Min Y:"),
                minYField,
                new Label("Max X:"),
                maxXField,
                new Label("Max Y:"),
                maxYField,
                new Label("Max Iterations:"),
                maxIterationsField,
                new Label("Constant (c):"),
                constantField
        );

        if (fractalType.equals("Mandelbrot")) {
            content.getChildren().add(new Label("Fill Color:"));
            content.getChildren().add(fillColorPicker);
        } else if (fractalType.equals("Brownian")) {
            content.getChildren().add(new Label("Line Color:"));
            content.getChildren().add(lineColorPicker);
        }

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == drawButtonType) {
                return new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    minX = Double.parseDouble(minXField.getText());
                    minY = Double.parseDouble(minYField.getText());
                    maxX = Double.parseDouble(maxXField.getText());
                    maxY = Double.parseDouble(maxYField.getText());
                    maxIterations = Integer.parseInt(maxIterationsField.getText());
                    double constant = Double.parseDouble(constantField.getText());

                    if (fractalType.equals("Mandelbrot")) {
                        Color fillColor = fillColorPicker.getValue();
                        drawMandelbrot(minX, minY, maxX, maxY, maxIterations, constant, fillColor);
                    } else if (fractalType.equals("Brownian")) {
                        Color lineColor = lineColorPicker.getValue();
                        drawBrownian(minX, minY, maxX, maxY, maxIterations, lineColor);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid format!");
                }
            }
        });
    }

    //Побудова фракталу мандельброта
    private void drawMandelbrot(double minX, double minY, double maxX, double maxY, int maxIterations,
                                double constant, Color fillColor) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        defaultMandelbrotColor = fillColor;

        gc.clearRect(0, 0, width, height);

        for (int xPixel = 0; xPixel < width; xPixel++) {
            for (int yPixel = 0; yPixel < height; yPixel++) {
                double x0 = map(xPixel, width, minX, maxX);
                double y0 = map(yPixel, height, minY, maxY);
                double x = 0;
                double y = 0;
                int iteration = 0;
                while (x * x + y * y < 4 && iteration < maxIterations) {
                    double xTemp = x * x - y * y + x0;
                    y = 2 * x * y + y0 + constant;
                    x = xTemp + constant;
                    iteration++;
                }
                if (iteration < maxIterations) {
                    double brightness = 1 - (double) iteration / maxIterations;
                    gc.setFill(fillColor);
                    gc.fillRect(xPixel, yPixel, 1, 1);
                }
            }
        }
    }

    //Побудова фракталу броунівський рух
    private void drawBrownian(double minX, double minY, double maxX, double maxY, int maxIterations, Color lineColor) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        defaultBrownianColor = lineColor;

        Random random = new Random();
        double x = canvas.getWidth() / 2;
        double y = canvas.getHeight() / 2;

        gc.setStroke(lineColor);

        for (int i = 0; i < maxIterations; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * 10;
            double nextX = x + Math.cos(angle) * distance;
            double nextY = y + Math.sin(angle) * distance;

            nextX = Math.max(0, Math.min(nextX, canvas.getWidth()));
            nextY = Math.max(0, Math.min(nextY, canvas.getHeight()));

            gc.strokeLine(x, y, nextX, nextY);
            x = nextX;
            y = nextY;
        }
    }

    //Функція для перемалювання фракталу після внесення змін
    private void drawFractal() {
        if (selectedFractal != null) {
            if (selectedFractal.equals("Mandelbrot")) {
                drawMandelbrot(minX, minY, maxX, maxY, maxIterations, constant, defaultMandelbrotColor);
            } else if (selectedFractal.equals("Brownian")) {
                drawBrownian(minX, minY, maxX, maxY, maxIterations, defaultBrownianColor);
            }
        }
    }

    //Збільшення масштабу зображення
    private void zoom(double mouseX, double mouseY) {
        double newWidth = (maxX - minX) / (double) 2;
        double newHeight = (maxY - minY) / (double) 2;
        double newX = minX + (mouseX / canvas.getWidth()) * (maxX - minX) - newWidth / 2;
        double newY = minY + (mouseY / canvas.getHeight()) * (maxY - minY) - newHeight / 2;
        minX = newX;
        minY = newY;
        maxX = newX + newWidth;
        maxY = newY + newHeight;
        if (selectedFractal != null && selectedFractal.equals("Brownian")) {
            drawBrownian(minX, minY, maxX, maxY, maxIterations, defaultBrownianColor);
        } else {
            drawFractal();
        }
    }

    //Перетворення координат пікселів на екрані (або вікна) в відповідні координати в області фрактала
    private double map(double value, double stop1, double start2, double stop2) {
        return start2 + (stop2 - start2) * ((value - (double) 0) / (stop1 - (double) 0));
    }

    //Перехід до стандартного зображення
    private void resetZoom() {
        minX = -2.5;
        minY = -1.5;
        maxX = 1.5;
        maxY = 1.5;
        if (selectedFractal != null && selectedFractal.equals("Brownian")) {
            drawBrownian(canvas.getWidth() / 2, canvas.getHeight() / 2,
                    canvas.getWidth() / 2, canvas.getHeight() / 2, maxIterations, defaultBrownianColor);
        } else {
            drawFractal();
        }
    }

    //Створення меню для користувача
    private Menu createMenu(Stage primaryStage) {
        Menu fractalMenu = new Menu("Draw Fractal");
        MenuItem mandelbrotItem = new MenuItem("Mandelbrot");
        mandelbrotItem.setOnAction(e -> showFractalInputDialog("Mandelbrot"));
        MenuItem brownianItem = new MenuItem("Brownian");
        brownianItem.setOnAction(e -> showFractalInputDialog("Brownian"));
        MenuItem resetZoom = new MenuItem("Reset Zoom");
        resetZoom.setOnAction(e -> resetZoom());
        MenuItem saveImage = new MenuItem("Save image");
        saveImage.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                } catch (IOException ex) {
                    System.out.println("Error saving image.");
                }
            }
        });
        fractalMenu.getItems().addAll(mandelbrotItem, brownianItem, resetZoom, saveImage);

        return fractalMenu;
    }

    //Функція для встновлення дій у відповідь на рух курсором
    private void createMouseListeners() {
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                zoom(mouseX, mouseY);
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.isPrimaryButtonDown()) {
                double deltaX = event.getX() - offsetX;
                double deltaY = event.getY() - offsetY;
                minX -= deltaX * (maxX - minX) / canvas.getWidth();
                maxX -= deltaX * (maxX - minX) / canvas.getWidth();
                minY -= deltaY * (maxY - minY) / canvas.getHeight();
                maxY -= deltaY * (maxY - minY) / canvas.getHeight();
                offsetX = event.getX();
                offsetY = event.getY();
                drawFractal();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
