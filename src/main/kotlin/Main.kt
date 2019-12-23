import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

data class User(val login: String, val password: String, val confirmPassword: String = password)

class LoginPage(val driver: WebDriver) {
    val loginForm = By.id("id_l.L.login")
    val passwordForm = By.id("id_l.L.password")
    val loginButton = By.id("id_l.L.loginButton")
    val wait = WebDriverWait(driver, 10)

    fun logIn(login: String, password: String): DashboardPage {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(loginForm))
        driver.findElement(loginForm).sendKeys(login)
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(passwordForm))
        driver.findElement(passwordForm).sendKeys(password)
        wait.until(ExpectedConditions.elementToBeClickable(loginButton))
        driver.findElement(loginButton).click()
        return DashboardPage(driver)
    }
}

class DashboardPage(val driver: WebDriver) {
    val cog = By.xpath("//*[@class=\"ring-menu__right\"]/a[2]")
    val userButton = By.xpath("//*[@class=\"ring-menu__right\"]/a[1]")
    val wait = WebDriverWait(driver, 10)

    fun goToUsersPage(): UsersPage {
        wait.until(ExpectedConditions.elementToBeClickable(cog))
        driver.findElement(cog).click()
        val usersLink = By.xpath("//*[@class=\"ring-dropdown__i\"]/a[2]")
        wait.until(ExpectedConditions.elementToBeClickable(usersLink))
        driver.findElement(usersLink).click()
        return UsersPage(driver)
    }

    fun logOut(): LoginPage {
        wait.until(ExpectedConditions.elementToBeClickable(userButton))
        driver.findElement(userButton).click()
        val logOutLink = By.xpath("//*[@class=\"ring-dropdown__i\"]/a[2]")
        wait.until(ExpectedConditions.elementToBeClickable(logOutLink))
        driver.findElement(logOutLink).click()
        return LoginPage(driver)
    }
}

class UsersPage(val driver: WebDriver) {
    val createButton = By.id("id_l.U.createNewUser")
    val wait = WebDriverWait(driver, 10)

    fun getUsers(): List<String> {
        val users = By.xpath("//*[@class=\"table users-table\"]/tbody/tr/td[1]/a")
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(users))
        return driver.findElements(users).map { it.text }
    }

    fun deleteUser(login: String) {
        driver.get(driver.currentUrl)
        val userNames = By.xpath("//*[@class=\"table users-table\"]/tbody/tr/td[1]/a")
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(userNames))
        val users = driver.findElements(userNames)
        for (u in users) {
            wait.until(ExpectedConditions.visibilityOf(u))
            if (u.text == login) {
                val userDeleteLink = u.findElement(By.xpath(".//../../td[6]/a[starts-with(@id, 'id_l.U.usersList.deleteUser')]"))
                userDeleteLink.click()
                wait.until(ExpectedConditions.alertIsPresent())
                driver.switchTo().alert().accept()
                break
            }
        }
    }

    fun deleteUsers(users: List<User>) {
        users.forEach { deleteUser(it.login) }
    }

    fun deleteUsersLogin(users: List<String>) {
        users.forEach { deleteUser(it) }
    }

    fun createUser(user: User): String {
        wait.until(ExpectedConditions.elementToBeClickable(createButton))
        driver.findElement(createButton).click()
        return CreateUserForm(driver).createUser(user)
    }

    fun logOut() {
        DashboardPage(driver).logOut()
    }
}

class UserPage(val driver: WebDriver) {
    val usersLink = By.xpath("//*[@id=\"id_l.E.AdminBreadcrumb.AdminBreadcrumb\"]/ul/li[1]/a")
    val wait = WebDriverWait(driver, 10)

    fun goToUsersPage(): UsersPage {
        wait.until(ExpectedConditions.elementToBeClickable(usersLink))
        driver.findElement(usersLink).click()
        return UsersPage(driver)
    }
}

class CreateUserForm(val driver: WebDriver) {
    val loginForm = By.id("id_l.U.cr.login")
    val passwordForm = By.id("id_l.U.cr.password")
    val confirmPasswordForm = By.id("id_l.U.cr.confirmPassword")
    val confirmButton = By.id("id_l.U.cr.createUserOk")
    val cancellButton = By.id("id_l.U.cr.createUserCancel")
    val error = By.xpath("//*[@class=\"errorSeverity\"]")
    val wait = WebDriverWait(driver, 10)


    fun createUser(user: User): String {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(loginForm))
        driver.findElement(loginForm).sendKeys(user.login)
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(passwordForm))
        driver.findElement(passwordForm).sendKeys(user.password)
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(confirmPasswordForm))
        driver.findElement(confirmPasswordForm).sendKeys(user.confirmPassword)
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton))
        driver.findElement(confirmButton).click()
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(error),
            ExpectedConditions.urlContains("editUser"),
            ExpectedConditions.elementToBeClickable(By.className("error-bulb2"))
        ))
        return try {
            val ret = driver.findElement(error).text
            val close = By.xpath("//*[@class=\"close\"]")
            wait.until(ExpectedConditions.elementToBeClickable(close))
            driver.findElement(close).click()
            wait.until(ExpectedConditions.elementToBeClickable(cancellButton))
            driver.findElement(cancellButton).click()
            ret
        } catch (e: NoSuchElementException) {
            try {
                val elem = driver.findElement(By.className("error-bulb2"))
                Actions(driver).moveToElement(elem).build().perform()
                val ret = driver.findElement(By.className("error-tooltip")).text
                wait.until(ExpectedConditions.elementToBeClickable(cancellButton))
                driver.findElement(cancellButton).click()
                ret
            } catch (e: NoSuchElementException) {
                UserPage(driver).goToUsersPage()
                ""
            }
        }
    }
}


fun createDriver(): WebDriver {
    System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver")
    val driver = FirefoxDriver()
    driver.get("http://localhost:8080")
    return driver
}

fun main() {
    val page = LoginPage(createDriver()).logIn("root", "1111").goToUsersPage()
    page.createUser(User("asdf", "asdge"))
    page.createUser(User("alsdga", "asgeag"))
    page.createUser(User("geaaa", "aage"))
    page.createUser(User("あなたはどれくらい学びましたか", "aage"))
    page.deleteUsers(listOf(User("asdf", "asdge"), User("alsdga", "asgeag"), User("geaaa", "aage"), User("あなたはどれくらい学びましたか", "aage")))
    page.logOut()
}