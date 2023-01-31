package ru.netology.store.sales;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.netology.store.customer.CustomerService;
import ru.netology.store.products.Item;
import ru.netology.store.products.StorageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesRegistrationServiceImpl implements SalesRegistrationService {

    private final StorageService storageService;
    private static final Scanner scanner = new Scanner(System.in);
    private static int saleCounter;
    private static SalesLogger salesLogger = SalesLogger.getInstance();
    private static LocalDate currentDate;

    public SalesRegistrationServiceImpl(StorageService storageService) {
        this.storageService = storageService;
        currentDate = LocalDateTime.now().toLocalDate();
    }

    @Override
    public void runSalesMenu(CustomerService customerService) {
        Basket basket = new Basket();
        while (true) {
            System.out.println("Please choose an action by entering it's number and press ENTER:");
            System.out.println("1. Add items to basket");
            System.out.println("2. Remove items from basket");
            System.out.println("3. Empty basket");
            System.out.println("4. View basket contents");
            System.out.println("5. Complete purchase");
            System.out.println("0. Return to previous menu");
            int choice = -1;
            try {
                choice = scanner.nextInt();
                String s = scanner.nextLine();
            } catch (Exception e) {
                String s = scanner.next();
            }
            if (choice == 0) {
                if (basket.getBasketContents().size() > 0) {
                    emptyBasket(basket);
                    break;
                } else {
                    break;
                }
            } else {
                switch (choice) {
                    case 1: {
                        //Add items
                        while (true) {
                            customerService.listAvailableItems();
                            System.out.println("Enter item id and quantity (Example: 1 3) " +
                                    "and press ENTER to continue or X to cancel");
                            String input = scanner.nextLine();
                            Pattern p = Pattern.compile("[0-9]+[ ]{1}[0-9]+");
                            Matcher m = p.matcher(input);
                            if (input.equals("X")) {
                                break;
                            } else if (!m.matches()) {
                                System.out.println("Illegal input");
                            } else {
                                String[] splitInput = new String[2];
                                splitInput = input.split(" ");
                                int id = Integer.parseInt(splitInput[0]);
                                int qty = Integer.parseInt(splitInput[1]);
                                addItem(basket, id, qty);
                            }
                        }
                        break;
                    }
                    case 2: {
                        //Remove item
                        while (true) {
                            if (basket.getBasketContents() == null
                                    || basket.getBasketContents().stream().allMatch(s -> s.getValue() == 0)) {
                                System.out.println("Nothing to remove. Your basket is empty.");
                                break;
                            } else {
                                viewBasketContents(basket);
                                System.out.println("Enter item id and quantity (Example: 1 3) " +
                                        "and press ENTER to continue or X to cancel");
                                String input = scanner.nextLine();
                                Pattern p = Pattern.compile("[0-9]+[ ]{1}[0-9]+");
                                Matcher m = p.matcher(input);
                                String[] splitInput = new String[2];
                                if (input.equals("X")) {
                                    break;
                                } else if (!m.matches()) {
                                    System.out.println("Illegal input");
                                    break;
                                } else {
                                    splitInput = input.split(" ");
                                }
                                int id = Integer.parseInt(splitInput[0]);
                                int qty = Integer.parseInt(splitInput[1]);
                                removeItem(basket, id, qty);
                            }
                        }
                        break;
                    }
                    case 3: {
                        //Empty basket
                        emptyBasket(basket);
                        break;
                    }
                    case 4: {
                        viewBasketContents(basket);
                        break;
                    }
                    case 5: {
                        //Complete purchase
                        if (basket.getBasketContents() == null
                                || basket.getBasketContents().stream().allMatch(s -> s.getValue() == 0)) {
                            System.out.println("Your basket is empty.");
                            break;
                        }
                        viewBasketContents(basket);
                        System.out.println("Would you like to complete your purchase?");
                        while (true) {
                            System.out.println("Enter Y or N and press ENTER to continue.");
                            String input = scanner.nextLine();
                            Pattern p = Pattern.compile("Y{1}|N{1}");
                            Matcher m = p.matcher(input);
                            if (!m.matches()) {
                                System.out.println("Illegal input");
                            } else if (input.equals("N")) {
                                break;
                            } else {
                                recordSale(basket);
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        System.out.println("Illegal input");
                        break;
                    }
                }
            }
        }
    }

    public void addItem(Basket basket, int id, int qty) {
        // Check if entered quantity is negative
        if (qty < 0) {
            System.out.println("Negative values are not allowed.");
            return;
        }
        Map.Entry<Item, Integer> itemEntry = storageService.getItemEntryById(id);
        // Check if item is available has a positive quantity and has a retail price
        if (itemEntry == null || itemEntry.getValue() <= 0 || itemEntry.getKey().getRetailPrice() <= 0) {
            System.out.println("Sorry, no items found with id " + id);
        } else {
            Item item = itemEntry.getKey();
            Integer availableQty = itemEntry.getValue();
            if (availableQty >= qty) {
                if (storageService.takeItem(item, qty)) {
                    basket.addItem(item, qty);
                    System.out.println(qty + " " + item.getName() +
                            " added to basket successfully");
                }
            } else {
                System.out.println("Not enough items. Only " + availableQty + " available");
            }
        }
    }

    public void removeItem(Basket basket, int id, int qty) {
        //Check if qty is not zero
        if (qty == 0) {
            System.out.println("You haven't entered any quantity to remove.");
        } else if (qty < 0) {
            System.out.println("Negative values are not allowed.");
        } else {
            //Check if storage contains the item with the id
            Item item = storageService.getItemEntryById(id).getKey();
            //If storage contains the item - check if basket contains the item
            if (item != null) {
                Optional<Map.Entry<Item, Integer>> optItem = basket.getBasketContents()
                        .stream()
                        .filter(s -> s.getKey().getId() == id && s.getValue() > 0)
                        .findFirst();
                Integer availableQty = 0;
                //If item is present in the basket get available quantity
                if (optItem.isPresent()) {
                    availableQty = optItem.get().getValue();
                    //If item is not present in the basket - print message to customer
                } else {
                    System.out.println("Sorry, no items in your basket with id " + id);
                }
                //Check if basket contains enough items to remove
                if (availableQty >= qty) {
                    storageService.addItem(item, qty);
                    basket.removeItem(item, qty);
                    System.out.println(qty + " " + item.getName() +
                            " removed from basket successfully");
                    //If there is not enough items in the basket to remove - print message to customer
                } else if (availableQty > 0) {
                    System.out.println("There's only " + availableQty + " items in your basket");
                }
                //If there is no item in the storage - print message to customer
            } else {
                System.out.println("Sorry, no items found with id " + id);
            }
        }

    }

    public void emptyBasket(Basket basket) {
        Set<Map.Entry<Item, Integer>> basketContents = basket.getBasketContents();
        if (basketContents == null) {
            System.out.println("Your basket is empty.");
        } else {
            basketContents.forEach(s -> storageService.addItem(s.getKey(), s.getValue()));
            basket.emptyBasket();
            System.out.println("Basket emptied successfully.");
        }
    }

    public void viewBasketContents(Basket basket) {
        Set<Map.Entry<Item, Integer>> basketContents = basket.getBasketContents();
        if (basketContents == null) {
            System.out.println("Your basket is empty");
        } else {
            System.out.printf("%-10s%-25s%-10s%-10s%s%n", "Id", "Name", "Price", "Qty", "Total");
            basketContents.stream()
                    .sorted((o1, o2) -> {
                        if (o1.getKey().getId() == o2.getKey().getId()) {
                            return 0;
                        } else {
                            return o1.getKey().getId() > o2.getKey().getId() ? 1 : -1;
                        }
                    })
                    .forEach(s -> {
                        if (s.getValue() > 0) {
                            System.out.printf("%-10s%-25s%-10d%-10d%d%n",
                                    s.getKey().getId(),
                                    s.getKey().getName(),
                                    s.getKey().getRetailPrice(),
                                    s.getValue(),
                                    s.getKey().getRetailPrice() * s.getValue());
                        }
                    });
            AtomicInteger subtotal = new AtomicInteger();
            basketContents.forEach(s -> {
                subtotal.addAndGet(s.getKey().getRetailPrice() * s.getValue());
            });
            System.out.printf("%55s%d%n", "SUBTOTAL:",
                    subtotal.get());
        }
    }

    @Override
    public void recordSale(Basket basket) {
        Set<Map.Entry<Item, Integer>> basketContents = basket.getBasketContents();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(basketContents.getClass(), new MapEntrySetAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(basketContents);
        saleCounter++;
        if (!LocalDateTime.now().toLocalDate().equals(currentDate)) {
            currentDate = LocalDateTime.now().toLocalDate();
            saleCounter = 1;
        } else {
            String fileName = currentDate + "_sale_" + saleCounter;
            salesLogger.log(json, "out", fileName);
            basket.emptyBasket();
        }
    }
}