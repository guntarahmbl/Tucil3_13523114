import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RushHourApp extends Application {

    private static final int CELL_SIZE = 70; 
    private static final String APP_TITLE = "Rush Hour Puzzle Solver";
    private GridPane grid = new GridPane();
    private Map<String, Rectangle> pieceRectangles = new HashMap<>();
    private Board initialBoard;
    private Label statsLabel = new Label();
    private TextArea solutionArea = new TextArea();
    private TextArea inputArea = new TextArea();
    private Algorithm algoObj = new Algorithm();
    private Stage primaryStage;
    private List<Board> currentSolution = new ArrayList<>();
    private String currentStats = "";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");

        VBox headerBox = createHeaderSection();
        
        VBox leftPanel = createInputPanel();
        
        VBox centerPanel = createBoardPanel();
        
        VBox rightPanel = createSolutionPanel();
        
        root.setTop(headerBox);
        root.setLeft(leftPanel);
        root.setCenter(centerPanel);
        root.setRight(rightPanel);
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    private VBox createHeaderSection() {
        Label titleLabel = new Label(APP_TITLE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("By Guntara Hambali");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        VBox headerBox = new VBox(5, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        headerBox.setStyle("-fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");
        
        return headerBox;
    }

    private VBox createInputPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setMinWidth(300);
        leftPanel.setMaxWidth(320);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label inputLabel = new Label("Konfigurasi Board");
        inputLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        inputArea.setText("""
            6 6
            11
            AAB..F
            ..BCDF
            GPPCDFK
            GH.III
            GHJ...
            LLJMM.""");
        inputArea.setPrefRowCount(8);
        inputArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px;");
        
        // Input/Output Buttons
        Button loadFileBtn = new Button("Load from File");
        loadFileBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        loadFileBtn.setMaxWidth(Double.MAX_VALUE);
        loadFileBtn.setOnAction(e -> loadFromFile());
        
        // Algorithm
        Label algoLabel = new Label("Algoritma");
        algoLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        algoLabel.setPadding(new Insets(10, 0, 5, 0));
        
        ComboBox<String> algoBox = new ComboBox<>();
        algoBox.getItems().addAll("UCS", "Greedy Best First Search", "A*");
        algoBox.setValue("A*");
        algoBox.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> heuristicBox = new ComboBox<>();
        heuristicBox.getItems().addAll("Blocking Cars", "Jarak Manhattan");
        heuristicBox.setValue("Blocking Cars");
        heuristicBox.setMaxWidth(Double.MAX_VALUE);
        
        algoBox.setOnAction(e -> {
            String selected = algoBox.getValue();
            heuristicBox.setDisable(selected.equals("UCS"));
        });
        
        VBox algoControls = new VBox(5);
        HBox algoBoxWrapper = new HBox(5, new Label("Algorithm:"), algoBox);
        algoBoxWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox heuristicBoxWrapper = new HBox(5, new Label("Heuristic:"), heuristicBox);
        heuristicBoxWrapper.setAlignment(Pos.CENTER_LEFT);
        algoControls.getChildren().addAll(algoBoxWrapper, heuristicBoxWrapper);
        
        // Action buttons
        Button solveBtn = new Button("Solve Puzzle");
        solveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        solveBtn.setMaxWidth(Double.MAX_VALUE);
        
        // left panel
        leftPanel.getChildren().addAll(
                inputLabel, 
                inputArea,
                loadFileBtn,
                algoLabel, 
                algoControls,
                solveBtn
        );
        
        // solve button action
        solveBtn.setOnAction(e -> {
            initialBoard = parseInput(inputArea.getText());
            if (initialBoard != null) {
                drawBoard(initialBoard);
                int heuristicIndex = heuristicBox.getValue().equals("Blocking Cars") ? 1 : 2;
                currentSolution = solve(algoBox.getValue(), heuristicIndex);
            } else {
                showAlert("Invalid Input", "Please check your board configuration.");
            }
        });
        
        return leftPanel;
    }

    private VBox createBoardPanel() {
        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(10));
        centerPanel.setAlignment(Pos.CENTER);
        
        Label boardLabel = new Label("Board");
        boardLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        StackPane gridContainer = new StackPane(grid);
        gridContainer.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        gridContainer.setMinSize(CELL_SIZE * 7, CELL_SIZE * 7);
        
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-background-color: #2c3e50;");
        
        centerPanel.getChildren().addAll(boardLabel, gridContainer);
        
        return centerPanel;
    }

    private VBox createSolutionPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setMinWidth(300);
        rightPanel.setMaxWidth(350);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label solutionLabel = new Label("Statistik Pencarian");
        solutionLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        statsLabel.setStyle("-fx-font-family: monospace; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        statsLabel.setWrapText(true);
        statsLabel.setMinHeight(100);
        
        Label stepsLabel = new Label("Gerakan");
        stepsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        stepsLabel.setPadding(new Insets(10, 0, 5, 0));
        
        solutionArea.setEditable(false);
        solutionArea.setWrapText(true);
        solutionArea.setStyle("-fx-font-family: 'Courier New', monospace;");
        solutionArea.setPrefHeight(300);
        
        Button saveBtn = new Button("Save Solution to File");
        saveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveSolutionToFile());
        
        rightPanel.getChildren().addAll(
                solutionLabel, 
                statsLabel, 
                stepsLabel, 
                solutionArea,
                saveBtn
        );
        
        return rightPanel;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Puzzle File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                inputArea.setText(content);
                
                initialBoard = parseInput(content);
                if (initialBoard != null) {
                    drawBoard(initialBoard);
                    showInfoAlert("File Loaded", "Puzzle configuration loaded successfully.");
                } else {
                    showAlert("Invalid Input", "The file contains an invalid puzzle configuration.");
                }
            } catch (IOException e) {
                showAlert("Error", "Could not read the file: " + e.getMessage());
            }
        }
    }

    private void saveSolutionToFile() {
        if (currentSolution.isEmpty()) {
            showAlert("No Solution", "There is no solution to save.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Solution");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("bismillah.txt");
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                int step = 1;
                for (int i = 1; i < currentSolution.size(); i++) {
                    Board current = currentSolution.get(i);
                    Board prev = currentSolution.get(i-1);
                    
                    String pieceValue = "";
                    String direction = "";
                    
                    for (String id : prev.getPieces().keySet()) {
                        Piece p1 = prev.getPieces().get(id);
                        Piece p2 = current.getPieces().get(id);
                        
                        if (p1.getRowPos() != p2.getRowPos() || p1.getColPos() != p2.getColPos()) {
                            pieceValue = id;
                            
                            if (p1.getOrientation()) { // Vertical piece
                                direction = p2.getRowPos() > p1.getRowPos() ? "Bawah" : "Atas";
                            } else { // Horizontal piece
                                direction = p2.getColPos() > p1.getColPos() ? "Kanan" : "Kiri";
                            }
                            break;
                        }
                    }
                    
                    writer.write("Gerakan " + step++ + ": " + pieceValue + "-" + direction + "\n");
                    
                    String[][] boardGrid = current.getGrid();
                    for (int row = 0; row < current.getRows(); row++) {
                        for (int col = 0; col < current.getCols(); col++) {
                            writer.write(boardGrid[row][col]);
                        }
                        writer.write("\n");
                    }
                    writer.write("\n");
                }
                
                showInfoAlert("Success", "Solution saved successfully to:\n" + file.getPath());
            } catch (IOException e) {
                showAlert("Error", "Failed to save the solution: " + e.getMessage());
            }
        }
    }

    private Board parseInput(String text) {
        String[] linesRaw = text.strip().split("\\R");
        if (linesRaw.length < 3) return null;
    
        String[] dim = linesRaw[0].trim().split("\\s+");
        int rows = Integer.parseInt(dim[0]);
        int cols = Integer.parseInt(dim[1]);
        int nPieces = Integer.parseInt(linesRaw[1].trim());
    
        List<String> lines = new ArrayList<>();
        for (int i = 2; i < linesRaw.length; i++) {
            lines.add(linesRaw[i]);
        }
    
        Position exit = detectExit(lines, rows, cols);
        if (exit.row == -1 && exit.col == -1) {
            showAlert("Invalid Input", "Exit not recognized in the board configuration.");
            return null;
        }
    
        int lowerBound = rows - 1, upperBound = 0, leftBound = 0, rightBound = cols - 1;
        if (exit.col == -1) { // exit di kiri
            leftBound = 1;
            rightBound = cols;
        } else if (exit.col == cols) { // exit di kanan
            leftBound = 0;
            rightBound = cols - 1;
        } else if (exit.row == -1) { // exit di atas
            upperBound = 1;
            lowerBound = rows;
        } else if (exit.row == rows) { // exit di bawah
            upperBound = 0;
            lowerBound = rows - 1;
        }
    
        String[][] grid = new String[rows][cols];
        String[][] charMatrix = new String[rows][cols];
        HashMap<String, Piece> pieces = new HashMap<>();
    
        int row = 0;
        for (int i = upperBound; i <= lowerBound; i++) {
            if (i >= lines.size()) break;
            String rawLine = lines.get(i);
            int col = 0;
            for (int j = leftBound; j <= rightBound; j++) {
                if (j >= rawLine.length()) continue;
                char ch = rawLine.charAt(j);
                if (ch == 'K') continue;
                String sCh = String.valueOf(ch);
                grid[row][col] = sCh;
                charMatrix[row][col] = sCh;
                col++;
            }
            while (col < cols) {
                grid[row][col] = ".";
                charMatrix[row][col] = ".";
                col++;
            }
            row++;
        }
    
        int primaryCount = 0;
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String ch = charMatrix[i][j];
                if (ch == null || ch.equals(".") || pieces.containsKey(ch)) continue;
    
                if (ch.equalsIgnoreCase("P")) {
                    primaryCount++;
                }
    
                int size = 1;
                boolean isVertical = false;
    
                if (j + 1 < cols && ch.equals(charMatrix[i][j + 1])) {
                    isVertical = false;
                    int tempJ = j + 1;
                    while (tempJ < cols && ch.equals(charMatrix[i][tempJ])) {
                        size++;
                        tempJ++;
                    }
                } else if (i + 1 < rows && ch.equals(charMatrix[i + 1][j])) {
                    isVertical = true;
                    int tempI = i + 1;
                    while (tempI < rows && ch.equals(charMatrix[tempI][j])) {
                        size++;
                        tempI++;
                    }
                }
    
                pieces.put(ch, new Piece(ch, i, j, size, isVertical));
            }
        }
    
        if (primaryCount > 1) {
            showAlert("Invalid Input", "Multiple primary pieces (P) detected in the board configuration.");
            return null;
        } else if (primaryCount == 0) {
            showAlert("Invalid Input", "No primary piece (P) found in the board configuration.");
            return null;
        }
    
        return new Board(rows, cols, exit, grid, pieces, null);
    }
    
    
    private Position detectExit(List<String> lines, int rows, int cols) {
        for (int i = 0; i < lines.size(); i++) {
            String rawLine = lines.get(i);
            int rawLen = rawLine.length();
            int startIdx = Math.max(0, rawLen - cols);
            int kIndex = rawLine.indexOf('K');
    
            if (kIndex != -1) {
                if (kIndex < startIdx && i < rows) {
                    return new Position(i, -1); // kiri
                } else if (kIndex >= cols && i < rows) {
                    return new Position(i, cols); // kanan
                } else if (i == 0 && kIndex >= startIdx && kIndex < startIdx + cols) {
                    return new Position(-1, kIndex - startIdx); // atas
                } else if (i == rows && kIndex >= startIdx && kIndex < startIdx + cols) {
                    return new Position(rows, kIndex - startIdx); // bawah
                }
            }
        }
        return new Position(-1, -1);
    }

    private void drawBoard(Board b) {
        grid.getChildren().clear();
        pieceRectangles.clear();
    
        for (int i = 0; i < b.getRows(); i++) {
            for (int j = 0; j < b.getCols(); j++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-background-color: #ecf0f1;");
                grid.add(cell, j, i);
            }
        }
    
        for (Map.Entry<String, Piece> entry : b.getPieces().entrySet()) {
            String id = entry.getKey();
            Piece piece = entry.getValue();
    
            int row = piece.getRowPos();
            int col = piece.getColPos();
            int width = piece.getOrientation() ? 1 : piece.getSize();
            int height = piece.getOrientation() ? piece.getSize() : 1;
    
            Rectangle rect = new Rectangle(width * CELL_SIZE - 4, height * CELL_SIZE - 4);
            rect.setFill(getPieceColor(id));
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1.5);
            rect.setArcWidth(15);
            rect.setArcHeight(15);
            rect.setEffect(new javafx.scene.effect.DropShadow(5, Color.color(0, 0, 0, 0.3)));
    
            Label label = new Label(id);
            label.setFont(Font.font("System", FontWeight.BOLD, 18));
            label.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 3, 0, 0, 0);");
    
            StackPane stack = new StackPane(rect, label);
            stack.setMaxSize(width * CELL_SIZE, height * CELL_SIZE);
            stack.setPrefSize(width * CELL_SIZE, height * CELL_SIZE);
    
            GridPane.setColumnIndex(stack, col);
            GridPane.setRowIndex(stack, row);
            GridPane.setColumnSpan(stack, width);
            GridPane.setRowSpan(stack, height);
    
            grid.getChildren().add(stack);
            pieceRectangles.put(id, rect);
        }
        
        Position exit = b.getExit();
        if (exit.col == b.getCols()) {
            int row = exit.row;
            
            Label exitLabel = new Label("➡");
            exitLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
            exitLabel.setStyle("-fx-text-fill: #e74c3c;");
            
            StackPane exitMarker = new StackPane(exitLabel);
            exitMarker.setStyle("-fx-background-color: transparent;");
            
            grid.add(exitMarker, b.getCols() - 1, row);
        }
    }
    
    private Color getPieceColor(String id) {
        if (id.equals("P")) {
            return Color.rgb(231, 76, 60); 
        }
        
        switch (id) {
            case "A": return Color.rgb(52, 152, 219);   
            case "B": return Color.rgb(155, 89, 182);   
            case "C": return Color.rgb(46, 204, 113);   
            case "D": return Color.rgb(241, 196, 15);   
            case "E": return Color.rgb(230, 126, 34);   
            case "F": return Color.rgb(52, 73, 94);     
            case "G": return Color.rgb(22, 160, 133);   
            case "H": return Color.rgb(192, 57, 43);    
            case "I": return Color.rgb(142, 68, 173);     
            case "J": return Color.rgb(39, 174, 96);      
            case "L": return Color.rgb(44, 62, 80);       
            case "M": return Color.rgb(243, 156, 18);   
            default:
                // Generate a color based on hash
                int hash = Math.abs(id.hashCode());
                double hue = (hash * 37) % 360;
                if (hue < 30 || hue > 330) {
                    hue = (hue + 180) % 360;
                }
                return Color.hsb(hue, 0.8, 0.8);
        }
    }
    
    private List<Board> solve(String algo, int optionHeuristic) {
        long startTime = System.nanoTime();
    
        TreeNode root = new TreeNode(initialBoard, null, 0, 0);
        TreeNode solution = null;
        int nodesProcessed = 0;
    
        PriorityQueue<TreeNode> queue;
        Set<Board> visited = new HashSet<>();
    
        Comparator<TreeNode> comparator = switch (algo) {
            case "UCS" -> Evaluator.UCS;
            case "Greedy Best First Search" -> Evaluator.GBFS;
            case "A*" -> Evaluator.AStar;
            default -> Evaluator.UCS;
        };
    
        queue = new PriorityQueue<>(comparator);
        queue.add(root);
    
        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            Board board = current.getBoard();
    
            if (visited.contains(board)) continue;
            visited.add(board);
    
            if (board.isGoal()) {
                solution = current;
                break;
            }
    
            for (Board successor : board.generateSuccessors()) {
                if (!visited.contains(successor)) {
                    TreeNode child = new TreeNode(
                            successor,
                            current,
                            algoObj.calculateCost(current),
                            algoObj.calculateHeuristic(successor, optionHeuristic)
                    );
                    queue.add(child);
                }
            }
    
            nodesProcessed++;
        }
    
        long endTime = System.nanoTime();
        double execTimeMs = (endTime - startTime) / 1_000_000.0;
    
        List<Board> path = new ArrayList<>();
        
        if (solution != null) {
            while (solution != null) {
                path.add(solution.getBoard());
                solution = solution.getParent();
            }
            Collections.reverse(path);
            
            animateSolution(path);
    
            int stepCount = path.size() - 1;
    
            currentStats = String.format("""
                    ✅ Solusi ditemukan!
                    Langkah yang dibutuhkan: %d
                    Jumlah node yang diproses: %d
                    Waktu eksekusi: %.2f ms
                    Algoritma: %s
                    """, stepCount, nodesProcessed, execTimeMs, algo);
                    
            statsLabel.setText(currentStats);
                    
            StringBuilder stepsText = new StringBuilder();
            for (int i = 1; i < path.size(); i++) {
                Board prev = path.get(i-1);
                Board curr = path.get(i);
                String move = determineMove(prev, curr);
                stepsText.append(String.format("Gerakan %d: %s\n", i, move));
            }
            solutionArea.setText(stepsText.toString());
            
        } else {
            currentStats = String.format("""
                    ❌ Solusi tidak ditemukan.
                    Jumlah node yang diproses: %d
                    Waktu eksekusi: %.2f ms
                    Algoritma: %s
                    """, nodesProcessed, execTimeMs, algo);
                    
            statsLabel.setText(currentStats);
            solutionArea.setText("No solution available.");
        }
        
        return path;
    }
    
    private String determineMove(Board prev, Board curr) {
        for (String id : prev.getPieces().keySet()) {
            Piece p1 = prev.getPieces().get(id);
            Piece p2 = curr.getPieces().get(id);
            
            if (p1.getRowPos() != p2.getRowPos() || p1.getColPos() != p2.getColPos()) {
                String direction;
                if (p1.getOrientation()) { // Vertical piece
                    direction = p2.getRowPos() > p1.getRowPos() ? "bawah" : "atas";
                } else { // Horizontal piece
                    direction = p2.getColPos() > p1.getColPos() ? "kanan" : "kiri";
                }
                
                
                return String.format("Piece '%s' ke %s", id, direction);
            }
        }
        return "No change detected";
    }

    private void animateSolution(List<Board> path) {
        Timeline timeline = new Timeline();
        double frameTime = 0.8; 
        
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            Board board = path.get(i);
            
            KeyFrame frame = new KeyFrame(Duration.seconds(i * frameTime), e -> {
                drawBoard(board);
                
                if (index > 0) {
                    int start = solutionArea.getText().indexOf("Gerakan " + index);
                    int end = solutionArea.getText().indexOf("\n", start);
                    if (end == -1) end = solutionArea.getText().length();
                    
                    solutionArea.selectRange(start, end);
                    solutionArea.requestFocus();
                }
            });
            timeline.getKeyFrames().add(frame);
        }
        
        timeline.setCycleCount(1);
        timeline.play();
    }

    public static void main(String[] args) {
        launch();
    }
}