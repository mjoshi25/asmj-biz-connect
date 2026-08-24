# ASMJ Biz Connect

> Enterprise Social Networking, Moderated B2B Marketplace & Automated Resume Engine.

ASMJ Biz Connect is a full-stack platform built on **Spring Boot 3**, **MongoDB**, **Thymeleaf**, and **Tailwind CSS**. It combines real-time messaging, a moderated commercial ad exchange, and automated resume PDF generation.

---

## 🌟 Key Features

* **Real-time B2B & Community Messaging:**
  * 1-on-1 and multi-user group chat channels.
  * Direct file attachments and image previews.
  * Live updates via Spring WebSocket and STOMP protocols.
* **Moderated Ad Marketplace:**
  * Self-service advertiser campaign submissions.
  * Admin review queue with one-click approval and rejection workflows.
  * Direct advertiser-to-buyer message routing.
* **Automated Excel-to-PDF Resume Builder:**
  * Multi-sheet Excel workbook parsing with Apache POI.
  * Passport photo integration.
  * PDF generation using Flying Saucer (HTML-to-PDF) across 4 styles (Modern, Classic, Minimal, Compact).
* **Role-Based Access Control:**
  * Spring Security session management with `ROLE_USER` and `ROLE_ADMIN`.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 17+, Spring Boot 3, Spring Security, Spring WebSocket |
| **Database** | MongoDB |
| **Frontend** | Thymeleaf, Tailwind CSS, HTMX, SockJS, StompJS |
| **Document Processing** | Apache POI, Flying Saucer PDF / OpenPDF |
| **Deployment** | Docker, Render / Railway, Custom DNS (GoDaddy) |

---

## 🚀 Quick Start

### 1. Prerequisites
* JDK 17 or later
* Apache Maven 3.8+
* Running MongoDB instance (Local or MongoDB Atlas)

### 2. Configure Environment
Set the following properties in `src/main/resources/application.properties` or as environment variables:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/asmj_biz_connect
server.port=8080