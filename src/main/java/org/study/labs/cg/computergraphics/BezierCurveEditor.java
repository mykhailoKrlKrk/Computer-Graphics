package org.labs.csg.csg2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BezierCurveEditor extends Application {

    private final List<Point2D> controlPoints = new ArrayList<>();
    private final List<Point2D> bezierCurvePoints = new ArrayList<>();
    private double tStep = 0.01;
    public static final String FILE_NAME = "coefficient_matrix.txt";
    private Point2D draggedPoint = null;
    private BezierCalculationMethod calculationMethod = BezierCalculationMethod.PARAMETRIC;


    // Метод, що ініціалізує графічний інтерфейс користувача
    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        primaryStage.setResizable(false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawGrid(gc);
        drawAxes(gc);
        createMouseActions(canvas, gc);
        MenuBar menuBar = createMenu(gc);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(menuBar, canvas);

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bezier Curve");
        primaryStage.show();
    }

    // Метод, що малює контрольні точки на площині та їх координати
    private void drawControlPoints(GraphicsContext gc) {
        gc.setFill(Color.RED);
        for (Point2D point : controlPoints) {
            gc.fillOval(point.getX() - 3, point.getY() - 3, 6, 6);
        }

        // Отримати текст з координатами всіх точок
        StringBuilder coordinatesText = new StringBuilder();
        for (int i = 0; i < controlPoints.size(); i++) {
            Point2D point = controlPoints.get(i);
            coordinatesText.append("Point ").append(i + 1).append(": (").append((int) point.getX()).append(", ").append((int) point.getY()).append(")\n\n");
        }

        // Відображення тексту з координатами
        gc.setFill(Color.BLACK);
        gc.fillText(coordinatesText.toString(), gc.getCanvas().getWidth() - 120, 20);
    }


    // Метод, що малює криву Без'є на площині
    private void drawBezierCurve(GraphicsContext gc) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1.5);

        if (controlPoints.size() > 1) {
            bezierCurvePoints.clear();
            for (double t = 0; t <= 1; t += tStep) {
                Point2D point;
                if (calculationMethod == BezierCalculationMethod.PARAMETRIC) {
                    point = calculateBezierParametric(controlPoints, t);
                } else {
                    point = calculateBezierPoint(controlPoints, t);
                }
                bezierCurvePoints.add(point);
            }

            Point2D prevPoint = bezierCurvePoints.get(0);
            for (Point2D point : bezierCurvePoints) {
                gc.strokeLine(prevPoint.getX(), prevPoint.getY(), point.getX(), point.getY());
                prevPoint = point;
            }
        }
    }

    // Метод, що малює характеристичний багатокутник на площині
    private void drawCharacteristicPolygon(GraphicsContext gc) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);

        if (controlPoints.size() > 1) {
            Point2D prevPoint = controlPoints.get(0);
            for (int i = 1; i < controlPoints.size(); i++) {
                Point2D currentPoint = controlPoints.get(i);
                gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
                prevPoint = currentPoint;
            }
        }
    }

    // Метод, що розраховує точку на кривій Без'є для заданого параметру t
    private Point2D calculateBezierPoint(List<Point2D> points, double t) {
        int n = points.size() - 1;
        double[][] matrix = new double[2][n + 1];

        for (int i = 0; i <= n; i++) {
            double binomial = binomialCoefficient(n, i);
            double term = binomial * Math.pow(t, i) * Math.pow(1 - t, n - i);
            matrix[0][i] = term * points.get(i).getX();
            matrix[1][i] = term * points.get(i).getY();
        }

        double x = 0, y = 0;
        for (int i = 0; i <= n; i++) {
            x += matrix[0][i];
            y += matrix[1][i];
        }

        return new Point2D(x, y);
    }

    // Метод, що обчислює параметричне представлення кривої Безьє для вказаного параметру t
    private Point2D calculateBezierParametric(List<Point2D> points, double t) {
        double x = 0;
        double y = 0;
        int n = points.size() - 1;

        for (int i = 0; i <= n; i++) {
            double b = binomialCoefficient(n, i) * Math.pow(t, i) * Math.pow(1 - t, n - i);
            x += points.get(i).getX() * b;
            y += points.get(i).getY() * b;
        }

        return new Point2D(x, y);
    }

    // Метод, що обчислює біноміальний коефіцієнт
    private int binomialCoefficient(int n, int k) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }

    // Метод, що обчислює факторіал числа
    private int factorial(int n) {
        if (n <= 1)
            return 1;
        return n * factorial(n - 1);
    }

    // Запис матриці коефіцієнтів в файл
    private void saveCoefficientMatrix() {
        try {
            FileWriter writer = new FileWriter(FILE_NAME);
            int n = controlPoints.size() - 1;

            for (Point2D point : controlPoints) {
                writer.write(point.getX() + ", " + point.getY() + "\n");
            }
            writer.write("\n");

            for (int i = 0; i <= n; i++) {
                for (int j = 0; j <= n; j++) {
                    int binomial = binomialCoefficient(n, j);
                    double coefficient = binomial * Math.pow(1 - (double) i / n, n - j) * Math.pow((double) i / n, j);
                    writer.write(coefficient + " ");
                }
                writer.write("\n");
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't write coefficients to file: " + FILE_NAME);
        }
    }

    //Створення меню для функцій
    private MenuBar createMenu(GraphicsContext gc) {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Options");

        // Створення пункту меню для вибору методу обчислення кривої Безьє
        Menu calculationMethodMenu = new Menu("Calculation Method");
        MenuItem parametricMenuItem = new MenuItem("Parametric");
        MenuItem matrixMenuItem = new MenuItem("Matrix");
        calculationMethodMenu.getItems().addAll(parametricMenuItem, matrixMenuItem);
        menu.getItems().add(calculationMethodMenu);

        parametricMenuItem.setOnAction(event -> {
            calculationMethod = BezierCalculationMethod.PARAMETRIC;
            redrawCanvas(gc);
        });

        matrixMenuItem.setOnAction(event -> {
            calculationMethod = BezierCalculationMethod.MATRIX;
            redrawCanvas(gc);
        });

        MenuItem clearMenuItem = new MenuItem("Clear Canvas");
        clearMenuItem.setOnAction(event -> {
            controlPoints.clear();
            bezierCurvePoints.clear();
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            drawGrid(gc);
            drawAxes(gc);
        });

        MenuItem computeMenuItem = new MenuItem("Compute Bezier Curve with Step");
        computeMenuItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(Double.toString(tStep));
            dialog.setTitle("Bezier Curve Step");
            dialog.setHeaderText("Enter the step for computing Bezier Curve points:");
            dialog.setContentText("Step:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(step -> {
                try {
                    tStep = Double.parseDouble(step);
                    if (tStep < 0 || tStep > 1) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Step Value");
                        alert.setHeaderText(null);
                        alert.setContentText("Step value should be between 0 and 1.");
                        alert.showAndWait();
                    } else {
                        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                        drawGrid(gc);
                        drawAxes(gc);
                        drawControlPoints(gc);
                        drawCharacteristicPolygon(gc);
                        drawBezierCurve(gc);
                        saveCoefficientMatrix();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Step Value");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid step value.");
                    alert.showAndWait();
                }
            });
        });


        menu.getItems().addAll(computeMenuItem, clearMenuItem);
        menuBar.getMenus().add(menu);
        return menuBar;
    }

    // Метод, що встановлює дії для миші на полотні
    private void createMouseActions(Canvas canvas, GraphicsContext gc) {
        canvas.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            // Перевірка, щоб курсор не виходив за межі канвасу
            if (x >= 0 && x <= canvas.getWidth() && y >= 0 && y <= canvas.getHeight()) {
                boolean overPoint = false;
                for (Point2D point : controlPoints) {
                    if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                        overPoint = true;
                        break;
                    }
                }
                if (!overPoint) {
                    controlPoints.add(new Point2D(x, y));
                    gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                    drawGrid(gc);
                    drawAxes(gc);
                    drawControlPoints(gc);
                    drawCharacteristicPolygon(gc);
                    drawBezierCurve(gc);
                    saveCoefficientMatrix();
                }
            }
        });

        // Дії під час руху курсором миші
        canvas.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            boolean overPoint = false;
            for (Point2D point : controlPoints) {
                if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                    overPoint = true;
                    break;
                }
            }
            if (overPoint) {
                canvas.setCursor(javafx.scene.Cursor.HAND);
            } else {
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        // Дії під час утримання курсору
        canvas.setOnMousePressed(event -> {
            double x = event.getX();
            double y = event.getY();
            for (Point2D point : controlPoints) {
                if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                    draggedPoint = point;
                    canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
                    return;
                }
            }
        });

        // Дії під час утримання та руху курсором
        canvas.setOnMouseDragged(event -> {
            if (draggedPoint != null) {
                double x = event.getX();
                double y = event.getY();
                // Перевірка, щоб курсор не виходив за межі канвасу
                if (x >= 0 && x <= canvas.getWidth() && y >= 0 && y <= canvas.getHeight()) {
                    if (controlPoints.contains(draggedPoint)) {
                        int index = controlPoints.indexOf(draggedPoint);
                        controlPoints.set(index, new Point2D(x, y));
                        draggedPoint = new Point2D(x, y);
                        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                        drawGrid(gc);
                        drawAxes(gc);
                        drawControlPoints(gc);
                        drawCharacteristicPolygon(gc);
                        drawBezierCurve(gc);
                        saveCoefficientMatrix();
                    }
                }
            }
        });

        // Дії після відпускання курсору
        canvas.setOnMouseReleased(event -> {
            if (draggedPoint != null) {
                draggedPoint = null;
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }

    // Метод, що малює осі X та Y разом з розміткою
    private void drawAxes(GraphicsContext gc) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        // Ось X
        gc.setStroke(Color.BLACK);
        gc.strokeLine(0, gc.getCanvas().getHeight() / 2, gc.getCanvas().getWidth(), gc.getCanvas().getHeight() / 2);

        // Ось Y
        gc.strokeLine(gc.getCanvas().getWidth() / 2, 0, gc.getCanvas().getWidth() / 2, gc.getCanvas().getHeight());

        // Розмітка по осі X
        double pixelSpacingX = 20; // Інтервал між пікселями
        for (double x = gc.getCanvas().getWidth() / 2 + pixelSpacingX; x < gc.getCanvas().getWidth(); x += pixelSpacingX) {
            gc.strokeLine(x, gc.getCanvas().getHeight() / 2 - 5, x, gc.getCanvas().getHeight() / 2 + 5);
        }
        for (double x = gc.getCanvas().getWidth() / 2 - pixelSpacingX; x > 0; x -= pixelSpacingX) {
            gc.strokeLine(x, gc.getCanvas().getHeight() / 2 - 5, x, gc.getCanvas().getHeight() / 2 + 5);
        }
        gc.strokeLine(canvasWidth - 10, canvasHeight / 2 - 5, canvasWidth, canvasHeight / 2);
        gc.strokeLine(canvasWidth - 10, canvasHeight / 2 + 5, canvasWidth, canvasHeight / 2);


        // Розмітка по осі Y
        double pixelSpacingY = 20; // Інтервал між пікселями
        for (double y = gc.getCanvas().getHeight() / 2 + pixelSpacingY; y < gc.getCanvas().getHeight(); y += pixelSpacingY) {
            gc.strokeLine(gc.getCanvas().getWidth() / 2 - 5, y, gc.getCanvas().getWidth() / 2 + 5, y);
        }
        for (double y = gc.getCanvas().getHeight() / 2 - pixelSpacingY; y > 0; y -= pixelSpacingY) {
            gc.strokeLine(gc.getCanvas().getWidth() / 2 - 5, y, gc.getCanvas().getWidth() / 2 + 5, y);
        }
        gc.strokeLine(canvasWidth / 2 - 5, 10, canvasWidth / 2, 0);
        gc.strokeLine(canvasWidth / 2 + 5, 10, canvasWidth / 2, 0);

        // Позначення осей
        gc.fillText("Y", gc.getCanvas().getWidth() / 2 + 10, 10);
        gc.fillText("X", gc.getCanvas().getWidth() - 10, gc.getCanvas().getHeight() / 2 + 15);
    }

    // Малювання розмітки на площині
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        for (double x = 20; x < gc.getCanvas().getWidth(); x += 20) {
            gc.strokeLine(x, 0, x, gc.getCanvas().getHeight());
        }
        for (double y = 20; y < gc.getCanvas().getHeight(); y += 20) {
            gc.strokeLine(0, y, gc.getCanvas().getWidth(), y);
        }
    }

    // Метод, що перемалює площину
    private void redrawCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        drawGrid(gc);
        drawAxes(gc);
        drawControlPoints(gc);
        drawCharacteristicPolygon(gc);
        drawBezierCurve(gc);
        saveCoefficientMatrix();
    }

    private enum BezierCalculationMethod {
        PARAMETRIC,
        MATRIX
    }

    public static void main(String[] args) {
        launch(args);
    }
}
