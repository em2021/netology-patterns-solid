package ru.netology.store;

import ru.netology.store.customer.CustomerService;
import ru.netology.store.customer.CustomerServiceImpl;
import ru.netology.store.products.Product;
import ru.netology.store.products.StorageService;
import ru.netology.store.products.StorageServiceImpl;
import ru.netology.store.sales.SalesRegistrationService;
import ru.netology.store.sales.SalesRegistrationServiceImpl;

public class Main {
    public static void main(String[] args) {

        Product apple = new Product("Apple");
        Product peach = new Product("Peach");
        Product pear = new Product("Pear");
        apple.setRetailPrice(20);
        peach.setRetailPrice(30);
        pear.setRetailPrice(25);
        StorageService mainStorage = new StorageServiceImpl("Main storage");
        mainStorage.addItem(apple, 10);
        mainStorage.addItem(pear, 5);
        mainStorage.addItem(peach, 5);
        mainStorage.getStorageContents();
        SalesRegistrationService salesRegistration = new SalesRegistrationServiceImpl(mainStorage);
        CustomerService customerService = new CustomerServiceImpl(mainStorage, salesRegistration);
        customerService.start();
    }
}