package com.beust.koolaid

import com.google.inject.Inject
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager

class ViewsDaoPostgres @Inject constructor(localProperties: LocalProperties) : ViewsDao {
    private val conn: Connection
    init {
        Class.forName("org.postgresql.Driver")

        // postgres://{user}:{password}@{hostname}:{port}/{database-name}
        val envDbUrl = System.getenv("DATABASE_URL")
        val (dbUrl, username, password) =
            if (envDbUrl != null) {
                // Heroku, extract username and password from DATABASE_URL
                URI(envDbUrl).let { dbUri ->
                    dbUri.userInfo.split(":").let { split ->
                        val username = split[0]
                        val password = split[1]
                        Triple(envDbUrl, username, password)
                    }
                }
            } else {
                // Local
                val envUsername = localProperties.get(LocalProperty.DATABASE_USER)
                val envPassword = localProperties.get(LocalProperty.DATABASE_PASSWORD)
                val database = localProperties.get(LocalProperty.DATABASE)
                val dbUrl = "jdbc:$database:demo"
                Triple(dbUrl, envUsername, envPassword)
            }

        println("Connecting to dbUrl: $dbUrl, user: $username, password: $password")
        conn = DriverManager.getConnection(dbUrl, username, password)
    }

    override fun getViewCountAndIncrement(): Int {
        val result = count
        count = result + 1
        return result
    }


    private var count: Int
        set(value) {
            conn.createStatement().let { st ->
                st.execute("UPDATE views SET count=$value")
                st.close()
            }
        }

        get() {
            var result = -1
            conn.createStatement().let { st ->
                val rs = st.executeQuery("SELECT * FROM views")
                val hasNext = rs.next()
                if (hasNext) {
                    result = rs.getInt(1)
                } else {
                    throw IllegalArgumentException("No views found")
                }
                rs.close()
                st.close()
            }
            return result
        }

}