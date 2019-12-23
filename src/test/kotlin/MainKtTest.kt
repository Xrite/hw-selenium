import org.junit.*

import org.junit.Assert.*
import org.openqa.selenium.WebDriver

class MainKtTest {
    lateinit var driver: WebDriver
    @Before
    //@JvmStatic
    fun initDriver() {
        driver = createDriver()
    }
    @After
    //@JvmStatic
    fun end() {
        driver.close()
    }

    fun logIn(): UsersPage = LoginPage(driver).logIn("root", "1111").goToUsersPage()

    @Test
    fun testSpecialCharactersInName1() {
        val names1 = listOf("!", "@", "#", "$", "%", "^", "&")
        val page = logIn()
        for (name in names1) {
            assertEquals("", page.createUser(User(name, "***")))
        }
        assertEquals((listOf("root", "guest") + names1).toSet(), page.getUsers().toSet())
        page.deleteUsersLogin(names1)
        page.logOut()
    }

    @Test
    fun testSpecialCharactersInName2() {
        val names2 = listOf("*", "(", "{", "[", "\"", ";", ":", "\\")
        val page = logIn()
        for (name in names2) {
            assertEquals("", page.createUser(User(name, "***")))
        }
        assertEquals((listOf("root", "guest") + names2).toSet(), page.getUsers().toSet())
        page.deleteUsersLogin(names2)
        page.logOut()
    }

    @Test
    fun testNumbersInLogin() {
        val page = logIn()
        assertEquals("", page.createUser(User("228", "***")))
        assertEquals(setOf("228", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("228"))
        page.logOut()
    }

    @Test
    fun testMultipleLettersLogin() {
        val page = logIn()
        assertEquals("", page.createUser(User("abc", "***")))
        assertEquals(setOf("abc", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("abc"))
        page.logOut()
    }

    @Test
    fun testCreateGuest() {
        val page = logIn()
        assertEquals("Removing null is prohibited", page.createUser(User("guest", "***")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testCreateRoot() {
        val page = logIn()
        assertEquals("Removing null is prohibited", page.createUser(User("root", "***")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testCreateRootWithCorrectPassword() {
        val page = logIn()
        assertEquals("Removing null is prohibited", page.createUser(User("root", "1111")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testUserCreation() {
        val page = logIn()
        assertEquals("", page.createUser(User("a", "***")))
        assertEquals(setOf("a", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("a"))
        page.logOut()
    }

    @Test
    fun testUnicodeNames() {
        val names = listOf("あなたはどれくらい学びましたか", "афвып")
        val page = logIn()
        for (name in names) {
            assertEquals("", page.createUser(User(name, "***")))
        }
        page.deleteUsersLogin(names)
        page.logOut()
    }

    @Test
    fun testDuplicates() {
        val names = listOf("a", "b", "c", "жаба")
        val page = logIn()
        assertEquals("", page.createUser(User("a", "***")))
        assertEquals("Value should be unique: login", page.createUser(User("a", "***")))
        assertEquals("", page.createUser(User("b", "***")))
        assertEquals("", page.createUser(User("c", "***")))
        assertEquals("Value should be unique: login", page.createUser(User("b", "***")))
        assertEquals("", page.createUser(User("жаба", "***")))
        assertEquals("Value should be unique: login", page.createUser(User("жаба", "***")))
        assertEquals((names + listOf("guest", "root")).toSet(), page.getUsers().toSet())
        page.deleteUsersLogin(names)
        page.logOut()
    }


    @Test
    fun testCorrectUserAdding() {
        val page = logIn()
        assertEquals("", page.createUser(User("a", "***")))
        assertEquals(setOf("a", "guest", "root"), page.getUsers().toSet())
        assertEquals("", page.createUser(User("b", "***")))
        assertEquals(setOf("a", "b", "guest", "root"), page.getUsers().toSet())
        assertEquals("", page.createUser(User("c", "***")))
        assertEquals(setOf("a", "b", "c", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("a", "b", "c"))
        page.logOut()
    }

    @Test
    fun testWhitespacesInLogin() {
        val page = logIn()
        assertEquals("Restricted character ' ' in the name", page.createUser(User("a a", "***")))
        assertEquals("Restricted character ' ' in the name", page.createUser(User(" a", "***")))
        assertEquals("Restricted character ' ' in the name", page.createUser(User("a ", "***")))
        assertEquals("", page.createUser(User("a\u2007a", "***"))) //Вот это youtrack принял (должен ли?)
        assertEquals("", page.createUser(User("a\u202Fa", "***"))) //Вот это youtrack принял (должен ли?)
        assertEquals("", page.createUser(User("a\u2060a", "***"))) //Вот это youtrack принял (должен ли?)
        assertEquals(setOf("a\u2007a", "a\u202Fa", "a\u2060a", "guest", "root"), page.getUsers().toSet())
        page.deleteUsersLogin(listOf("a\u2007a", "a\u202Fa", "a\u2060a"))
        page.logOut()
    }

    @Test
    fun testDots() {
        val page = logIn()
        assertEquals("Can't use \"..\", \".\" for login: login", page.createUser(User(".", "***")))
        assertEquals("Can't use \"..\", \".\" for login: login", page.createUser(User("..", "***")))
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testForbidden() {
        val page = logIn()
        val names = listOf("/", "<", ">", "a/", "a>", "a<")
        for (name in names) {
            assertEquals("login shouldn't contain characters \"<\", \"/\", \">\": login", page.createUser(User(name, "***")))
        }
        assertEquals(setOf("guest", "root"), page.getUsers().toSet())
        page.logOut()
    }

    @Test
    fun testTripleDots() {
        val page = logIn()
        assertEquals("", page.createUser(User("...", "asdge")))
        assertEquals(setOf("...", "guest", "root"), page.getUsers().toSet())
        page.deleteUser("...")
        page.logOut()
    }

    @Test
    fun testEmptyLogin() {
        val page = logIn()
        assertEquals("Login is required!", page.createUser(User("", "asdge")))
        page.logOut()
    }

    @Test
    fun testEmptyWhitespaceLogin() {
        val page = logIn()
        assertEquals("Login is required!", page.createUser(User(" ", "asdge")))
        page.logOut()
    }

    @Test
    fun testEmptyTabLogin() {
        val page = logIn()
        assertEquals("Login is required!", page.createUser(User("\t", "asdge")))
        page.logOut()
    }

    @Test
    fun testEmptyPassword() {
        val page = logIn()
        assertEquals("Password is required!", page.createUser(User("aa", "")))
        page.logOut()
    }

    @Test
    fun testPasswordDoesNotMatch() {
        val page = logIn()
        assertEquals("Password doesn't match!", page.createUser(User("aa", "a", "b")))
        page.logOut()
    }

    @Test
    fun testPasswordMatchWithoutWhitespaceBefore() {
        val page = logIn()
        assertEquals("Password doesn't match!", page.createUser(User("aa", "a", " a")))
        page.logOut()
    }

    @Test
    fun testPasswordMatchWithoutWhitespaceAfter() {
        val page = logIn()
        assertEquals("Password doesn't match!", page.createUser(User("aa", "a", "a ")))
        page.logOut()
    }

    @Test
    fun testLongLogin() {
        val page = logIn()
        val longString = "a".repeat(50)
        assertEquals("", page.createUser(User(longString, "***")))
        assertEquals(setOf("guest", "root", longString), page.getUsers().toSet())
        page.deleteUser(longString)
        page.logOut()
    }

    @Test
    fun testLongPassword() {
        val page = logIn()
        val longString = "a".repeat(50)
        assertEquals("", page.createUser(User("a", longString)))
        assertEquals(setOf("guest", "root", "a"), page.getUsers().toSet())
        page.deleteUser("a")
        page.logOut()
    }

    @Test
    fun testLongLoginExceed() {
        val page = logIn()
        val longString = "a".repeat(50)
        assertEquals("", page.createUser(User(longString + "b", "***")))
        assertEquals(setOf("guest", "root", longString), page.getUsers().toSet())
        page.deleteUser(longString)
        page.logOut()
    }

    @Test
    fun testLongPasswordExceed() {
        val page = logIn()
        val longString = "a".repeat(50)
        assertEquals("", page.createUser(User("a", longString + "b")))
        assertEquals(setOf("guest", "root", "a"), page.getUsers().toSet())
        page.deleteUser("a")
        page.logOut()
    }
}