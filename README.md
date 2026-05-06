# Fast Car Fix Bot

Telegram bot for creating car repair requests and finding nearby service centers based on user location.

This project demonstrates a state-driven Telegram bot built with Spring Boot, including REST API, geolocation search, and PostgreSQL integration with PostGIS.

## Features

- Create car repair requests via Telegram
- Select repair service type (oil change, tires, diagnostics, etc.)
- Add problem description
- Share user location
- Find nearby service centers
- REST API for managing requests
- State machine-based conversation flow

## Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- PostGIS
- Docker
- Telegram Bots API

## Bot Flow

/start  
→ Select repair type  
→ Enter problem description  
→ Send location  
→ Receive list of nearby service centers  
→ Choose next action  

## Requirements

- Java 17+
- Maven
- Docker
- Docker Compose
- PostgreSQL with PostGIS

## Database (Docker)

The project uses PostgreSQL with PostGIS running in Docker.

Start database:

docker-compose up -d

Example `docker-compose.yml`:

version: "3.8"

services:
  db:
    image: postgis/postgis:15-3.3
    container_name: fast_car_fix_db
    restart: always
    environment:
      POSTGRES_DB: fast_car_fix_bot
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: docker
    ports:
      - "5432:5432"

Enable PostGIS (if needed):

CREATE EXTENSION postgis;

## Configuration

Create `application.yml`:

spring:
  application:
    name: fast-car-fix-bot

  datasource:
    url: jdbc:postgresql://localhost:5432/fast_car_fix_bot
    username: postgres
    password: docker

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

telegram:
  bot:
    username: CarFixBot
    token: ${TELEGRAM_BOT_TOKEN}

## Security

Sensitive data must not be stored in the repository.

Use environment variables:

export TELEGRAM_BOT_TOKEN=your_token

## Running the Project Locally

git clone https://github.com/your-username/fast-car-fix-bot.git  
cd fast-car-fix-bot  

Start database:

docker-compose up -d  

Run application:

mvn spring-boot:run  

## REST API

### Get all repair requests

GET /api/requests

### Create a repair request

POST /api/requests

Example request:

{
  "userId": 123,
  "description": "Engine makes strange noise",
  "latitude": 52.52,
  "longitude": 13.40
}

## Project Structure

bot/ - Telegram bot logic  
workflow/ - state machine (conversation flow)  
service/ - business logic layer  
repository/ - database access layer  
entity/ - JPA entities  
dto/ - data transfer objects  
controller/ - REST API layer  
exception/ - global exception handling  

## Architecture Notes

- State Machine controls user interaction flow
- Geolocation search uses PostGIS (ST_DWithin)
- Distance calculated using Haversine formula
- Only one active request per user

## Vadim Danilcheko

Junior backend project built with Spring Boot and Telegram Bots API
