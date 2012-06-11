package com.rackspace.com.papi.components.checker

import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import com.rackspace.com.papi.components.checker.step._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorSuite extends BaseValidatorSuite {
  //
  // validator_EMPTY does not allow ANY HTTP requests. The validator
  // is used in the following tests.
  //
  val validator_EMPTY = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val start = new Start("START", "Start", Array(urlFail, methodFail))
    start
  }, assertConfig)

  test ("GET on / should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/"),response,chain), 405)
  }

  test ("an empty GET should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET",""),response,chain), 405)
  }

  test ("GET on /a should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a"),response,chain), 404)
  }

  test ("POST on /a/b/c/hello/there should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a/b/c/hello/there"),response,chain), 404)
  }

  test ("a completely empty request should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("",""),response,chain), 405)
  }

  //
  // validator_AB allows a GET on /a/b. The validator is used in the
  // following tests.
  //
  val validator_AB = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailB = new URLFailMatch("UFB", "URLFail","b".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", "GET".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val b = new URI("b","b", "b".r, Array(get, urlFail, methodFailGet))
    val a = new URI("a","a", "a".r, Array(b, urlFailB, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/b should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b"),response,chain)
  }

  test ("GET on /a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b/"),response,chain)
  }

  test ("GET on a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","a/b/"),response,chain)
  }

  test ("GET on / should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/"),response,chain), 405)
  }

  test ("an empty GET should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET",""),response,chain), 405)
  }

  test ("GET on /a should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a"),response,chain), 405)
  }

  test ("GET on /a/b/c/d should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a/b/c/d"),response,chain), 404)
  }

  test ("POST on /a/b should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("POST","/a/b"),response,chain), 405)
  }

  test ("GET on /index.html should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/index.html"),response,chain), 404)
  }

  //
  // validator_REG1 allows a GET on /a/\-*?[0-9]+/c. That is, the 2nd
  // URI component MUST be a (possibly negative) integer of
  // unspecified size. The validator is used in the following tests.
  //
  val validator_REG1 = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailC = new URLFailMatch("UFA", "URLFail","c".r)
    val urlFailDigit = new URLFailMatch("UFA", "URLFailDigit","""\-*[0-9]+""".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", "GET".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val c = new URI("c","c", "c".r, Array(get, urlFail, methodFailGet))
    val digit = new URI("digit","digit", """\-*[0-9]+""".r, Array(c, urlFailC, methodFail))
    val a = new URI("a","a", "a".r, Array(digit, urlFailDigit, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/7/c should succeed on validator_REG1") {
    validator_REG1.validate(request("GET","/a/7/c"),response,chain)
  }

  test ("GET on /a/-7/c should succeed on validator_REG1") {
    validator_REG1.validate(request("GET","/a/-7/c"),response,chain)
  }

  test ("GET on /a/<randomLong>/c should succeed on validator_REG1") {
    val rl = new Random(new Date().getTime()).nextLong()
    validator_REG1.validate(request("GET","/a/"+rl+"/c"),response,chain)
  }

  test ("GET on /a/<bigInt>/c should succeed on validator_REG1") {
    val bi = new BigInteger(1024, new Random(new Date().getTime()).self)
    validator_REG1.validate(request("GET","/a/"+bi+"/c"),response,chain)
  }

  test ("GET on /a/<randomDouble>/c should fail validator_REG1") {
    val rf = new Random(new Date().getTime()).nextDouble()
    assertResultFailed(validator_REG1.validate(request("GET","/a/"+rf+"/c"),response,chain), 404)
  }

  test ("GET on /a//c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a//c"),response,chain), 404)
  }

  test ("GET on /a/b/c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a/b/c"),response,chain), 404)
  }

  test ("GET on /a should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a"),response,chain), 405)
  }

  test ("GET on /a/+7/c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a/+7/c"),response,chain), 404)
  }

  //
  // validator_REG2 allows a GET on /a/.*/c. That is, the 2nd URI
  // component can be anything. The validator is used in the
  // following tests.
  //
  val validator_REG2 = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailC = new URLFailMatch("UFA", "URLFail","c".r)
    val urlFailDigit = new URLFailMatch("UFA", "URLFailDigit",""".*""".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", "GET".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val c = new URI("c","c", "c".r, Array(get, urlFail, methodFailGet))
    val digit = new URI("any","any", """.*""".r, Array(c, urlFailC, methodFail))
    val a = new URI("a","a", "a".r, Array(digit, urlFailDigit, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/7/c should succeed on validator_REG2") {
    validator_REG2.validate(request("GET","/a/7/c"),response,chain)
  }

  test ("GET on /a/-7/c should succeed on validator_REG2") {
    validator_REG2.validate(request("GET","/a/-7/c"),response,chain)
  }

  test ("GET on /a/<randomLong>/c should succeed on validator_REG2") {
    val rl = new Random(new Date().getTime()).nextLong()
    validator_REG2.validate(request("GET","/a/"+rl+"/c"),response,chain)
  }

  test ("GET on /a/<bigInt>/c should succeed on validator_REG2") {
    val bi = new BigInteger(1024, new Random(new Date().getTime()).self)
    validator_REG2.validate(request("GET","/a/"+bi+"/c"),response,chain)
  }

  test ("GET on /a/<randomDouble>/c should succeed validator_REG2") {
    val rf = new Random(new Date().getTime()).nextDouble()
    validator_REG2.validate(request("GET","/a/"+rf+"/c"),response,chain)
  }

  test ("GET on /a/<uuid>/c should succeed validator_REG2") {
    val uuid = UUID.randomUUID().toString()
    validator_REG2.validate(request("GET","/a/"+uuid+"/c"),response,chain)
  }

  test ("GET on /a/<katakana>/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/c"),response,chain)
  }

  test ("GET on /a/<arrows>/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/c"),response,chain)
  }

  test ("GET on /a/<snowman>/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/%E2%98%83/c"),response,chain)
  }

  test ("GET on /a/b/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/b/c"),response,chain)
  }

  test ("GET on /a/ 7/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/+7/c"),response,chain)
  }

  test ("GET on /a/+7/c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/%2B7/c"),response,chain)
  }

  test ("GET on /a/    /c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/++++/c"),response,chain)
  }

  test ("GET on /a/  hi  /c should succeed validator_REG2") {
    validator_REG2.validate(request("GET","/a/++hi++/c"),response,chain)
  }

  test ("GET on /a//c should fail validator_REG2") {
    assertResultFailed(validator_REG2.validate(request("GET","/a//c"),response,chain), 405)
  }

  test ("GET on /a should fail validator_REG2") {
    assertResultFailed(validator_REG2.validate(request("GET","/a"),response,chain), 405)
  }

  test ("GET on /a/b/d should fail validator_REG2") {
    assertResultFailed(validator_REG2.validate(request("GET","/a/b/d"),response,chain), 404)
  }

  //
  // validator_REG3 tests that GET and X-GET are both supported on
  // /a/b. The validator is used in the following tests.
  //
  //
  val validator_REG3 = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailB = new URLFailMatch("UFB", "URLFail","b".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", """X?GET""".r)
    val get = new Method("GET/XGET", "GET/XGET", """X?GET""".r, Array (accept))
    val b = new URI("b","b", "b".r, Array(get, urlFail, methodFailGet))
    val a = new URI("a","a", "a".r, Array(b, urlFailB, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/b should succeed on validator_REG3") {
    validator_REG3.validate(request("GET","/a/b"),response,chain)
  }

  test ("XGET on /a/b should succeed on validator_REG3") {
    validator_REG3.validate(request("XGET","/a/b"),response,chain)
  }

  test ("PUT on /a/b should fail validator_REG3") {
    assertResultFailed(validator_REG3.validate(request("PUT","/a/b"),response,chain), 405)
  }

  test ("POST on /a/b should fail validator_REG3") {
    assertResultFailed(validator_REG3.validate(request("POST","/a/b"),response,chain), 405)
  }

  test ("X on /a/b should fail validator_REG3") {
    assertResultFailed(validator_REG3.validate(request("X-","/a/b"),response,chain), 405)
  }

  test ("XPUT on /a/b should fail validator_REG3") {
    assertResultFailed(validator_REG3.validate(request("XPUT","/a/b"),response,chain), 405)
  }

  //
  // validator_CPLX1 tests the following:
  //
  // GET   /a/b
  // GET   /a/b/c
  // POST  /a/b/c
  // PUT   /a/<any not b or d>/c
  // GET   /a/d/c
  // POST  /a/d/c
  //
  // c in /a/b/c and a/d/c is the same node.
  // c in /a/<any not b or d >/c is different
  //
  // That is a PUT on /a/b/c or /a/d/c should fail.
  //
  // The validator is used in the following tests.
  //
  val validator_CPLX1 = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailC = new URLFailMatch("UFA", "URLFail","c".r)
    val urlFailABNotAB = new URLFailMatch("UFABNAB", "URLFail","""b|d|[^bd]""".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailPut = new MethodFailMatch ("MFG", "MethodFail", "PUT".r)
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", "GET".r)
    val methodFailGetPost = new MethodFailMatch ("MFG", "MethodFail", "GET|POST".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val put = new Method("PUT", "PUT", "PUT".r, Array (accept))
    val post = new Method("POST", "POST", "POST".r, Array (accept))
    val c1 = new URI("c1","c1", "c".r, Array(get, post, urlFail, methodFailGetPost))
    val b = new URI("b","b", "b".r, Array(get, c1, urlFailC, methodFailGet))
    val d = new URI("d","d", "d".r, Array(c1, urlFailC, methodFail))
    val c2 = new URI("c2","c2", "c".r, Array(put, urlFail, methodFailPut))
    val anynotbd = new URI("any","any", """[^bd]""".r, Array(c2, urlFailC, methodFail))
    val a = new URI("a","a", "a".r, Array(b, d, anynotbd, urlFailABNotAB, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/b should succeed on validator_CPLX1") {
    validator_CPLX1.validate(request("GET","/a/b"),response,chain)
  }

  test ("GET on /a/b/c should succeed on validator_CPLX1") {
    validator_CPLX1.validate(request("GET","/a/b/c"),response,chain)
  }

  test ("POST on /a/b/c should succeed on validator_CPLX1") {
    validator_CPLX1.validate(request("POST","/a/b/c"),response,chain)
  }

  test ("GET on /a/d/c should succeed on validator_CPLX1") {
    validator_CPLX1.validate(request("GET","/a/d/c"),response,chain)
  }

  test ("POST on /a/d/c should succeed on validator_CPLX1") {
    validator_CPLX1.validate(request("POST","/a/d/c"),response,chain)
  }

  test ("PUT on /a/!/c should succeed validator_CPLX1") {
    validator_CPLX1.validate(request("PUT","/a/!/c"),response,chain)
  }

  test ("PUT on /a/z/c should succeed validator_CPLX1") {
    validator_CPLX1.validate(request("PUT","/a/z/c"),response,chain)
  }

  test ("PUT on /a/<katakana>/c should succeed validator_CPLX1") {
    validator_CPLX1.validate(request("PUT","/a/%E3%83%84/c"),response,chain)
  }

  test ("PUT on /a/<arrow>/c should succeed validator_CPLX1") {
    validator_CPLX1.validate(request("PUT","/a/%E2%86%90/c"),response,chain)
  }

  test ("PUT on /a/<snowman>/c should succeed validator_CPLX1") {
    validator_CPLX1.validate(request("PUT","/a/%E2%98%83/c"),response,chain)
  }

  test ("PUT on /a/b/c should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("PUT","/a/b/c"),response,chain), 405)
  }

  test ("PUT on /a/d/c should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("PUT","/a/d/c"),response,chain), 405)
  }

  test ("PUT on /a/atest/c should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("PUT","/a/atest/c"),response,chain), 404)
  }

  test ("GET on /a/d should fail on validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("GET","/a/d"),response,chain), 405)
  }

  test ("GET on /a/z should fail on validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("GET","/a/z"),response,chain), 405)
  }

  test ("GET on /a/<katakana> should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("GET","/a/%E3%83%84"),response,chain), 405)
  }

  test ("GET on /a/z/c should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("GET","/a/z/c"),response,chain), 405)
  }

  test ("GET on /a/<katakana>/c should fail validator_CPLX1") {
    assertResultFailed(validator_CPLX1.validate(request("GET","/a/%E3%83%84/c"),response,chain), 405)
  }

  //
  // validator_AM allows:
  //
  // GET /.*/b
  // PUT /a/b (accepting application/xml)
  // POST /c/b (accepting application/xml and application/json)
  //
  // The of course means that a GET on /a/b is allowed. As is a get on
  // /c/b.
  //
  // The validator is used in the following tests.
  //
  val validator_AM = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val urlFailB = new URLFailMatch("UFB", "URLFail","b".r)
    val reqTFail = new ReqTypeFail("RTF", "RTFail", "(?i)application/xml".r)
    val reqTFail2 = new ReqTypeFail("RTF2", "RTFail", "(?i)application/xml|(?i)application/json".r)
    val methodFail = new MethodFail ("MF", "MethodFail")
    val methodFailGet = new MethodFailMatch ("MFG", "MethodFail", "GET".r)
    val methodFailPut = new MethodFailMatch ("MFP", "MethodFail", "PUT".r)
    val methodFailPost = new MethodFailMatch ("MFPo", "MethodFail", "POST".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val putIn = new ReqType("ReqType", "XML", "(?i)application/xml".r, Array(accept))
    val postIn = new ReqType("ReqType", "XML|JSON", "(?i)application/xml|(?i)application/json".r, Array(accept))
    val put = new Method("PUT", "PUT", "PUT".r, Array (putIn, reqTFail))
    val post = new Method("POST", "POST", "POST".r, Array (postIn, reqTFail2))
    val b = new URI("b","b", "b".r, Array(put, urlFail, methodFailPut))
    val b2 = new URI("b2","b2", "b".r, Array(get, urlFail, methodFailGet))
    val b3 = new URI("b3","b3", "b".r, Array(post, urlFail, methodFailPost))
    val a = new URI("a","a", "a".r, Array(b, urlFailB, methodFail))
    val any = new URI("any","any", ".*".r, Array(b2, urlFailB, methodFail))
    val c = new URI("c","c", "c".r, Array(b3, urlFailB, methodFail))
    val start = new Start("START", "Start", Array(a, c, any, methodFail))
    start
  }, assertConfig)

  test ("GET on /a/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/a/b"),response,chain)
  }

  test ("GET on /c/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/c/b"),response,chain)
  }

  test ("PUT on /a/b should succeed on validator_AM if the media type is application/xml") {
    validator_AM.validate(request("PUT","/a/b","application/xml"),response,chain)
  }

  test ("PUT on /a/b should succeed on validator_AM if the media type is application/XML") {
    validator_AM.validate(request("PUT","/a/b","application/XML"),response,chain)
  }

  test ("PUT on /a/b should succeed on validator_AM if the media type is AppLication/XML") {
    validator_AM.validate(request("PUT","/a/b","AppLication/XML"),response,chain)
  }

  test ("PUT on /a/b should fail on validator_AM if the media type is application/json") {
    assertResultFailed(validator_AM.validate(request("PUT","/a/b","application/json"),response,chain), 415)
  }

  test ("PUT on /a/b should fail on validator_AM if the media type is text/html") {
    assertResultFailed(validator_AM.validate(request("PUT","/a/b","text/html"),response,chain), 415)
  }

  test ("PUT on /a/b should fail on validator_AM if the media type is not specified") {
    assertResultFailed(validator_AM.validate(request("PUT","/a/b"),response,chain), 415)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is application/xml") {
    validator_AM.validate(request("POST","/c/b","application/xml"),response,chain)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is application/XML") {
    validator_AM.validate(request("POST","/c/b","application/XML"),response,chain)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is AppLication/XML") {
    validator_AM.validate(request("POST","/c/b","AppLication/XML"),response,chain)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is application/json") {
    validator_AM.validate(request("POST","/c/b","application/json"),response,chain)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is application/JSON") {
    validator_AM.validate(request("POST","/c/b","application/JSON"),response,chain)
  }

  test ("POST on /c/b should succeed on validator_AM if the media type is AppLication/JSON") {
    validator_AM.validate(request("POST","/c/b","AppLication/JSON"),response,chain)
  }

  test ("POST on /c/b should fail on validator_AM if the media type is application/atom+xml") {
    assertResultFailed(validator_AM.validate(request("POST","/c/b","application/atom+xml"),response,chain), 415)
  }

  test ("POST on /c/b should fail on validator_AM if the media type is text/html") {
    assertResultFailed(validator_AM.validate(request("POST","/c/b","text/html"),response,chain), 415)
  }

  test ("POST on /c/b should fail on validator_AM if the media type is not specified") {
    assertResultFailed(validator_AM.validate(request("POST","/c/b"),response,chain), 415)
  }

  test ("GET on /b/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/b/b"),response,chain)
  }

  test ("GET on /<katakana>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/b"),response,chain)
  }

  test ("GET on /<arrows>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/b"),response,chain)
  }

  test ("POST on /a/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("POST","/a/b","application/xml"),response,chain), 405)
  }

  test ("PUT on /c/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("PUT","/c/b","application/xml"),response,chain), 405)
  }

  test ("DELETE on /z/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("DELETE","/z/b"),response,chain), 405)
  }

  test ("GET on /a/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/a/c"),response,chain), 404)
  }

  test ("GET on /z/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/z/c"),response,chain), 404)
  }


  //
  //  validator_XML allows:
  //
  //  GET /a/b
  //  PUT /a/b (accepting valid application/xml)
  //
  val validator_XML = Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val urlFailA = new URLFailMatch("UFA", "URLFail","a".r)
    val urlFailB = new URLFailMatch("UFA", "URLFail","b".r)
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val methodFailGetPut = new MethodFailMatch ("MFG", "MethodFail", "GET|PUT".r)
    val reqTFail = new ReqTypeFail("RTF", "RTFail", "(?i)application/xml".r)
    val contentFail = new ContentFail ("CF", "CONTENTFAIL")
    val wellXML = new WellFormedXML ("WXML", "WELLXML", Array(accept))
    val putIn = new ReqType("ReqType", "XML", "(?i)application/xml".r, Array(wellXML, contentFail))
    val put = new Method("PUT", "PUT", "PUT".r, Array (putIn, reqTFail))
    val b = new URI("b","b", "b".r, Array(put, get, urlFail, methodFailGetPut))
    val a = new URI("a","a", "a".r, Array(b, urlFailB, methodFail))
    val start = new Start("START", "Start", Array(a, urlFailA, methodFail))
    start
  }, config(false, true))

  test ("GET on /a/b should succeed on validator_XML") {
    validator_XML.validate(request("GET","/a/b"),response,chain)
  }

  test ("PUT on /a/b with valid XML should succeed on validator_XML") {
    validator_XML.validate(request("PUT","/a/b","application/xml",
                                   <some_xml att='1' xmlns='test.org'>
                                     <an_element>
                                         <another_element />
                                     </an_element>
                                   </some_xml>
                                 ),response,chain)
  }

  test ("GET on /a/c should fail on validator_XML") {
    assertResultFailed(validator_XML.validate(request("GET","/a/c"),response,chain), 404)
  }

  test ("a POST on /a/b should fail on validator_XML") {
    assertResultFailed(validator_XML.validate(request("POST","/a/b", "application/xml",
                                                      <some_xml att='1' xmlns='test.org'>
                                                        <an_element>
                                                         <another_element />
                                                        </an_element>
                                                      </some_xml>
                                                    ),response,chain), 405)
  }

  test ("PUT on /a/b with valid JSON should fail with 415") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/json",
                                                      """
                                                      {
                                                        \"flavor\" : {
                                                          \"id\" : \"52415800-8b69-11e0-9b19-734f1195ff37\",
                                                          \"name\" : \"256 MB Server\"
                                                        }
                                                      }
                                                      """),response,chain), 415)
  }

  test ("PUT on /a/b with valid JSON mislabed as XML should fail with 400") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/xml",
                                                      """
                                                      {
                                                        \"flavor\" : {
                                                          \"id\" : \"52415800-8b69-11e0-9b19-734f1195ff37\",
                                                          \"name\" : \"256 MB Server\"
                                                        }
                                                      }
                                                      """),response,chain), 400)
  }

  test ("PUT on /a/b with malformed XML should fail on validator_XML (unclosed tag)") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org'>
                                     <an_element>
                                         <another_element>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("PUT on /a/b with malformed XML should fail on validator_XML (unclosed attribute)") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1 xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("PUT on /a/b with malformed XML should fail on validator_XML (bad tag)") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                         <another
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("PUT on /a/b with malformed XML should fail on validator_XML (bad namespace)") {
    assertResultFailed(validator_XML.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org' xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

}
