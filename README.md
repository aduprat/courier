# courier

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.daddykotex/courier_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.daddykotex/courier_2.12)

deliver electronic mail with scala from the [future](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future)

![courier](http://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Courrier.jpg/337px-Courrier.jpg)

## install

Via the copy and paste method

```scala
libraryDependencies += "com.github.daddykotex" %% "courier" % "1.0.0"
```

## usage

deliver electronic mail via gmail

```scala
import courier._, Defaults._
val mailer = Mailer("smtp.gmail.com", 587)
               .auth(true)
               .as("you@gmail.com", "p@$$w3rd")
               .startTls(true)()
mailer(Envelope.from("you" `@` "gmail.com")
        .to("mom" `@` "gmail.com")
        .cc("dad" `@` "gmail.com")
        .subject("miss you")
        .content(Text("hi mom"))).onSuccess {
          case _ => println("message delivered")
        }

mailer(Envelope.from("you" `@` "work.com")
         .to("boss" `@` "work.com")
         .subject("tps report")
         .content(Multipart()
           .attach(new java.io.File("tps.xls"))
           .html("<html><body><h1>IT'S IMPORTANT</h1></body></html>")))
           .onSuccess {
             case _ => println("delivered report")
           }
```

If using SSL/TLS instead of STARTTLS, substitute `.startTls(true)` with `.ssl(true)` when setting up the `Mailer`.

### S/MIME
Courier supports sending S/MIME signed email through its optional dependencies on the Bouncycastle cryptography libraries. It does not yet support sending encrypted email.

Make sure the Mailer is instantiated with a signer, and then wrap your message in a Signed() object.

```scala
import courier._
import java.security.cert.X509Certificate
import java.security.PrivateKey

val certificate: X509Certificate = ???
val privateKey: PrivateKey = ???
val trustChain: Set[X509Certificate] = ???

val signer = Signer(privateKey, certificate, trustChain)
val mailer = Mailer("smtp.gmail.com", 587)
               .auth(true)
               .as("you@gmail.com", "p@$$w3rd")
               .withSigner(signer)
               .startTtls(true)()
val envelope = Envelope
        .from("mr_pink" `@` "gmail.com")
        .to("mr_white" `@` "gmail.com")
        .subject("the jewelry store")
        .content(Signed(Text("For all I know, you're the rat.")))

mailer(envelope)
```


## testing

Since courier is based on JavaMail, you can use [Mock JavaMail](https://java.net/projects/mock-javamail) to execute your tests. Simply add the following to your `build.sbt`:

```scala
libraryDependencies += "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test"
```

Having this in your test dependencies will automatically enable Mock JavaMail during tests. You can then test for email sends, etc.

```scala
import courier._
import org.specs2.mutable.Specification
import scala.concurrent.duration._

// Need NoTimeConversions to prevent conflict with scala.concurrent.duration._
class MailSpec extends Specification with NoTimeConversions {
  "the mailer" should {
  	"send an email" in {
  	  val mailer = Mailer("localhost", 25)()
  	  val future = mailer(Envelope.from("someone@example.com".addr)
          	.to("mom@gmail.com".addr)
          	.cc("dad@gmail.com".addr)
          	.subject("miss you")
          	.content(Text("hi mom")))

          Await.ready(future, 5.seconds)
          val momsInbox = Mailbox.get("mom@gmail.com")
          momsInbox.size === 1
          val momsMsg = momsInbox.get(0)
          momsMsg.getContent === "hi mom"
          momsMsg.getSubject === "miss you"
        }
  	}
  }
}
```

[Here](https://community.oracle.com/blogs/kohsuke/2007/04/26/introducing-mock-javamail-project) is an excellent article on using Mock JavaMail.

(C) Doug Tangren (softprops) and others, 2013-2018
