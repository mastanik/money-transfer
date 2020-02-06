# Simple Money Transfer Project

The application exposes 3 REST endpoints in order to create Customer, Account and make an Operations. It uses in-memory H2 database for persistence.
The following main libraries are used:
* Java 11
* Google Guice
* Spark
* JOOQ
* HikariCP
* Flyway

## Building the app

To generate sources and execute tests run the following command:
```$java
mvn clean package
```

By default the following database properties are used by Flyway migration plugin and jooq generation plugin:
```bash
db.url: jdbc:h2:~/revolut-db
db.username: revolut-user
db.password: revolut-password
```

You can override it by providing the properties via command line, like this:

```bash
mvn clean package -Ddb.url=$URL -Ddb.username=$USERNAME -Ddb.password=$PASSWORD
```

## Rest endpoints

### Customer

The following endpoints exposed:
```bash
/customers
/customer/:id
/customer
```

#### Sample requests
GET http://localhost:4567/customers
```json
200
[
    {
      "id": 8,
      "firstName": "Kobe",
      "lastName": "Bryant",
      "dateOfBirth": "1978-08-23"
    },
    {
      "id": 10,
      "firstName": "John",
      "lastName": "Doe",
      "dateOfBirth": "1999-09-09"
    }
]
```

GET http://localhost:4567/customer/8
```json
200
{
    "id": 8,
    "firstName": "Kobe",
    "lastName": "Bryant",
    "dateOfBirth": "1978-08-23"
}
```

POST http://localhost:4567/customer
```json
200
{
    "firstName": "Test",
    "lastName": "User",
    "dateOfBirth": "1999-09-09"
}
```
```json
200
{
    "id": 18,
    "firstName": "Test",
    "lastName": "User",
    "dateOfBirth": "1999-09-09"
}
```

### Account 

The following endpoints exposed:
```bash
/accounts
/account/:id
/account
```
#### Sample requests
GET http://localhost:4567/accounts
```json
200
{
    "id": 2,
    "customerId": 6,
    "currency": "EUR",
    "balance": 102.75
}
```

GET http://localhost:4567/accounts
```json
200
{
    "id": 2,
    "customerId": 6,
    "currency": "EUR",
    "balance": 102.75
}
```

POST http://localhost:4567/account
```json
{
    "customerId": 2,
    "currency": "GBP"
}
```
```json
200
{
    "id": 14,
    "customerId": 2,
    "currency": "GBP",
    "balance": 0.0000
}
```

### Operation

The following endpoints exposed:
```bash
/operation/deposit
/operation/withdraw
/operation/transfer
```
#### Sample requests
POST http://localhost:4567/operation/deposit
```json
{
    "accountId": 4,
    "currency": "USD",
    "amount": 50
}
```
```json
200
{
    "success": true,
    "httpStatusCode": 200,
    "date": "2020-02-05T16:49:41.689"
}
```

POST http://localhost:4567/operation/withdraw
```json
{
    "accountId": 3,
    "currency": "USD",
    "amount": 10
}
```
```json
200
{
    "success": true,
    "httpStatusCode": 200,
    "date": "2020-02-05T16:50:39.817"
}
```
POST http://localhost:4567/operation/transfer
```json
{
    "accountIdFrom": 4,
    "accountIdTo": 6,
    "currency": "EUR",
    "amount": 10
    }
```
```json
200
{
    "success": true,
    "httpStatusCode": 200,
    "date": "2020-02-05T16:51:08.254"
}
```