package ru.netology.store.sales;

import ru.netology.store.customer.CustomerService;

public interface SalesRegistrationService {

    void runSalesMenu(CustomerService customerService);

    void recordSale(Basket basket);

}
