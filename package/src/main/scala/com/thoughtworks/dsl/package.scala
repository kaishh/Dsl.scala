package com.thoughtworks

/** This project, '''Dsl.scala''', is a framework to create embedded '''D'''omain-'''S'''pecific '''L'''anguages.
  *
  * DSLs written in '''Dsl.scala''' are collaborative with others DSLs and Scala control flows.
  * DSL users can create functions that contains interleaved DSLs implemented by different vendors,
  * along with ordinary Scala control flows.
  *
  * We also provide some built-in DSLs for asynchronous programming, collection manipulation,
  * and adapters to [[scalaz.Monad]] or [[cats.Monad]].
  * Those built-in DSLs can be used as a replacement of
  * [[https://docs.scala-lang.org/tour/for-comprehensions.html `for` comprehension]],
  * [[https://github.com/scala/scala-continuations scala-continuations]],
  * [[https://github.com/scala/scala-async scala-async]],
  * [[http://monadless.io/ Monadless]],
  * [[https://github.com/pelotom/effectful effectful]]
  * and [[https://github.com/ThoughtWorksInc/each ThoughtWorks Each]].
  *
  * = Introduction =
  *
  * == Reinventing control flow in DSL ==
  *
  * Embedded DSLs usually consist of a set of domain-specific keywords,
  * which can be embedded in the their hosting languages.
  *
  * Ideally, a domain-specific keyword should be an optional extension,
  * which can be present everywhere in the ordinary control flow of the hosting language.
  * However, previous embedded DSLs usually badly interoperate with hosting language control flow.
  * Instead, they reinvent control flow in their own DSL.
  *
  * For example, the [[https://akka.io akka]] provides
  * [[https://doc.akka.io/docs/akka/2.5.10/fsm.html a DSL to create finite-state machines]],
  * which consists of some domain-specific keywords like [[akka.actor.AbstractFSM#when when]],
  * [[akka.actor.AbstractFSM#goto goto]] and [[akka.actor.AbstractFSM#stay stay]].
  * Unfortunately, you cannot embedded those keywords into your ordinary `if` / `while` / `try` control flows,
  * because Akka's DSL is required to be split into small closures,
  * preventing ordinary control flows from crossing the boundary of those closures.
  *
  * TensorFlow's [[https://www.tensorflow.org/api_guides/python/control_flow_ops control flow operations]] and
  * Caolan's [[https://github.com/caolan/async async]] library are examples of reinventing control flow
  * in languages other than Scala.
  *
  * == Monad: the generic interface of control flow ==
  *
  * It's too trivial to reinvent the whole set of control flows for each DSL.
  * A simpler approach is only implementing a minimal interface required for control flows for each domain,
  * while the syntax of other control flow operations are derived from the interface, shared between different domains.
  *
  * Since [[https://www.sciencedirect.com/science/article/pii/0890540191900524 computation can be represented as monads]],
  * some libraries use monad as the interface of control flow,
  * including [[scalaz.Monad]], [[cats.Monad]] and [[com.twitter.algebird.Monad]].
  * A DSL author only have to implement two abstract method in [[scalaz.Monad]],
  * and all the derived control flow operations
  * like [[scalaz.syntax.MonadOps.whileM]], [[scalaz.syntax.BindOps.ifM]] are available.
  * In addition, those monadic data type can be created and composed
  * from Scala's built-in [[https://docs.scala-lang.org/tour/for-comprehensions.html `for` comprehension]].
  *
  * For example, you can use the same [[scalaz.syntax syntax]] or `for` comprehension
  * to create [[org.scalacheck.Gen random value generators]]
  * and [[com.thoughtworks.binding.Binding data-binding expressions]],
  * as long as there are [[scalaz.Monad Monad]] instances
  * for [[org.scalacheck.Gen]] and [[com.thoughtworks.binding.Binding]] respectively.
  *
  * Although the effort of creating a DSL is minimized with the help of monads,
  * the syntax is still unsatisfactory.
  * Methods in `MonadOps` still seem like a duplicate of ordinary control flow,
  * and `for` comprehension supports only a limited set of functionality in comparison to ordinary control flows.
  * `if` / `while` / `try` and other block expressions cannot appear in the enumerator clause of `for` comprehension.
  *
  * == Enabling ordinary control flows in DSL via macros ==
  *
  * An idea to avoid inconsistency between domain-specific control flow and ordinary control flow is
  * converting ordinary control flow to domain-specific control flow at compiler time.
  *
  * For example, [[https://github.com/scala/scala-async scala.async]] provides a macro
  * to generate asynchronous control flow.
  * The users just wrap normal synchronous code in a [[scala.async]] block,
  * and it runs asynchronously.
  *
  * This approach can be generalized to any monadic data types.
  * [[https://github.com/ThoughtWorksInc/each ThoughtWorks Each]], [[http://monadless.io/ Monadless]]
  * and [[https://github.com/pelotom/effectful effectful]] are macros
  * that convert ordinary control flow to monadic control flow.
  *
  * For example, with the help of [[https://github.com/ThoughtWorksInc/each ThoughtWorks Each]],
  * [[https://github.com/ThoughtWorksInc/Binding.scala Binding.scala]] is used to create reactive HTML templating
  * from ordinary Scala control flow.
  *
  * == Delimited continuations ==
  *
  * Another generic interface of control flow is continuation,
  * which is known as
  * [[https://www.schoolofhaskell.com/user/dpiponi/the-mother-of-all-monads the mother of all monads]],
  * where control flows in specific domain can be supported by specific final result types of continuations.
  *
  * [[https://github.com/scala/scala-continuations scala-continuations]]
  * and [[https://github.com/qifun/stateless-future stateless-future]]
  * are two delimited continuation implementations.
  * Both projects can convert ordinary control flow to continuation-passing style closure chains at compiler time.
  *
  * For example, [[https://github.com/qifun/stateless-future-akka stateless-future-akka]],
  * based on `stateless-future`,
  * provides a special final result type for akka actors.
  * Unlike [[akka.actor.AbstractFSM]]'s inconsistent control flows, users can create complex finite-state machines
  * from simple ordinary control flows along with `stateless-future-akka`'s domain-specific keyword `nextMessage`.
  *
  * == Collaborative DSLs ==
  *
  * All the above approaches lack of the ability to collaborate with other DSLs.
  * Each of the above DSLs can be only exclusively enabled in a code block.
  * For example,
  * [[https://github.com/scala/scala-continuations scala-continuations]]
  * enables calls to `@cps` method in [[scala.util.continuations.reset]] blocks,
  * and [[https://github.com/ThoughtWorksInc/each ThoughtWorks Each]]
  * enables the magic `each` method for [[scalaz.Monad]] in [[com.thoughtworks.each.Monadic.monadic]] blocks.
  * It is impossible to enable both DSLs in one function.
  *
  * This [[https://github.com/ThoughtWorksInc/Dsl.scala Dsl.scala]] project resolves this problem.
  *
  * We also provide adapters to all the above kinds of DSLs.
  * Instead of switching different DSLs between different functions,
  * DSL users can use multiple DSLs together in one function,
  * by simply adding [[com.thoughtworks.dsl.compilerplugins.BangNotation our Scala compiler plug-in]].
  *
  * @example Suppose you want to create an [[https://en.wikipedia.org/wiki/Xorshift Xorshift]] random number generator.
  *
  *          The generated numbers should be stored in a lazily evaluated infinite [[scala.collection.immutable.Stream Stream]],
  *          which can be implemented as a recursive function that produce the next random number in each iteration,
  *          with the help of our built-in domain-specific keyword [[com.thoughtworks.dsl.keywords.Yield Yield]].
  *
  *          {{{
  *          import com.thoughtworks.dsl.Dsl.reset
  *          import com.thoughtworks.dsl.keywords.Yield
  *
  *          def xorshiftRandomGenerator(seed: Int): Stream[Int] = {
  *            val tmp1 = seed ^ (seed << 13)
  *            val tmp2 = tmp1 ^ (tmp1 >>> 17)
  *            val tmp3 = tmp2 ^ (tmp2 << 5)
  *            !Yield(tmp3)
  *            xorshiftRandomGenerator(tmp3)
  *          }: @reset
  *
  *          val myGenerator = xorshiftRandomGenerator(seed = 123)
  *
  *          myGenerator(0) should be(31682556)
  *          myGenerator(1) should be(-276305998)
  *          myGenerator(2) should be(2101636938)
  *          }}}
  *
  *          [[com.thoughtworks.dsl.keywords.Yield Yield]] is an keyword to produce a value
  *          for a lazily evaluated [[scala.collection.immutable.Stream Stream]].
  *          That is to say, [[scala.collection.immutable.Stream Stream]] is the domain
  *          where the DSL [[com.thoughtworks.dsl.keywords.Yield Yield]] can be used,
  *          which was interpreted like the `yield` keyword in C#, JavaScript or Python.
  *
  *          Note that the body of `xorshiftRandomGenerator` is annotated as `@reset`,
  *          which enables the [[Dsl.Keyword#unary_$bang !-notation]] in the code block.
  *
  *          Alternatively, you can also use the
  *          [[com.thoughtworks.dsl.compilerplugins.ResetEverywhere ResetEverywhere]] compiler plug-in,
  *          which enable [[Dsl.Keyword#unary_$bang !-notation]] for every methods and functions.
  *
  *
  * @example [[com.thoughtworks.dsl.keywords.Yield Yield]] and [[scala.collection.immutable.Stream Stream]]
  *          can be also used for logging.
  *
  *          Suppose you have a function to parse an JSON file,
  *          you can append log records to a [[scala.collection.immutable.Stream Stream]] during parsing.
  *
  *          {{{
  *          import com.thoughtworks.dsl.keywords.Yield
  *          import com.thoughtworks.dsl.Dsl.!!
  *          import scala.util.parsing.json._
  *          def parseAndLog1(jsonContent: String, defaultValue: JSONType): Stream[String] !! JSONType = { (callback: JSONType => Stream[String]) =>
  *            !Yield(s"I am going to parse the JSON text $jsonContent...")
  *            JSON.parseRaw(jsonContent) match {
  *              case Some(json) =>
  *                !Yield(s"Succeeded to parse $jsonContent")
  *                callback(json)
  *              case None =>
  *                !Yield(s"Failed to parse $jsonContent")
  *                callback(defaultValue)
  *            }
  *          }
  *          }}}
  *
  *          Since the function produces both a [[scala.util.parsing.json.JSONType JSONType]]
  *          and a [[scala.collection.immutable.Stream Stream]] of logs,
  *          the return type is now `Stream[String] !! JSONType`,
  *          where [[com.thoughtworks.dsl.Dsl.$bang$bang !!]] is
  *          `(JSONType => Stream[String]) => Stream[String]`,
  *          an alias of continuation-passing style function,
  *          indicating it produces both a [[scala.util.parsing.json.JSONType JSONType]] and a [[Stream]] of logs.
  *
  *          {{{
  *          val logs = parseAndLog1(""" { "key": "value" } """, JSONArray(Nil)) { json =>
  *            json should be(JSONObject(Map("key" -> "value")))
  *            Stream("done")
  *          }
  *
  *          logs should be(Stream("I am going to parse the JSON text  { \"key\": \"value\" } ...",
  *                                "Succeeded to parse  { \"key\": \"value\" } ",
  *                                "done"))
  *          }}}
  *
  *
  * @example The the closure in the previous example can be simplified with the help of Scala's placeholder syntax:
  *
  *          {{{
  *          import com.thoughtworks.dsl.keywords.Yield
  *          import com.thoughtworks.dsl.Dsl.!!
  *          import scala.util.parsing.json._
  *          def parseAndLog2(jsonContent: String, defaultValue: JSONType): Stream[String] !! JSONType = _ {
  *            !Yield(s"I am going to parse the JSON text $jsonContent...")
  *            JSON.parseRaw(jsonContent) match {
  *              case Some(json) =>
  *                !Yield(s"Succeeded to parse $jsonContent")
  *                json
  *              case None =>
  *                !Yield(s"Failed to parse $jsonContent")
  *                defaultValue
  *            }
  *          }
  *
  *          val logs = parseAndLog2(""" { "key": "value" } """, JSONArray(Nil)) { json =>
  *            json should be(JSONObject(Map("key" -> "value")))
  *            Stream("done")
  *          }
  *
  *          logs should be(Stream("I am going to parse the JSON text  { \"key\": \"value\" } ...",
  *                                "Succeeded to parse  { \"key\": \"value\" } ",
  *                                "done"))
  *          }}}
  *
  *          Note that `parseAndLog2` is equivelent to `parseAndLog1`.
  *          The code block after underscore is still inside a function whose return type is `Stream[String]`.
  *
  * @example Instead of manually create the continuation-passing style function,
  *          you can also create the function from [[com.thoughtworks.dsl.Dsl.Continuation.delay delay]].
  *
  *          {{{
  *          import com.thoughtworks.dsl.keywords.Yield
  *          import com.thoughtworks.dsl.Dsl.!!, !!.delay
  *          import scala.util.parsing.json._
  *          def parseAndLog3(jsonContent: String, defaultValue: JSONType): Stream[String] !! JSONType = delay {
  *            !Yield(s"I am going to parse the JSON text $jsonContent...")
  *            JSON.parseRaw(jsonContent) match {
  *              case Some(json) =>
  *                !Yield(s"Succeeded to parse $jsonContent")
  *                json
  *              case None =>
  *                !Yield(s"Failed to parse $jsonContent")
  *                defaultValue
  *            }
  *          }
  *
  *          val logs = parseAndLog3(""" { "key": "value" } """, JSONArray(Nil)) { json =>
  *            json should be(JSONObject(Map("key" -> "value")))
  *            Stream("done")
  *          }
  *
  *          logs should be(Stream("I am going to parse the JSON text  { \"key\": \"value\" } ...",
  *                                "Succeeded to parse  { \"key\": \"value\" } ",
  *                                "done"))
  *          }}}
  *
  *          Unlike the `parseAndLog2` example, The code inside a `delay` block is not in an anonymous function.
  *          Instead, they are directly inside `parseAndLog3`, whose return type is `Stream[String] !! JSONType`.
  *
  *          That is to say,
  *          the domain of those [[com.thoughtworks.dsl.keywords.Yield Yield]] keywords in `parseAndLog3`
  *          is not `Stream[String]` any more, the domain is `Stream[String] !! JSONType` now,
  *          which supports more keywords, which you will learnt from the next examples,
  *          than the `Stream[String]` domain.
  *
  * @example [[com.thoughtworks.dsl.Dsl.$bang$bang !!]], or [[com.thoughtworks.dsl.Dsl.Continuation Continuation]],
  *          is the preferred approach to enable multiple domains in one function.
  *
  *          For example, you can create a function that
  *          lazily read each line of a [[java.io.BufferedReader BufferedReader]] to a [[Stream]],
  *          automatically close the [[java.io.BufferedReader BufferedReader]] after reading the last line,
  *          and finally return the total number of lines in the `Stream[String] !! Throwable !! Int` domain.
  *
  *          {{{
  *          import com.thoughtworks.dsl.Dsl.!!, !!.delay
  *          import com.thoughtworks.dsl.keywords.AutoClose
  *          import com.thoughtworks.dsl.keywords.Yield
  *          import com.thoughtworks.dsl.keywords.Shift._
  *          import java.io._
  *
  *          def readerToStream(createReader: () => BufferedReader): Stream[String] !! Throwable !! Int = delay {
  *            val reader = !AutoClose(createReader())
  *
  *            def loop(lineNumber: Int): Stream[String] !! Throwable !! Int = _ {
  *              reader.readLine() match {
  *                case null =>
  *                  lineNumber
  *                case line =>
  *                  !Yield(line)
  *                  !loop(lineNumber + 1)
  *              }
  *            }
  *
  *            !loop(0)
  *          }
  *          }}}
  *
  *          `!loop(0)` is a shortcut of `!Shift(loop(0))`,
  *          because there is [[com.thoughtworks.dsl.keywords.Shift#implicitShift an implicit conversion]]
  *          from `Stream[String] !! Throwable !! Int` to a [[com.thoughtworks.dsl.keywords.Shift Shift]] case class,
  *          which is similar to the `await` keyword in JavaScript, Python or C#.
  *
  *          A type like `A !! B !! C` means a domain-specific value of type `C` in the domain of `A` and `B`.
  *          When `B` is [[Throwable]], the [[com.thoughtworks.dsl.keywords.AutoClose AutoClose]]
  *          is available, which will close a resource when exiting the current function.
  *
  *          {{{
  *          import scala.util.Success
  *
  *          var isClosed = false
  *          def createReader() = {
  *            new BufferedReader(new StringReader("line1\nline2\nline3")) {
  *              override def close() = {
  *                isClosed = true
  *              }
  *            }
  *          }
  *
  *          val stream = readerToStream(createReader _) { numberOfLines: Int =>
  *            numberOfLines should be(3)
  *
  *            Function.const(Stream.empty)(_)
  *          } { e: Throwable =>
  *            throw new AssertionError("Unexpected exception during readerToStream", e)
  *          }
  *
  *          isClosed should be(false)
  *          stream should be(Stream("line1", "line2", "line3"))
  *          isClosed should be(true)
  *          }}}
  *
  * @example If you don't need to collaborate to [[scala.collection.immutable.Stream Stream]] or other domains,
  *          you can use `TailRect[Unit] !! Throwable !! A` as the return type,
  *
  *          {{{
  *          import com.thoughtworks.dsl.Dsl.!!
  *          import scala.util.control.TailCalls._
  *          type Task[A] = TailRec[Unit] !! Throwable !! A
  *          }}}
  *
  *          which can be used as an asynchronous task that support RAII,
  *          as a higher-performance replacement of
  *          [[scala.concurrent.Future]], [[scalaz.concurrent.Task]] or [[monix.eval.Task]].
  *
  *          There are some keywords [[com.thoughtworks.dsl.keywords.AsynchronousIo]]
  *          to asynchronously perform Java NIO.2 IO operations.
  *
  *          You can implement an HTTP client from very simple code:
  *
  *          {{{
  *          import com.thoughtworks.dsl.keywords._
  *          import com.thoughtworks.dsl.keywords.Shift.implicitShift
  *          import com.thoughtworks.dsl.keywords.AsynchronousIo._
  *          import java.io._
  *          import java.net._
  *          import java.nio._, channels._
  *
  *          def readAll(channel: AsynchronousByteChannel, destination: ByteBuffer): Task[Unit] = _ {
  *            if (destination.remaining > 0) {
  *              val numberOfBytesRead: Int = !Read(channel, destination)
  *              numberOfBytesRead match {
  *                case -1 =>
  *                case _  => !readAll(channel, destination)
  *              }
  *            } else {
  *              throw new IOException("The response is too big to read.")
  *            }
  *          }
  *
  *          def writeAll[Domain](channel: AsynchronousByteChannel, destination: ByteBuffer): Task[Unit] = _ {
  *            while (destination.remaining > 0) {
  *              !Write(channel, destination)
  *            }
  *          }
  *
  *          def httpClient(url: URL): Task[String] = _ {
  *            val socket = AsynchronousSocketChannel.open()
  *            try {
  *              val port = if (url.getPort == -1) 80 else url.getPort
  *              val address = new InetSocketAddress(url.getHost, port)
  *              !AsynchronousIo.Connect(socket, address)
  *              val request = ByteBuffer.wrap(s"GET ${url.getPath} HTTP/1.1\r\nHost:${url.getHost}\r\nConnection:Close\r\n\r\n".getBytes)
  *              !writeAll(socket, request)
  *              val response = ByteBuffer.allocate(100000)
  *              !readAll(socket, response)
  *              response.flip()
  *              io.Codec.UTF8.decoder.decode(response).toString
  *            } finally {
  *              socket.close()
  *            }
  *          }
  *          }}}
  *
  *          The usage of `Task` is similar to previous examples.
  *          You can check the result or exception in asynchronous handlers,
  *          and perform the actions inside a [[scala.util.control.TailCalls.TailRec TailRec]]
  *          by calling [[scala.util.control.TailCalls.TailRec#result result]].
  *
  *          {{{
  *          httpClient(new URL("http://deb.debian.org/")) { page =>
  *            page should include("Welcome to deb.debian.org!")
  *
  *            Function.const(done(()))(_)
  *          } { e =>
  *            throw new AssertionError("Unexpected exception during downloading", e)
  *          }.result
  *          }}}
  *
  * @see [[Dsl]] for the guideline to create your custom DSL.
  * @see [[domains.scalaz]] for using [[Dsl.Keyword#unary_$bang !-notation]] with [[scalaz]].
  * @see [[domains.cats]] for using [[Dsl.Keyword#unary_$bang !-notation]] with [[cats]].
  *
  *
  */
package object dsl

package dsl {

  /** Contains built-in domain-specific [[com.thoughtworks.dsl.Dsl.Keyword Keyword]]s and their corresponding interpreters.
    *
    *
    */
  package object keywords
}
