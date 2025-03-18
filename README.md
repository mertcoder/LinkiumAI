# LinkiumAI
LinkiumAI is an open-source AI-powered LinkedIn post automation tool.

The tool fetches a list of popular products from Product Hunt, selects a random product, or asks the user to choose a product. 

It then sends a prompt to the Gemini API and performs automatic sharing via the LinkedIn API, achieving full automation.

# Example Gemini Output
![image](https://github.com/user-attachments/assets/86eeebc7-800c-4b40-95d3-ce8927ae4244)

# Örnek Gemini Çıktısı
![image](https://github.com/user-attachments/assets/86eeebc7-800c-4b40-95d3-ce8927ae4244)
``` 
Gemini API Response
Ever wish you could understand your website visitors' behavior *without* drowning in complex analytics dashboards?
Crawl AI might be the answer. This tool uses AI to analyze user sessions, providing clear, actionable insights into how people interact with your site.
Think heatmaps, session recordings, and conversion funnels, all automatically segmented by user behavior patterns.
It's like having a virtual user researcher constantly observing and interpreting your site traffic.
What's particularly interesting is its ability to pinpoint friction points and suggest improvements, potentially boosting conversions and user engagement.
Definitely worth checking out for anyone focused on optimizing their web experience.
#weboptimization #AI #userexperience #analytics
``` 

## Usage
You can use the project by directly cloning it to your computer and running it through the IDE console.

![image](https://github.com/user-attachments/assets/eaa02781-7b61-4e77-8cb7-061e352d3b57)

## Setting Up Credentials
From the menu that appears when the program starts, select option 3, and choose "Set your API's and Credentials."  
Enter your Gemini API Key (completely free, so it's preferred), LinkedIn Client-ID, and LinkedIn Primary Client Secret, and you're all set.  
You can refer to simple tutorials on YouTube to get your LinkedIn Client credentials; it's an easy process.

## Technologies Used
- **Retrofit** for API management.
- **Kotlin Coroutines** for asynchronous tasks.
- **Ktor** for port operations (used by LinkedIn to create the auth token).
- **OkHTTP** for HTTP requests.
- **Jsoup** for HTML parsing.
