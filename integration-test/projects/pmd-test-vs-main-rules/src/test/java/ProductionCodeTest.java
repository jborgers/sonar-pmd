public class ProductionCodeTest extends junit.framework.TestCase {
	private int unused;

	public void testProductionCode() {
		unused = 1;
		assertEquals(1, unused);
		assertFalse(2 == unused);
	}

    public void makeTwoWhenOne() {
        if (unused == 1) unused = 2;
    }
}
