Scaloid-ksoap2 through PPP over USB
===================================

Tutorial to communicate between Android and the Host using PPP over USB (adb) and a WebService (ksoap2).

### Libraries

* ksoap2-android 3.1.1 (base, j2se, android, android-assembly)
* kxml 2.2.3
* kobjects-j2me 0.0-SNAPSHOT-20040926-2

I decided to import each library separately because the standard way described on the [how to use](https://code.google.com/p/ksoap2-android/wiki/HowToUse) which is using the `ksoap2-android-assembly-3.1.1-jar-with-dependencies.jar` library failed using **sbt**.

## Android Reverse Tethering

To enable the link between the host and Android. We are going to use `adb ppp` which runs PPP over USB:

```Bash
sudo adb ppp "shell:pppd nodetach noauth noipdefault defaultroute /dev/tty" nodetach noauth noipdefault notty 192.168.0.100:192.168.0.101
```

We then launch `adb logcat` to check that the link has been sucessfuly established.

```
adb logcat -s pppd
D/pppd    (  523): using channel 1
I/pppd    (  523): Using interface ppp0
I/pppd    (  523): Connect: ppp0 <--> /dev/tty
I/pppd    (  523): local  IP address 192.168.0.101
I/pppd    (  523): remote IP address 192.168.0.100
```

Using `ifconfig` we can also see the ppp0 interface on both the host (OS X) and the Android device:

```
$ ifconfig ppp0
ppp0: flags=8051<UP,POINTOPOINT,RUNNING,MULTICAST> mtu 1500
	inet 192.168.0.100 --> 192.168.0.101 netmask 0xffffff00

$ adb shell ifconfig ppp0
ppp0: ip 192.168.0.101 mask 255.255.255.255 flags [up point-to-point running multicast]
```

## Example

### Host (WebService)

We are going to use IBM Java EE JAX-WS to create our WebService.

```scala
import java.net.URL
import javax.jws._
import javax.xml.namespace.QName
import javax.xml.ws.Endpoint
import javax.xml.ws.Service

@WebService(endpointInterface = "ch.thmx.tests.MinimalSoapServer")
private class MinimalSoapServer {
  @WebMethod
  def test(@WebParam(name = "value") value: String) = "Hi " + value

}

object WebServiceTest extends App {

  val wsURL = "http://192.168.0.100:8080/wstest"
  val endpoint = Endpoint.publish(wsURL, new MinimalSoapServer())
  println("WebService launched... Waiting for requests...")

  println("Press enter to kill the WebService...")
  Console.readLine()

}
```

Compile & run the code above using the sbt project in the [WebService directory](https://github.com/ThmX/scaloid-usb-comm/tree/master/WebService/):

```Bash
$ sbt run
```

### Android (Scaloid)

On the Android side, we are going to use the [**Scaloid** "hello world" template](https://github.com/pocorall/hello-scaloid-sbt) and modify it to make the connection with our WebService. The following function is the one that does the call and retrieve the result.

```scala
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
```

Compile & run the sbt project in the [Android directory](https://github.com/ThmX/scaloid-usb-comm/tree/master/Android/) as follows:

```Bash
$ sbt android:run
```

When clicking on the button, it should display "Hi Thomas" inside a Toast, and also inside the logcat.

## Troubleshooting

* If you can't establish **ppp over usb**. Make sure that your Android device is **Rooted**.
* Try to disable all other connection (Ethernet, Wifi, ...) while testing the connection.

## References

* [Android Reverse Tethering](https://github.com/ajasmin/android-linux-tethering)
* [Scaloid](http://blog.scaloid.org/)
* [ksoap2-android](https://code.google.com/p/ksoap2-android/)
* [Android Webservice example in Java](http://android.programmerguru.com/android-webservice-example/)

## License

The MIT License (MIT)

Copyright (c) 2014 Thomas Denoréaz

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.