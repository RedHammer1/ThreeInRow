package edu.mephi.java.engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, представляющий игровую доску.
 */
public class Board {
    // Количество строк и столбцов на доске
    private int numrows, numcols;
    // Двумерный массив для хранения значений плиток
    private int[][] board;
    // Генератор случайных чисел для заполнения доски
    private Random random;

    // Размер плитки в пикселях
    protected final int TILE_SIZE = 40;
    // Текущий счет игрока
    public int score = 0;
    // Флаг, указывающий на необходимость обмена только соседними плитками
    private boolean enforceAdjacent;

    /**
     * Создает новую игровую доску с указанным количеством строк и столбцов.
     *
     *  numrows Количество строк на доске.
     *  numcols Количество столбцов на доске.
     */
    Board(int numrows, int numcols) {
        this.numrows = numrows;
        this.numcols = numcols;
        board = new int[numrows][numcols];
        random = new Random();
        enforceAdjacent = true;
        fillBoard(); // Заполнить доску случайными значениями
        score = 0; // Сбросить счет
    }

    /**
     * Заполняет доску случайными значениями для плиток (от 0 до 3).
     */
    public void fillBoard() {
        for (int x = 0; x < numrows; x++) {
            for (int y = 0; y < numcols; y++) {
                // Генерировать случайное значение для каждой плитки
                setValueAt(x, y, random.nextInt(0, 4));
            }
        }
    }

    /**
     * Обменивает местами две плитки на доске.
     *
     *  first  Первая плитка.
     *  second Вторая плитка.
     */
    public void swapTiles(Point first, Point second) {
        // Если нет совпадений, то вернуть плитки на место
        int temp = board[first.x][first.y];
        board[first.x][first.y] = board[second.x][second.y];
        board[second.x][second.y] = temp;
    }

    /**
     * Обновляет счет игрока на основе количества совпадений.
     *
     *  points Количество совпадений.
     */
    public void updateScore(int points) {
        if (points > 0) {
            score += points * 100; // Увеличить счет на 100 очков за каждое совпадение
        }
    }

    /**
     * Возвращает количество строк на доске.
     *
     *  Количество строк.
     */
    public int getNumRows() {
        return numrows;
    }

    /**
     * Возвращает количество столбцов на доске.
     *
     *  Количество столбцов.
     */
    public int getNumCols() {
        return numcols;
    }

    /**
     * Возвращает значение плитки по указанным координатам.
     *
     *  row    Номер строки.
     *  col    Номер столбца.
     *  Значение плитки.
     */
    public int getValueAt(int row, int col) {
        return board[row][col];
    }

    /**
     * Устанавливает значение плитки по указанным координатам.
     *
     *  row     Номер строки.
     *  col     Номер столбца.
     *  tile_id Значение плитки.
     */
    public void setValueAt(int row, int col, int tile_id) {
        board[row][col] = tile_id;
    }

    /**
     * Проверяет, есть ли возможные ходы на доске.
     *
     *  true, если есть возможные ходы, false иначе.
     */
    public boolean hasPossibleMoves() {
        for (int x = 0; x < numrows; x++) {
            for (int y = 0; y < numcols; y++) {
                // Проверить обмен с соседними плитками
                if (x < numrows - 1) { // Проверить обмен с плиткой справа
                    Point p1 = new Point(x, y);
                    Point p2 = new Point(x + 1, y);
                    List<Point> matches = getMatchesFromSwap(p1, p2);
                    if (!matches.isEmpty()) {
                        return true; // Если есть совпадения, то есть возможные ходы
                    }
                }
                if (y < numcols - 1) { // Проверить обмен с плиткой ниже
                    Point p1 = new Point(x, y);
                    Point p2 = new Point(x, y + 1);
                    List<Point> matches = getMatchesFromSwap(p1, p2);
                    if (!matches.isEmpty()) {
                        return true; // Если есть совпадения, то есть возможные ходы
                    }
                }
            }
        }
        return false; // Если нет совпадений, то ходов нет
    }

    /**
     * Возвращает список совпадений после обмена двух плиток.
     *
     *  p1 Первая плитка.
     *  p2 Вторая плитка.
     *  Список совпадений.
     */
    public List<Point> getMatchesFromSwap(Point p1, Point p2) {
        List<Point> matched = new ArrayList<>();
        // Reject invalid swap if enforcing an adjacent swap
        int diffx = Math.abs(p1.x - p2.x);
        int diffy = Math.abs(p1.y - p2.y);
        if (enforceAdjacent && !((diffx == 0 || diffy == 0) && (diffx == 1 || diffy == 1)))
            return matched; // Если обмен не соседних плиток, то возвращаем пустой список

        swapTiles(p1, p2); // Обменять плитки

        getMatchesOnRow(p1.y, matched); // Проверить совпадения в строке первой плитки
        if (!enforceAdjacent || diffy > 0)
            getMatchesOnRow(p2.y, matched); // Проверить совпадения в строке второй плитки
        getMatchesOnColumn(p1.x, matched); // Проверить совпадения в столбце первой плитки
        if (!enforceAdjacent || diffx > 0)
            getMatchesOnColumn(p2.x, matched); // Проверить совпадения в столбце второй плитки

        // Reverse the swapping of cells
        swapTiles(p1, p2); // Вернуть плитки на место

        return matched;
    }

    /**
     * Возвращает список позиций, которые необходимо заполнить после удаления совпадений.
     *
     *  positions Список позиций совпадений.
     *  Список позиций для заполнения.
     */
    public List<Point> shuffledDownToFill(List<Point> positions) {
        int[] affectedColumns = new int[numrows];
        for (int x = 0; x < numrows; x++) {
            affectedColumns[x] = 0;
        }

        for (Point pos : positions) {
            affectedColumns[pos.x]++;
            shuffleDownToFill(pos); // Опустить плитки в столбце
        }

        return getAffectedPositionsFromAffectedColumns(affectedColumns);
    }

    /**
     * Заполняет указанные позиции на доске случайными значениями.
     *
     *  positions Список позиций для заполнения.
     */
    public void fillPositions(List<Point> positions) {
        for (Point p : positions) {
            board[p.x][p.y] = random.nextInt(4); // Установить случайное значение
        }
    }

    /**
     * Заполняет одну позицию на доске случайным значением.
     *
     *  p Позиция для заполнения.
     */
    public void fillTile(Point p) {
        board[p.x][p.y] = random.nextInt(4); // Установить случайное значение
    }

    /**
     * Возвращает список всех совпадений на доске.
     *
     *  Список совпадений.
     */
    public List<Point> findAllMatches() {
        List<Point> matched = new ArrayList<>();
        for (int y = 0; y < numcols; y++) {
            getMatchesOnRow(y, matched); // Проверить совпадения в строке
        }

        for (int x = 0; x < numrows; x++) {
            getMatchesOnColumn(x, matched); // Проверить совпадения в столбце
        }

        return matched;
    }

    /**
     * Возвращает список совпадений в указанной строке.
     *
     *  y       Номер строки.
     *  matchedRef Список для добавления совпадений.
     */
    private void getMatchesOnRow(int y, List<Point> matchedRef) {
        for (int x = 0; x < numrows - 2; x++) {
            if (board[x][y] != -1 &&
                    board[x][y] == board[x + 1][y] && board[x][y] == board[x + 2][y]) {
                int matchValue = board[x][y];
                Point p = new Point(x, y);
                if (!matchedRef.contains(p))
                    matchedRef.add(p); // Добавить совпадение в список
                x++;
                while (x < numrows && board[x][y] == matchValue && board[x][y] != -1) {
                    p = new Point(x, y);
                    if (!matchedRef.contains(p))
                        matchedRef.add(p); // Добавить все последовательные совпадения
                    x++;
                }
                x--;
            }
        }
    }

    /**
     * Возвращает список совпадений в указанном столбце.
     *
     *  x       Номер столбца.
     *  matchedRef Список для добавления совпадений.
     */
    private void getMatchesOnColumn(int x, List<Point> matchedRef) {
        for (int y = 0; y < numcols - 2; y++) {

            if (board[x][y] != -1 &&
                    board[x][y] == board[x][y + 1] && board[x][y] == board[x][y + 2]) {
                int matchValue = board[x][y];
                Point p = new Point(x, y);
                if (!matchedRef.contains(p))
                    matchedRef.add(p); // Добавить совпадение в список
                y++;
                while (y < numcols && board[x][y] == matchValue && board[x][y] != -1) {
                    p = new Point(x, y);
                    if (!matchedRef.contains(p))
                        matchedRef.add(p); // Добавить все последовательные совпадения
                    y++;
                }
                y--;
            }
        }
    }

    /**
     * Опускает плитки в столбце, чтобы заполнить пустые места.
     *
     *  pos Позиция, начиная с которой опустить плитки.
     */
    public void shuffleDownToFill(Point pos) {
        for (int y = pos.y; y >= 1; y--) {
            board[pos.x][y] = board[pos.x][y - 1]; // Переместить плитку вниз
        }
    }

    /**
     * Возвращает список позиций, которые необходимо заполнить после удаления совпадений.
     *
     *  affectedColumns Количество пустых мест в каждом столбце.
     *  Список позиций для заполнения.
     */
    private List<Point> getAffectedPositionsFromAffectedColumns(int[] affectedColumns) {
        List<Point> affectedPositions = new ArrayList<>();
        for (int x = 0; x < affectedColumns.length; x++) {
            if (affectedColumns[x] > 0) {
                for (int y = 0; y < affectedColumns[x]; y++) {
                    affectedPositions.add(new Point(x, y)); // Добавить позиции для заполнения
                }
            }
        }
        return affectedPositions;
    }

    /**
     * Отрисовывает доску с плитками.
     *
     *  g Графический контекст для рисования.
     */
    public void draw(Graphics g) {
        for (int x = 0; x < numrows; x++) {
            for (int y = 0; y < numcols; y++) {

                // Установить цвет плитки в зависимости от ее значения
                g.setColor(getColorForTile(board[x][y]));
                // Нарисовать плитку
                g.fillOval(x * TILE_SIZE + 2, y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);

                // Нарисовать границу плитки
                g.setColor(Color.GRAY);
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /**
     * Возвращает цвет плитки в зависимости от ее значения.
     *
     *  tileValue Значение плитки.
     *  Цвет плитки.
     */
    private Color getColorForTile(int tileValue) {
        switch (tileValue) {
            case 0:
                return Color.RED; // Красный цвет для значения 0
            case 1:
                return Color.GREEN; // Зеленый цвет для значения 1
            case 2:
                return Color.BLUE; // Синий цвет для значения 2
            case 3:
                return Color.YELLOW; // Желтый цвет для значения 3
            default:
                return Color.WHITE; // Белый цвет для пустой плитки
        }
    }
}
