**Technologies:** Akka HTTP, Slick, Cats Validated, Postgres (docker), H2 Database (integration tests), Spray Json, Scalatest, Typesafe Config \
**Entities:** users, topics, posts \
**API:**
<ul>
<li>add a topic</li>
<li>add a post in a topic</li>
<li>update a post (needs a secret key)</li>
<li>delete a post (needs a secret key)</li>
<li>get N topics that have the most recent posts</li>
<li>posts pagination in a topic</li>
</ul>