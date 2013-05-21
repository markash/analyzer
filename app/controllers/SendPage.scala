package controllers

import javax.naming.{NamingException, InitialContext, Context}
import play.api.mvc.{Controller, Action}
import java.util.{Hashtable => JHashtable}
import javax.jms._
import play.api.libs.iteratee.Enumerator
import play.api._
import play.api.mvc._

import play.api.libs.{ Comet }
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random

case class Val(x: String)

object SendPage extends Controller with ApplicationMenu {

  val DEFAULT_QCF_NAME = "ecs.jms.cf"
  val DEFAULT_QUEUE_NAME = "ecs.jms.queue.in.price"
  val DEFAULT_URL = "t3://127.0.0.1:7001"
  val DEFAULT_USER = "system"
  val DEFAULT_PASSWORD =  "password1*"

  val DEFAULT_INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";

  def operations(): Enumerator[Val] = Enumerator.generateM[Val] {
    Promise.timeout(Some(Val("Hello")), Random.nextInt(500))
  }

  def send() = Action {
    sendTextMessage("Hello from Play Framework")
    Ok(views.html.send(sections))
  }

  def sendTextMessage(message: String) {
     sendTextMessage(
         url = DEFAULT_URL,
         user = DEFAULT_USER,
         password = DEFAULT_PASSWORD,
         cf = DEFAULT_QCF_NAME,
         queueName = DEFAULT_QUEUE_NAME,
         messageText = message)
  }

  def sendTextMessage(url : String, user : String, password : String, cf : String, queueName : String, messageText : String) {

    try {
      val ctx = initialContext(DEFAULT_INITIAL_CONTEXT_FACTORY, url, user, password)
      println("Got InitialContext " + ctx.toString())

      val qcf = lookupConnectionFactory(ctx, cf)
      println("Got ConnectionFactory " + qcf.toString())

      // create QueueConnection
      val qc = qcf.createConnection()
      println("Got QueueConnection " + qc.toString())

      // create QueueSession
      val session = qc.createSession(false, 0)
      println("Got QueueSession " + session.toString())

      val destination = lookupDestination(ctx, queueName)
      println("Got Destination " + destination.toString())

      // create QueueSender
      val producer = session.createProducer(destination)
      println("Got QueueSender " + producer.toString())

      // create TextMessage
      val message = session.createTextMessage()
      println("Got TextMessage " + message.toString())

      // set message text in TextMessage
      message.setText(messageText)
      println("Set text in TextMessage " + message.toString())

      producer.send(message)
      println("Sent message ")

    } catch {
     case ne : NamingException =>
         ne.printStackTrace(System.err)
     case jmse : JMSException =>
         jmse.printStackTrace(System.err)
     case _ : Throwable =>
         println("Got other/unexpected exception")
    }
  }

  def main(args: Array[String]) = {
    val i = Iteratee.foreach[Val]{(e) => println(e)}
     val x = operations run i
  }

  def lookupConnectionFactory(context: Context, jndi: String): ConnectionFactory = {
    context.lookup(jndi).asInstanceOf[ConnectionFactory];
  }

  def lookupQueue(context: Context, jndi: String): Queue = {
    context.lookup(jndi).asInstanceOf[Queue];
  }

  def lookupDestination(context: Context, jndi: String): Destination = {
    context.lookup(jndi).asInstanceOf[Destination];
  }

  def initialContext(initialContextFactory: String, url: String, user: String, password: String): Context = {
    new InitialContext(connectionProperties(initialContextFactory, url, user, password))
  }

  def connectionProperties(initialContextFactory: String, url: String, user: String, password: String): JHashtable[String, String] = {
    val properties = new JHashtable[String, String]
    properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory)
    properties.put(Context.PROVIDER_URL, url)
    properties.put(Context.SECURITY_PRINCIPAL, user)
    properties.put(Context.SECURITY_CREDENTIALS, password)

    properties
  }
}
