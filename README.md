# 📈 Automated Trading Bot

Welcome to the **Automated Trading Bot** project!

---

## 📌 Project Description
This project is an **automated trading bot** that uses **technical analysis** to make buy and sell decisions in the cryptocurrency market. The application is written in **Java** and utilizes the **TA4J** library for analyzing technical indicators. It integrates with the **Binance API** to execute trades sends trade data to a **PostgreSQL** database for storage and further analysis.

---

## ✨ Features
- ✅ Fetching market data from **Binance**
- ✅ Technical analysis using **TA4J indicators**
- ✅ Generating **buy and sell signals**
- ✅ Automatic order execution via **Binance API**
- ✅ Sending email notifications
- ✅ Storing trade data in a **PostgreSQL** database


---

## 📋 Requirements
- ☕ **Java 21**
- 🛠 **Maven**
- 📊 **Binance account** (for market data retrieval)
- 💰 **Google Gmail account** (for sending notifications)
- 🗄 **PostgreSQL** (for trade data storage)

---

## 🚀 Installation
1. **Clone the repository:**
   ```sh
   git clone https://github.com/your-repo/trading-bot.git
   cd trading-bot

2. Install dependencies:

 ```sh
mvn install   
```

3. Configure the config.properties file by providing your Binance and Google OAuth keys.


## ▶️ Running the Bot
Execute the following command:

 ```sh
java -jar target/trading-bot.jar
```

##💡 Usage Examples
Fetch Market Data & Analyze Signals:
The bot retrieves data from Binance and performs technical analysis to determine if a buy or sell signal is generated.

Automatic Trading:
When a signal is generated, the bot places the corresponding order through the XTB API.

Email Notifications:
Trade notifications are sent via email upon executing trades.

## 🔄 Project Status
Development Stage: Actively maintained and under continuous development.
Last Update: [Insert Date]
