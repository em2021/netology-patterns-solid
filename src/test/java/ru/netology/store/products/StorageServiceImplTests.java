package ru.netology.store.products;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageServiceImplTests {

    private StorageServiceImpl ssi;

    @BeforeAll
    public static void startAll() {
        System.out.println("StorageServiceImpl tests started");
    }

    @BeforeEach
    public void init() {
        ssi = new StorageServiceImpl("TestStorage");
        System.out.println("StorageServiceImpl test started");
    }

    @AfterEach
    public void completed() {
        System.out.println("StorageServiceImpl test completed");
    }

    @AfterAll
    public static void completedAll() {
        System.out.println("StorageServiceImpl tests completed");
    }

    @Test
    public void testAddItemWithSameId_shouldAddQty() {
        //given:
        Product product = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        ssi.addItem(product, 2);
        int expected = 3;
        //then:
        int actual = ssi.getItemEntryById(product.getId()).getValue();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testAddNewItem_shouldGenerateNewId() {
        //given:
        Product product = new Product("testProduct");
        Product product2 = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        ssi.addItem(product2, 2);
        int expected = 2;
        //then:
        int actual = product2.getId();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testAddItemWithNegativeQty_shouldNotAdd() {
        //given:
        Product product = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        boolean negativeValueAdditionResult = ssi.addItem(product, -1);
        int expected = 1;
        //then:
        int actual = ssi.getItemEntryById(product.getId()).getValue();
        Assertions.assertEquals(expected, actual);
        Assertions.assertFalse(negativeValueAdditionResult);
    }

    @Test
    public void testTakeItemWithSameQty_shouldTake() {
        //given:
        Product product = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        boolean equalValueRetrievalResult = ssi.takeItem(product, 1);
        int expected = 0;
        //then:
        int actual = ssi.getItemEntryById(product.getId()).getValue();
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(equalValueRetrievalResult);
    }

    @Test
    public void testTakeItemWithExceedingQty_shouldNotTake() {
        //given:
        Product product = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        boolean exceedingValueRetrievalResult = ssi.takeItem(product, 2);
        int expected = 1;
        //then:
        int actual = ssi.getItemEntryById(product.getId()).getValue();
        Assertions.assertEquals(expected, actual);
        Assertions.assertFalse(exceedingValueRetrievalResult);
    }

    @Test
    public void testTakeItemNotInStorage_shouldNot_andShouldPrintMessage() {
        //given:
        Product product = new Product("testProductAddedToStorage");
        Product productNotInStorage = new Product("testProductNotAdded");
        ssi.addItem(product, 1);
        PrintStream standardOut = System.out;
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        String expected = "Storage \"TestStorage\" does not contain " + productNotInStorage.getName();
        //when:
        System.setOut(new PrintStream(outputStreamCaptor));
        boolean notInStorageRetrievalResult = ssi.takeItem(productNotInStorage, 1);
        System.setOut(standardOut);
        //then:
        Assertions.assertFalse(notInStorageRetrievalResult);
        Assertions.assertEquals(expected, outputStreamCaptor.toString().trim());
    }

    @Test
    public void testGetItemEntryByExistingId_shouldReturn() {
        //given:
        Product expected = new Product("testProduct");
        ssi.addItem(expected, 1);
        //when:
        Map.Entry<Item, Integer> mapEntry = ssi.getItemEntryById(1);
        Item actual = mapEntry.getKey();
        //then:
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGetItemEntryByNonExistingId_shouldReturn() {
        //given:
        Product product = new Product("testProduct");
        ssi.addItem(product, 1);
        //when:
        Map.Entry<Item, Integer> actual = ssi.getItemEntryById(2);
        //then:
        Assertions.assertNull(actual);
    }

    @Test
    public void testGetStorageContentsReturnsAddedItems_shouldReturn() {
        //given:
        Product product = new Product("testProduct");
        Product product2 = new Product("testProduct2");
        product.setRetailPrice(132);
        product2.setRetailPrice(145);
        int productQty = 5;
        int product2Qty = 3;
        ssi.addItem(product, productQty);
        ssi.addItem(product2, product2Qty);
        final AtomicInteger matchesInMapEntrySet = new AtomicInteger();
        //when:
        Set<Map.Entry<Item, Integer>> mapEntrySet = ssi.getStorageContents();
        //then:
        mapEntrySet.forEach(s -> {
            Item i = s.getKey();
            int actualQty = s.getValue();
            if (i.equals(product)) {
                Assertions.assertEquals(product, i);
                Assertions.assertEquals(product.getRetailPrice(), i.getRetailPrice());
                Assertions.assertEquals(productQty, actualQty);
                matchesInMapEntrySet.getAndIncrement();
            } else {
                Assertions.assertEquals(product2, i);
                Assertions.assertEquals(product2.getRetailPrice(), i.getRetailPrice());
                Assertions.assertEquals(product2Qty, actualQty);
                matchesInMapEntrySet.getAndIncrement();
            }
        });
        Assertions.assertTrue(matchesInMapEntrySet.get() == mapEntrySet.size());
    }
}
