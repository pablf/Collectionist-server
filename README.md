# Collectionist-server
<p align="center">
<img src="https://user-images.githubusercontent.com/131800808/235451407-d432e24c-0024-4831-b937-912ba1659c15.svg" width=50% height=50% >
</p>

Collectionist is a REST API for Library management in Scala with ZIO, Spark, Slick and PostgreSQL.

(Work-in-progress)



## Structure


The functionality is divided between client and server using a REST API. 
You can deploy the server wherever you want and connect with it using the Client.
It is possible to store user accounts
and register book so that users may borrow them. Only authorized users may be able to add or delete books and allow borrowing a book.
A ban system for late returning is also implemented.

The implemented functionality is the following: 
- Book search system with spelling suggestions generator
- Different permission levels of user
- An authentication system 
- A system of book recommendation
- Scalability to deal with a big number of users, books and interactions.

There are five modules in Collectionist-server. 
Common defines the object that both the client and server must use: Book, User... and also 
some utilities like JSON encoding. The DB module takes care of all the databases transactions. 
Furthermore, Collectionist has three microservices to enrich the user experience: Recommendation, Spelling and Validator modules.

<p align="center">
<img src="https://user-images.githubusercontent.com/131800808/235464599-255aad41-55f8-426a-a51b-044c89a076ee.svg" width=50% height=50% >
</p>

### Common
The objects JWT and JSON are used for authentication and communication with a client. The case class Book, PlainBook, User and Rating extends the trait WithId.
All these case classes shape the corresponding databases (BookDB, PlainBookDB, UserDB and RatingDB) in the module DB.

<p align="center">
<img src="https://user-images.githubusercontent.com/131800808/235449725-17f51e1d-a8de-4d40-ac67-a594fb7f108e.png" width=50% height=50% >
</p>

### DB

Databases for book (BookDB) and user (UserDB) storing are implemented using PostgreSQL with Slick. The MarkedDB class allows to select of a particular parameter
and defines some methods to add, delete and find registers related to that parameter. In this way, it is possible to have CRUD operations
predefined for classes that might have different parameters.

<p align="center">
<img src="https://user-images.githubusercontent.com/131800808/235449699-a9b428fb-8323-4540-9494-3a79c2bf8545.png" width=50% height=50%>
</p>


### Spelling

The Spelling module is a Spell Checker. It can detect errors like a wrong letter inserted or changed in a word or a letter that is missing
using a database of words modelled with MongoDB to allow further scalability (class WordDB).
Information like the frequency of a word, the context of the
search or the historial of the user will be used in a future release to construct a better Spell Checker.

### Validator

The Validator microservice manages authentication. It also allows to create new users or to obtain the ID of an user.
The interface Validator describes the functionality. It is used by the server through the HTTP routes in method `login` in `Main.scala`.
The Client allows to sign up to new users. Different functionalities are allowed depending on the category of the user.

Request from the client through the REST API are screened depending
on the permission level of the user through the implementation of middlewares.

### Recommendation

The client can ask for book recommendations that may interest the logged user. At the moment the algorithm employed is an Alternating
Least Square Matrix Factorization, but it makes more difficult to take into account the information of the book lists created by the user.
In a future release, this must be implemented replacing the ALS algorithm by another that allows further scalability.
In particular, a more extensive modelling of the book information and an algorithm that uses this metadata seems the most optimal way.

## How to use

You need to setup PostgreSQL databases called bookdb, userdb and ratingdb and a MongoDB words.
