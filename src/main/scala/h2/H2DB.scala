package h2

import java.sql.DriverManager
import com.typesafe.config.ConfigFactory


object H2DB {

  private val config = ConfigFactory.load
  private val driver = config.getString("dbConfig.h2.driver")
  private val url = config.getString("dbConfig.h2.url")
  private val user = config.getString("dbConfig.h2.user")
  private val password = config.getString("dbConfig.h2.password")
  Class.forName(driver).newInstance()
  private val connection = DriverManager.getConnection(url, user, password)

  def initDB: Unit = {
    val statement = connection.createStatement()
    statement.execute(createSchema)
    statement.execute(addRecords)
  }

  def shutdownDB: Unit = {
    connection.createStatement().execute("SHUTDOWN")
    connection.close()
  }

  private val createSchema =
    s"""
      |create table users(
      |  user_id int auto_increment primary key,
      |  nickname varchar(50) not null,
      |  email varchar(50) not null,
      |  unique(nickname, email)
      |);
      |
      |create table topics(
      | topic_id int auto_increment primary key,
      | subject varchar(50) not null,
      | last_post_timestamp timestamp with time zone not null
      |);
      |
      |create table posts(
      | post_id int auto_increment primary key,
      | content varchar(1000) not null,
      | secret_key varchar(50) not null,
      | post_timestamp timestamp with time zone not null,
      | user_id_fk int not null,
      | topic_id_fk int not null,
      | foreign key (user_id_fk) references users(user_id),
      | foreign key (topic_id_fk) references topics(topic_id)
      |);
    """.stripMargin

  private val addRecords =
    s"""
      |insert into users(nickname, email) values
      | ('nick_1', 'nick1@gmail.com'),
      | ('nick_2', 'nick2@gmail.com');
      |
      |insert into topics(subject, last_post_timestamp) values
      | ('subject_1', '2020-04-22T19:10:25.474Z'),
      | ('subject_2', '2019-03-27T08:15:52.194Z');
      |
      |insert into posts(content, secret_key, post_timestamp, user_id_fk, topic_id_fk) values
      | ('content_1', 'secret_key_1', '2020-04-22T19:10:25.474Z', 1, 1),
      | ('content_2', 'secret_key_2', '2020-03-22T11:03:33.532Z', 2, 1),
      | ('content_3', 'secret_key_3', '2019-03-27T08:15:52.194Z', 1, 2);
    """.stripMargin
}
