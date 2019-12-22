import org.junit.AfterClass
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.openqa.selenium.WebDriver

class MainKtTest {
    companion object {
        @JvmStatic
        lateinit var driver: WebDriver

        @BeforeClass @JvmStatic
        fun initDriver() {
            driver = createDriver()
        }

        @AfterClass @JvmStatic
        fun end() {
            driver.close()
        }
    }

    fun logIn(): UsersPage = LoginPage(driver).logIn("root", "1111").goToUsersPage()


    @Test
    fun testDuplicates() {
        val page = logIn()
        assertEquals("", page.createUser(User("asdf", "asdge")))
        assertEquals("", page.createUser(User("alsdga", "asgeag")))
        assertEquals("", page.createUser(User("あなたはどれくらい学びましたか", "aage")))
        assertEquals("", page.createUser(User("geaaa", "aage")))
        assertEquals("Value should be unique: login", page.createUser(User("asdf", "asdf")))
        assertEquals("Value should be unique: login", page.createUser(User("あなたはどれくらい学びましたか", "ggg")))
        page.deleteUsersLogin(listOf("asdf", "alsdga", "geaaa", "あなたはどれくらい学びましたか"))
        page.logOut()
    }

    @Test
    fun testMultipleUsers() {
        val page = logIn()
        assertEquals("", page.createUser(User("asdf", "asdge")))
        assertEquals("", page.createUser(User("alsdga", "asgeag")))
        assertEquals("", page.createUser(User("geaaa", "aage")))
        assertEquals(setOf("asdf", "alsdga", "geaaa", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("asdf", "alsdga", "geaaa"))
        page.logOut()
    }

    @Test
    fun testWhitespace() {
        val page = logIn()
        assertEquals("Restricted character ' ' in the name", page.createUser(User("as df", "asdge")))
        assertEquals("Restricted character ' ' in the name", page.createUser(User("alsd ga", "asgeag")))
        assertEquals("", page.createUser(User("alsd\u2007ga", "asgeag"))) //Вот это youtrack принял (должен ли?)
        assertEquals("", page.createUser(User("alsd\u202Fga", "asgeag"))) //Вот это youtrack принял (должен ли?)
        assertEquals("", page.createUser(User("alsd\u2060ga", "asgeag"))) //Вот это youtrack принял (должен ли?)
        assertEquals("Restricted character ' ' in the name", page.createUser(User("   geaaa   ", "aage")))
        assertEquals(setOf("alsd\u2007ga", "alsd\u202Fga", "alsd\u2060ga", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("alsd\u2007ga", "alsd\u202Fga", "alsd\u2060ga"))
        page.logOut()
    }

    @Test
    fun testForbidden() {
        val page = logIn()
        assertEquals("Can't use \"..\", \".\" for login: login", page.createUser(User(".", "asdge")))
        assertEquals("Can't use \"..\", \".\" for login: login", page.createUser(User("..", "asdge")))
        assertEquals("", page.createUser(User("...", "asdge")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsd/ga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("/alsdga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsdga/", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsd<ga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("<alsdga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsdga<", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsd>ga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User(">alsdga", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("alsdga>", "asgeag")))
        assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User("<al/sdga>", "asgeag")))
        assertEquals(setOf("...", "guest", "root"), page.getUsers().toSet())
        page.deleteUser("...")
        page.logOut()
    }

    @Test
    fun testEmpty() {
        val page = logIn()
        assertEquals("Empty", page.createUser(User("", "asdge")))
        assertEquals("Empty", page.createUser(User(" ", "asdge")))
        assertEquals("Empty", page.createUser(User("\t", "asdge")))
        assertEquals("Empty", page.createUser(User("aa", "")))
        assertEquals("Empty", page.createUser(User("aa", "aa", "")))
        assertEquals("Empty", page.createUser(User("aa", "", "asdf")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun differentPasswords () {
        val page = logIn()
        assertEquals("Empty", page.createUser(User("af", "asdge", "asdfe")))
        assertEquals("Empty", page.createUser(User("a", "asdge", "af")))
        assertEquals("Empty", page.createUser(User("aa", " ss", "ss")))
        assertEquals("Empty", page.createUser(User("aa", "   ", "  ")))
        assertEquals("Empty", page.createUser(User("aa", " ", "\t")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testLong() {
        val page = logIn()
        val longString = "a".repeat(50)
        assertEquals("", page.createUser(User(longString, "a", "a")))
        assertEquals("", page.createUser(User("a", longString,  longString)))
        assertEquals("Value should be unique: login", page.createUser(User(longString + "a", "a", "a")))
        assertEquals("Value should be unique: login", page.createUser(User("a", longString + "a",  longString + "a")))
        assertEquals(setOf("guest", "root", "a", longString), page.getUsers().toSet())
        page.deleteUser("a")
        page.deleteUser(longString)
        page.logOut()
    }
}