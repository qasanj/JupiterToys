package Jupitertoys_pckg;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.*;
import org.openqa.selenium.By;


import java.time.Duration;
import java.util.List;


public class JupiterToysTests {
    private WebDriver driver;

    @BeforeMethod
    public void beforeClass() {
        System.out.println("Before Class");

        WebDriverManager.chromedriver().browserVersion("133.0.6943.127").setup();
        ChromeOptions dc = new ChromeOptions();
        dc.addArguments("--no-sandbox");
        driver = new ChromeDriver(dc);
    }

    @AfterMethod
    public void afterClass() {
        System.out.println("After Class");
        if (driver != null) {
            driver.quit();
        }
    }


    @Test (priority = 0, description = "Verify contact page form validation and submission")
    public void testContactForm() throws InterruptedException {
        driver.get("http://jupiter.cloud.planittesting.com");

        // 1. Navigate to Contact page
        driver.findElement(By.linkText("Contact")).click();
        Thread.sleep(3000);

        // 2. Click Submit button
        driver.findElement(By.xpath("//a[contains(text(),'Submit')]")).click();
        Thread.sleep(3000);
        
        // 3. Verify error messages
        String forenameError = driver.findElement(By.id("forename-err")).getText();
        System.out.println(forenameError);
        String emailError = driver.findElement(By.id("email-err")).getText();
        System.out.println(emailError);
        String messageError = driver.findElement(By.id("message-err")).getText();
        System.out.println(messageError);

        Assert.assertEquals(forenameError, "Forename is required");
        Assert.assertEquals(emailError, "Email is required");
        Assert.assertEquals(messageError, "Message is required");


        // 4. Populate mandatory fields
        driver.findElement(By.id("forename")).sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("message")).sendKeys("Test message");

        //Explicitly wait for the errors to disappear
        WebDriverWait wait = new WebDriverWait(driver , Duration.ofSeconds(30));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("forename-err")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("email-err")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("message-err")));



        // 5. Validate errors are gone  (Asserts modified for dynamic error check)
        Assert.assertFalse(driver.findElement(By.id("forename")).getAttribute("class").contains("ng-invalid"),"Forename error is still present");
        Assert.assertFalse(driver.findElement(By.id("email")).getAttribute("class").contains("ng-invalid"), "Email error is still present");
        Assert.assertFalse(driver.findElement(By.id("message")).getAttribute("class").contains("ng-invalid"), "Message error is still present");

        System.out.println("test Case-1 Completed");

    }

    @Test (priority = 1,description = "Verify contact page form submission and success message", invocationCount = 5)
    public void testContactFormSubmission() throws InterruptedException {
        driver.get("http://jupiter.cloud.planittesting.com");

        // 1. Navigate to Contact page
        driver.findElement(By.linkText("Contact")).click();

        Thread.sleep(3000);
        // 2. Populate mandatory fields
        driver.findElement(By.id("forename")).sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("message")).sendKeys("Test message");

        // 3. Click Submit button
        driver.findElement(By.xpath("//a[contains(text(),'Submit')]")).click();

        // 4. Validate successful submission message
        WebDriverWait wait = new WebDriverWait(driver , Duration.ofSeconds(30));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assert.assertTrue(successMessage.isDisplayed());
        Assert.assertEquals(successMessage.getText(), "Thanks Test User, we appreciate your feedback.");

        System.out.println("test Case-2 Completed");

    }

    @Test (priority = 2,description = "Verify cart calculations")
    public void testCartCalculations() throws InterruptedException {
        driver.get("http://jupiter.cloud.planittesting.com");

        // 1. Navigate to shop page
        driver.findElement(By.linkText("Shop")).click();

        Thread.sleep(3000);
        // 1. Add items to cart
        addItem("Stuffed Frog", 2);
        addItem("Fluffy Bunny", 5);
        addItem("Valentine Bear", 3);

        Thread.sleep(3000);
        // 2. Go to cart page
        driver.findElement(By.xpath("//*[@id=\"nav-cart\"]/a")).click();

        Thread.sleep(2000);
        // 3 & 4. Verify subtotal and price for each product
        List<WebElement> cartItems = driver.findElements(By.cssSelector("tbody tr"));
        double totalCalculated = 0;  // Initialize calculated total

        for (WebElement itemRow : cartItems) {
            String productName = itemRow.findElement(By.cssSelector("td:nth-child(1)")).getText();
            System.out.println(itemRow.findElement(By.cssSelector("td:nth-child(2)")).getText().substring(1));
            double price = Double.parseDouble(itemRow.findElement(By.cssSelector("td:nth-child(2)")).getText().substring(1)); //Extract and parse the price
            int quantity = Integer.parseInt(itemRow.findElement(By.cssSelector("td:nth-child(3) input")).getAttribute("value"));
            System.out.println(itemRow.findElement(By.cssSelector("td:nth-child(4)")).getText().substring(1));
            double subtotal = Double.parseDouble(itemRow.findElement(By.cssSelector("td:nth-child(4)")).getText().substring(1));

            Assert.assertEquals(subtotal, price * quantity, 0.001, "Subtotal is incorrect for " + productName); // Use delta for double comparison
            totalCalculated += subtotal;
            System.out.println("TotalCalculated is: " + totalCalculated);
        }

        Thread.sleep(3000);
        // 5. Verify total
        double cartTotal = Double.parseDouble(driver.findElement(By.cssSelector("tfoot tr:nth-child(1) td:nth-child(1)")).getText().substring(7));
        System.out.println("cartTotal is: "+cartTotal);
        Assert.assertEquals(cartTotal, totalCalculated, 0.001, "Cart total is incorrect");
        System.out.println("test Case-3 Completed");


    }

    private void addItem(String itemName, int quantity) {
        // Find the item by name
        WebElement item = driver.findElement(By.xpath("//h4[text()='" + itemName + "']/ancestor::li"));

        // Find the "Buy" button within the item element
        WebElement buyButton = item.findElement(By.xpath(".//a[contains(text(), 'Buy')]"));

        for (int i = 0; i < quantity; i++) {
            buyButton.click(); // Click the button multiple times based on quantity
        }

    }


}
