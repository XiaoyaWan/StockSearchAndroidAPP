# StockSearchAndroidAPP
Developed by Android Studio; Backend: Node.js; Deployed on AWS.

## 1 App Icon and Splash Screen
This image is also the app icon as shown in Figure 1. The app begins with a welcome Splash Screen (Figure 1).

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure1.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure2.png?raw=true)

Figure 1: App Icon Figure 2: Splash Screen

## 2 Home screen
When you open the app, there will be an initial spinner while the data is being fetched using volley as shown in Figure 3. The home screen will have a toolbar at the top with Stocks title and the search icon. Below that, it will show the current date as shown in Figure 3. 

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure3.1.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure3.2.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure3.3.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure3.4.png?raw=true)

Figure 3: Home screen

There are 2 sections on the home screen:

- Portfolio Section - This section will show the total net worth of the user, which is calculated as the sum of number of shares of a stock multiplied by the current price, plus
uninvested cash. This is followed by the list of stocks in the user portfolio with their current price, change in price and total shares owned information.

- Favorites Section - This section will show all the stocks that have been favorited by the user to allow the user to easily check the prices of stocks in their watchlist. The stock symbol, current price, change in price and company name will be displayed as shown in Figure 3. In case the favorited stock is present in the user portfolio, instead of the company name, the stocks owned will be displayed.

Additionally, the symbol next to the change in price value will either be trending down or up based on the change price value. In case the change in price is zero i.e. no change, no symbol will be displayed, and the change price value will be grey.

Each stock listing also has a button on the extreme right, next to the current price field. On clicking
the button or the stock listing, the detailed information screen will open for the selected stock.

The home screen view supports multiple functionalities like:

- The swipe to delete functionality allows the user to remove/delete the stock from the favorite section. On removing a stock from the favorite section, the stock will be
removed from the favorite stocks in the local storage and the view.

- The drag and reorder functionality allows the user to reorder the stocks in either section. The user is able to long press the stock listing and drag it to the new position. The list will be updated accordingly to ensure the new order going forward. Note: The user cannot drag the stock from the favorite section into the portfolio section. The stock can only be dragged and dropped in the same section.
At the bottom of the 2 sections, we have a ‘Powered by tiingo’ text in italic. On clicking this text, the App will open the Tiingo homepage in chrome. (The URL is https://www.tiingo.com/ )

The price information for each stock will be updated every 15 seconds. 
The home screen has been implemented by using a RecyclerView with the SectionedRecyclerViewAdapter. Each of the stock listings has been implemented using ConstraintLayout, TextView, ImageView.

### 2.1 Search Functionality

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure4.png?raw=true)

Figure 4: Search Functionality

- On top right side, there will be a search button which opens a textbox where the user can enter a keyword to search for a stock symbol.

- The user is provided with suggestions of keywords using the Tiingo Autocomplete API.

- When the user taps on a suggestion, it is filled inside the search box and clicking enter/next takes the user to the detailed information screen.

- Before you get the data from your backend server, a progress bar displays on the screen as indicated in the detailed information section.

- The user can only search for valid stock symbols in the search bar. The search will redirect to the detailed information screen only if the user selected one of the autocomplete

- In the Autosuggest, only make an API call after the user enters 3 characters.

## 3 Detailed Stock Information Screen

On clicking the Goto button on any stock listing or searching for a stock symbol, the loading spinner symbol will be displayed while the details are being fetched (see Figure 4). Once the data has been fetched except the chart (since the chart takes longer to load), the spinner will disappear and information regarding the stock will be available to the user (Figure 5 and Figure 6).

The top action bar has the ‘Stock’ title and the back button to go back to the home screen (which has the filter values that were used for the current search if triggered by using the search functionality). The action bar also contains a favorite icon to add or remove the stock from favorites. The favorite icon will either be filled or bordered based on whether the stock is favorited or not. Adding/Removing the stock from favorites will also display a toast message.
Below the action bar, there are 4 fields: stocks symbol, current price with ‘$’ sign, company name and the change price with ‘$’ sign (the text color will be green, red or grey based on the change price value being positive, negative or zero respectively). The App then has a WebView element which is blank till the chart loads. (More details later in this section)

The Portfolio section allows the user to trade the shares of the stock. It contains a left section which shows the market value of the stock in the user portfolio and the number of shares the user owns. The right section contains the trade button. Initially, when the user starts the app for the first time, they will not have any stocks/shares in the portfolio and an initial pre-loaded amount of $20,000 to trade on the app. This amount can change based on the trading done by the user.

If the user does not own any shares of the given stock, the left section will have the message as shown in Figure 7, else the left section will look like Figure 6.

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure5.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure6.png?raw=true)

Figure 5: Loading Screen    Figure 6: Detailed Screen Figure

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure7.png?raw=true)

Figure 7: Portfolio section

The Stats section displays the trading statistics for the given stock in a grid. The grid has 7 fields namely: Current price, Low, Bid price, Open price, Mid, High and Volume. If any of these fields are missing in the JSON, set them as 0.0. The GridView element is to be used for this section.

The About section displays the description of the company. If the description is longer than 2 lines, ellipsize the end of the 2nd line and display a ‘Show more…’ button. On clicking this button, the complete description becomes visible and the button text changes to ‘Show less’.(Figure 8) If the description is less than 2 lines, do not display the button.

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure8.png?raw=true)

Figure 8: About section

The News section displays the news articles related to the given stock symbol. The first article has a different format/layout than the rest of the articles in the list. On clicking the news article, the original article is opened in chrome using the article URL. On long press, a dialog box opens with options to share on twitter and open in chrome. (Figure 9) For each article, the information displayed is Article source, Article title, Article image and the time ago when the article was published. The time ago supports ‘days ago’ and ‘minutes ago’ by calculating the difference
between the timestamp the article was published and the current timestamp.
The news section uses RecyclerView and ArticleDialog elements.

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure9.1.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure9.2.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure9.3.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure9.4.png?raw=true)

Figure 9: News section

From every twitter button, on clicking the button, the article will be shared by opening a
browser with Twitter Intent.

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure10.1.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure10.2.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure10.3.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure10.4.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure10.5.png?raw=true)

Figure 10: Trade Dialog

![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure11.1.png?raw=true)
![alt text](https://github.com/XiaoyaWan/StockSearchAndroidAPP/blob/main/ReadmeImage/Figure11.2.png?raw=true)

Figure 11: Trade Success Message

The Trade button in the Portfolio section opens a new dialog box for trading (Figure 10). The dialog shows an input box which only accepts numeric input. Below the input field, there is a calculation text box which updates based on the numeric input to display the final price of the trade. The trade dialog also displays the current available amount to trade for the user. The user can either buy or sell the shares. Based on the trade, the amount available to trade will be updated accordingly. There are 5 error conditions to be checked before executing the trade and displaying the trade successful dialog (Figure 11). The error conditions are:

- Users try to sell more shares than they own - The trade dialog box will remain open and a toast message with text ‘Not enough shares to sell’ will be displayed.

- User tries to buy more shares than money available - The trade dialog box will remain open and a toast message with text ‘Not enough money to buy’ will be displayed.

- User tries to sell zero or negative shares - The trade dialog box will remain open and a toast message with text ‘Cannot sell less than 0 shares’ will be displayed.

- User tries to buy zero or negative shares - The trade dialog box will remain open and a toast message with text ‘Cannot buy less than 0 shares’ will be displayed.

- User enters invalid input like text or punctuations - The trade dialog box will remain open and a toast message with text ‘Please enter valid amount’ will be displayed.

### 3.1 HighCharts in Android

The Chart section in the detailed stock information screen uses a WebView element to load the HighCharts stock chart. To load the chart, the App will load
a local HTML file with the necessary JavaScript to request the data from the NodeJS server and display the chart when the data is fetched.

## 4 Progress bar

Every time the user has to wait before they can see the data, app will display a progress bar as shown in Figure 4. The progress bar is to be present across the Home screen, Detailed Stock screen and just says “Fetching Data…”.
