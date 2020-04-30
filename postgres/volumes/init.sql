CREATE USER api;
CREATE DATABASE forum;
GRANT ALL PRIVILEGES ON DATABASE forum TO api;

CREATE TABLE users(
  user_id serial PRIMARY KEY,
  nickname VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE topics(
  topic_id serial PRIMARY KEY,
  subject VARCHAR(50) NOT NULL,
  last_post_datetime TIMESTAMP NOT NULL
);

CREATE TABLE posts(
  post_id serial PRIMARY KEY,
  content VARCHAR(1000) NOT NULL,
  secret_key VARCHAR(50) NOT NULL,
  post_datetime TIMESTAMP NOT NULL,
  user_id_fk INTEGER NOT NULL,
  topic_id_fk INTEGER NOT NULL,
  FOREIGN KEY (user_id_fk) REFERENCES users(user_id),
  FOREIGN KEY (topic_id_fk) REFERENCES topics(topic_id)
);

CREATE SEQUENCE user_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE post_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE topic_id_seq START 1 INCREMENT 1;

INSERT INTO users(user_id, nickname, email) VALUES
(nextval('user_id_seq'), 'nick_1', 'nick1@gmail.com'),
(nextval('user_id_seq'), 'nick_2', 'nick2@gmail.com');

INSERT INTO topics(topic_id, subject, last_post_datetime) VALUES
(nextval('topic_id_seq'), 'subject_1', '2020-04-22 19:10:25'),
(nextval('topic_id_seq'), 'subject_2', '2019-11-05 23:04:13');

INSERT INTO posts(post_id, content, secret_key, post_datetime, user_id_fk, topic_id_fk) VALUES
(nextval('post_id_seq'), 'content_1', 'secret_key_1', '2020-04-22 19:10:25', 1, 1),
(nextval('post_id_seq'), 'content_2', 'secret_key_2', '2020-03-22 11:03:33', 2, 1),
(nextval('post_id_seq'), 'content_3', 'secret_key_3', '2019-11-05 23:04:13', 1, 2);