package courier

import javax.mail.{ PasswordAuthentication, Session => MailSession }
import java.util.Properties

object Session {
  case class Builder(
    mailer: Mailer,
    _auth: Option[Boolean] = None,
    _startTls: Option[Boolean] = None,
    _ssl: Option[Boolean] = None,
    _socketFactory: Option[String] = None,
    _host: Option[String] = None,
    _port: Option[Int] = None,
    _debug: Option[Boolean] = None,
    _creds: Option[(String, String)] = None,
    _signer: Option[Signer] = None) {
    def auth(a: Boolean) = copy(_auth= Some(a))
    def startTls(s: Boolean) = copy(_startTls = Some(s))
    def ssl(l: Boolean) = copy(_ssl = Some(l))
    def host(h: String) = copy(_host = Some(h))
    def port(p: Int) = copy(_port = Some(p))
    def debug(d: Boolean) = copy(_debug = Some(d))
    def as(user: String, pass: String) =
      copy(_creds = Some((user, pass)))
    def socketFactory(cls: String) = copy(_socketFactory = Some(cls))
    def sslSocketFactory = socketFactory("javax.net.ssl.SSLSocketFactory")
    def withSigner(s: Signer): Builder = copy(_signer=Some(s))
    def apply() =
      mailer.copy(_session = MailSession.getInstance(
        new Properties(System.getProperties) {
        _debug.map(d => put("mail.smtp.debug", d.toString))
        _auth.map(a => put("mail.smtp.auth", a.toString))
        // enable ESMTP
        _startTls.map(s => put("mail.smtp.starttls.enable", s.toString))
        _ssl.map(l => put("mail.smtp.ssl.enable", l.toString))
        _socketFactory.map(put("mail.smtp.socketFactory.class", _))
        _host.map(put("mail.smtp.host", _))
        _port.map(p => put("mail.smtp.port", p.toString))
      }, _creds.map {
        case (user, pass) =>
          new javax.mail.Authenticator {
            protected override def getPasswordAuthentication() =
              new PasswordAuthentication(user, pass)
          }
      }
      .orNull),
        signer=_signer)
  }
}
