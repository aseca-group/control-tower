from locust import HttpUser, between, task
import random


class Client(HttpUser):
    wait_time = between(1, 5)
    host = "http://0.0.0.0:8080"

    def __init__(self, *args, **kwargs):
        super().__init__(args, kwargs)
        self.address_id = None
        self.customer_id = None

    def on_start(self):
        client_id = self.client.post("/customer", json={"name": "John Doe"})
        address_id = self.client.post("/address", json={"city": "New York", "road": "5th Avenue", "number": 123})
        self.customer_id = client_id.json().get("id")
        self.address_id = address_id.json().get("id")

    @task
    def create_order(self):
        self.client.post("/order", json={
            "productsId": [
                {
                    "productId": 1,
                    "qty": 2
                },
                {
                    "productId": 2,
                    "qty": 1
                }
            ],
            "addressId": self.address_id,
            "customerId": self.customer_id,
            "total": 89.99
        })


class StoreAdmin(HttpUser):
    wait_time = between(1, 5)
    host = "http://0.0.0.0:8080"

    def __init__(self, *args, **kwargs):
        super().__init__(args, kwargs)
        self.product_ids = []
        self.inventory_ids = []

    @task(1)
    def create_product(self):
        product_id = self.client.post("/product", json={"price": 1.0, "name": "bread"})
        self.product_ids.append(product_id.json().get("id"))

    @task(1)
    def create_inventory(self):
        if len(self.product_ids) == 0:
            return
        rnd = random.randint(0, len(self.product_ids) - 1)
        inventory_id = self.client.post("/inventory", json={"productId": self.product_ids[rnd], "stock": 10})
        self.inventory_ids.append(inventory_id.json().get("id"))

    @task(5)
    def add_stock(self):
        if len(self.product_ids) == 0:
            return
        if len(self.inventory_ids) == 0:
            return
        rnd = random.randint(0, len(self.product_ids) - 1)
        rndStock = random.randint(0, 60)
        self.client.patch("/addStock", json={"productId": self.product_ids[rnd], "stockToAdd": rndStock})


class ControlTowerCoordinator(HttpUser):
    wait_time = between(1, 5)
    host = "http://0.0.0.0:8080"

    @task
    def get_all_orders(self):
        self.client.get("/order")

    @task
    def get_all_inventories(self):
        self.client.get("/inventory")

    @task
    def get_all_deliveries(self):
        self.client.get("/delivery")
