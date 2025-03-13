import tech.archlinux.githubStarManager.sql.ConnManager
import kotlin.test.Test

class ConnManagerTest {
    @Test
    fun testConnManager() {
        ConnManager.createTable(3072)
    }

    @Test
    fun testDropTable() {
        ConnManager.dropTable()
    }
}