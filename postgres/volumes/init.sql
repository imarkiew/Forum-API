CREATE TABLE users(
  user_id SERIAL PRIMARY KEY,
  nickname VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL,
  UNIQUE(nickname, email)
);

CREATE TABLE topics(
  topic_id SERIAL PRIMARY KEY,
  subject VARCHAR(50) NOT NULL,
  last_post_timestamp TIMESTAMPTZ NOT NULL
);

CREATE TABLE posts(
  post_id SERIAL PRIMARY KEY,
  content VARCHAR(1000) NOT NULL,
  secret_key VARCHAR(50) NOT NULL,
  post_timestamp TIMESTAMPTZ NOT NULL,
  user_id_fk INTEGER NOT NULL,
  topic_id_fk INTEGER NOT NULL,
  FOREIGN KEY (user_id_fk) REFERENCES users(user_id),
  FOREIGN KEY (topic_id_fk) REFERENCES topics(topic_id)
);

INSERT INTO users(nickname, email) VALUES
('nick_1', 'nick1@gmail.com'),
('nick_2', 'nick2@gmail.com');

INSERT INTO topics(subject, last_post_timestamp) VALUES
('subject_1', '2020-04-22T19:10:25.474Z'),
('subject_2', '2019-03-27T08:15:52.194Z');

INSERT INTO posts(content, secret_key, post_timestamp, user_id_fk, topic_id_fk) VALUES
('content_1', 'secret_key_1', '2020-04-22T19:10:25.474Z', 1, 1),
('content_2', 'secret_key_2', '2020-03-22T11:03:33.532Z', 2, 1),
('content_3', 'secret_key_3', '2019-03-27T08:15:46.194Z', 1, 2),
('content_4', 'secret_key_4', '2020-05-09T22:00:00.103Z', 1, 1),
('content_5', 'secret_key_5', '2018-10-08T14:28:54.374Z', 2, 1),
('content_6', 'secret_key_6', '2020-03-27T08:15:52.004Z', 2, 1),
('content_7', 'secret_key_7', '2020-05-14T16:23:45.456Z', 1, 1),
('content_8', 'secret_key_8', '2019-02-06T17:02:29.000Z', 2, 1);