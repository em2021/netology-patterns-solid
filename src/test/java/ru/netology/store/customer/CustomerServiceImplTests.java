package ru.netology.store.customer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.store.products.Item;
import ru.netology.store.products.Product;
import ru.netology.store.products.StorageService;
import ru.netology.store.sales.SalesRegistrationService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CustomerServiceImplTests {

    private CustomerServiceImpl csi;
    private final PrintStream standardOut = System.out;
    private final InputStream standardIn = System.in;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public static void startAll() {
        System.out.println("CustomerServiceImpl tests started");
    }

    @BeforeEach
    public void init() {
        System.out.println("CustomerServiceImpl test started");
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void completed() {
        System.setOut(standardOut);
        System.out.println("CustomerServiceImpl test completed");
    }

    @AfterAll
    public static void completedAll() {
        System.out.println("CustomerServiceImpl tests completed");
    }

    @Test
    public void testPrintMenu_shouldPrint() {
        //given:
        StorageService storageServiceMock = Mockito.mock(StorageService.class);
        SalesRegistrationService srsMock = Mockito.mock(SalesRegistrationService.class);
        csi = new CustomerServiceImpl(storageServiceMock, srsMock);
        StringBuilder expected = new StringBuilder();
        expected.append("Please choose an action by entering it's number and press ENTER:\n");
        expected.append("1. Look what's in stock\n");
        expected.append("2. Make a new purchase\n");
        expected.append("3. Return a previously made purchase\n");
        expected.append("0. Exit\n");
        //when:
        csi.printMenu();
        //then:
        Assertions.assertEquals(expected.toString().trim(), outputStreamCaptor.toString().trim());
    }

    @ParameterizedTest
    @MethodSource("emptyMapEntrySource")
    public void testListAvailableItemsWithEmptyOrNullMapEntry_shouldPrintMessage(Set<Map.Entry<Item, Integer>> testMapEntry,
                                                              String expected) {
        //given:
        StorageService storageServiceMock = Mockito.mock(StorageService.class);
        SalesRegistrationService srsMock = Mockito.mock(SalesRegistrationService.class);
        csi = new CustomerServiceImpl(storageServiceMock, srsMock);
        //when:
        Mockito.when(storageServiceMock.getStorageContents())
                .thenReturn(testMapEntry);
        csi.listAvailableItems();
        //then:
        Assertions.assertEquals(expected, outputStreamCaptor.toString().trim());
    }

    @ParameterizedTest
    @MethodSource("validMapEntrySource")
    public void testListAvailableItemsFiltering_shouldFilter(Set<Map.Entry<Item, Integer>> testMapEntry,
                                                String expected) {
        //given:
        StorageService storageServiceMock = Mockito.mock(StorageService.class);
        SalesRegistrationService srsMock = Mockito.mock(SalesRegistrationService.class);
        csi = new CustomerServiceImpl(storageServiceMock, srsMock);
        //when:
        Mockito.when(storageServiceMock.getStorageContents())
                .thenReturn(testMapEntry);
        csi.listAvailableItems();
        //then:
        Assertions.assertEquals(expected.trim(), outputStreamCaptor.toString().trim());
    }

    public static Stream<Arguments> emptyMapEntrySource() {
        Product testProductNoQty = new Product("testProductNoQty");
        testProductNoQty.setRetailPrice(100);
        Map.Entry<Item, Integer> testMapEntryNoQty = new AbstractMap.SimpleEntry<>(testProductNoQty, 0);
        Set<Map.Entry<Item, Integer>> testMapEntrySetNoQty = new HashSet<>();
        testMapEntrySetNoQty.add(testMapEntryNoQty);
        Product testProductNoPrice = new Product("testProductNoPrice");
        testProductNoPrice.setRetailPrice(0);
        Map.Entry<Item, Integer> testMapEntryNoPrice = new AbstractMap.SimpleEntry<>(testProductNoPrice, 1);
        Set<Map.Entry<Item, Integer>> testMapEntrySetNoPrice = new HashSet<>();
        testMapEntrySetNoPrice.add(testMapEntryNoPrice);
        return Stream.of(
                Arguments.of(testMapEntrySetNoQty, "Sorry, nothing in stock at the moment."),
                Arguments.of(testMapEntrySetNoPrice, "Sorry, nothing in stock at the moment."),
                Arguments.of(new HashSet<>(), "Sorry, nothing in stock at the moment.")
        );
    }

    public static Stream<Arguments> validMapEntrySource() {
        Product testProductNoQty = new Product("testProductNoQty");
        testProductNoQty.setRetailPrice(100);
        Product testProductNoPrice = new Product("testProductNoPrice");
        testProductNoPrice.setRetailPrice(0);
        Product testProduct = new Product("testProduct");
        testProduct.setRetailPrice(50);
        Map.Entry<Item, Integer> testMapEntryNoQty = new AbstractMap.SimpleEntry<>(testProductNoQty, 0);
        Map.Entry<Item, Integer> testMapEntryNoPrice = new AbstractMap.SimpleEntry<>(testProductNoPrice, 5);
        Map.Entry<Item, Integer> testMapEntry = new AbstractMap.SimpleEntry<>(testProduct, 5);
        Set<Map.Entry<Item, Integer>> testMapEntrySet = new HashSet<>();
        testMapEntrySet.add(testMapEntryNoQty);
        testMapEntrySet.add(testMapEntryNoPrice);
        testMapEntrySet.add(testMapEntry);
        StringBuilder sb = new StringBuilder()
                .append("The following items are available:\n")
                .append(String.format("%-10s %-25s %-15s %s%n", "Id", "Name", "Price", "Qty"))
                .append(String.format("%-10d %-25s %-15d %d%n", 0, "testProduct", 50, 5));
        System.out.println(sb);
        return Stream.of(
                Arguments.of(testMapEntrySet, sb.toString())
        );
    }
}
