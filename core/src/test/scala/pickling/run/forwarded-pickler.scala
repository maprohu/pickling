package scala.pickling.forwardedpickler

import org.scalatest.FunSuite
import scala.pickling._
import json._
import static.StaticOnly

/* Note: importing AllPicklers.{genPickler, genUnpickler} leads to issues
         if the primitive picklers are *not* imported at the same time!
         (e.g., the *generated* pickler for type `Int` is nonsense)
 */
import AllPicklers._

sealed trait F { val fld: Int }

final case class G(fld: Int) extends F
final case class H(fld: Int) extends F

class PicklerCanBeForwarded extends FunSuite {

  // we should be able to use a passed-in implicit pickler
  // rather than trying to generate one, this allows people
  // to call pickle/unpickle in a different place from the
  // spot where they generate the pickler.
  private def doPickle[T](t: T)(implicit pickler1: SPickler[T]): JSONPickle =
    t.pickle

  private def doUnpickle[T](p: JSONPickle)(implicit unpickler1: Unpickler[T]): T =
    p.unpickle[T]

  test("main") {
    val x: F = G(42)
    val pickle: JSONPickle = doPickle(x)
    assert(doUnpickle[F](pickle).fld == 42)
  }
}