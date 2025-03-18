import client.AuthRetrofitClient
import client.GeminiRetrofitClient
import client.GetProfileRetrofitClient
import client.ShareRetrofitClient
import com.google.gson.Gson
import data.UserCredentials
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import request.Content
import request.GeminiRequest
import request.Part
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.random.Random
import kotlin.system.exitProcess

const val URL = "https://www.producthunt.com"
const val REDIRECT_URL = "http://localhost:8080/callback"
const val SCOPE = "w_member_social%20profile%20email%20openid"

fun main(args: Array<String>) {


    val projectDir = System.getProperty("user.dir")
    val directory = File("$projectDir/src/main/kotlin/credentials")
    while(true){

        println("----- Linkium AI -----")
        println("Advanced AI automation tool to create LinkedIn Posts via AI and API Request.")
        println("----------------------------")
        println("1) Create Product Hunter based GeminiAI Post to LinkedIn. (not for share, test only)")
        println("2) Create and Share Product Hunter based GeminiAI Post to LinkedIn.")
        println("3) Set Your APIs and Credentials.")
        println("4) Show Credentials.")
        println("5) Github link.")
        print("Select an option: ")
        val option = readln()
        when(option){
            "1"->{
                println("Scraping products from Product Hunt...")
                val productList = scrapeProductList()
                println("Scraped succesfully. Listing...")
                listProductList(productList)
                val requestedProductIndex = selectProductToCreatePost(productList)
                val userCredentials = checkUserCredentials(directory)
                CoroutineScope(Dispatchers.IO).launch{
                    val geminiContent = async (Dispatchers.IO) {
                        sendGeminiAPIRequest(productList, requestedProductIndex,userCredentials.geminiApiString)
                    }.await()
                }
                Thread.sleep(7000)

            }
            "2"->{
                val job = Job()
                val scope = CoroutineScope(Dispatchers.Default + job)
                val userCredentials = checkUserCredentials(directory)
                println("Scraping products from Product Hunt...")
                val productList = scrapeProductList()
                println("Scraped succesfully. Listing...")
                listProductList(productList)
                val requestedProductIndex = selectProductToCreatePost(productList)

                println("Sending request...")
                scope.launch {
                    try {
                        val geminiContent = async (Dispatchers.IO) {
                            sendGeminiAPIRequest(productList, requestedProductIndex,userCredentials.geminiApiString)
                        }.await()

                        withContext(Dispatchers.IO) {

                            println("Would you like to post this content to LinkedIn? (yes/no)")
                            val shouldPost = readLine()

                            if (shouldPost?.lowercase() == "yes") {


                                val AUTH_URL = "https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=${userCredentials.linkedinClientID}&redirect_uri=$REDIRECT_URL&scope=$SCOPE"

                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().browse(URI(AUTH_URL)) // Tarayıcıda OAuth sayfasını aç
                                } else {
                                    println("Also, you can manually open it by pasting your browser::")
                                    println(AUTH_URL)
                                }

                                // Start CallBack server
                                val server = embeddedServer(Netty, port = 8080) {
                                        routing {
                                        get("/callback") {
                                            val code = call.request.queryParameters["code"]
                                            if (code != null) {
                                                println("✅ OAuth Authorization Code: $code")
                                                call.respondText(" You can close the window, you are successfully logged in. Your Code: $code", ContentType.Text.Plain)


                                                val authCode = code


                                                // get linkedIn token
                                                println("Getting LinkedIn access token...")
                                                val accessToken = withContext(Dispatchers.IO) {
                                                    getLinkedInAccessToken(authCode, userCredentials.linkedinClientID, userCredentials.linkedinSecretCode)
                                                }

                                                if (accessToken != null) {
                                                    println("Successfully obtained LinkedIn access token.")
                                                    println("Access Token is: $accessToken")
                                                    println("Sharing content...")
                                                    if (geminiContent != null) {

                                                        //there is no userUrn saved. Get it and save it.
                                                        if(userCredentials.userUrn==null){
                                                            val response = GetProfileRetrofitClient.instance.getUserProfile(
                                                                authToken = "Bearer $accessToken"
                                                            )

                                                            if(response.isSuccessful){
                                                                val userURN = response.body()?.id
                                                                val copiedCredentials = userCredentials.copy()
                                                                copiedCredentials.userUrn = userURN
                                                                saveUserCredentials(copiedCredentials,directory)
                                                                withContext(Dispatchers.IO) {
                                                                    Thread.sleep(5000)
                                                                }
                                                                shareLinkedInTextPost(accessToken,geminiContent,"urn:li:person:${userURN}")

                                                            }else{
                                                                println("Error occured while getting User Urn ${response.errorBody()}")
                                                            }
                                                        }else{
                                                            shareLinkedInTextPost(accessToken,geminiContent,"urn:li:person:${userCredentials.userUrn}")
                                                        }


                                                    }else{
                                                        println("Gemini content can't be found.")
                                                    }

                                                } else {
                                                    println("Failed to obtain LinkedIn access token.")
                                                }
                                            } else {
                                                call.respondText("There is no code!", ContentType.Text.Plain)
                                            }
                                        }
                                    }
                                }.start(wait = true)
                                server.stop(1000, 5000)
                            } else {
                                println("Content will not be posted to LinkedIn.")
                            }

                            // job complete is important
                            job.complete()
                        }
                    }catch (e: Exception){
                        println("An error occurred: ${e.message}")
                        e.printStackTrace()
                        job.complete()
                    }

                }
                runBlocking {
                    job.join()
                }

                println("Process completed.")
            }
            "3"->{
                print("Enter your Gemini API Key: ")
                val geminiApiString = readLine().toString()
                print("Enter your LinkedIn Client-ID: ")
                val linkedinClientID = readLine().toString()
                print("Enter your LinkedIn Primary Client Secret: ")
                val linkedinSecretCode = readLine().toString()

                val userCredentials = UserCredentials(geminiApiString,linkedinClientID,linkedinSecretCode, userUrn = null)

                saveUserCredentials(userCredentials,directory)
            }
            "4"->{
                val userCredentials = getUserCredentials(directory)
                if(userCredentials==null){
                    checkUserCredentials(directory)
                }else{
                    println("Your Gemini API: " + userCredentials.geminiApiString)
                    println("Your LinkedIn Client-ID: " + userCredentials.linkedinClientID)
                    println("Your LinkedIn Secret Code: " + userCredentials.linkedinSecretCode)

                }
            }
        }

    }

}

fun checkUserCredentials(directory: File): UserCredentials {
    val credentialsFile = File(directory, "user_credentials.json")

    if (!credentialsFile.exists() || credentialsFile.readText().isBlank()) {
        println("Your credentials are empty. Please set them to continue.")

        print("Enter your Gemini API Key: ")
        val geminiApiString = readLine()?.takeIf { it.isNotBlank() } ?: return errorExit("Gemini API Key is required!")

        print("Enter your LinkedIn Client-ID: ")
        val linkedinClientID = readLine()?.takeIf { it.isNotBlank() } ?: return errorExit("LinkedIn Client-ID is required!")

        print("Enter your LinkedIn Primary Client Secret: ")
        val linkedinSecretCode = readLine()?.takeIf { it.isNotBlank() } ?: return errorExit("LinkedIn Secret Code is required!")

        val userCredentials = UserCredentials(geminiApiString, linkedinClientID, linkedinSecretCode, userUrn = null)
        saveUserCredentials(userCredentials, directory)

        return userCredentials
    }

    return getUserCredentials(directory) ?: errorExit("Failed to load credentials!")
}

fun errorExit(message: String): Nothing {
    println(message)
    exitProcess(1)
}

fun saveUserCredentials(credentials: UserCredentials, directory: File){
    val gson = Gson()
    val json = gson.toJson(credentials)
    File(directory,"user_credentials.json").writeText(json)
}
fun getUserCredentials(directory: File): UserCredentials?{
    val gson = Gson()
    val json = File(directory,"user_credentials.json").readText()
    return if(json.isNotEmpty()) gson.fromJson(json, UserCredentials::class.java) else null
}

suspend fun shareLinkedInTextPost(
    accessToken: String,
    content: String,
    personUrn: String
): String? {
    try {
        // create text via gemini
        val shareContent = ShareContent(
            shareCommentary = ShareCommentary(text = content)
        )

        // create post request
        val postRequest = UgcPostRequest(
            author = personUrn,
            specificContent = SpecificContent(shareContent = shareContent),
            visibility = Visibility()
        )

        // send api request
        val response = ShareRetrofitClient.instance.createPost(
            authToken = "Bearer $accessToken",
            shareContent = postRequest
        )

        if (response.isSuccessful) {
            val postId = response.body()?.id
            println("Post succesfully shared! Post ID: $postId")
            return postId
        } else {
            println("Post error happened. HTTP kodu: ${response.code()}")
            println("Error details: ${response.errorBody()?.string()}")
            return null
        }
    } catch (e: Exception) {
        println("Error occured while posting: ${e.message}")
        e.printStackTrace()
        return null
    }
}


suspend fun getLinkedInAccessToken(authCode: String, clientId: String, clientSecret: String) : String?{
    try{
            val response = AuthRetrofitClient.instance.getAccessToken(code = authCode,
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = "http://localhost:8080/callback"
            )
            if(response.isSuccessful){
                println("Access token created sucessfully.")
                return response.body()?.accessToken
            }else{
                println("Token creation failed. HTTP error code: ${response.code()}")
                println("Error body: ${response.errorBody()?.string()}")
                return null
            }
    }catch (e: Exception){
        println("Error while trying to get Token: ${e.localizedMessage}")
        return null
    }
}


fun selectProductToCreatePost(productList: ArrayList<String>): Int {
    var selection: String?

    while (true) {
        println("1) Select random product to post.")
        println("2) Select product from list.")
        selection = readLine()

        if (selection == "1" || selection == "2") break
        println("Please choose a valid option.")
    }

    if (selection == "1") {
        if (productList.isNotEmpty()) {
            val randomProductIndex = Random.nextInt(productList.size)
            val randomProduct = productList[randomProductIndex]
            println("Selected random product: $randomProduct")
            return randomProductIndex
        } else {
            println("Empty product list, if you used this function too much please use VPN!")

            return -1
        }
    } else {
        println("Choose an item from list...")
        productList.forEachIndexed { index, product ->
            println("${index + 1}) $product")
        }
        val chosenElement = readLine()?.toIntOrNull()
        val chosenIndex = chosenElement?.minus(1)
        if (chosenElement != null && chosenElement in 1..productList.size) {
            println("Selected product: ${productList[chosenIndex!!]}")
            return  chosenIndex
        } else {
            println("Invalid option!")
            return -1
        }

    }
}

fun listProductList(productList: ArrayList<String>) {
    println("----- Product List ----")
    for(product in productList){
        println(product)
    }
    println("Summary: ${productList.count()} product found.")
    println("Press Enter to contiuniue.")

}

fun scrapeProductList() : ArrayList<String>{
    val website = Jsoup.connect(URL).userAgent("Mozilla/5.0")
        .timeout(5000)
        .get()
    var productsList = ArrayList<String>();
    val elements = website.select("a[data-test^=post-name-]")
    for(element in elements){
        val fullText = element.text();
        val postName = fullText.substringAfter(". ").trim()
        productsList.add(postName)
    }
    return productsList
}

suspend fun sendGeminiAPIRequest(productList: ArrayList<String>, requestedProductIndex: Int, GEMINI_API: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = "Write only 100-150 words length to explain that technology from producthunt for a linkedin post. Write it like a professional blogger to inform your connections. But don't start with like my fellow enthausants ex. be more friendly but still be professional. Don't say that is from Product Hunt, talk like you just see this. You can add hastags or not, you choose it randomly: ${productList[requestedProductIndex]}")
                        )
                    )
                )
            )

            val response = GeminiRetrofitClient.instance.generateContent(GEMINI_API, request)

            if (response.isSuccessful) {
                val result = response.body()
                val text = result?.candidates?.get(0)?.content?.parts?.get(0)?.text
                println("Gemini API Response: $text")
                return@withContext text
            } else {
                println("API Error: ${response.code()}")
                return@withContext null
            }
        } catch (e: Exception) {
            println("Connection Error: ${e.message}")
            return@withContext null
        }
    }
}