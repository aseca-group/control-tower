from locust import HttpUser, between, task
import random

base_path = "http://control-tower-control-tower-1:8080"


class Client(HttpUser):
    wait_time = between(1, 5)
    address_id = None
    customer_id = None

    def on_start(self):
        client_id = self.client.post(base_path + "/customer", json={"name": "John Doe"})
        address_id = self.client.post(base_path + "/address",
                                      json={"city": "New York", "road": "5th Avenue", "number": 123})
        self.customer_id = client_id.json().get("id")
        self.address_id = address_id.json().get("id")

    @task
    def create_order(self):
        self.client.post(base_path + "/order", json={
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
            "customerId": self.customer_id
        })


class StoreAdmin(HttpUser):
    wait_time = between(1, 5)
    product_ids = []
    inventory_ids = []

    @task
    def create_product(self):
        product_id = self.client.post(base_path + "/product", json={"price": 1.0, "name": "bread"})
        p_id = product_id.json().get("id")
        inventory_id = self.client.post(base_path + "/inventory", json={"productId": p_id, "stock": 10})
        i_id = inventory_id.json().get("productId")
        rndStock = random.randint(1, 60)
        self.client.patch(base_path + "/inventory/addStock", json={"productId": i_id, "stockToAdd": rndStock})


class ControlTowerCoordinator(HttpUser):
    wait_time = between(1, 5)

    @task
    def get_all_orders(self):
        self.client.get(base_path + "/order")

    @task
    def get_all_inventories(self):
        self.client.get(base_path + "/inventory/")

    @task
    def get_all_deliveries(self):
        self.client.get(base_path + "/delivery")
