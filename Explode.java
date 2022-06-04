import java.awt.*;

class Explode {
    private final int x;
    private final int y;
    private int step = 0;
    private final int[] diameter = {1, 4, 7, 12, 18, 26, 32, 40, 49, 30, 20, 14, 6};

    private boolean live;

    public boolean getLive() {
        return live;
    }

    Explode(int x, int y) {
        this.x = x;
        this.y = y;
        this.live = true;
    }

    void draw(Graphics graphics) {
        if (step == diameter.length) {
            live = false;
            step = 0;
            return;
        }

        Color color = graphics.getColor();
        graphics.setColor(Color.ORANGE);
        graphics.fillOval(x, y, diameter[step], diameter[step]);
        graphics.setColor(color);
        step++;
    }
}