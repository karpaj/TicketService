# TicketService
## Assumptions:
- Only a single thread of execution may access a single instance of this class at a time.
- The lower the number seat, the better the seat is.
- Customers may have multiple holds.
- If a given hold expires there is no guarantee about the safety of their seats. Other holds might have picked them up in the meantime.
- If a hold expires and a single seat is lost to another hold, all seats held by the first hold are voided.
- A hold picks the best seats at the time of the hold.

## Test and Build
#### Requirements
- Java 7+
- Maven

#### Test
$ mvn test

#### Build Target Jar
$ mvn package
