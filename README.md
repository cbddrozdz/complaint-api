# Complaint API

## Overview
The Complaint API is a RESTful service designed to handle complaints about products. The service supports creating, updating, and retrieving complaints. It also integrates with an external GeoLocation API to determine the country of the user based on their IP address. This feature helps in identifying the location from which the complaint is being made, enhancing the contextual data available for each complaint.

## Features
- **Add a new complaint**: Allows users to submit a complaint for a product.
- **Retrieve a complaint**: Retrieve the details of a complaint using its unique identifier.
- **Retrieve all complaints**: Returns a list of all complaints with pagination support.
- **Update a complaint**: Allows users to update the content of an existing complaint.

## API Documentation
The API is documented using Swagger and can be accessed via the `/swagger-ui.html` endpoint.

### Endpoints

1. **Add a New Complaint**
    - **URL**: `/api/v1/complaints`
    - **Method**: `POST`
    - **Request Body**:
      ```json
      {
        "productId": "string",
        "content": "string",
        "reporter": "string",
        "country": "string"
      }
      ```
      > **Note**: The `country` field is automatically set based on the user's IP address and should not be included in the request.
    - **Response**: `200 OK` with the created complaint details.
    - **Errors**:
        - `400 Bad Request` for invalid input.
        - `502 Bad Gateway` if the external GeoLocation service fails.
        - `500 Internal Server Error` for general server issues.

2. **Get a Complaint by ID**
    - **URL**: `/api/v1/complaints/{id}`
    - **Method**: `GET`
    - **Response**: `200 OK` with the complaint details.
    - **Errors**:
        - `404 Not Found` if the complaint does not exist.
        - `500 Internal Server Error` for general server issues.

3. **Get All Complaints**
    - **URL**: `/api/v1/complaints/all`
    - **Method**: `GET`
    - **Request Params**:
        - `page`: The page number (default `0`).
        - `size`: The number of items per page (default `10`).
    - **Response**: `200 OK` with a paginated list of complaints.
    - **Errors**:
        - `500 Internal Server Error` for general server issues.

4. **Update a Complaint**
    - **URL**: `/api/v1/complaints`
    - **Method**: `PUT`
    - **Request Body**:
      ```json
      {
        "id": "string",
        "content": "string"
      }
      ```
    - **Response**: `200 OK` with the updated complaint details.
    - **Errors**:
        - `404 Not Found` if the complaint does not exist.
        - `400 Bad Request` for invalid input.
        - `500 Internal Server Error` for general server issues.

## Handling IP Address and Country
The API uses the `X-Forwarded-For` header to retrieve the client's IP address. This is important in real-world deployments, especially behind proxies or load balancers, where the `X-Forwarded-For` header is used to convey the original client's IP address.

If this header is not present, the `request.getRemoteAddr()` method can be used to retrieve the IP address directly from the request. For demonstration purposes, this is shown as a comment in the code:

```java
// complaintRequest.setCountry(geoLocationService.getCountryByIp(request.getRemoteAddr()));
```

This ensures that the service can operate correctly both in environments with and without a proxy. If neither the `X-Forwarded-For` header nor `request.getRemoteAddr()` provide a valid IP address, the country field will default to 'Unknown' or an appropriate fallback value.

## Resilience4j Integration
The service uses Resilience4j to add fault tolerance when communicating with the external GeoLocation service. The following resilience patterns are implemented:

- **Circuit Breaker**: Protects the service from external API failures by stopping further requests to the API when failures are detected.
- **Retry**: Automatically retries failed requests to the external API before triggering the circuit breaker.

These configurations are specified in the `application.yml` file:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      geoLocationService:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
  retry:
    instances:
      geoLocationServiceRetry:
        max-attempts: 3
        wait-duration: 500ms
```

## Optimistic Locking and Race Conditions
To handle potential race conditions when updating complaint records (for example, to increment the report count), the application uses pessimistic locking. This ensures that if two updates conflict, one of them will fail and can be retried. The following configuration is applied in the `ComplaintRepository`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Complaint> findById(UUID id);
```

## Additional Notes
- **`modifiedAt` Field**: The `Complaint` entity includes a `modifiedAt` field to track the last modification timestamp, which is helpful in conjunction with pessimistic locking.
- **Liquibase**: The project uses Liquibase for database migrations. Ensure that the necessary changes are reflected in the `db/changelog` files.

## Running the Application

To run the application locally using Docker:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/complaint-api.git
   cd complaint-api
   ```

2. **Ensure Docker and Docker Compose are installed** on your machine.

3. **Build and run the Docker containers**:
   Use the following command to build the Docker image for the application and start the PostgreSQL container along with the application container.
   ```bash
   docker-compose up --build
   ```

4. **Access the application**:
   Once the containers are running, you can access the API documentation via Swagger at:
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

5. **Stopping the containers**:
   To stop the running containers, use:
   ```bash
   docker-compose down
   ```

### Docker Configuration Details

- **PostgreSQL Container**:
    - Image: `postgres:15`
    - Environment:
        - `POSTGRES_USER`: `user`
        - `POSTGRES_PASSWORD`: `userpassword`
        - `POSTGRES_DB`: `complaints_db`
    - Ports: `5432:5432`
    - Volume: `postgres-data:/var/lib/postgresql/data`

- **Spring Boot Application Container**:
    - Build context: The current directory (which contains the Dockerfile)
    - Environment:
        - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://postgres:5432/complaints_db`
        - `SPRING_DATASOURCE_USERNAME`: `user`
        - `SPRING_DATASOURCE_PASSWORD`: `userpassword`
    - Ports: `8080:8080`
    - Dependency: The application container waits for the PostgreSQL container to be ready before starting.

### Notes

- The `application.yml` is configured to use the database connection provided by the Docker container.
- All necessary database changes will be applied automatically using Liquibase on application startup.
- Ensure that Docker Desktop or Docker Engine is running before executing the `docker-compose up` command.
- For local development without Docker, consider using the `application-dev.yml` profile, which connects to a locally hosted PostgreSQL instance.