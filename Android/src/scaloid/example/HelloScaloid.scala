package scaloid.example

import org.scaloid.common._
import android.graphics.Color

import org.ksoap2._
import org.ksoap2.serialization._
import org.ksoap2.transport._

class HelloScaloid extends SActivity {

  onCreate {
    contentView = new SVerticalLayout {
      style {
        case b: SButton => b.textColor(Color.RED).onClick(soapAction)
        case t: STextView => t textSize 10.dip
        case v => v.backgroundColor(Color.YELLOW)
      }

      STextView("I am 10 dip tall")
      STextView("Me too")
      STextView("I am taller than you") textSize 15.dip // overriding
      SEditText("Yellow input field")
      SButton(R.string.red)
    } padding 20.dip
  }

  def soapAction = {
    val URL = "http://192.168.0.100:8080/wstest/MinimalSoapServerService?wsdl"

    val NAMESPACE = "http://tests.thmx.ch/"
    val METHOD_NAME = "test"
    val SOAP_ACTION = "\"" + NAMESPACE + METHOD_NAME + "\""

    //Initialize soap request + add parameters
    val request = new SoapObject(NAMESPACE, METHOD_NAME)
    request.addProperty("value", "Thomas")

    val envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11)
    envelope.implicitTypes = true
    envelope.setOutputSoapObject(request)

    try {
      // Make the soap call.
      val androidHttpTransport = new HttpTransportSE(URL)
      androidHttpTransport.debug = true
      androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>")

      //this is the actual part that will call the webservice
      androidHttpTransport.call(SOAP_ACTION, envelope)

      // Get the SoapResult from the envelope body.
      envelope.getResponse match {
        case sp: SoapPrimitive =>
          val str = sp.toString
          println("str = " + str)
          toast(str)

        case _ => System.out.println("url not found")
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace
    }

  }

}
