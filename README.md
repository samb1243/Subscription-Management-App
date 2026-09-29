# Subscription Manager

A desktop app, written in Java, for keeping track of your subscriptions and what they cost you each month.

- Add each subscription with its name, price, and how often it's billed (weekly, monthly, quarterly, or yearly)
- See the **total cost per month** (and per year) update as you go
- Non-monthly subscriptions are converted to a monthly equivalent (e.g. a £95/year plan counts as £7.92/month)
- Click a subscription to edit it; select it and press **Delete** to remove it
- Your list is saved automatically and is there next time you open the app

## Requirements

- JDK 17 or newer
- Maven 3.6+

## Run it

```bash
mvn package
java -jar target/subscription-manager.jar
```

Or run straight from source with `mvn compile exec:java`.

## Build a standalone app

`package-app.sh` uses `jpackage` (included with the JDK) to build a real desktop app with its own Java runtime
bundled in, so it launches like any other app on your computer. Run it on the OS you want the app for:

```bash
./package-app.sh              # an app folder in dist/ that you can run directly
./package-app.sh installer    # a native installer: .msi (Windows), .dmg (macOS), .deb (Linux)
```

On Windows, run the script from Git Bash. Building a `.msi` also needs the [WiX Toolset](https://wixtoolset.org/)
installed.

## Where your data is stored

Subscriptions are saved to `~/.subscription-manager/subscriptions.csv` in your home folder
(on Windows: `C:\Users\<you>\.subscription-manager\subscriptions.csv`). It's a plain CSV file, so you can back it up
or open it in a spreadsheet.

## Project layout

```
src/main/java/com/subscriptionmanager/
├── App.java                      Entry point
├── model/
│   ├── BillingCycle.java         Weekly / Monthly / Quarterly / Yearly, and conversion to a monthly cost
│   ├── Subscription.java         A single subscription
│   └── SubscriptionManager.java  The list of subscriptions and the monthly/yearly totals
├── storage/
│   └── SubscriptionStore.java    Saves and loads the CSV file
└── ui/
    ├── MainWindow.java           The app window (Swing)
    ├── SubscriptionTableModel.java
    └── MoneyFormat.java          Currency display and price input parsing
```

Run the tests with `mvn test`.
