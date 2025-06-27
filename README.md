# Got Stock — Your Personal Stock Portfolio Assistant
Got Stock is your intelligent companion for monitoring your stock investments. This application helps you track your portfolio, stay updated with price changes, set alerts, and manage your holdings — all from one place.

## What Does It Do?
 - Upload and manage your stock holdings
 - Set alerts for stock prices (e.g., "Alert me when TCS > ₹4000")
 - Track your portfolio performance
 - Calculate gain/loss for each stock and the overall portfolio
 - Secure login and registration

## Technologies Used
| Backend          | Database | Security          |
| ---------------- | -------- | ----------------- |
| Java Spring Boot | MySQL    | JWT (Token-Based) |

 ## How It Works
    A --> A[User logs in / signs up] --> B[Frontend]
    B --> C[API Call to Spring Boot (AuthController)]
    C --> D[Validate with Database (MySQL)]
    D --> E[Return JWT Token to Frontend]
    E --> F[User is Authenticated]
    
    G[User uploads stock CSV / enters data] --> B
    B --> H[API Call to PortfolioController]
    H --> I[Spring Boot processes & saves to MySQL]
    
    J[User sets alert] --> B
    B --> K[API Call to AlertController]
    K --> L[Save alert in DB, background jobs run checks]
    L --> M[User notified if condition met]


## Features & API Routes
| Feature              | API Endpoint             | Method | Description                      |
| -------------------- | ------------------------ | ------ | -------------------------------- |
| Register             | `/auth/register`         | POST   | Creates new user                 |
| Login                | `/auth/login`            | POST   | Authenticates user               |
| Upload Portfolio     | `/portfolio/upload`      | POST   | Upload CSV with stock details    |
| Add Holding Manually | `/portfolio/add`         | POST   | Add a single stock manually      |
| View Portfolio       | `/portfolio/user/{id}`   | GET    | View all holdings of a user      |
| Set Alert            | `/alert/add`             | POST   | Create a price-based stock alert |
| Get All Alerts       | `/alert/user/{id}`       | GET    | View alerts for the user         |
| Delete Holding/Alert | `/portfolio/delete/{id}` | DELETE | Delete specific holding          |









