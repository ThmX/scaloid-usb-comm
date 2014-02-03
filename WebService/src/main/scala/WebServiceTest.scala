package ch.thmx.tests

import javax.jws._
import javax.xml.ws.Endpoint
import javax.xml.ws.Service

@WebService
trait SoapTrait {
  @WebMethod
  def test(@WebParam(name = "value") value: String): String
}

@WebService(endpointInterface = "ch.thmx.tests.SoapTrait")
private class MinimalSoapServer extends SoapTrait {

  def test(value: String) = "Hi " + value

}

import java.net.URL
import javax.xml.namespace.QName

object WebServiceTest extends App {

  val wsURL = "http://192.168.0.100:8080/wstest"

  val endpoint = Endpoint.publish(wsURL, new MinimalSoapServer())
  println("WebService launched... Waiting for requests...")

  println("Press enter to kill the WebService...")
  Console.readLine()

}