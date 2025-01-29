package edu.mephi.java.engine;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Класс, представляющий игру.
 */
public class Game extends JPanel {
    // Увеличить размер плиток
    private final int WIDTH = 8; // Уменьшить количество плиток по ширине
    private final int HEIGHT = 8; // Уменьшить количество плиток по высоте
    private Board board; // Игровая доска
    public boolean gameOver = false; // Флаг окончания игры
    private boolean allMatchesProcessed = false; // Флаг обработки всех совпадений

    // Текущая выбранная плитка
    Point selectedPoint = new Point(-1, -1);

    /**
     * Находит стабильные совпадения на доске и заполняет их новыми плитками.
     */
    private void FindStableMatches() {
        List<Point> matches = board.findAllMatches();

        while (!matches.isEmpty()) {
            board.fillPositions(matches); // Заполнить позиции новыми плитками
            matches = board.findAllMatches(); // Найти новые совпадения
        }
    }

    /**
     * Конструктор игры.
     */
    public Game() {
        board = new Board(WIDTH, HEIGHT); // Создать новую доску
        FindStableMatches(); // Найти стабильные совпадения

        // Установить предпочтительный размер панели
        setPreferredSize(new Dimension(WIDTH * board.TILE_SIZE, HEIGHT * board.TILE_SIZE + board.TILE_SIZE / 2));
        // Установить цвет фона панели
        setBackground(Color.BLACK);
        // Сделать панель фокусируемой для обработки событий мыши
        setFocusable(true);

        // Создать таймер для обновления экрана
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Найти все совпадения на доске
                List<Point> matches = board.findAllMatches();
                board.updateScore(matches.size()); // Обновить счет

                if (!matches.isEmpty()) {
                    UpdateTiles(matches); // Обработать совпадения
                    allMatchesProcessed = false; // Сбросить флаг
                } else {
                    allMatchesProcessed = true; // Установить флаг
                    gameOver = !board.hasPossibleMoves(); // Проверить окончание игры
                }
                repaint(); // Перерисовать экран
            }
        });

        if (allMatchesProcessed) {
            timer.stop(); // Остановить таймер, если все совпадения обработаны
        } else {
            timer.setRepeats(true); // Установить повтор таймера
            timer.start(); // Запустить таймер
        }

        // Добавить слушатель событий мыши для обработки кликов
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Рассчитать координаты плитки, по которой был сделан клик
                Point clickedPoint = new Point(e.getX() / board.TILE_SIZE, e.getY() / board.TILE_SIZE);
                // Обработать клик по плитке
                handleTileClick(clickedPoint);
            }
        });
    }

    /**
     * Проверяет, является ли позиция на доске допустимой.
     *
     *  point Позиция для проверки.
     *  true, если позиция допустима, false иначе.
     */
    private boolean isValidPosition(Point point) {
        if (point.x < 0 || point.y < 0 || point.x >= WIDTH || point.y >= HEIGHT)
            return false;
        return true;
    }

    /**
     * Обрабатывает клик по плитке.
     *
     *  point Позиция клика.
     */
    private void handleTileClick(Point point) {
        if (!isValidPosition(point))
            return;
        if (board.getValueAt(point.x, point.y) != -1) {
            if (selectedPoint.x == -1 && selectedPoint.y == -1) {
                selectedPoint = point; // Выбрать первую плитку
            } else {
                List<Point> matches = board.getMatchesFromSwap(selectedPoint, point);
                board.updateScore(matches.size()); // Обновить счет
                if (!matches.isEmpty()) {
                    UpdateTiles(matches); // Обработать совпадения
                }
                selectedPoint.x = -1;
                selectedPoint.y = -1; // Сбросить выбранную плитку
            }
        }
        repaint(); // Перерисовать экран
    }

    /**
     * Обновляет плитки после нахождения совпадений.
     *
     *  points Список позиций совпадений.
     */
    private void UpdateTiles(List<Point> points) {
        List<Point> cellsToReplace = board.shuffledDownToFill(points);

        // Запустить таймер для заполнения позиций после короткой задержки
        Timer fillTimer = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.fillPositions(cellsToReplace); // Заполнить позиции новыми плитками

                repaint(); // Перерисовать экран с заполненными позициями
                ((Timer) e.getSource()).stop(); // Остановить таймер
            }
        });

        fillTimer.setRepeats(false); // Таймер должен сработать только один раз
        fillTimer.start(); // Запустить таймер
    }

    /**
     * Метод для отрисовки компонента.
     *
     *  g Графический контекст для рисования.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameOver) {
            if (selectedPoint.x != -1 && selectedPoint.y != -1) {
                if (board.getValueAt(selectedPoint.x, selectedPoint.y) != -1) {
                    g.setColor(Color.ORANGE); // Установить цвет выбранной плитки
                    g.fillRect(selectedPoint.x * board.TILE_SIZE, selectedPoint.y * board.TILE_SIZE, board.TILE_SIZE,
                            board.TILE_SIZE);
                }
            }

            board.draw(g); // Отрисовать доску

            g.setColor(Color.WHITE); // Установить цвет текста
            g.setFont(new Font("Arial", Font.BOLD, 24)); // Установить шрифт текста
            g.drawString("Score: " + board.score, 10, HEIGHT * board.TILE_SIZE + board.TILE_SIZE / 2); // Вывести счет
        } else {
            g.setColor(Color.WHITE); // Установить цвет текста
            g.setFont(new Font("Arial", Font.BOLD, 24)); // Установить шрифт текста
            g.drawString("Game over", (WIDTH * board.TILE_SIZE) / 2 - board.TILE_SIZE,
                    (HEIGHT / 2) * board.TILE_SIZE + board.TILE_SIZE / 2); // Вывести сообщение об окончании игры
            g.drawString("Score: " + board.score, (WIDTH * board.TILE_SIZE) / 2 - board.TILE_SIZE + 15,
                    (HEIGHT / 2) * board.TILE_SIZE + board.TILE_SIZE / 2 + 25); // Вывести счет
        }
    }

    /**
     * Перезапускает игру.
     */
    public void restart() {
        board.score = 0; // Сбросить счет
        board.fillBoard(); // Заполнить доску новыми плитками
        FindStableMatches(); // Найти стабильные совпадения
        repaint(); // Перерисовать экран
    }
}
