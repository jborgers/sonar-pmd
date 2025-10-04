public class ProductionCode {
	private int unused;
	private int used;

    public void makeTwoWhenOne() {
        if (used == 1) used = 2;
    }
}
