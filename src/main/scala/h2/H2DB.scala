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
      | ('subject_1', '2020-05-14T16:23:45.456Z'),
      | ('subject_2', '2019-03-27T08:15:52.194Z'),
      | ('subject_3', '2019-11-03T10:10:34.122Z'),
      | ('subject_4', '2018-08-09T22:04:07.677Z');
      |
      |insert into posts(content, secret_key, post_timestamp, user_id_fk, topic_id_fk) values
      | ('content_1', 'secret_key_1', '2020-04-22T19:10:25.474Z', 1, 1),
      | ('content_2', 'secret_key_2', '2020-03-22T11:03:33.532Z', 2, 1),
      | ('content_3', 'secret_key_3', '2019-03-27T08:15:46.194Z', 1, 2),
      | ('content_4', 'secret_key_4', '2020-05-09T22:00:00.103Z', 1, 1),
      | ('content_5', 'secret_key_5', '2018-10-08T14:28:54.374Z', 2, 1),
      | ('content_6', 'secret_key_6', '2020-03-27T08:15:52.004Z', 2, 1),
      | ('content_7', 'secret_key_7', '2020-05-14T16:23:45.456Z', 1, 1),
      | ('content_8', 'secret_key_8', '2019-02-06T17:02:29.000Z', 2, 1),
      | ('content_9', 'secret_key_9', '2019-11-03T10:10:34.122Z', 2, 3),
      | ('content_10', 'secret_key_10', '2018-08-09T22:04:07.677Z', 1, 4);
    """.stripMargin
}
