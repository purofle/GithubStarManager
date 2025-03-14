package tech.archlinux.githubStarManager.sql

import com.pgvector.PGvector
import java.sql.Connection
import java.sql.DriverManager

object ConnManager {
    private val conn: Connection =
        DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "purofle", "114514")

    init {
        val setupStmt = conn.createStatement()
        setupStmt.execute("CREATE EXTENSION IF NOT EXISTS vector")

        PGvector.addVectorType(conn)


    }

    fun createTable(vectorSize: Int) {
        val stmt = conn.createStatement()
        stmt.execute("CREATE TABLE IF NOT EXISTS repo (id SERIAL PRIMARY KEY, repo text not null unique, embedding vector($vectorSize))")
    }

    fun dropTable() {
        val stmt = conn.createStatement()
        stmt.execute("DROP TABLE IF EXISTS repo")
    }

    fun insertRepo(repo: String, embedding: List<Float>): Int {
        val stmt = conn.prepareStatement("INSERT INTO repo (repo, embedding) VALUES (?, ?) RETURNING id")
        stmt.setString(1, repo)
        stmt.setObject(2, PGvector(embedding))
        val rs = stmt.executeQuery()

        return if (rs.next()) {
            val generatedId = rs.getInt(1)
            generatedId
        } else {
            0
        }
    }

    fun searchRepo(embedding: List<Float>) {
        val stmt = conn.prepareStatement("SELECT repo FROM repo WHERE embedding <=> ? < 0.8 ORDER BY embedding <-> ?")
        stmt.setObject(1, PGvector(embedding))
        stmt.setObject(2, PGvector(embedding))
        val rs = stmt.executeQuery()

        while (rs.next()) {
            println("Repo: ${rs.getString(1)}")
        }
    }

    fun close() {
        conn.close()
    }
}